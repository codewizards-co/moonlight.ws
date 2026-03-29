package moonlight.ws.api.party;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.DateTime;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class PartyFilter extends Filter {

	@QueryParam("filter.code")
	private String filterCode;

	@QueryParam("filter.name")
	private String filterName;

	@QueryParam("filter.countryIsoCode")
	private String filterCountryIsoCode;

	@QueryParam("filter.createdFromIncl")
	private DateTime filterCreatedFromIncl;

	@QueryParam("filter.createdToExcl")
	private DateTime filterCreatedToExcl;

	@QueryParam("filter.changedFromIncl")
	private DateTime filterChangedFromIncl;

	@QueryParam("filter.changedToExcl")
	private DateTime filterChangedToExcl;
}
