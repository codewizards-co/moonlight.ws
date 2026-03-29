package moonlight.ws.business.booking;

import static java.util.Objects.*;
import static moonlight.ws.base.util.JsonUtil.*;
import static moonlight.ws.base.util.StringUtil.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.Channel;
import com.liferay.headless.commerce.admin.channel.client.resource.v1_0.ChannelResource;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Page;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseItemResource;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;
import com.liferay.headless.commerce.admin.order.client.dto.v1_0.BillingAddress;
import com.liferay.headless.commerce.admin.order.client.dto.v1_0.CustomField;
import com.liferay.headless.commerce.admin.order.client.dto.v1_0.CustomValue;
import com.liferay.headless.commerce.admin.order.client.dto.v1_0.Order;
import com.liferay.headless.commerce.admin.order.client.dto.v1_0.OrderItem;
import com.liferay.headless.commerce.admin.order.client.dto.v1_0.ShippingAddress;
import com.liferay.headless.commerce.admin.order.client.resource.v1_0.OrderItemResource;
import com.liferay.headless.commerce.admin.order.client.resource.v1_0.OrderResource;
import com.liferay.headless.commerce.admin.shipment.client.dto.v1_0.Shipment;
import com.liferay.headless.commerce.admin.shipment.client.dto.v1_0.ShipmentItem;
import com.liferay.headless.commerce.admin.shipment.client.dto.v1_0.Status;
import com.liferay.headless.commerce.admin.shipment.client.resource.v1_0.ShipmentItemResource;
import com.liferay.headless.commerce.admin.shipment.client.resource.v1_0.ShipmentResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.api.PriceDto;
import moonlight.ws.api.invoice.InvoiceInclude;
import moonlight.ws.api.invoice.InvoiceItemFilter;
import moonlight.ws.api.invoice.InvoiceWorkflow;
import moonlight.ws.api.liferay.customfield.InvoiceItemGroupJson;
import moonlight.ws.api.liferay.customfield.InvoiceJson;
import moonlight.ws.api.liferay.customfield.OrderCustomFieldConst;
import moonlight.ws.api.liferay.customfield.OrderItemCustomFieldConst;
import moonlight.ws.api.warehouse.WarehouseItemProductDto;
import moonlight.ws.business.mapper.PriceMapper;
import moonlight.ws.business.rest.impl.liferay.SkuCache;
import moonlight.ws.business.rest.impl.liferay.WarehouseItemCache;
import moonlight.ws.liferay.LiferayResourceFactory;
import moonlight.ws.liferay.OrderStatus;
import moonlight.ws.liferay.PaymentStatus;
import moonlight.ws.liferay.ShipmentStatus;
import moonlight.ws.persistence.SearchResult;
import moonlight.ws.persistence.invoice.InvoiceConfigDao;
import moonlight.ws.persistence.invoice.InvoiceConfigEntity;
import moonlight.ws.persistence.invoice.InvoiceDao;
import moonlight.ws.persistence.invoice.InvoiceEntity;
import moonlight.ws.persistence.invoice.InvoiceItemDao;
import moonlight.ws.persistence.invoice.InvoiceItemEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;

@RequestScoped
@Slf4j
public class LiferayBookingHelper {

	@Inject
	private WarehouseItemMovementDao warehouseItemMovementDao;

	@Inject
	private InvoiceDao invoiceDao;

	@Inject
	private InvoiceItemDao invoiceItemDao;

	@Inject
	private InvoiceConfigDao invoiceConfigDao;

	@Inject
	private PriceMapper priceMapper;

	@Inject
	private SkuCache skuCache;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	private ObjectMapper objectMapper;
	protected OrderResource orderResource;
	protected OrderItemResource orderItemResource;
	protected ShipmentResource shipmentResource;
	protected ShipmentItemResource shipmentItemResource;
	protected WarehouseResource warehouseResource;
	protected WarehouseItemResource warehouseItemResource;

	@Inject
	protected WarehouseItemCache warehouseItemCache;

	private String currencyCode;
	private Set<Long> warehouseIds;

