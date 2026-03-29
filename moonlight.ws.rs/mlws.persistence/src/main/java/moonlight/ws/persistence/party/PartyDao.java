package moonlight.ws.persistence.party;

import java.util.HashMap;

import jakarta.enterprise.context.RequestScoped;
import moonlight.ws.api.party.PartyFilter;
import moonlight.ws.persistence.AbstractDao;
import moonlight.ws.persistence.SearchResult;

@RequestScoped
public class PartyDao extends AbstractDao<PartyEntity> {

	public SearchResult<PartyEntity> searchEntities(PartyFilter filter) {
		if (filter == null) {
			filter = new PartyFilter();
		}
		var params = new HashMap<String, Object>();
		var jpqlCriteria = "";
		if (!filter.getIncludeDeletedOrDefault()) {
			jpqlCriteria += " and e.deleted = 0";
		}
		if (filter.getFilterCode() != null) {
			jpqlCriteria += " and lower(e.code) like lower(:code)";
			params.put("code", prepareLikeCriterion(filter.getFilterCode()));
		}
		if (filter.getFilterName() != null) {
			jpqlCriteria += " and lower(e.name) like lower(:name)";
			params.put("name", prepareLikeCriterion(filter.getFilterName()));
		}
		return searchEntities(jpqlCriteria, params, filter);
	}
}
