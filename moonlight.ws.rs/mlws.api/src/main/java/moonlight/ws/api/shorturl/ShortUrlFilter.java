package moonlight.ws.api.shorturl;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.DateTime;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class ShortUrlFilter extends Filter {

	@QueryParam("filter.code")
	private String filterCode;

	@QueryParam("filter.longUrl")
	private String filterLongUrl;

	@QueryParam("filter.shortUrl")
	private String filterShortUrl;

	@QueryParam("filter.createdFromIncl")
	private DateTime filterCreatedFromIncl;

	@QueryParam("filter.createdToExcl")
	private DateTime filterCreatedToExcl;

	@QueryParam("filter.changedFromIncl")
	private DateTime filterChangedFromIncl;

	@QueryParam("filter.changedToExcl")
	private DateTime filterChangedToExcl;
}
