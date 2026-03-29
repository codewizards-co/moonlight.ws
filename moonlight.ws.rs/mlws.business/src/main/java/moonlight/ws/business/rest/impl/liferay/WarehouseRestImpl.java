package moonlight.ws.business.rest.impl.liferay;

import static moonlight.ws.base.util.StringUtil.*;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.liferay.LiferayDtoPage;
import moonlight.ws.api.liferay.WarehouseFilter;
import moonlight.ws.api.liferay.WarehouseRest;
import moonlight.ws.liferay.LiferayResourceFactory;

@RequestScoped
@Transactional(TxType.SUPPORTS)
@Slf4j
public class WarehouseRestImpl implements WarehouseRest {

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Override
	public Warehouse getWarehouse(Long id) throws Exception {
		WarehouseResource resource = liferayResourceFactory.getResource(WarehouseResource.class);
		return resource.getWarehouseId(id);
	}

	@Override
	public Warehouse getWarehouseByExternalReferenceCode(String externalReferenceCode) throws Exception {
		WarehouseResource resource = liferayResourceFactory.getResource(WarehouseResource.class);
		return resource.getWarehouseByExternalReferenceCode(externalReferenceCode);
	}

	@Override
	public LiferayDtoPage<Warehouse> getWarehousesPage(WarehouseFilter warehouseFilter) throws Exception {
		WarehouseResource resource = liferayResourceFactory.getResource(WarehouseResource.class);
		String filterString = warehouseFilter.getFilter();
		if (warehouseFilter.getFilterActive() != null) {
			if (!isEmpty(filterString)) {
				throw new BadRequestException("Cannot combine query-params 'filter.___' and 'filter'!");
			}
			filterString = "active eq " + warehouseFilter.getFilterActive();
		}
		return LiferayDtoPage.of(resource.getWarehousesPage(warehouseFilter.getSearch(), filterString,
				warehouseFilter.getPagination(), warehouseFilter.getSort()));
	}
}
