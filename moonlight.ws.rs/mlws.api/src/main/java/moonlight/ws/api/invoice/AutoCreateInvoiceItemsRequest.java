package moonlight.ws.api.invoice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoCreateInvoiceItemsRequest {

	/**
	 * The invoice to be processed. Only its {@link InvoiceDto#getId() id} is taken
	 * into account. All other properties are ignored.
	 */
	private InvoiceDto invoice;

}
