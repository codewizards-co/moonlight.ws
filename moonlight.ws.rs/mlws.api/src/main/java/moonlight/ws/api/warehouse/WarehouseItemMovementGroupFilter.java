package moonlight.ws.api.warehouse;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class WarehouseItemMovementGroupFilter extends Filter {

	@QueryParam("filter.draft")
	private Boolean filterDraft;
}
