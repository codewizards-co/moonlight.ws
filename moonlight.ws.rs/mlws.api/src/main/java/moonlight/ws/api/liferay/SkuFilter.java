package moonlight.ws.api.liferay;

import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class SkuFilter extends LiferayFilter {

	@QueryParam("filter.sku")
	private String filterSku;

	public Pagination getPagination() {
		return Pagination.of(getPageNumberOrDefault(), getPageSizeOrDefault());
	}
}
