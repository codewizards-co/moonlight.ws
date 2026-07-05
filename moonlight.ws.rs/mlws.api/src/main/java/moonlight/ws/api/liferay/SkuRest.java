package moonlight.ws.api.liferay;

import static jakarta.ws.rs.core.MediaType.*;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import moonlight.ws.api.auth.RequiresAuthentication;
import moonlight.ws.api.auth.RequiresRole;
import moonlight.ws.api.auth.Role;

@Path("sku")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
@RequiresRole(Role.BASIC)
public interface SkuRest {

	@GET
	@Path("{id}")
	Sku getSku(@PathParam("id") Long id) throws Exception;

	@GET
	LiferayDtoPage<Sku> getSkusPage(@BeanParam SkuFilter filter) throws Exception;
}
