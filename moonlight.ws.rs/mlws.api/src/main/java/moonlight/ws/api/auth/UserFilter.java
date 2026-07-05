package moonlight.ws.api.auth;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class UserFilter extends Filter {

	@QueryParam("filter.username")
	private String filterUsername;

}
