package moonlight.ws.api.liferay.customfield;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import moonlight.ws.api.warehouse.WarehouseItemProductDto;

/**
 * An instance of this file is stored as JSON-serialized custom-field named
 * {@value OrderItemCustomFieldConst#INVOICE_ITEM_GROUP_JSON}.
 */
@Getter
@Setter
public class InvoiceItemGroupJson {

	private int version;

	private String sku;

	/**
	 * The list of products related to the {@linkplain #getSku() SKU}.
	 */
	private List<WarehouseItemProductDto> products;

	private List<Long> invoiceItemIds = new ArrayList<>();
}
