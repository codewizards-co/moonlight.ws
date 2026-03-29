package moonlight.ws.api.liferay;

import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ProductFilter extends LiferayFilter {

	public Pagination getPagination() {
		return Pagination.of(getPageNumberOrDefault(), getPageSizeOrDefault());
	}
}
