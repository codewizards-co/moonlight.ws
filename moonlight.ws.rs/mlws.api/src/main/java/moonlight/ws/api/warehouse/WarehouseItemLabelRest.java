package moonlight.ws.api.warehouse;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import moonlight.ws.api.RequiresAuthentication;

@Path("warehouse-item-label")
@Consumes(APPLICATION_JSON)
@RequiresAuthentication
public interface WarehouseItemLabelRest {

	@GET
	@Produces(TEXT_HTML)
	String getWarehouseItemLabelHtml(@BeanParam WarehouseItemLabelFilter filter) throws Exception;

}
