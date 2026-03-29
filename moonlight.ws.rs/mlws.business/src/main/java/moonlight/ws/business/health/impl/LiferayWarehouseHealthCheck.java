package moonlight.ws.business.health.impl;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Page;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import jakarta.enterprise.context.RequestScoped;
import moonlight.ws.base.health.HealthStatus;

@RequestScoped
public class LiferayWarehouseHealthCheck extends LiferayHealthCheck {

	public static final String NAME = "liferay.warehouse";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected HealthStatus _check() throws Exception {
		WarehouseResource resource = liferayResourceFactory.getResource(WarehouseResource.class);
		Page<Warehouse> warehousesPage = resource.getWarehousesPage(null, null, null, null);
		if (warehousesPage == null || warehousesPage.getItems() == null || warehousesPage.getItems().size() == 0) {
			return new HealthStatus(NAME, MALADY_CODE_NO_WAREHOUSE_FOUND, "Not a single warehouse was found.");
		}
		return null;
	}

}
