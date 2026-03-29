package moonlight.ws.api.liferay;

import com.liferay.headless.admin.address.client.pagination.Pagination;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class CountryFilter extends LiferayFilter {

	@QueryParam("filter.active")
	private Boolean filterActive;

	public Pagination getPagination() {
		return Pagination.of(getPageNumberOrDefault(), getPageSizeOrDefault());
	}
}
