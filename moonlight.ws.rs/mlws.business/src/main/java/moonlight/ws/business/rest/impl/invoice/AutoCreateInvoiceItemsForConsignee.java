package moonlight.ws.business.rest.impl.invoice;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.party.ConsigneeFilter;
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

		// TODO we need to find the price like this:
		// https://dragonkingchocolate.com:1443/liferay/o/api?endpoint=https://dragonkingchocolate.com:1443/liferay/o/headless-commerce-admin-pricing/v2.0/openapi.json
		// Liferay Commerce Admin Pricing API
		// => Maybe introduce a separate price-list?!? Need to evaluate...
		// Anyway, get the price-lists (maybe configure a specific one) and fetch the
		// price-entries. Maybe add a cache like the SkuCache.
//		List<Sku> skus;
//		try {
//			String sku = warehouseItemMovement.getSku();
//			skus = skuCache.getSkus().stream().filter(skuObj -> sku.equals(skuObj.getSku())).toList();
//		} catch (Exception e) {
//			log.warn("createInvoiceItem: Getting SKUs failed: " + e, e);
//		}
//		for (Sku sku : skus) {
//		}
		return invoiceItem;
	}
}
