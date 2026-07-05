package moonlight.ws.api.liferay;

import static jakarta.ws.rs.core.MediaType.*;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import moonlight.ws.api.auth.RequiresAuthentication;
import moonlight.ws.api.auth.RequiresRole;
import moonlight.ws.api.auth.Role;

@Path("warehouse")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
@RequiresRole(Role.BASIC)
public interface WarehouseRest {

	@GET
	@Path("{id}")
	Warehouse getWarehouse(@PathParam("id") Long id) throws Exception;

	@GET
	LiferayDtoPage<Warehouse> getWarehousesPage(@BeanParam WarehouseFilter warehouseFilter) throws Exception;

	@GET
	@Path("by-externalReferenceCode/{externalReferenceCode}")
	Warehouse getWarehouseByExternalReferenceCode(@PathParam("externalReferenceCode") String externalReferenceCode)
			throws Exception;
}