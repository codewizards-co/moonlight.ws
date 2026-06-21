package moonlight.ws.business.rest.impl.liferay;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceList;
import com.liferay.headless.commerce.admin.pricing.client.pagination.Page;
import com.liferay.headless.commerce.admin.pricing.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.pricing.client.resource.v2_0.PriceListResource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.liferay.LiferayConfig;
import moonlight.ws.liferay.LiferayResourceFactory;

@ApplicationScoped
@Slf4j
public class PriceListCache {

	@Inject
	private LiferayConfig liferayConfig;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	private static volatile long EXPIRY_MS = Long.MIN_VALUE;

	private static class CacheEntry {
		public List<PriceList> priceLists;
		public long timestamp;

		public boolean isExpired() {
			return System.currentTimeMillis() - timestamp > EXPIRY_MS;
		}
	}

	private final CacheEntry cacheEntry = new CacheEntry();

	public List<PriceList> getPriceLists() throws Exception {
		if (EXPIRY_MS < 0) {
			EXPIRY_MS = liferayConfig.getCacheExpiryMs();
		}
		synchronized (cacheEntry) {
			if (cacheEntry.priceLists == null || cacheEntry.isExpired()) {
				cacheEntry.priceLists = requireNonNull(loadPriceLists(), "loadPriceLists(...)");
				cacheEntry.timestamp = System.currentTimeMillis();
			}
		}
		return cacheEntry.priceLists;
	}

	protected List<PriceList> loadPriceLists() throws Exception {
		log.debug("loadPriceLists");
		PriceListResource resource = liferayResourceFactory.getResource(PriceListResource.class);
		int pageNumber = 0;
		ArrayList<PriceList> priceLists = new ArrayList<>();
		while (true) {
			++pageNumber;
			Page<PriceList> liferayPage = resource.getPriceListsPage(null, null,
					Pagination.of(pageNumber, Filter.MAX_PAGE_SIZE), null);
			if (liferayPage.getItems() != null) {
				priceLists.addAll(liferayPage.getItems());
			}
			if (pageNumber >= liferayPage.getLastPage()) {
				break;
			}
		}
		priceLists.trimToSize();
		return Collections.unmodifiableList(priceLists);
	}
}