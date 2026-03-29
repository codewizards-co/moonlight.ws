package moonlight.ws.business.rest.impl.liferay;

import com.liferay.headless.admin.address.client.dto.v1_0.Country;
import com.liferay.headless.admin.address.client.resource.v1_0.CountryResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.NonNull;
import moonlight.ws.api.liferay.CountryFilter;
import moonlight.ws.api.liferay.CountryRest;
import moonlight.ws.api.liferay.LiferayDtoPage;
import moonlight.ws.liferay.LiferayResourceFactory;

@RequestScoped
@Transactional(TxType.SUPPORTS)
public class CountryRestImpl implements CountryRest {

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Override
	public Country getCountry(@NonNull Long id) throws Exception {
		CountryResource resource = liferayResourceFactory.getResource(CountryResource.class);
		return resource.getCountry(id);
	}

	@Override
	public Country getCountryByAlpha2(@NonNull String a2) throws Exception {
		CountryResource resource = liferayResourceFactory.getResource(CountryResource.class);
		return resource.getCountryByA2(a2);
	}

	@Override
	public Country getCountryByAlpha3(@NonNull String a3) throws Exception {
		CountryResource resource = liferayResourceFactory.getResource(CountryResource.class);
		return resource.getCountryByA3(a3);
	}

	@Override
	public LiferayDtoPage<Country> getCountries(CountryFilter filter) throws Exception {
		CountryResource resource = liferayResourceFactory.getResource(CountryResource.class);
		return LiferayDtoPage.of(resource.getCountriesPage(filter.getFilterActive(), filter.getSearch(),
				filter.getPagination(), filter.getSort()));
	}
}
