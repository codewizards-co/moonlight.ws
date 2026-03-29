package moonlight.ws.api.liferay;

import static jakarta.ws.rs.core.MediaType.*;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.NonNull;
import moonlight.ws.api.RequiresAuthentication;

@Path("warehouse-item")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface WarehouseItemRest {

	@GET
	@Path("{id}")
	WarehouseItem getWarehouseItem(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	LiferayDtoPage<WarehouseItem> getWarehouseItemsPage(@NonNull @BeanParam WarehouseItemFilter filter)
			throws Exception;

	@POST
	WarehouseItem createWarehouseItem(@NonNull WarehouseItemDto warehouseItem) throws Exception;
}
