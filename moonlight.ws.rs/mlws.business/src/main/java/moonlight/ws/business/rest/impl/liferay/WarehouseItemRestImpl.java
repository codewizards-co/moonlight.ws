package moonlight.ws.business.rest.impl.liferay;

import static java.util.Objects.*;
import static moonlight.ws.api.RestConst.*;
import static moonlight.ws.base.util.FetchUtil.*;
import static moonlight.ws.base.util.JsonUtil.*;
import static moonlight.ws.base.util.SortUtil.*;
import static moonlight.ws.base.util.StringUtil.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
			List<WarehouseItem> warehouseItems = warehouseItemCache.getWarehouseItems(filter);
			return fetchRelations(toWarehouseItemDtoPage(LiferayDtoPage.of(warehouseItems, filter)));
		}
		return fetchRelations(toWarehouseItemDtoPage(LiferayDtoPage.of(resource.getWarehouseIdWarehouseItemsPage(warehouseId, filter.getPagination()))));
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
		dto.setProducts(skuCache.getWarehouseItemProductDtos(sku));
	}
}
