package moonlight.ws.api.invoice;

import java.time.Instant;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class InvoiceFilter extends Filter {

	@QueryParam("filter.party.id")
	private Long filterPartyId;

	@QueryParam("filter.createdFromIncl")
	private Instant filterCreatedFromIncl;

	@QueryParam("filter.createdToExcl")
	private Instant filterCreatedToExcl;

	@QueryParam("filter.changedFromIncl")
	private Instant filterChangedFromIncl;

	@QueryParam("filter.changedToExcl")
	private Instant filterChangedToExcl;

	@QueryParam("filter.bookedFromIncl")
	private Instant filterBookedFromIncl;

	@QueryParam("filter.bookedToExcl")
	private Instant filterBookedToExcl;

	@QueryParam("filter.booked")
	private Boolean filterBooked;

	@QueryParam("filter.draft")
	private Boolean filterDraft;
}
