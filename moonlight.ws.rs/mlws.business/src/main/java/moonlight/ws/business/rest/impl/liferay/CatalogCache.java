package moonlight.ws.business.rest.impl.liferay;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Catalog;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.CatalogResource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.liferay.LiferayConfig;
import moonlight.ws.liferay.LiferayResourceFactory;

@ApplicationScoped
@Slf4j
public class CatalogCache {

	@Inject
	private LiferayConfig liferayConfig;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	private static volatile long EXPIRY_MS = Long.MIN_VALUE;

	private static class CacheEntry {
		public List<Catalog> catalogs;
		public long timestamp;

		public boolean isExpired() {
			return System.currentTimeMillis() - timestamp > EXPIRY_MS;
		}
	}

	private final CacheEntry cacheEntry = new CacheEntry();

	public List<Catalog> getCatalogs() throws Exception {
		if (EXPIRY_MS < 0) {
			EXPIRY_MS = liferayConfig.getCacheExpiryMs();
		}
		synchronized (cacheEntry) {
			if (cacheEntry.catalogs == null || cacheEntry.isExpired()) {
				cacheEntry.catalogs = requireNonNull(loadCatalogs(), "loadCatalogs(...)");
				cacheEntry.timestamp = System.currentTimeMillis();
			}
		}
		return cacheEntry.catalogs;
	}

	protected List<Catalog> loadCatalogs() throws Exception {
		log.debug("loadCatalogs");
		CatalogResource resource = liferayResourceFactory.getResource(CatalogResource.class);
		int pageNumber = 0;
		ArrayList<Catalog> catalogs = new ArrayList<>();
		while (true) {
			++pageNumber;
			Page<Catalog> liferayPage = resource.getCatalogsPage(null, null,
					Pagination.of(pageNumber, Filter.MAX_PAGE_SIZE), null);
			if (liferayPage.getItems() != null) {
				catalogs.addAll(liferayPage.getItems());
			}
			if (pageNumber >= liferayPage.getLastPage()) {
				break;
			}
		}
		catalogs.trimToSize();
		return Collections.unmodifiableList(catalogs);
	}
}