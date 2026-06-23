package moonlight.ws.business.rest.impl.invoice;

import static java.util.Objects.*;
import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.business.RoundingConst.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Catalog;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Product;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.ProductResource;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceEntry;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceList;
import com.liferay.headless.commerce.admin.pricing.client.pagination.Page;
import com.liferay.headless.commerce.admin.pricing.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.pricing.client.resource.v2_0.PriceEntryResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.PriceDto;
import moonlight.ws.api.party.ConsigneeFilter;
import moonlight.ws.business.mapper.PriceMapper;
import moonlight.ws.business.rest.impl.liferay.CatalogCache;
import moonlight.ws.business.rest.impl.liferay.PriceListCache;
import moonlight.ws.business.rest.impl.liferay.SkuCache;
import moonlight.ws.liferay.LiferayResourceFactory;
import moonlight.ws.persistence.invoice.InvoiceItemEntity;
import moonlight.ws.persistence.party.ConsigneeDao;
import moonlight.ws.persistence.party.ConsigneeEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;

@RequestScoped
@Slf4j
public class AutoCreateInvoiceItemsForConsignee extends AutoCreateInvoiceItemsForX {

	@Inject
	private ConsigneeDao consigneeDao;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Inject
	private SkuCache skuCache;

	@Inject
	private CatalogCache catalogCache;

	@Inject
	private PriceListCache priceListCache;

	@Inject
	private PriceMapper priceMapper;

	private Long catalogId;

	private List<PriceList> priceLists;

	private Map<Long, Product> productId2Product = new HashMap<>();

	@Override
	protected @NonNull List<WarehouseItemMovementEntity> getWarehouseItemMovementsToProcess() {
		List<WarehouseItemMovementEntity> warehouseItemMovements = new ArrayList<>();
		for (ConsigneeEntity consignee : getConsignees()) {
			warehouseItemMovements.addAll(warehouseItemMovementDao
					.getWarehouseItemMovementsWithoutInvoiceItemForSale(consignee.getWarehouseId()));
		}
		return warehouseItemMovements;
	}

	private List<ConsigneeEntity> getConsignees() {
		List<ConsigneeEntity> consignees = new ArrayList<>();
		var filter = new ConsigneeFilter();
		filter.setFilterPartyId(party.getId());
		while (true) {
			var searchResult = consigneeDao.searchEntities(filter);
			if (searchResult.getEntities().isEmpty()) {
				return consignees;
			}
			consignees.addAll(searchResult.getEntities());
			if (consignees.size() >= searchResult.getTotalSize()) {
				return consignees;
			}
			filter.setPageNumber(filter.getPageNumberOrDefault() + 1);
		}
	}

	@Override
	protected @NonNull InvoiceItemEntity createInvoiceItem(@NonNull WarehouseItemMovementEntity warehouseItemMovement) {
		var invoiceItem = super.createInvoiceItem(warehouseItemMovement);

		BigDecimal taxPercent = party.getTaxPercent();
		if (taxPercent == null) {
			taxPercent = requireNonNull(partyDefault.getTaxPercent(), "partyDefault.taxPercent");
		}

		PriceDto price = new PriceDto();
		price.setQuantity(requireNonNull(invoiceItem.getQuantity(), "invoiceItem.quantity"));
		price.setTaxPercent(taxPercent);
		price.setPriceSingleNet(determinePriceSingleNet(invoiceItem));
		price = priceMapper.calculateMissingProperties(price);

		invoiceItem.setTaxPercent(taxPercent);
		invoiceItem.setPriceTotalNet(price.getPriceTotalNet());
		invoiceItem.setPriceTotalGross(price.getPriceTotalGross());
		return invoiceItem;
	}

	protected BigDecimal determinePriceSingleNet(@NonNull InvoiceItemEntity invoiceItem) {
		BigDecimal productPriceSingleNet = readProductPriceSingleNet(invoiceItem);
		if (productPriceSingleNet == null) {
			return null;
		}
		BigDecimal tradeDiscountPercent = party.getTradeDiscountPercent();
		if (tradeDiscountPercent == null) {
			tradeDiscountPercent = requireNonNull(partyDefault.getTradeDiscountPercent(),
					"partyDefault.tradeDiscountPercent");
		}
		final BigDecimal _100 = BigDecimal.valueOf(100L);
		return productPriceSingleNet.multiply(_100.subtract(tradeDiscountPercent)).divide(_100, PRICE_SINGLE_SCALE,
				ROUNDING_MODE);
	}

