package moonlight.ws.api.invoice;

import static jakarta.ws.rs.core.MediaType.*;

import java.util.List;

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

@Path("invoice-item")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface InvoiceItemRest {
	@GET
	@Path("{id}")
	InvoiceItemDto getInvoiceItem(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	InvoiceItemDtoPage getInvoiceItems(@BeanParam InvoiceItemFilter filter) throws Exception;

	@POST
	InvoiceItemDto createInvoiceItem(@NonNull InvoiceItemDto dto) throws Exception;

	@PUT
	@Path("{id}")
	InvoiceItemDto updateInvoiceItem(@NonNull @PathParam("id") Long id, @NonNull InvoiceItemDto dto) throws Exception;

	@DELETE
	@Path("{id}")
	void deleteInvoiceItem(@NonNull @PathParam("id") Long id) throws Exception;

	@POST
	@Path("auto-create")
	List<InvoiceItemDto> autoCreateInvoiceItems(@NonNull AutoCreateInvoiceItemsRequest request) throws Exception;
}