	@Transactional(value = TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
	public void bookWarehouseItemMovement(long warehouseItemMovementId) throws Exception {
		log.info("bookWarehouseItemMovement: id={}: entered.", warehouseItemMovementId);
		initLiferayResources();
		var warehouseItemMovement = readAndLockWarehouseItemMovement(warehouseItemMovementId);
		if (warehouseItemMovement.getFinalized() == 0) {
			throw new IllegalStateException(
					"WarehouseItemMovement[id=%d] not finalized!".formatted(warehouseItemMovementId));
		}
		if (warehouseItemMovement.getBooked() != 0) {
			log.info("bookWarehouseItemMovement: id={}: already booked => skip.", warehouseItemMovementId);
			return;
		}

		WarehouseItem warehouseItem = warehouseItemResource
				.getWarehouseItem(warehouseItemMovement.getWarehouseItemId());
		warehouseItem.setQuantity(warehouseItem.getQuantity().add(warehouseItemMovement.getQuantity()));
		warehouseItemResource.patchWarehouseItem(warehouseItem.getId(), warehouseItem);

		warehouseItemMovement.setBooked(System.currentTimeMillis());
		log.info("bookWarehouseItemMovement: id={}: done.", warehouseItemMovementId);
	}

	@Transactional(value = TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
	public void bookInvoice(long invoiceId) throws Exception {
		log.info("bookInvoice: id={}: entered.", invoiceId);
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		initLiferayResources();
		var invoice = readAndLockInvoice(invoiceId);
		if (invoice.getFinalized() == 0) {
			throw new IllegalStateException("Invoice[id=%d] not finalized!".formatted(invoiceId));
		}
		if (invoice.getBooked() != 0) {
			log.info("bookInvoice: invoiceId={}: already booked => skip.", invoiceId);
			return;
		}
		List<InvoiceItemEntity> invoiceItems = readAndLockInvoiceItems(invoice);
		if (invoiceItems.isEmpty()) {
			invoice.setBooked(System.currentTimeMillis());
			log.info(
					"bookInvoice: invoiceId={}: There are no included invoice-items. Marked this invoice booked without creating a Liferay-order.",
					invoiceId);
			return;
		}

		Order order;
		try {
			order = createOrUpdateLiferayOrder(invoice, invoiceItems);
		} catch (Exception x) {
			log.error("bookInvoice: invoiceId=%d: createOrUpdateLiferayOrder(1): %s".formatted(invoiceId, x), x);
			return; // must not set invoice.booked!
		}
		log.info("bookInvoice: invoiceId={}: created (or initially fetched): liferayOrderId={}", invoiceId,
				order.getId());

		if (isLiferayOrderStatus(OrderStatus.OPEN, order)) {
			int orderItemIndex = -1;
			for (var me : groupBySku(invoiceItems).entrySet()) {
				++orderItemIndex;
				String sku = requireNonNull(me.getKey(), "sku");
				List<InvoiceItemEntity> skuInvoiceItems = requireNonNull(me.getValue(), "skuInvoiceItems");
				OrderItem orderItem;
				try {
					orderItem = createOrUpdateLiferayOrderItem(orderItemIndex, invoice, order, sku, skuInvoiceItems);
				} catch (Exception x) {
					log.error("bookInvoice: invoiceId=%d invoiceItemIds=%s: createOrUpdateLiferayOrderItem: %s"
							.formatted(invoiceId, skuInvoiceItems.stream().map(InvoiceItemEntity::getId).toList(), x),
							x);
					return; // must not set invoice.booked!
				}
				log.info("bookInvoice: invoiceId={} invoiceItemIds={}: liferayOrderItemId={}", invoiceId,
						skuInvoiceItems.stream().map(InvoiceItemEntity::getId).toList(), orderItem.getId());
			}

			try {
				order = createOrUpdateLiferayOrder(invoice, invoiceItems);
			} catch (Exception x) {
				log.error("bookInvoice: invoiceId=%d: createOrUpdateLiferayOrder(2): %s".formatted(invoiceId, x), x);
				return; // must not set invoice.booked!
			}
			log.info("bookInvoice: invoiceId={}: updated: liferayOrderId={}", invoiceId, order.getId());

			try {
				order = checkoutOrder(order.getId());
			} catch (Exception x) {
				log.error("bookInvoice: invoiceId=%d: checkoutOrder: %s".formatted(invoiceId, x), x);
				return; // must not set invoice.booked!
			}
			log.info("bookInvoice: invoiceId={}: checkout done: liferayOrderId={}", invoiceId, order.getId());
		}

		if (PaymentStatus.PENDING.ordinal() == requireNonNull(order.getPaymentStatus(), "order.paymentStatus")) {
			try {
				order = markOrderPaid(order.getId());
			} catch (Exception x) {
				log.error("bookInvoice: invoiceId=%d: markOrderPaid: %s".formatted(invoiceId, x), x);
				return; // must not set invoice.booked!
			}
			log.info("bookInvoice: invoiceId={}: marked paid: liferayOrderId={}", invoiceId, order.getId());
		}

		if (isLiferayOrderStatus(OrderStatus.PENDING, order)) {
			try {
				order = acceptOrder(order.getId());
			} catch (Exception x) {
				log.error("bookInvoice: invoiceId=%d: acceptOrder: %s".formatted(invoiceId, x), x);
				return; // must not set invoice.booked!
			}
			log.info("bookInvoice: invoiceId={}: accepted order: liferayOrderId={}", invoiceId, order.getId());
		}

		Shipment shipment;
		try {
			shipment = createOrUpdateLiferayShipment(invoice);
		} catch (Exception x) {
			log.error("bookInvoice: invoiceId=%d: createOrUpdateLiferayShipment: %s".formatted(invoiceId, x), x);
			return; // must not set invoice.booked!
		}
		log.info("bookInvoice: invoiceId={}: created (or initially fetched): liferayShipmentId={}", invoiceId,
				shipment.getId());

		if (ShipmentStatus.PROCESSING.ordinal() == shipment.getStatus().getCode()) {
			for (var me : groupByOrderItemId(invoiceItems).entrySet()) {
				Long orderItemId = requireNonNull(me.getKey(), "orderItemId");
				List<InvoiceItemEntity> groupedInvoiceItems = requireNonNull(me.getValue(), "groupedInvoiceItems");
				ShipmentItem shipmentItem;
				try {
					shipmentItem = createOrUpdateLiferayShipmentItem(invoice, order, shipment, orderItemId,
							groupedInvoiceItems);
				} catch (Exception x) {
					log.error("bookInvoice: invoiceId=%d invoiceItemIds=%s: createOrUpdateLiferayShipmentItem: %s"
							.formatted(invoiceId, groupedInvoiceItems.stream().map(InvoiceItemEntity::getId).toList(),
									x),
							x);
					return; // must not set invoice.booked!
				}
				log.info("bookInvoice: invoiceId={} invoiceItemIds={}: liferayShipmentItemId={}", invoiceId,
						groupedInvoiceItems.stream().map(InvoiceItemEntity::getId).toList(), shipmentItem.getId());
			}

			// has no effect :-(
//			try {
//				shipment = markShipmentReadyToShip(shipment.getId());
//			} catch (Exception x) {
//				log.error("bookInvoice: invoiceId=%d: markShipmentReadyToShip: %s".formatted(invoiceId, x), x);
//				return; // must not set invoice.booked!
//			}
//			log.info("bookInvoice: invoiceId={}: marked ready-to-ship: liferayShipmentId={}", invoiceId,
//					shipment.getId());
		}

		// has no effect :-(
//		if (ShipmentStatus.READY_TO_SHIP.ordinal() == shipment.getStatus().getCode()) {
//			try {
//				shipment = markShipmentShipped(shipment.getId());
//			} catch (Exception x) {
//				log.error("bookInvoice: invoiceId=%d: markShipmentShipped: %s".formatted(invoiceId, x), x);
//				return; // must not set invoice.booked!
//			}
//			log.info("bookInvoice: invoiceId={}: marked shipped: liferayShipmentId={}", invoiceId, shipment.getId());
//		}

		try {
			order = markOrderShipped(order.getId());
		} catch (Exception x) {
			log.error("bookInvoice: invoiceId=%d: markOrderShipped: %s".formatted(invoiceId, x), x);
			return; // must not set invoice.booked!
		}
		log.info("bookInvoice: invoiceId={}: marked shipped: liferayOrderId={}", invoiceId, order.getId());

		// mark it booked when everything was successful!
		invoice.setBooked(System.currentTimeMillis());
		log.info("bookInvoice: invoiceId={}: done.", invoiceId);
	}

	private boolean isLiferayOrderStatus(@NonNull OrderStatus orderStatus, @NonNull Order order) {
		return orderStatus.ordinal() == requireNonNull(order.getOrderStatus(), "order.orderStatus");
	}

	private Order checkoutOrder(@NonNull Long orderId) throws Exception {
		var patchOrder = new Order();
		patchOrder.setOrderStatus(OrderStatus.PENDING.ordinal());
		return orderResource.patchOrder(orderId, patchOrder);
	}

	private Order markOrderPaid(@NonNull Long orderId) throws Exception {
		var patchOrder = new Order();
		patchOrder.setPaymentStatus(PaymentStatus.COMPLETED.ordinal());
		return orderResource.patchOrder(orderId, patchOrder);
	}

	private Order acceptOrder(@NonNull Long orderId) throws Exception {
		var patchOrder = new Order();
		patchOrder.setOrderStatus(OrderStatus.PROCESSING.ordinal());
		return orderResource.patchOrder(orderId, patchOrder);
	}

	private Order markOrderShipped(@NonNull Long orderId) throws Exception {
		var patchOrder = new Order();
		patchOrder.setOrderStatus(OrderStatus.SHIPPED.ordinal());
		return orderResource.patchOrder(orderId, patchOrder);
	}

	private Shipment markShipmentReadyToShip(@NonNull Long shipmentId) throws Exception { // has no effect :-(
		var patchShipment = new Shipment();
		patchShipment.setStatus(new Status());
		patchShipment.getStatus().setCode(ShipmentStatus.READY_TO_SHIP.ordinal());
		return shipmentResource.patchShipment(shipmentId, patchShipment);
	}

	private Shipment markShipmentShipped(@NonNull Long shipmentId) throws Exception { // has no effect :-(
		var patchShipment = new Shipment();
		patchShipment.setStatus(new Status());
		patchShipment.getStatus().setCode(ShipmentStatus.SHIPPED.ordinal());
		return shipmentResource.patchShipment(shipmentId, patchShipment);
	}

	private SortedMap<String, List<InvoiceItemEntity>> groupBySku(List<InvoiceItemEntity> invoiceItems) {
		SortedMap<String, List<InvoiceItemEntity>> sku2InvoiceItems = new TreeMap<>();
		for (var invoiceItem : invoiceItems) {
			var warehouseItemMovement = invoiceItem.getWarehouseItemMovement();
//			if (warehouseItemMovement == null) {
//				continue;
//			}
			requireNonNull(warehouseItemMovement, "warehouseItemMovement"); // other invoice-items not yet supported.
			String sku = requireNonEmpty(warehouseItemMovement.getSku(), "warehouseItemMovement.sku");
			sku2InvoiceItems.computeIfAbsent(sku, s -> new ArrayList<>()).add(invoiceItem);
		}
		return sku2InvoiceItems;
	}

	private SortedMap<Long, List<InvoiceItemEntity>> groupByOrderItemId(List<InvoiceItemEntity> invoiceItems) {
		SortedMap<Long, List<InvoiceItemEntity>> orderItemId2InvoiceItems = new TreeMap<>();
		for (var invoiceItem : invoiceItems) {
			Long orderItemId = requireNonNull(invoiceItem.getOrderItemId(), "invoiceItem.orderItemId");
			orderItemId2InvoiceItems.computeIfAbsent(orderItemId, i -> new ArrayList<>()).add(invoiceItem);
		}
		return orderItemId2InvoiceItems;
	}

	private WarehouseItemMovementEntity readAndLockWarehouseItemMovement(long warehouseItemMovementId) {
		var warehouseItemMovement = requireNonNull(warehouseItemMovementDao.getEntity(warehouseItemMovementId),
				"WarehouseItemMovement[id=%d]".formatted(warehouseItemMovementId));
		warehouseItemMovementDao.lock(warehouseItemMovement);
		return warehouseItemMovement;
	}

	private Order createOrUpdateLiferayOrder(@NonNull InvoiceEntity invoice,
			@NonNull List<InvoiceItemEntity> invoiceItems) throws Exception {
		if (invoice.getOrderId() != null) {
			Order order = orderResource.getOrder(invoice.getOrderId());
			if (order == null) {
				throw new IllegalStateException(
						"No order found in Liferay for invoice.orderId=%d!".formatted(invoice.getOrderId()));
			}
			if (!isLiferayOrderStatus(OrderStatus.OPEN, order)) {
				return order; // already read-only! must not patch!
			}
			order = new Order();
			order.setId(invoice.getOrderId());
			populateOrder(order, invoice, invoiceItems);
			requireNonNull(order.getId(), "order.id");
			orderResource.patchOrder(order.getId(), order);
			requireNonNull(order.getId(), "order.id");
			return order;
		}
		Order order = new Order();
		populateOrder(order, invoice, invoiceItems);
		order = orderResource.postOrder(order);
		invoice.setOrderId(requireNonNull(order.getId(), "order.id"));
		return order;
	}

	private OrderItem createOrUpdateLiferayOrderItem(int orderItemIndex, @NonNull InvoiceEntity invoice,
			@NonNull Order order, @NonNull String sku, @NonNull List<InvoiceItemEntity> invoiceItems) throws Exception {
		InvoiceItemEntity invoiceItem0 = invoiceItems.get(0);
		if (invoiceItem0.getOrderItemId() != null) {
			OrderItem orderItem = orderItemResource.getOrderItem(invoiceItem0.getOrderItemId());
			if (orderItem == null) {
				throw new IllegalStateException("No order-item found in Liferay for invoiceItem.orderItemId=%d!"
						.formatted(invoiceItem0.getOrderItemId()));
			}
			orderItem = new OrderItem();
			orderItem.setId(invoiceItem0.getOrderItemId());
			populateOrderItem(orderItemIndex, orderItem, invoiceItems, invoice, order, sku);
			orderItemResource.patchOrderItem(requireNonNull(orderItem.getId(), "orderItem.id"), orderItem);
			requireNonNull(orderItem.getId(), "orderItem.id");
			return orderItem;
		}
		OrderItem orderItem = new OrderItem();
		populateOrderItem(orderItemIndex, orderItem, invoiceItems, invoice, order, sku);
		orderItem = orderItemResource.postOrderIdOrderItem(requireNonNull(order.getId(), "order.id"), orderItem);
		final var orderItemId = requireNonNull(orderItem.getId(), "orderItem.id");
		invoiceItems.forEach(ii -> ii.setOrderItemId(orderItemId));
		return orderItem;
	}

	private Shipment createOrUpdateLiferayShipment(@NonNull InvoiceEntity invoice) throws Exception {
		if (invoice.getShipmentId() != null) {
			Shipment shipment = shipmentResource.getShipment(invoice.getShipmentId());
			if (shipment == null) {
				throw new IllegalStateException(
						"No shipment found in Liferay for invoice.shipmentId=%d!".formatted(invoice.getShipmentId()));
			}
			requireNonNull(shipment.getId(), "shipment.id");
			return shipment;
		}
		Shipment shipment = new Shipment();
		shipment.setOrderId(requireNonNull(invoice.getOrderId(), "invoice.orderId"));
		shipment = shipmentResource.postShipment(shipment);
		invoice.setShipmentId(requireNonNull(shipment.getId(), "shipment.id"));
		return shipment;
	}

	private ShipmentItem createOrUpdateLiferayShipmentItem(@NonNull InvoiceEntity invoice, @NonNull Order order,
			@NonNull Shipment shipment, @NonNull Long orderItemId, @NonNull List<InvoiceItemEntity> invoiceItems)
			throws Exception {
		InvoiceItemEntity invoiceItem0 = invoiceItems.get(0);
		if (invoiceItem0.getShipmentItemId() != null) {
			ShipmentItem shipmentItem = shipmentItemResource.getShipmentItem(invoiceItem0.getShipmentItemId());
			if (shipmentItem == null) {
				throw new IllegalStateException("No shipment-item found in Liferay for invoiceItem.shipmentItemId=%d!"
						.formatted(invoiceItem0.getShipmentItemId()));
			}
			requireNonNull(shipmentItem.getId(), "shipmentItem.id");
			return shipmentItem;
		}
		OrderItem orderItem = orderItemResource.getOrderItem(orderItemId);
		if (orderItem == null) {
			throw new IllegalStateException(
					"No order-item found in Liferay for invoiceItem.orderItemId=%d!".formatted(orderItemId));
		}
		ShipmentItem shipmentItem = new ShipmentItem();
		shipmentItem.setOrderItemId(orderItemId);
		shipmentItem.setValidateInventory(true);
		shipmentItem.setQuantity(orderItem.getQuantity());
		shipmentItem.setWarehouseId(getWarehouseIdHaving(orderItem.getSku()));
		shipmentItem = shipmentItemResource.postShipmentItem(shipment.getId(), shipmentItem);
		final var shipmentItemId = requireNonNull(shipmentItem.getId(), "shipmentItem.id");
		invoiceItems.forEach(ii -> ii.setShipmentItemId(shipmentItemId));
		return shipmentItem;
	}

	private Long getWarehouseIdHaving(@NonNull String sku) throws Exception {
		for (Long warehouseId : getWarehouseIds()) {
			WarehouseItem warehouseItem = warehouseItemCache.getWarehouseItems(warehouseId).stream() //
					.filter(whi -> sku.equals(whi.getSku()) && whi.getQuantity().compareTo(BigDecimal.ONE) >= 0) //
					.findFirst() //
					.orElse(null);
			if (warehouseItem != null) {
				return warehouseId;
			}
		}
		throw new IllegalStateException("There is no warehouse having the item with sku=%s in stock!".formatted(sku));
	}

	private Set<Long> getWarehouseIds() throws Exception {
		if (warehouseIds == null) {
			Set<Long> _warehouseIds = new TreeSet<>();
			Pagination pagination = Pagination.of(1, 500);
			while (true) {
				Page<Warehouse> warehousesPage = warehouseResource.getWarehousesPage(null, "active eq true", pagination,
						null);
				_warehouseIds.addAll(warehousesPage.getItems().stream() //
						.filter(wh -> wh.getActive().booleanValue()) //
						.map(wh -> wh.getId()) //
						.toList());
				if (!warehousesPage.hasNext()) {
					break;
				}
				pagination = Pagination.of(pagination.getPage() + 1, pagination.getPageSize());
			}
			warehouseIds = _warehouseIds;
		}
		return warehouseIds;
	}

	private String getSkuPrefix(@NonNull InvoiceWorkflow invoiceWorkflow) {
		InvoiceConfigEntity invoiceConfig = invoiceConfigDao.getInvoiceConfig();
		switch (invoiceWorkflow) {
			case CONSIGNEE:
				return requireNonEmpty(invoiceConfig.getConsignmentSaleSkuPrefix(),
						"invoiceConfig.consignmentSaleSkuPrefix");
			case SUPPLIER:
				return requireNonEmpty(invoiceConfig.getSupplyPurchaseSkuPrefix(),
						"invoiceConfig.supplyPurchaseSkuPrefix");
			default:
				throw new IllegalArgumentException("Unknown invoiceWorkflow: " + invoiceWorkflow);
		}
	}

	private void populateOrderItem(int orderItemIndex, @NonNull OrderItem orderItem,
			@NonNull List<InvoiceItemEntity> invoiceItems, @NonNull InvoiceEntity invoice, @NonNull Order order,
			@NonNull String realSku) throws Exception {
		orderItem.setOrderId(requireNonNull(order.getId(), "order.id"));

		var skuPrefix = getSkuPrefix(invoice.getWorkflow());
		var sku = skuPrefix + (orderItemIndex + 1);
		Sku skuObj = skuCache.getSkus().stream().filter(s -> sku.equals(s.getSku())).findFirst().orElse(null);
		requireNonNull(skuObj, "Sku[sku=%s]".formatted(sku));

		orderItem.setExternalReferenceCode("moonlight_invoice_%d_sku_%s".formatted(invoice.getId(), realSku));
		orderItem.setSku(skuObj.getSku());
		orderItem.setSkuId(skuObj.getId());

		var quantity = BigDecimal.ZERO;
		var priceTotalGross = BigDecimal.ZERO;
		var priceTotalNet = BigDecimal.ZERO;
		BigDecimal taxPercent = null;
		for (InvoiceItemEntity invoiceItem : invoiceItems) {
			quantity = quantity.add(requireNonNull(invoiceItem.getQuantity(), "invoiceItem.quantity"));
			priceTotalGross = priceTotalGross
					.add(requireNonNull(invoiceItem.getPriceTotalGross(), "invoiceItem.priceTotalGross"));
			priceTotalNet = priceTotalNet
					.add(requireNonNull(invoiceItem.getPriceTotalNet(), "invoiceItem.priceTotalNet"));
			if (taxPercent == null) {
				taxPercent = requireNonNull(invoiceItem.getTaxPercent(), "invoiceItem.taxPercent");
			} else if (taxPercent
					.compareTo(requireNonNull(invoiceItem.getTaxPercent(), "invoiceItem.taxPercent")) != 0) {
				log.warn(
						"populateOrderItem: InvoiceItem[id={}].taxPercent={} does not match InvoiceItem[id={}].taxPercent={}",
						invoiceItems.get(0).getId(), taxPercent, invoiceItem.getId());
			}
		}

		if (invoice.getWorkflow() == InvoiceWorkflow.SUPPLIER) {
			// Liferay doesn't support negative orders. We thus negate the values and fake
			// it in the invoice-layout.
			quantity = quantity.negate();
			priceTotalGross = priceTotalGross.negate();
			priceTotalNet = priceTotalNet.negate();
		}

		var invoiceItemGroupJson = new InvoiceItemGroupJson();
		invoiceItemGroupJson.setVersion(1);
		invoiceItemGroupJson.setSku(realSku);
		List<Sku> realSkuObjs = skuCache.getSkus().stream().filter(s -> realSku.equals(s.getSku())).toList();
		invoiceItemGroupJson.setProducts(realSkuObjs.stream()
				.map(realSkuObj -> new WarehouseItemProductDto(realSkuObj.getProductId(), realSkuObj.getProductName()))
				.toList());
		invoiceItemGroupJson.setInvoiceItemIds(invoiceItems.stream().map(InvoiceItemEntity::getId).toList());
		String invoiceItemGroupJsonString = objectMapper.writeValueAsString(invoiceItemGroupJson);

		CustomField customField = new CustomField();
		customField.setName(OrderItemCustomFieldConst.INVOICE_ITEM_GROUP_JSON);
		CustomValue customValue = new CustomValue();
		customValue.setData(jsonEscape(invoiceItemGroupJsonString));
		customField.setCustomValue(customValue);
		orderItem.setCustomFields(new CustomField[] { customField });

		orderItem.setQuantity(quantity);
		orderItem.setDecimalQuantity(quantity);

		var price = new PriceDto();
		price.setQuantity(quantity);
		price.setPriceTotalGross(priceTotalGross);
		price.setPriceTotalNet(priceTotalNet);
		price.setTaxPercent(requireNonNull(taxPercent, "taxPercent"));
		price = priceMapper.calculateMissingProperties(price);

		orderItem.setPriceManuallyAdjusted(true);
		orderItem.setUnitPrice(requireNonNull(price.getPriceSingleNet(), "price.priceSingleNet"));
		orderItem.setUnitPriceWithTaxAmount(requireNonNull(price.getPriceSingleGross(), "price.priceSingleGross"));
		orderItem.setFinalPrice(requireNonNull(price.getPriceTotalNet(), "price.priceTotalNet"));
		orderItem.setFinalPriceWithTaxAmount(requireNonNull(price.getPriceTotalGross(), "price.priceTotalGross"));
		orderItem.setSubscription(false);

//		orderItem.setShippingAddressId(order.getShippingAddressId());

//		if (invoiceItem.getWarehouseItemMovement() != null) { // should always be there, but we better check.
//			orderItem.setName(Map.of(Locale.US.toString(),
//					"Consignment-sale of %s".formatted(invoiceItem.getWarehouseItemMovement().getSku())));
//		}
	}

	private String getCurrencyCode() throws Exception {
		if (currencyCode == null) {
			InvoiceConfigEntity invoiceConfig = invoiceConfigDao.getInvoiceConfig();
			ChannelResource channelResource = liferayResourceFactory.getResource(ChannelResource.class);
			Channel channel = channelResource
					.getChannel(requireNonNull(invoiceConfig.getChannelId(), "invoiceConfig.channelId"));
			requireNonNull(channel, "Channel[id=%d]".formatted(invoiceConfig.getChannelId()));
			currencyCode = requireNonEmpty(channel.getCurrencyCode(),
					"Channel[id=%d].currencyCode".formatted(invoiceConfig.getChannelId()));
		}
		return currencyCode;
	}

	private void populateOrder(@NonNull Order order, @NonNull InvoiceEntity invoice,
			List<InvoiceItemEntity> invoiceItems) throws Exception {
		InvoiceConfigEntity invoiceConfig = invoiceConfigDao.getInvoiceConfig();
		// accountId, channelId and currencyCode are the minimum
		order.setAccountId(requireNonNull(invoiceConfig.getAccountId(), "invoiceConfig.accountId"));
		order.setChannelId(requireNonNull(invoiceConfig.getChannelId(), "invoiceConfig.channelId"));
		order.setCurrencyCode(getCurrencyCode());

		order.setExternalReferenceCode("moonlight_invoice_" + invoice.getId());
		order.setBillingAddress(createBillingAddress(invoice));
		order.setShippingAddress(createShippingAddress(invoice));

		order.setOrderStatus(OrderStatus.OPEN.ordinal());

		var invoiceJson = new InvoiceJson();
		invoiceJson.setVersion(1);
		invoiceJson.setInvoiceId(invoice.getId());
		invoiceJson.setWorkflow(invoice.getWorkflow());
		invoiceJson.setBillingEmail(invoice.getParty().getEmail());
		String invoiceJsonString = objectMapper.writeValueAsString(invoiceJson);

		// funnily, this does not need to be escaped, while the order-item's custom
		// field requires it.
		order.setCustomFields(Map.of(OrderCustomFieldConst.INVOICE_JSON, invoiceJsonString));

		BigDecimal priceTotalGrossSum = BigDecimal.ZERO;
		BigDecimal priceTotalNetSum = BigDecimal.ZERO;
		for (InvoiceItemEntity invoiceItem : invoiceItems) {
			priceTotalGrossSum = priceTotalGrossSum.add(invoiceItem.getPriceTotalGross());
			priceTotalNetSum = priceTotalNetSum.add(invoiceItem.getPriceTotalNet());
		}

		if (invoice.getWorkflow() == InvoiceWorkflow.SUPPLIER) {
			// Liferay doesn't support negative orders. We thus negate the values and fake
			// it in the invoice-layout.
			priceTotalGrossSum = priceTotalGrossSum.negate();
			priceTotalNetSum = priceTotalNetSum.negate();
		}

		order.setSubtotal(priceTotalNetSum);
		order.setSubtotalWithTaxAmount(priceTotalGrossSum);
		order.setTaxAmount(priceTotalGrossSum.subtract(priceTotalNetSum));
		order.setTotal(priceTotalNetSum);
		order.setTotalWithTaxAmount(priceTotalGrossSum);
	}

	private BillingAddress createBillingAddress(@NonNull InvoiceEntity invoice) {
		var party = invoice.getParty();
		var address = new BillingAddress();
		address.setCity(party.getCity());
		address.setCountryISOCode(party.getCountryIsoCode());
		address.setName(party.getName());
		address.setPhoneNumber(party.getPhone());
		address.setStreet1(party.getStreet1());
		address.setStreet2(party.getStreet2());
		address.setStreet3(party.getStreet3());
		address.setRegionISOCode(party.getRegionCode());
		address.setVatNumber(party.getTaxNo());
		address.setZip(party.getZip());
		address.setDescription(party.getDescription());
		return address;
	}

	private ShippingAddress createShippingAddress(@NonNull InvoiceEntity invoice) {
		var party = invoice.getParty();
		var address = new ShippingAddress();
		address.setCity(party.getCity());
		address.setCountryISOCode(party.getCountryIsoCode());
		address.setName(party.getName());
		address.setPhoneNumber(party.getPhone());
		address.setStreet1(party.getStreet1());
		address.setStreet2(party.getStreet2());
		address.setStreet3(party.getStreet3());
		address.setRegionISOCode(party.getRegionCode());
		address.setZip(party.getZip());
		address.setDescription(party.getDescription());
		return address;
	}

	private InvoiceEntity readAndLockInvoice(long invoiceId) {
		var invoice = requireNonNull(invoiceDao.getEntity(invoiceId), "Invoice[id=%d]".formatted(invoiceId));
		invoiceDao.lock(invoice);
		return invoice;
	}

	private List<InvoiceItemEntity> readAndLockInvoiceItems(@NonNull InvoiceEntity invoice) {
		List<InvoiceItemEntity> result = new ArrayList<>();
		var filter = new InvoiceItemFilter();
		filter.setFilterInvoiceId(requireNonNull(invoice.getId(), "invoice.id"));
		filter.setFilterInclude(InvoiceInclude.INCLUDE);
		filter.setPageSize(Filter.MAX_PAGE_SIZE);
		while (true) {
			SearchResult<InvoiceItemEntity> searchResult = invoiceItemDao.searchEntities(filter);
			if (searchResult.getEntities().isEmpty()) {
				return result;
			}
			searchResult.getEntities().forEach(ii -> invoiceItemDao.lock(ii));
			result.addAll(searchResult.getEntities());
			if (result.size() >= searchResult.getTotalSize()) {
				return result;
			}
			filter.setPageNumber(filter.getPageNumberOrDefault() + 1);
		}
	}

	protected void initLiferayResources() {
		orderResource = liferayResourceFactory.getResource(OrderResource.class);
		orderItemResource = liferayResourceFactory.getResource(OrderItemResource.class);
		shipmentResource = liferayResourceFactory.getResource(ShipmentResource.class);
		shipmentItemResource = liferayResourceFactory.getResource(ShipmentItemResource.class);
		warehouseItemResource = liferayResourceFactory.getResource(WarehouseItemResource.class);
		warehouseResource = liferayResourceFactory.getResource(WarehouseResource.class);
	}
}
