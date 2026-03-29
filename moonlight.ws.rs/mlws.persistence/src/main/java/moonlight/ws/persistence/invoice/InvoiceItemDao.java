package moonlight.ws.persistence.invoice;

import java.util.HashMap;

import jakarta.enterprise.context.RequestScoped;
import moonlight.ws.api.invoice.InvoiceItemFilter;
import moonlight.ws.persistence.AbstractDao;
import moonlight.ws.persistence.SearchResult;

@RequestScoped
public class InvoiceItemDao extends AbstractDao<InvoiceItemEntity> {

	public SearchResult<InvoiceItemEntity> searchEntities(InvoiceItemFilter filter) {
		if (filter == null) {
			filter = new InvoiceItemFilter();
		}
		var params = new HashMap<String, Object>();
		var jpqlCriteria = "";
		if (!filter.getIncludeDeletedOrDefault()) {
			jpqlCriteria += " and e.deleted = 0";
		}
		if (filter.getFilterInvoiceId() != null) {
			jpqlCriteria += " and e.invoice.id = :invoiceId";
			params.put("invoiceId", filter.getFilterInvoiceId());
		}
		if (filter.getFilterWarehouseItemMovementId() != null) {
			jpqlCriteria += " and e.warehouseItemMovement.id = :warehouseItemMovementId";
			params.put("warehouseItemMovementId", filter.getFilterWarehouseItemMovementId());
		}
		if (filter.getFilterInclude() != null) {
			jpqlCriteria += " and e.include = :include";
			params.put("include", filter.getFilterInclude());
		}
		return searchEntities(jpqlCriteria, params, filter);
	}
}
