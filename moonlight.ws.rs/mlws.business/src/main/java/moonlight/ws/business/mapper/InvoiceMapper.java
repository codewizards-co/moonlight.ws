package moonlight.ws.business.mapper;

import static java.util.Objects.*;
import static moonlight.ws.base.util.FetchUtil.*;
import static moonlight.ws.business.util.TimeUtil.*;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import moonlight.ws.api.invoice.InvoiceDto;
import moonlight.ws.api.party.PartyDto;
import moonlight.ws.persistence.invoice.InvoiceEntity;
import moonlight.ws.persistence.party.PartyDao;

@ApplicationScoped
public class InvoiceMapper extends AbstractMapper<InvoiceEntity, InvoiceDto> {

	@Inject
	private PartyDao partyDao;

	@Inject
	private PartyMapper partyMapper;

	@Getter
	@Setter
	private String fetch;

	@Override
	protected void copyPropertiesToEntity(@NonNull InvoiceEntity entity, @NonNull InvoiceDto dto) {
		// id, created*, changed*, deleted cannot be written by client!
		// finalized and paid are also written in a different way
		var partyId = dto.getParty() == null ? null : dto.getParty().getId();
		var party = partyId == null ? null
				: requireNonNull(partyDao.getEntity(partyId), "Party[id=%d]".formatted(partyId));
		entity.setWorkflow(dto.getWorkflow());
		entity.setParty(party);
//		entity.setOrderId(dto.getOrderId()); // not writable by REST-client
//		entity.setShipmentId(dto.getShipmentId()); // not writable by REST-client

		if (entity.getIncludedTotalGross() == null) {
			entity.setIncludedTotalGross(BigDecimal.ZERO);
		}
		if (entity.getIncludedTotalNet() == null) {
			entity.setIncludedTotalNet(BigDecimal.ZERO);
		}
		if (entity.getExcludedTotalGross() == null) {
			entity.setExcludedTotalGross(BigDecimal.ZERO);
		}
		if (entity.getExcludedTotalNet() == null) {
			entity.setExcludedTotalNet(BigDecimal.ZERO);
		}
	}

	@Override
	protected void copyPropertiesToDto(@NonNull InvoiceDto dto, @NonNull InvoiceEntity entity) {
		Set<String> fetchSet = getFetchSet(fetch);

		dto.setId(entity.getId());
		dto.setChanged(entity.getChanged());
		dto.setChangedByUserId(entity.getChangedByUserId());
		dto.setCreated(entity.getCreated());
		dto.setCreatedByUserId(entity.getCreatedByUserId());
		dto.setDeleted(instantFromMillis(entity.getDeleted()));
		dto.setDeletedByUserId(entity.getDeletedByUserId());
		dto.setDraft(entity.getFinalized() == 0);
		dto.setFinalized(instantFromMillis(entity.getFinalized()));
		dto.setFinalizedByUserId(entity.getFinalizedByUserId());
		dto.setBooked(instantFromMillis(entity.getBooked()));

		dto.setWorkflow(entity.getWorkflow());
		if (entity.getParty() != null) {
			if (fetchSet.contains("party")) {
				dto.setParty(partyMapper.toDto(entity.getParty()));
			} else {
				dto.setParty(new PartyDto(entity.getParty().getId()));
			}
		}
		dto.setOrderId(entity.getOrderId());
		dto.setShipmentId(entity.getShipmentId());
		dto.setIncludedTotalGross(entity.getIncludedTotalGross());
		dto.setIncludedTotalNet(entity.getIncludedTotalNet());
		dto.setExcludedTotalGross(entity.getExcludedTotalGross());
		dto.setExcludedTotalNet(entity.getExcludedTotalNet());
		dto.setPaid(entity.getPaid());
		dto.setMarkedPaid(instantFromMillis(entity.getMarkedPaid()));
		dto.setMarkedPaidByUserId(entity.getMarkedPaidByUserId());
	}

}
