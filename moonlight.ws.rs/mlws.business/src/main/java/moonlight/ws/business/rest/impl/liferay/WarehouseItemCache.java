package moonlight.ws.business.rest.impl.liferay;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Page;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseItemResource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.liferay.LiferayConfig;
import moonlight.ws.liferay.LiferayResourceFactory;

@ApplicationScoped
@Slf4j
public class WarehouseItemCache {

	@Inject
	private LiferayConfig liferayConfig;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	public static volatile long EXPIRY_MS = Long.MIN_VALUE;

	private static class CacheEntry {
		public List<WarehouseItem> warehouseItems;
		public long timestamp;

		public boolean isExpired() {
			return System.currentTimeMillis() - timestamp > EXPIRY_MS;
		}
	}

	private final Map<Long, CacheEntry> warehouseId2cacheEntry = new HashMap<>();

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
}
