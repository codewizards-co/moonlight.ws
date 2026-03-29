package moonlight.ws.api.liferay;

import com.liferay.headless.commerce.admin.inventory.client.pagination.Pagination;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class WarehouseFilter extends LiferayFilter {

	@QueryParam("filter.active")
	Boolean filterActive;

	public Pagination getPagination() {
		return Pagination.of(getPageNumberOrDefault(), getPageSizeOrDefault());
	}
}