	protected BigDecimal readProductPriceSingleNet(@NonNull InvoiceItemEntity invoiceItem) {
		try {
			PriceEntryResource priceEntryResource = liferayResourceFactory.getResource(PriceEntryResource.class);

			@NonNull
			Long catalogId = getCatalogId();

			BigDecimal result = null;

			List<Sku> skus = skuCache.getSkusBySku(invoiceItem.getWarehouseItemMovement().getSku());
			iterateSku: for (Sku sku : skus) {
				@NonNull
				Long skuId = sku.getId();
				Product product = getProduct(requireNonNull(sku.getProductId(), "sku.productId"));
				if (!catalogId.equals(product.getCatalogId())) {
					log.info(
							"determinePriceTotalNet: Skipping product with id={}, because its catalogId={} does not match configured catalogId={}!",
							product.getId(), product.getCatalogId(), catalogId);
					continue iterateSku;
				}
				iteratePriceList: for (PriceList priceList : getPriceLists()) {
					@NonNull
					Long priceListId = priceList.getId();
					String filterString = "skuId eq %d".formatted(skuId);
					Page<PriceEntry> priceEntriesPage = priceEntryResource.getPriceListIdPriceEntriesPage(priceListId,
							null, filterString, Pagination.of(1, 50), null);
					if (priceEntriesPage.getItems() == null || priceEntriesPage.getItems().isEmpty()) {
						log.warn(
								"determinePriceTotalNet: Skipping price-list with id={} and name='{}', because it does not contain skuId={}!",
								priceListId, priceList.getName(), skuId);
						continue iteratePriceList;
					}
					if (priceEntriesPage.getItems().size() != 1) {
						log.warn(
								"determinePriceTotalNet: Skipping price-list with id={} and name='{}', because it contains multiple price-entries for skuId={}!",
								priceListId, priceList.getName(), skuId);
						continue iteratePriceList;
					}
					// The property "netPrice" is broken on the client side! It is correctly sent by
					// the server (seen in Restfox), but it is always false here. We thus just
					// ignore it as we can rely on it being configured correctly.
//					boolean netPrice = Boolean.TRUE.equals(priceList.getNetPrice());
//					if (!netPrice) {
//						log.warn(
//								"determinePriceTotalNet: Skipping price-list with id={} and name='{}', because it is not configured for net prices (gross instead of net)!",
//								priceListId, priceList.getName());
//						continue iteratePriceList;
//					}
					for (PriceEntry priceEntry : priceEntriesPage.getItems()) {
						BigDecimal price = BigDecimal.valueOf(priceEntry.getPrice()); // idiots! using double :-(
						if (result == null || result.compareTo(price) < 0) {
							result = price;
						}
					}
				}
			}
			return result;
		} catch (Exception x) {
			log.error("determinePriceTotalNet: " + x, x);
			return null;
		}
	}

	protected Product getProduct(Long productId) throws Exception {
		Product product = productId2Product.get(productId);
		if (product == null) {
			ProductResource productResource = liferayResourceFactory.getResource(ProductResource.class);
			product = productResource.getProduct(productId);
			productId2Product.put(productId, product);
		}
		return product;
	}

	protected Long getCatalogId() throws Exception {
		if (catalogId == null) {
			catalogId = readCatalogId();
		}
		return catalogId;
	}

	protected Long readCatalogId() throws Exception {
		String catalogName = party.getCatalogName();
		if (isEmpty(catalogName)) {
			catalogName = requireNonEmpty(partyDefault.getCatalogName(), "partyDefault.catalogName");
			log.debug("getCatalogId: catalogName='{}' from partyDefault", catalogName);
		} else {
			log.debug("getCatalogId: catalogName='{}' from party[id={}]", catalogName, party.getId());
		}
		if (isEmpty(catalogName)) {
			throw new IllegalStateException(
					"No catalog-name configured for specific party or as default!".formatted(catalogName));
		}
		for (Catalog catalog : catalogCache.getCatalogs()) {
			if (catalogName.equals(catalog.getName())) {
				log.debug("getCatalogId: catalogId={}", catalog.getId());
				return catalog.getId();
			}
		}
		throw new IllegalStateException("No catalog found with name='%s'!".formatted(catalogName));
	}

	protected List<PriceList> getPriceLists() throws Exception {
		if (priceLists == null) {
			@NonNull
			Long catalogId = getCatalogId();

			priceLists = priceListCache.getPriceLists().stream().filter(pl -> catalogId.equals(pl.getCatalogId()))
					.collect(Collectors.toUnmodifiableList());
		}
		return priceLists;
	}

}
