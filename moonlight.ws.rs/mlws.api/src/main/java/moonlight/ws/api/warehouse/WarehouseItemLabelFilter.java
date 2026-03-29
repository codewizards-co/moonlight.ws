package moonlight.ws.api.warehouse;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.CommaSeparatedListOfLong;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class WarehouseItemLabelFilter extends Filter {

	@QueryParam("warehouseItemIds")
	private CommaSeparatedListOfLong warehouseItemIds;

	@QueryParam("locale")
	private String locale;

	public CommaSeparatedListOfLong getWarehouseItemIds() {
		if (warehouseItemIds == null) {
			warehouseItemIds = new CommaSeparatedListOfLong();
		}
		return warehouseItemIds;
	}
}
