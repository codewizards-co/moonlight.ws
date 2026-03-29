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

@Path("supplier")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface SupplierRest {

	@GET
	@Path("{id}")
	SupplierDto getSupplier(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	SupplierDtoPage getSuppliers(@BeanParam SupplierFilter filter) throws Exception;

	@POST
	SupplierDto createSupplier(@NonNull SupplierDto dto) throws Exception;

	@PUT
	@Path("{id}")
	SupplierDto updateSupplier(@NonNull @PathParam("id") Long id, @NonNull SupplierDto dto) throws Exception;

	@DELETE
	@Path("{id}")
	void deleteSupplier(@NonNull @PathParam("id") Long id) throws Exception;
}
