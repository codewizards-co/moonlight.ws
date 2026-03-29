package moonlight.ws.business.rest.impl.liferay;

import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.business.util.FilterUtil.*;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.SkuResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import moonlight.ws.api.liferay.LiferayDtoPage;
import moonlight.ws.api.liferay.SkuFilter;
import moonlight.ws.api.liferay.SkuRest;
import moonlight.ws.liferay.LiferayResourceFactory;

@RequestScoped
@Transactional(TxType.SUPPORTS)
public class SkuRestImpl implements SkuRest {

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Inject
	private SkuCache skuCache;

	@Override
	public Sku getSku(Long id) throws Exception {
		SkuResource resource = liferayResourceFactory.getResource(SkuResource.class);
		return resource.getSku(id);
	}

	@Override
	public LiferayDtoPage<Sku> getSkusPage(SkuFilter filter) throws Exception {
		SkuResource resource = liferayResourceFactory.getResource(SkuResource.class);
		String sku = filter.getFilterSku();
		if (!isEmpty(sku)) {
			Pattern pattern = getPatternIfRegex(sku);
			List<Sku> skusFiltered;
			if (pattern == null) {
				skusFiltered = skuCache.getSkus().stream() //
						.filter(whi -> equalsFilterValue(whi, Sku::getSku, sku)) //
						.collect(Collectors.toList());
			} else {
				skusFiltered = skuCache.getSkus().stream() //
						.filter(whi -> matchesFilterValue(whi, Sku::getSku, pattern)) //
						.collect(Collectors.toList());
			}
			return LiferayDtoPage.of(skusFiltered, filter);
		}
		return LiferayDtoPage.of(
				resource.getSkusPage(filter.getSearch(), filter.getFilter(), filter.getPagination(), filter.getSort()));
	}

}
