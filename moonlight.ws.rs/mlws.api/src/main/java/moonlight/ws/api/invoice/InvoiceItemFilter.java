package moonlight.ws.api.invoice;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moonlight.ws.api.Filter;

@Getter
@Setter
@ToString(callSuper = true)
public class InvoiceItemFilter extends Filter {

	@QueryParam("filter.invoice.id")
	private Long filterInvoiceId;

	@QueryParam("filter.warehouseItemMovement.id")
	private Long filterWarehouseItemMovementId;

	@QueryParam("filter.include")
	private InvoiceInclude filterInclude;
}
