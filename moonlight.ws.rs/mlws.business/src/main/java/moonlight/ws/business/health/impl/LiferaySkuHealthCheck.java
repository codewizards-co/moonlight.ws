package moonlight.ws.business.health.impl;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.SkuResource;

import jakarta.enterprise.context.RequestScoped;
import moonlight.ws.base.health.HealthStatus;

@RequestScoped
public class LiferaySkuHealthCheck extends LiferayHealthCheck {

	public static final String NAME = "liferay.sku";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected HealthStatus _check() throws Exception {
		SkuResource resource = liferayResourceFactory.getResource(SkuResource.class);
		Page<Sku> skusPage = resource.getSkusPage(null, null, null, null);
		if (skusPage == null || skusPage.getItems() == null || skusPage.getItems().size() == 0) {
			return new HealthStatus(NAME, MALADY_CODE_NO_SKU_FOUND, "Not a single SKU was found.");
		}
		return null;
	}

}
