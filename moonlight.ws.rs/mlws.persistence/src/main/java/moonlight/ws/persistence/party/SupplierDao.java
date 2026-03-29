package moonlight.ws.persistence.party;

import java.util.HashMap;

import jakarta.enterprise.context.RequestScoped;
import moonlight.ws.api.party.SupplierFilter;
import moonlight.ws.persistence.AbstractDao;
import moonlight.ws.persistence.SearchResult;

@RequestScoped
public class SupplierDao extends AbstractDao<SupplierEntity> {

	public SearchResult<SupplierEntity> searchEntities(SupplierFilter filter) {
		if (filter == null) {
			filter = new SupplierFilter();
		}
		var params = new HashMap<String, Object>();
		var jpqlCriteria = "";
		if (!filter.getIncludeDeletedOrDefault()) {
			jpqlCriteria += " and e.deleted = 0";
		}
		if (filter.getFilterPartyId() != null) {
			jpqlCriteria += " and e.party.id = :partyId";
			params.put("partyId", filter.getFilterPartyId());
		}
		return searchEntities(jpqlCriteria, params, filter);
	}
}
