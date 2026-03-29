package moonlight.ws.persistence.warehouse;

import java.util.HashMap;

import jakarta.enterprise.context.RequestScoped;
import moonlight.ws.api.warehouse.WarehouseItemMovementGroupFilter;
import moonlight.ws.persistence.AbstractDao;
import moonlight.ws.persistence.SearchResult;

@RequestScoped
public class WarehouseItemMovementGroupDao extends AbstractDao<WarehouseItemMovementGroupEntity> {

	public SearchResult<WarehouseItemMovementGroupEntity> searchEntities(WarehouseItemMovementGroupFilter filter) {
		var params = new HashMap<String, Object>();
		var jpqlCriteria = "";
		if (filter != null) {
			if (filter.getFilterDraft() != null) {
				if (filter.getFilterDraft()) {
					jpqlCriteria += " and e.finalized = 0";
				} else {
					jpqlCriteria += " and e.finalized <> 0";
				}
			}
		}
		return searchEntities(jpqlCriteria, params, filter);
	}
}
