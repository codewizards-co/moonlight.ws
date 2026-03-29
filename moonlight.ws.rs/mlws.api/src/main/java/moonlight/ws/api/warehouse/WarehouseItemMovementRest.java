package moonlight.ws.api.warehouse;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.NonNull;
import moonlight.ws.api.RequiresAuthentication;

@Path("warehouse-item-movement")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface WarehouseItemMovementRest {

	@GET
	@Path("{id}")
	WarehouseItemMovementDto getWarehouseItemMovement(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	WarehouseItemMovementDtoPage getWarehouseItemMovements(@BeanParam WarehouseItemMovementFilter filter)
			throws Exception;

	@POST
	WarehouseItemMovementDto createWarehouseItemMovement(@NonNull WarehouseItemMovementDto dto) throws Exception;

	@POST
	@Path("admin/sync-sku")
	void admin_syncSku() throws Exception;
}
