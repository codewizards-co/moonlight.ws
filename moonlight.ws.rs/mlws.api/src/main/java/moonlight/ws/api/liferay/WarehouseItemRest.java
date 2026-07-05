package moonlight.ws.api.liferay;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.NonNull;
import moonlight.ws.api.auth.RequiresAuthentication;
import moonlight.ws.api.auth.RequiresRole;
import moonlight.ws.api.auth.Role;

@Path("warehouse-item")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
@RequiresRole(Role.BASIC)
public interface WarehouseItemRest {

	@GET
	@Path("{id}")
	WarehouseItemDto getWarehouseItem(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	LiferayDtoPage<WarehouseItemDto> getWarehouseItemsPage(@NonNull @BeanParam WarehouseItemFilter filter)
			throws Exception;

	@POST
	WarehouseItemDto createWarehouseItem(@NonNull WarehouseItemDto warehouseItem) throws Exception;
}
