package moonlight.ws.api.warehouse;

import java.time.Instant;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class WarehouseItemMovementFilter extends Filter {

	@QueryParam("filter.warehouseItemId")
	private Long filterWarehouseItemId;

	@QueryParam("filter.warehouseItemErc")
	private String filterWarehouseItemErc;

	@QueryParam("filter.sku")
	private String filterSku;

	@QueryParam("filter.warehouseId")
	private Long filterWarehouseId;

	@QueryParam("filter.warehouseErc")
	private String filterWarehouseErc;

	@QueryParam("filter.type")
	private WarehouseItemMovementType filterType;

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

	@QueryParam("filter.groupId")
	private Long filterGroupId;
}
