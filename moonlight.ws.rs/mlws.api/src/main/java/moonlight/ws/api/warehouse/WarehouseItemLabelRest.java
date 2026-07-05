package moonlight.ws.api.warehouse;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import moonlight.ws.api.auth.RequiresAuthentication;
import moonlight.ws.api.auth.RequiresRole;
import moonlight.ws.api.auth.Role;

@Path("warehouse-item-label")
@Consumes(APPLICATION_JSON)
@RequiresAuthentication
@RequiresRole(Role.LOGISTICIAN)
public interface WarehouseItemLabelRest {

	@GET
	@Produces(TEXT_HTML)
	String getWarehouseItemLabelHtml(@BeanParam WarehouseItemLabelFilter filter) throws Exception;

}
