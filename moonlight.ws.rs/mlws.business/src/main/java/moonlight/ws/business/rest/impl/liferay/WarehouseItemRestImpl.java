package moonlight.ws.business.rest.impl.liferay;

import static java.util.Objects.*;
import static moonlight.ws.api.RestConst.*;
import static moonlight.ws.api.liferay.WarehouseItemFilter.*;
import static moonlight.ws.base.util.FetchUtil.*;
import static moonlight.ws.base.util.JsonUtil.*;
import static moonlight.ws.base.util.SortUtil.*;
import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.business.util.FilterUtil.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import jakarta.ws.rs.QueryParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.api.liferay.LiferayDtoPage;
import moonlight.ws.api.liferay.WarehouseItemDto;
import moonlight.ws.api.liferay.WarehouseItemFilter;
import moonlight.ws.api.liferay.WarehouseItemRest;
import moonlight.ws.api.warehouse.WarehouseItemProductDto;
import moonlight.ws.liferay.LiferayResourceFactory;

@RequestScoped
@Transactional(TxType.SUPPORTS)
@Slf4j
public class WarehouseItemRestImpl implements WarehouseItemRest {

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Inject
	private WarehouseItemCache warehouseItemCache;

	@Inject
	private SkuCache skuCache;

	@QueryParam(QUERY_FETCH)
	protected String fetch;

	@Override
	public WarehouseItemDto getWarehouseItem(@NonNull Long id) throws Exception {
		WarehouseItemResource resource = liferayResourceFactory.getResource(WarehouseItemResource.class);
		return fetchRelations(toWarehouseItemDto(resource.getWarehouseItem(id)));
	}

	@Override
	public LiferayDtoPage<WarehouseItemDto> getWarehouseItemsPage(@NonNull WarehouseItemFilter filter) throws Exception {
		Long warehouseId = filter.getFilterWarehouseId();
		if (warehouseId == null) {
			throw new BadRequestException("filter.warehouseId is required!");
		}

		WarehouseItemResource resource = liferayResourceFactory.getResource(WarehouseItemResource.class);

		String sku = filter.getFilterSku();
		String productName = filter.getFilterProductName();
		Map<String, Boolean> propName2Descending = getSortPropName2DescendingMap(filter);
		boolean includeInternal = Boolean.TRUE.equals(filter.getFilterIncludeInternal());
		if (!isEmpty(sku) || !isEmpty(productName) || !propName2Descending.isEmpty() || !includeInternal) {
			Pattern skuPattern = getPatternIfRegex(sku);
			Pattern productNamePattern = getPatternIfRegex(productName);
			Stream<WarehouseItem> warehouseItemsFilteredStream = warehouseItemCache.getWarehouseItems(warehouseId)
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
			List<WarehouseItem> warehouseItemsFiltered = warehouseItemsFilteredStream //
					.collect(Collectors.toList());
			return fetchRelations(toWarehouseItemDtoPage(LiferayDtoPage.of(warehouseItemsFiltered, filter)));
		}
		return fetchRelations(toWarehouseItemDtoPage(LiferayDtoPage.of(resource.getWarehouseIdWarehouseItemsPage(warehouseId, filter.getPagination()))));
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
	public WarehouseItemDto createWarehouseItem(@NonNull WarehouseItemDto warehouseItem) throws Exception {
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
		return fetchRelations(toWarehouseItemDto(newWarehouseItem));
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

	protected LiferayDtoPage<WarehouseItemDto> toWarehouseItemDtoPage(@NonNull LiferayDtoPage<WarehouseItem> page) {
		LiferayDtoPage<WarehouseItemDto> resultPage = jsonClone(page, WarehouseItemDto.class);
		return resultPage;
	}

	protected WarehouseItemDto toWarehouseItemDto(WarehouseItem warehouseItem) {
		return jsonClone(warehouseItem, WarehouseItemDto.class);
	}

	protected @NonNull LiferayDtoPage<WarehouseItemDto> fetchRelations(@NonNull LiferayDtoPage<WarehouseItemDto> dtoPage)
			throws Exception {
		if (!isEmpty(fetch)) {
			for (var dto : dtoPage.getItems()) {
				fetchRelations(dto);
			}
		}
		return dtoPage;
	}

	protected @NonNull WarehouseItemDto fetchRelations(@NonNull WarehouseItemDto dto) throws Exception {
		if (!isEmpty(fetch)) {
			Set<String> fetchSet = getFetchSet(fetch);
			if (fetchSet.contains("products")) {
				fetchProducts(dto);
			}
		}
		return dto;
	}

	protected void fetchProducts(@NonNull WarehouseItemDto dto) throws Exception {
		final String sku = requireNonNull(dto.getSku(), "dto.sku");
		dto.setProducts(getWarehouseItemProductDtos(sku));
	}

	protected List<WarehouseItemProductDto> getWarehouseItemProductDtos(@NonNull final String sku) throws Exception {
		final List<WarehouseItemProductDto> products = new ArrayList<>();
		skuCache.getSkus().stream().filter(s -> sku.equals(s.getSku())).forEach(skuObj -> {
			products.add(new WarehouseItemProductDto(skuObj.getProductId(), skuObj.getProductName()));
		});
		return products;
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
		List<WarehouseItemProductDto> products = getWarehouseItemProductDtos(sku);
		List<String> result = new ArrayList<String>();
		for (WarehouseItemProductDto product : products) {
			result.addAll(product.getProductName().values());
		}
		return result;
	}
}
