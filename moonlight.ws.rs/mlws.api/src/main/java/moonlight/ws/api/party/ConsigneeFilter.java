package moonlight.ws.api.party;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class ConsigneeFilter extends Filter {

	@QueryParam("filter.party.id")
	private Long filterPartyId;

	@QueryParam("filter.warehouseId")
	private Long filterWarehouseId;
}
