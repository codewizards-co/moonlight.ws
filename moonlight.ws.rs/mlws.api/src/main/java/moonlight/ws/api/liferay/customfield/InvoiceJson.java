package moonlight.ws.api.liferay.customfield;

import lombok.Getter;
import lombok.Setter;
import moonlight.ws.api.invoice.InvoiceWorkflow;

/**
 * An instance of this file is stored as JSON-serialized custom-field named
 * {@value OrderCustomFieldConst#INVOICE_JSON}.
 */
@Getter
@Setter
public class InvoiceJson {

	private int version;

	private Long invoiceId;

	private InvoiceWorkflow workflow;

	private String billingEmail;
}
