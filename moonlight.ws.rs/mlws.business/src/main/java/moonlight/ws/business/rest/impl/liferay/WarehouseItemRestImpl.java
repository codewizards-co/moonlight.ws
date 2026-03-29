package moonlight.ws.business.rest.impl.liferay;

import static java.util.Objects.*;
import static moonlight.ws.base.util.SortUtil.*;
import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.business.util.FilterUtil.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Page;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseItemResource;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.BadRequestException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.api.liferay.LiferayDtoPage;
import moonlight.ws.api.liferay.WarehouseItemDto;
import moonlight.ws.api.liferay.WarehouseItemFilter;
import moonlight.ws.api.liferay.WarehouseItemRest;
import moonlight.ws.liferay.LiferayResourceFactory;

@RequestScoped
@Transactional(TxType.SUPPORTS)
@Slf4j
public class WarehouseItemRestImpl implements WarehouseItemRest {

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Inject
	private WarehouseItemCache warehouseItemCache;

	@Override
	public WarehouseItem getWarehouseItem(@NonNull Long id) throws Exception {
		WarehouseItemResource resource = liferayResourceFactory.getResource(WarehouseItemResource.class);
		return resource.getWarehouseItem(id);
	}

	@Override
	public LiferayDtoPage<WarehouseItem> getWarehouseItemsPage(@NonNull WarehouseItemFilter filter) throws Exception {
		Long warehouseId = filter.getFilterWarehouseId();
		if (warehouseId == null) {
			throw new BadRequestException("filter.warehouseId is required!");
		}

		WarehouseItemResource resource = liferayResourceFactory.getResource(WarehouseItemResource.class);

		String sku = filter.getFilterSku();
		Map<String, Boolean> propName2Descending = getSortPropName2DescendingMap(filter);
		if (!isEmpty(sku) || !propName2Descending.isEmpty()) {
			Pattern pattern = getPatternIfRegex(sku);
			Stream<WarehouseItem> warehouseItemsFilteredStream = warehouseItemCache.getWarehouseItems(warehouseId)
					.stream();
			if (!isEmpty(sku)) {
				if (pattern == null) {
					warehouseItemsFilteredStream = warehouseItemsFilteredStream //
							.filter(whi -> equalsFilterValue(whi, WarehouseItem::getSku, sku));

				} else {
					warehouseItemsFilteredStream = warehouseItemsFilteredStream //
							.filter(whi -> matchesFilterValue(whi, WarehouseItem::getSku, pattern));
				}
			}
			if (!propName2Descending.isEmpty()) {
				warehouseItemsFilteredStream = warehouseItemsFilteredStream
						.sorted(new WarehouseItemComparator(propName2Descending));
			}
			List<WarehouseItem> warehouseItemsFiltered = warehouseItemsFilteredStream //
					.collect(Collectors.toList());
			return LiferayDtoPage.of(warehouseItemsFiltered, filter);
		}
		return LiferayDtoPage.of(resource.getWarehouseIdWarehouseItemsPage(warehouseId, filter.getPagination()));
	}

	private static class WarehouseItemComparator implements Comparator<WarehouseItem> {

		@NonNull
		private Map<String, Boolean> propName2Descending;

		public WarehouseItemComparator(@NonNull Map<String, Boolean> propName2Descending) {
			this.propName2Descending = propName2Descending;
		}

		@Override
		public int compare(WarehouseItem o1, WarehouseItem o2) {
			if (o1.getId() != null && o1.getId().equals(o2.getId())) {
				return 0;
			}
			for (Map.Entry<String, Boolean> me : propName2Descending.entrySet()) {
				var propName = me.getKey();
				int res = 0;
				boolean descending = me.getValue();
				if ("id".equalsIgnoreCase(propName)) {
					comparePropValue(o1.getId(), o2.getId());
				} else if ("externalReferenceCode".equalsIgnoreCase(propName)) {
					res = comparePropValue(o1.getExternalReferenceCode(), o2.getExternalReferenceCode());
				} else if ("sku".equalsIgnoreCase(propName)) {
					res = comparePropValue(o1.getSku(), o2.getSku());
				}
				if (descending) {
					res = -1 * res;
				}
				if (res != 0) {
					return res;
				}
			}
			return comparePropValue(o1.getId(), o2.getId());
		}
	}

	protected static int comparePropValue(String v1, String v2) {
		return nullToEmpty(v1).compareTo(nullToEmpty(v2));
	}

	protected static int comparePropValue(Long v1, Long v2) {
		if (v1 == null) {
			return v2 == null ? 0 : -1;
		}
		if (v2 == null) {
			return 1;
		}
		return v1.compareTo(v2);
	}

	@Override
	public WarehouseItem createWarehouseItem(@NonNull WarehouseItemDto warehouseItem) throws Exception {
		if (warehouseItem.getWarehouseId() == null) {
			throw new BadRequestException("warehouseId missing/empty!");
		}
		if (isEmpty(warehouseItem.getSku())) {
			throw new BadRequestException("sku missing/empty!");
		}
		var oldWarehouseItem = requireNonNull(findWarehouseItemBySkuInAnyWarehouse(warehouseItem.getSku()),
				"oldWarehouseItem");
		warehouseItem.setUnitOfMeasureKey(oldWarehouseItem.getUnitOfMeasureKey()); // we force the correct value!
		WarehouseItemResource resource = liferayResourceFactory.getResource(WarehouseItemResource.class);
		var newWarehouseItem = resource.postWarehouseIdWarehouseItem(warehouseItem.getWarehouseId(), warehouseItem);
		warehouseItemCache.clear(newWarehouseItem.getWarehouseId());
		return newWarehouseItem;
	}

	protected WarehouseItem findWarehouseItemBySkuInAnyWarehouse(@NonNull String sku) throws Exception {
		WarehouseResource resource = liferayResourceFactory.getResource(WarehouseResource.class);
		int warehousePageNumber = 0;
		while (true) {
			++warehousePageNumber;
			Page<Warehouse> warehousesPage = resource.getWarehousesPage(null, null,
					Pagination.of(warehousePageNumber, Filter.MAX_PAGE_SIZE), null);
			if (warehousesPage.getItems() != null) {
				for (Warehouse warehouse : warehousesPage.getItems()) {
					WarehouseItem warehouseItem = warehouseItemCache.getWarehouseItems(warehouse.getId()).stream()
							.filter(wi -> sku.equals(wi.getSku())).findAny().orElse(null);
					if (warehouseItem != null) {
						return warehouseItem;
					}
				}
			}
			if (warehousePageNumber > warehousesPage.getLastPage()) {
				throw new BadRequestException("sku unknown: " + sku);
			}
		}
	}
}
