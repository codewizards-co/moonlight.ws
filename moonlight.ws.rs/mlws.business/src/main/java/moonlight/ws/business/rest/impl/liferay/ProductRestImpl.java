package moonlight.ws.business.rest.impl.liferay;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Product;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.ProductResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import moonlight.ws.api.liferay.LiferayDtoPage;
import moonlight.ws.api.liferay.ProductFilter;
import moonlight.ws.api.liferay.ProductRest;
import moonlight.ws.liferay.LiferayResourceFactory;

@RequestScoped
@Transactional(TxType.SUPPORTS)
public class ProductRestImpl implements ProductRest {

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Override
	public Product getProduct(Long id) throws Exception {
		ProductResource resource = liferayResourceFactory.getResource(ProductResource.class);
		return resource.getProduct(id);
	}

	@Override
	public LiferayDtoPage<Product> getProducts(ProductFilter filter) throws Exception {
		ProductResource resource = liferayResourceFactory.getResource(ProductResource.class);
		return LiferayDtoPage.of(resource.getProductsPage(filter.getSearch(), filter.getFilter(),
				filter.getPagination(), filter.getSort()));
	}

}
