package moonlight.ws.business.rest.impl.liferay;

import static java.util.Objects.*;
import static moonlight.ws.api.liferay.WarehouseItemFilter.*;
import static moonlight.ws.base.util.SortUtil.*;
import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.business.util.FilterUtil.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Page;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseItemResource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.api.liferay.WarehouseItemFilter;
import moonlight.ws.api.warehouse.WarehouseItemProductDto;
import moonlight.ws.liferay.LiferayConfig;
import moonlight.ws.liferay.LiferayResourceFactory;

@ApplicationScoped
@Slf4j
public class WarehouseItemCache {

	@Inject
	private LiferayConfig liferayConfig;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Inject
	private SkuCache skuCache;

	public static volatile long EXPIRY_MS = Long.MIN_VALUE;

	private static class CacheEntry {
		public List<WarehouseItem> warehouseItems;
		public long timestamp;

		public boolean isExpired() {
			return System.currentTimeMillis() - timestamp > EXPIRY_MS;
		}
	}

	private final Map<Long, CacheEntry> warehouseId2cacheEntry = new HashMap<>();

	public List<WarehouseItem> getWarehouseItems(@NonNull WarehouseItemFilter filter) throws Exception {
		Long warehouseId = requireNonNull(filter.getFilterWarehouseId(), "filter.filterWarehouseId");
		String sku = filter.getFilterSku();
		String productName = filter.getFilterProductName();
		Map<String, Boolean> propName2Descending = getSortPropName2DescendingMap(filter);
		boolean includeInternal = Boolean.TRUE.equals(filter.getFilterIncludeInternal());
		Pattern skuPattern = getPatternIfRegex(sku);
		Pattern productNamePattern = getPatternIfRegex(productName);
		Stream<WarehouseItem> warehouseItemsFilteredStream = getWarehouseItems(warehouseId)
				.stream();
		if (!isEmpty(sku)) {
			if (skuPattern == null) {
				warehouseItemsFilteredStream = warehouseItemsFilteredStream //
						.filter(whi -> equalsFilterValue(whi, WarehouseItem::getSku, sku));

			} else {
				warehouseItemsFilteredStream = warehouseItemsFilteredStream //
						.filter(whi -> matchesFilterValue(whi, WarehouseItem::getSku, skuPattern));
			}
		}
		if (!includeInternal) {
			warehouseItemsFilteredStream = warehouseItemsFilteredStream //
					.filter(whi -> !isInternalSku(whi.getSku()));
		}
		if (!isEmpty(productName)) {
			if (productNamePattern == null) {
				warehouseItemsFilteredStream = warehouseItemsFilteredStream //
						.filter(whi -> equalsFilterValue(whi, whi2 -> _getProductNames(whi2), productName));
			} else {
				warehouseItemsFilteredStream = warehouseItemsFilteredStream //
						.filter(whi -> matchesFilterValueI18n(whi, whi2 -> _getProductNames(whi2), productNamePattern));
			}
		}
		if (!propName2Descending.isEmpty()) {
			warehouseItemsFilteredStream = warehouseItemsFilteredStream
					.sorted(new WarehouseItemComparator(propName2Descending));
		}
		return warehouseItemsFilteredStream.collect(Collectors.toList());
	}

	protected List<String> _getProductNames(@NonNull WarehouseItem warehouseItem) {
		try {
			return getProductNames(warehouseItem);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected List<String> getProductNames(@NonNull WarehouseItem warehouseItem) throws Exception {
		final String sku = requireNonNull(warehouseItem.getSku(), "warehouseItem.sku");
		List<WarehouseItemProductDto> products = skuCache.getWarehouseItemProductDtos(sku);
		List<String> result = new ArrayList<String>();
		for (WarehouseItemProductDto product : products) {
			result.addAll(product.getProductName().values());
		}
		return result;
	}

	private boolean isInternalSku(String sku) {
		if (sku == null) {
			return false;
		}
		for (Pattern pattern : INTERNAL_SKU_PATTERNS) {
			if (pattern.matcher(sku).matches()) {
				return true;
			}
		}
		return false;
	}

	public List<WarehouseItem> getWarehouseItems(@NonNull Long warehouseId) throws Exception {
		if (EXPIRY_MS < 0) {
			EXPIRY_MS = liferayConfig.getCacheExpiryMs();
		}
		CacheEntry cacheEntry;
		synchronized (warehouseId2cacheEntry) {
			cacheEntry = warehouseId2cacheEntry.computeIfAbsent(warehouseId, id -> new CacheEntry());
		}
		synchronized (cacheEntry) {
			if (cacheEntry.warehouseItems == null || cacheEntry.isExpired()) {
				cacheEntry.warehouseItems = requireNonNull(loadWarehouseItems(warehouseId), "loadWarehouseItems(...)");
				cacheEntry.timestamp = System.currentTimeMillis();
			}
		}
		return cacheEntry.warehouseItems;
	}

	protected List<WarehouseItem> loadWarehouseItems(@NonNull Long warehouseId) throws Exception {
		log.debug("loadWarehouseItems: warehouseId={}", warehouseId);
		WarehouseItemResource resource = liferayResourceFactory.getResource(WarehouseItemResource.class);
		int pageNumber = 0;
		ArrayList<WarehouseItem> warehouseItems = new ArrayList<>();
		while (true) {
			++pageNumber;
			Page<WarehouseItem> liferayPage = resource.getWarehouseIdWarehouseItemsPage(warehouseId,
					Pagination.of(pageNumber, Filter.MAX_PAGE_SIZE));
			if (liferayPage.getItems() != null) {
				warehouseItems.addAll(liferayPage.getItems());
			}
			if (pageNumber >= liferayPage.getLastPage()) {
				break;
			}
		}
		warehouseItems.trimToSize();
		return warehouseItems;
	}

	public void clear(Long warehouseId) {
		synchronized (warehouseId2cacheEntry) {
			if (warehouseId == null) {
				warehouseId2cacheEntry.clear();
			} else {
				warehouseId2cacheEntry.remove(warehouseId);
			}
		}
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
}
