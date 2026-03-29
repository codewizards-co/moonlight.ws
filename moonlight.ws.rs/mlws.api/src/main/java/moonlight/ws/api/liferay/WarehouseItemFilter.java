package moonlight.ws.api.liferay;

import com.liferay.headless.commerce.admin.inventory.client.pagination.Pagination;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class WarehouseItemFilter extends Filter {

	@QueryParam("filter.warehouseId")
	private Long filterWarehouseId;

	@QueryParam("filter.sku")
	private String filterSku;

	public Pagination getPagination() {
		return Pagination.of(getPageNumberOrDefault(), getPageSizeOrDefault());
	}
}
