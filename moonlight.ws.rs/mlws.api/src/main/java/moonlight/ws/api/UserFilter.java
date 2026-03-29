package moonlight.ws.api;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class UserFilter extends Filter {

	@QueryParam("filter.username")
	private String filterUsername;

}
