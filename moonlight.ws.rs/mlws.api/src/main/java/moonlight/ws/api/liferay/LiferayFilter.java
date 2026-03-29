package moonlight.ws.api.liferay;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class LiferayFilter extends Filter {

	@QueryParam("search")
	String search;

	@QueryParam("filter")
	String filter;

}
