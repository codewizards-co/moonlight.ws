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

@Path("warehouse-item-movement-group")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface WarehouseItemMovementGroupRest {

	@GET
	@Path("{id}")
	WarehouseItemMovementGroupDto getWarehouseItemMovementGroup(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	WarehouseItemMovementGroupDtoPage getWarehouseItemMovementGroups(@BeanParam WarehouseItemMovementGroupFilter filter)
			throws Exception;

	@POST
	WarehouseItemMovementGroupDto createWarehouseItemMovementGroup(@NonNull WarehouseItemMovementGroupDto dto) throws Exception;

	/**
	 * Finalizes the group. If the group is already final, this is a no-op (returning immediately with a success-HTTP-code).
	 * @param id references {@link WarehouseItemMovementGroupDto#getId() WarehouseItemMovementGroupDto.id} to be finalized.
	 * @return the resource after the finalization was done.
	 * @throws Exception if the specified group does not exist (404) or any unforeseen error occurs.
	 */
	@POST
	@Path("{id}/finalize")
	WarehouseItemMovementGroupDto finalize(@NonNull @PathParam("id") Long id) throws Exception;
}
