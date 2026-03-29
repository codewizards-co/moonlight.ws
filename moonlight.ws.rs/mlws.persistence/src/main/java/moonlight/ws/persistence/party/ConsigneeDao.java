package moonlight.ws.persistence.party;

import java.util.HashMap;

import jakarta.enterprise.context.RequestScoped;
import lombok.NonNull;
import moonlight.ws.api.party.ConsigneeFilter;
import moonlight.ws.persistence.AbstractDao;
import moonlight.ws.persistence.SearchResult;

@RequestScoped
public class ConsigneeDao extends AbstractDao<ConsigneeEntity> {

	public SearchResult<ConsigneeEntity> searchEntities(ConsigneeFilter filter) {
		if (filter == null) {
			filter = new ConsigneeFilter();
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
		if (filter.getFilterWarehouseId() != null) {
			jpqlCriteria += " and e.warehouseId = :warehouseId";
			params.put("warehouseId", filter.getFilterWarehouseId());
		}
		return searchEntities(jpqlCriteria, params, filter);
	}

	public ConsigneeEntity getConsignee(@NonNull Long warehouseId) {
		var consigneeFilter = new ConsigneeFilter();
		consigneeFilter.setFilterWarehouseId(warehouseId);
		SearchResult<ConsigneeEntity> searchResult = searchEntities(consigneeFilter);
		var consignees = searchResult.getEntities();
		if (consignees.size() == 1) {
			return consignees.get(0);
		}
		if (consignees.isEmpty()) {
			return null;
		}
		throw new IllegalStateException(
				"There must be only 0 or 1 non-deleted Consignees assigned to a warehouse, but here are multiple: "
						+ consignees.stream().map(c -> c.getId()).toList());
	}
}
