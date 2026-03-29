package moonlight.ws.business.rest.impl.liferay;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.SkuResource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.liferay.LiferayConfig;
import moonlight.ws.liferay.LiferayResourceFactory;

@ApplicationScoped
@Slf4j
public class SkuCache {

	@Inject
	private LiferayConfig liferayConfig;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	private static volatile long EXPIRY_MS = Long.MIN_VALUE;

	private static class CacheEntry {
		public List<Sku> skus;
		public long timestamp;

		public boolean isExpired() {
			return System.currentTimeMillis() - timestamp > EXPIRY_MS;
		}
	}

	private final CacheEntry cacheEntry = new CacheEntry();

	public List<Sku> getSkus() throws Exception {
		if (EXPIRY_MS < 0) {
			EXPIRY_MS = liferayConfig.getCacheExpiryMs();
		}
		synchronized (cacheEntry) {
			if (cacheEntry.skus == null || cacheEntry.isExpired()) {
				cacheEntry.skus = requireNonNull(loadSkus(), "loadSkus(...)");
				cacheEntry.timestamp = System.currentTimeMillis();
			}
		}
		return cacheEntry.skus;
	}

	protected List<Sku> loadSkus() throws Exception {
		log.debug("loadSkus");
		SkuResource resource = liferayResourceFactory.getResource(SkuResource.class);
		int pageNumber = 0;
		ArrayList<Sku> skus = new ArrayList<>();
		while (true) {
			++pageNumber;
			Page<Sku> liferayPage = resource.getSkusPage(null, null, Pagination.of(pageNumber, Filter.MAX_PAGE_SIZE),
					null);
			if (liferayPage.getItems() != null) {
				skus.addAll(liferayPage.getItems());
			}
			if (pageNumber >= liferayPage.getLastPage()) {
				break;
			}
		}
		skus.trimToSize();
		return skus;
	}
}