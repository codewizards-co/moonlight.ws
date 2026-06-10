package moonlight.ws.api.liferay;

import java.util.List;
import java.util.regex.Pattern;

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

	public static final List<Pattern> INTERNAL_SKU_PATTERNS = List.of( //
			Pattern.compile("CS\\d+"), //
			Pattern.compile("SP\\d+") //
	);

	@QueryParam("filter.warehouseId")
	private Long filterWarehouseId;

	@QueryParam("filter.sku")
	private String filterSku;

	@QueryParam("filter.productName")
	private String filterProductName;

	@QueryParam("filter.includeInternal")
	private Boolean filterIncludeInternal;

	public Pagination getPagination() {
		return Pagination.of(getPageNumberOrDefault(), getPageSizeOrDefault());
	}
}
