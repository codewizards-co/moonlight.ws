package moonlight.logistics.liferay;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.http.HttpInvoker;
import com.liferay.headless.commerce.admin.inventory.client.pagination.Page;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;
import moonlight.ws.liferay.LiferayConfig;
import moonlight.ws.liferay.LiferayResourceFactory;

/**
 * Tests the {@link WarehouseResource} and thus the
 * {@link LiferayResourceFactory} more in depth. This was dragged out, because
 * it contains large JSON-text and also it requires the VM-arguments
 *
 * <pre>
 * --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED
 * </pre>
 */
@ExtendWith(WeldJunit5Extension.class)
public class MockedWarehouseResourceTest {

	static MockedStatic<HttpInvoker> httpInvokerStatic;
	static HttpInvoker httpInvoker;
	static HttpInvoker.HttpResponse httpResponse;

	@WeldSetup
	public WeldInitiator weld = WeldInitiator //
			.from(LiferayResourceFactory.class) //
			.addBeans(createLiferayConfigBean()) //
			.activate(RequestScoped.class) //
			.build();

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	private Bean<?> createLiferayConfigBean() {
		return MockBean.builder().types(LiferayConfig.class).scope(ApplicationScoped.class)
				.create((creationalContext) -> {
					LiferayConfig liferayConfig = mock(LiferayConfig.class);
					when(liferayConfig.getUrl()).thenReturn("https://my-host.my-domain.com/liferay");
					when(liferayConfig.getUser()).thenReturn("my-daemon");
					when(liferayConfig.getPassword()).thenReturn("my-secret");
					return liferayConfig;
				}).build();
	}

	@BeforeAll
	static void beforeAll() throws Exception {
		httpInvoker = mock(HttpInvoker.class);
		httpResponse = mock(HttpInvoker.HttpResponse.class);
		httpInvokerStatic = mockStatic(HttpInvoker.class);
		httpInvokerStatic.when(HttpInvoker::newHttpInvoker).thenReturn(httpInvoker);
		when(httpInvoker.invoke()).thenReturn(httpResponse);
	}

	@AfterAll
	static void afterAll() throws Exception {
		if (httpInvokerStatic != null) {
			httpInvokerStatic.close();
			httpInvokerStatic = null;
		}
	}

	@Test
	void getWarehousesPage() throws Exception {
		WarehouseResource resource = liferayResourceFactory.getResource(WarehouseResource.class);
		assertThat(resource).isNotNull();

		when(httpResponse.getStatusCode()).thenReturn(200);
		when(httpResponse.getContentType()).thenReturn("application/json");
		when(httpResponse.getContent()).thenReturn(
				"""
						{
						  "actions": {},
						  "facets": [],
						  "items": [
						    {
						      "actions": {
						        "permissions": {
						          "method": "PATCH",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32380"
						        },
						        "get": {
						          "method": "GET",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses"
						        },
						        "update": {
						          "method": "PATCH",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32380"
						        },
						        "delete": {
						          "method": "DELETE",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32380"
						        }
						      },
						      "active": false,
						      "city": "Los Alamos",
						      "countryISOCode": "US",
						      "description": {},
						      "externalReferenceCode": "840NM87544",
						      "id": 32380,
						      "latitude": 35.887737,
						      "longitude": -106.326283,
						      "name": {
						        "en_US": "United States - Southwest"
						      },
						      "regionISOCode": "NM",
						      "street1": "1450 47th St",
						      "street2": "",
						      "street3": "",
						      "type": "",
						      "zip": "87544"
						    },
						    {
						      "actions": {
						        "permissions": {
						          "method": "PATCH",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32382"
						        },
						        "get": {
						          "method": "GET",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses"
						        },
						        "update": {
						          "method": "PATCH",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32382"
						        },
						        "delete": {
						          "method": "DELETE",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32382"
						        }
						      },
						      "active": false,
						      "city": "Middleburg",
						      "countryISOCode": "0",
						      "description": {},
						      "externalReferenceCode": "840VA50309",
						      "id": 32382,
						      "latitude": 40.741895,
						      "longitude": -73.989308,
						      "name": {
						        "en_US": "United States - Northeast"
						      },
						      "regionISOCode": "VA",
						      "street1": "36205 Snake Hill Rd",
						      "street2": "",
						      "street3": "",
						      "type": "",
						      "zip": "50309"
						    },
						    {
						      "actions": {
						        "permissions": {
						          "method": "PATCH",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32384"
						        },
						        "get": {
						          "method": "GET",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses"
						        },
						        "update": {
						          "method": "PATCH",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32384"
						        },
						        "delete": {
						          "method": "DELETE",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/32384"
						        }
						      },
						      "active": false,
						      "city": "Borgorose",
						      "countryISOCode": "0",
						      "description": {},
						      "externalReferenceCode": "380RI2021",
						      "id": 32384,
						      "latitude": 42.214601,
						      "longitude": 12.796434,
						      "name": {
						        "en_US": "Italy"
						      },
						      "regionISOCode": "",
						      "street1": "Via delle Coste 24",
						      "street2": "",
						      "street3": "",
						      "type": "",
						      "zip": "2021"
						    },
						    {
						      "actions": {
						        "permissions": {
						          "method": "PATCH",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/34549"
						        },
						        "get": {
						          "method": "GET",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses"
						        },
						        "update": {
						          "method": "PATCH",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/34549"
						        },
						        "delete": {
						          "method": "DELETE",
						          "href": "https://dragonkingchocolate.com:1443/liferay/liferay/o/headless-commerce-admin-inventory/v1.0/warehouses/34549"
						        }
						      },
						      "active": true,
						      "city": "Chiangmai",
						      "countryISOCode": "TH",
						      "description": {},
						      "externalReferenceCode": "e1409d48-3439-89f6-31ee-89ca317d85c7",
						      "id": 34549,
						      "latitude": 18.69571,
						      "longitude": 99.08155,
						      "name": {
						        "en_US": "Thailand - Chiangmai - Factory"
						      },
						      "regionISOCode": "50.0",
						      "street1": "84 M. 11",
						      "street2": "T. Buakkhang",
						      "street3": "A. San Kamphaeng",
						      "type": "",
						      "zip": "50130"
						    }
						  ],
						  "lastPage": 1,
						  "page": 1,
						  "pageSize": 20,
						  "totalCount": 4
						}""");

		Page<Warehouse> warehousesPage = resource.getWarehousesPage(null, null, null, null);
		assertThat(warehousesPage).isNotNull();
	}
}
