package moonlight.ws.api.liferay.customfield;

import com.liferay.headless.commerce.admin.order.client.dto.v1_0.OrderItem;

public interface OrderCustomFieldConst {
	/**
	 * Custom-field-name for storing a JSON-encoded {@link InvoiceJson} in the
	 * {@link OrderItem#getCustomFields() OrderItem.customFields}.
	 */
	String INVOICE_JSON = "moonlight_invoiceJson";
}
