package moonlight.ws.api.party;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.NonNull;
import moonlight.ws.api.RequiresAuthentication;

@Path("consignee")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface ConsigneeRest {

	@GET
	@Path("{id}")
	ConsigneeDto getConsignee(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	ConsigneeDtoPage getConsignees(@BeanParam ConsigneeFilter filter) throws Exception;

	@POST
	ConsigneeDto createConsignee(@NonNull ConsigneeDto dto) throws Exception;

	@PUT
	@Path("{id}")
	ConsigneeDto updateConsignee(@NonNull @PathParam("id") Long id, @NonNull ConsigneeDto dto) throws Exception;

	@DELETE
	@Path("{id}")
	void deleteConsignee(@NonNull @PathParam("id") Long id) throws Exception;
}
