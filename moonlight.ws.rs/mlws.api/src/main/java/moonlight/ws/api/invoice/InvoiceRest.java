package moonlight.ws.api.invoice;

import static jakarta.ws.rs.core.MediaType.*;

import java.time.LocalDate;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import lombok.NonNull;

@Path("invoice")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface InvoiceRest {
	@GET
	@Path("{id}")
	InvoiceDto getInvoice(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	InvoiceDtoPage getInvoices(@BeanParam InvoiceFilter filter) throws Exception;

	@POST
	InvoiceDto createInvoice(@NonNull InvoiceDto dto) throws Exception;

	@PUT
	@Path("{id}")
	InvoiceDto updateInvoice(@NonNull @PathParam("id") Long id, @NonNull InvoiceDto dto) throws Exception;

	@DELETE
	@Path("{id}")
	void deleteInvoice(@NonNull @PathParam("id") Long id) throws Exception;

	/**
	 * Finalizes the invoice. If the invoice is already final, this is a no-op
	 * (returning immediately with a success-HTTP-code).
	 *
	 * @param id references {@link InvoiceDto#getId() InvoiceDto.id} to be
	 *           finalized.
	 * @return the resource after the finalization was done.
	 * @throws Exception if the specified invoice does not exist (404) or any
	 *                   unforeseen error occurs.
	 */
	@POST
	@Path("{id}/finalize")
	InvoiceDto finalize(@NonNull @PathParam("id") Long id) throws Exception;

	/**
	 * Marks the invoice paid. Can also be used to mark it unpaid after having
	 * marked it as paid, before.
	 *
	 * @param id   references {@link InvoiceDto#getId() InvoiceDto.id} to be marked
	 *             as paid.
	 * @param paid the date of payment. If {@code null}, the invoice is marked as
	 *             not yet paid.
	 * @return the resource after the payment was written.
	 * @throws Exception if the specified invoice does not exist (404) or any
	 *                   unforeseen error occurs.
	 */
	@POST
	@Path("{id}/mark-paid")
	InvoiceDto markPaid(@NonNull @PathParam("id") Long id, @QueryParam("paid") LocalDate paid) throws Exception;
}
