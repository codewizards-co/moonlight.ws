package moonlight.ws.api.liferay;

import static jakarta.ws.rs.core.MediaType.*;

import com.liferay.headless.admin.address.client.dto.v1_0.Country;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.NonNull;
import moonlight.ws.api.RequiresAuthentication;

@Path("country")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface CountryRest {

	@GET
	@Path("{id}")
	Country getCountry(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	@Path("by-a2/{a2}")
	Country getCountryByAlpha2(@NonNull @PathParam("a2") String a2) throws Exception;

	@GET
	@Path("by-a3/{a3}")
	Country getCountryByAlpha3(@NonNull @PathParam("a3") String a3) throws Exception;

	@GET
	LiferayDtoPage<Country> getCountries(@BeanParam CountryFilter filter) throws Exception;

}
