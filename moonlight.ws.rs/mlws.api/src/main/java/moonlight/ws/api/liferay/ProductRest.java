package moonlight.ws.api.liferay;

import static jakarta.ws.rs.core.MediaType.*;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Product;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import moonlight.ws.api.RequiresAuthentication;

@Path("product")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface ProductRest {

	@GET
	@Path("{id}")
	Product getProduct(@PathParam("id") Long id) throws Exception;

	@GET
	LiferayDtoPage<Product> getProducts(@BeanParam ProductFilter filter) throws Exception;
}
