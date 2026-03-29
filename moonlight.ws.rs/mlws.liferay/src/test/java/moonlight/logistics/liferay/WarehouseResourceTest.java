package moonlight.logistics.liferay;

import static org.assertj.core.api.Assertions.*;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Page;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import moonlight.ws.liferay.LiferayConfig;

@Disabled("only for local dev-tests -- for now")
public class WarehouseResourceTest {

	private LiferayConfig liferayConfig;

	@BeforeEach
	void before() {
		liferayConfig = new LiferayConfig();
	}

	@Test
	void readWarehouses() throws Exception {
		URL url = new URL(liferayConfig.getUrl());
		WarehouseResource warehouseResource = WarehouseResource.builder() //
				.endpoint(url.getHost(), url.getPort(), url.getProtocol()) //
				.contextPath(url.getPath()) //
				.authentication(liferayConfig.getUser(), liferayConfig.getPassword()) //
				.build();
		assertThat(warehouseResource).isNotNull();

		Page<Warehouse> warehousesPage = warehouseResource.getWarehousesPage(null, null, Pagination.of(0, 10000), null);
		assertThat(warehousesPage).isNotNull();
		assertThat(warehousesPage.getItems()).isNotNull();
		assertThat(warehousesPage.getItems().size()).isGreaterThanOrEqualTo(1);
		for (Warehouse warehouse : warehousesPage.getItems()) {
			assertThat(warehouse).isNotNull();
			assertThat(warehouse.getId()).isNotNull();
			assertThat(warehouse.getActive()).isNotNull();
		}
	}

}
