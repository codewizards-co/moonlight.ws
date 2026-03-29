package moonlight.ws.api.liferay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import moonlight.ws.api.AbstractDtoPage;
import moonlight.ws.api.Filter;

@Getter
@Setter
public class LiferayDtoPage<T> extends AbstractDtoPage<T> {

	private Map<String, Map<String, String>> actions;
//	private List<Facet> facets = new ArrayList<>();

	public static <T> LiferayDtoPage<T> of(@NonNull com.liferay.headless.admin.address.client.pagination.Page<T> page) {
		LiferayDtoPage<T> result = new LiferayDtoPage<T>();
		result.setPageNumber(page.getPage());
		result.setPageSize(page.getPageSize());
		result.setTotalSize(page.getTotalCount());
		result.setLastPageNumber(page.getLastPage());
		if (page.getItems() != null) {
			result.setItems(new ArrayList<T>(page.getItems()));
		}
		result.setActions(page.getActions());
//		result.setFacets(page.getFacets());
		return result;
	}

	public static <T> LiferayDtoPage<T> of(
			@NonNull com.liferay.headless.commerce.admin.catalog.client.pagination.Page<T> page) {
		LiferayDtoPage<T> result = new LiferayDtoPage<T>();
		result.setPageNumber(page.getPage());
		result.setPageSize(page.getPageSize());
		result.setTotalSize(page.getTotalCount());
		result.setLastPageNumber(page.getLastPage());
		if (page.getItems() != null) {
			result.setItems(new ArrayList<T>(page.getItems()));
		}
		result.setActions(page.getActions());
//		result.setFacets(page.getFacets());
		return result;
	}

	public static <T> LiferayDtoPage<T> of(
			@NonNull com.liferay.headless.commerce.admin.inventory.client.pagination.Page<T> page) {
		LiferayDtoPage<T> result = new LiferayDtoPage<T>();
		result.setPageNumber(page.getPage());
		result.setPageSize(page.getPageSize());
		result.setTotalSize(page.getTotalCount());
		result.setLastPageNumber(page.getLastPage());
		if (page.getItems() != null) {
			result.setItems(new ArrayList<T>(page.getItems()));
		}
		result.setActions(page.getActions());
//		result.setFacets(page.getFacets());
		return result;
	}

	public static <T> LiferayDtoPage<T> of(@NonNull List<T> list, @NonNull Filter filter) {
		int fromItemIndexIncl = filter.getPageSizeOrDefault() * (filter.getPageNumberOrDefault() - 1);
		int toItemIndexExcl = fromItemIndexIncl + filter.getPageSizeOrDefault();
		List<T> subListForPage;
		if (fromItemIndexIncl >= list.size()) {
			subListForPage = new ArrayList<>(0);
		} else {
			toItemIndexExcl = Math.min(toItemIndexExcl, list.size());
			subListForPage = list.subList(fromItemIndexIncl, toItemIndexExcl);
		}
		LiferayDtoPage<T> page = new LiferayDtoPage<>();
		page.setTotalSize(list.size());
		page.setItems(subListForPage);
		page.setPageSize(filter.getPageSizeOrDefault());
		page.setPageNumber(filter.getPageNumberOrDefault());
		int lastPageNumber = list.size() / filter.getPageSizeOrDefault();
		if (lastPageNumber * filter.getPageSizeOrDefault() < list.size()) {
			++lastPageNumber;
		}
		page.setLastPageNumber(lastPageNumber);
		return page;
	}

	public Map<String, Map<String, String>> getActions() {
		if (actions == null) {
			actions = new LinkedHashMap<>();
		}
		return actions;
	}

//	public List<Facet> getFacets() {
//		if (facets == null) {
//			facets = new ArrayList<Facet>();
//		}
//		return facets;
//	}
}
