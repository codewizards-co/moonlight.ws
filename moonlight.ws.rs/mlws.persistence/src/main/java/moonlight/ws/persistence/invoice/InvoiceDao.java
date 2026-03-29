package moonlight.ws.persistence.invoice;

import java.util.HashMap;

import jakarta.enterprise.context.RequestScoped;
import moonlight.ws.api.invoice.InvoiceFilter;
import moonlight.ws.persistence.AbstractDao;
import moonlight.ws.persistence.SearchResult;

@RequestScoped
public class InvoiceDao extends AbstractDao<InvoiceEntity> {

	public SearchResult<InvoiceEntity> searchEntities(InvoiceFilter filter) {
		if (filter == null) {
			filter = new InvoiceFilter();
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

		if (filter.getFilterCreatedFromIncl() != null) {
			jpqlCriteria += " and e.created >= :createdFromIncl";
			params.put("createdFromIncl", filter.getFilterCreatedFromIncl());
		}
		if (filter.getFilterCreatedToExcl() != null) {
			jpqlCriteria += " and e.created < :createdToExcl";
			params.put("createdToExcl", filter.getFilterCreatedToExcl());
		}

		if (filter.getFilterChangedFromIncl() != null) {
			jpqlCriteria += " and e.changed >= :changedFromIncl";
			params.put("changedFromIncl", filter.getFilterChangedFromIncl());
		}
		if (filter.getFilterChangedToExcl() != null) {
			jpqlCriteria += " and e.changed < :changedToExcl";
			params.put("changedToExcl", filter.getFilterChangedToExcl());
		}

		if (filter.getFilterBookedFromIncl() != null) {
			jpqlCriteria += " and e.booked >= :bookedFromIncl";
			params.put("bookedFromIncl", filter.getFilterBookedFromIncl().toEpochMilli());
		}
		if (filter.getFilterBookedToExcl() != null) {
			jpqlCriteria += " and e.booked < :bookedToExcl";
			params.put("bookedToExcl", filter.getFilterBookedToExcl().toEpochMilli());
		}
		if (filter.getFilterBooked() != null) {
			if (filter.getFilterBooked()) {
				jpqlCriteria += " and e.booked <> 0";
			} else {
				jpqlCriteria += " and e.booked = 0";
			}
		}
		if (filter.getFilterDraft() != null) {
			if (filter.getFilterDraft()) {
				jpqlCriteria += " and e.finalized = 0";
			} else {
				jpqlCriteria += " and e.finalized <> 0";
			}
		}
		return searchEntities(jpqlCriteria, params, filter);
	}
}
