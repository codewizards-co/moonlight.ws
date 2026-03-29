package moonlight.ws.api.liferay.customfield;

import com.liferay.headless.commerce.admin.order.client.dto.v1_0.OrderItem;

public interface OrderItemCustomFieldConst {
	/**
	 * Custom-field-name for storing a JSON-encoded {@link InvoiceItemGroupJson} in
	 * the {@link OrderItem#getCustomFields() OrderItem.customFields}.
	 */
	String INVOICE_ITEM_GROUP_JSON = "moonlight_invoiceItemGroupJson";
}
