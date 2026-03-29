package moonlight.ws.business.mapper;

import static java.util.Objects.*;
import static moonlight.ws.base.util.FetchUtil.*;
import static moonlight.ws.business.util.TimeUtil.*;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import moonlight.ws.api.party.PartyDto;
import moonlight.ws.api.party.SupplierDto;
import moonlight.ws.persistence.party.PartyDao;
import moonlight.ws.persistence.party.PartyEntity;
import moonlight.ws.persistence.party.SupplierEntity;

@RequestScoped
public class SupplierMapper extends AbstractMapper<SupplierEntity, SupplierDto> {

	@Getter
	@Setter
	private String fetch;

	@Inject
	private PartyDao partyDao;

	@Inject
	private PartyMapper partyMapper;

	@Override
	protected void copyPropertiesToEntity(@NonNull SupplierEntity entity, @NonNull SupplierDto dto) {
		// id, created*, changed*, deleted cannot be written by client!
		if (dto.getParty() == null) {
			entity.setParty(null);
		} else {
			Long partyId = requireNonNull(dto.getParty().getId(), "dto.party.id");
			PartyEntity partyEntity = requireNonNull(partyDao.getEntity(partyId),
					"partyDao.getEntity(" + partyId + ")");
			entity.setParty(partyEntity);
		}
	}

	@Override
	protected void copyPropertiesToDto(@NonNull SupplierDto dto, @NonNull SupplierEntity entity) {
		dto.setId(entity.getId());
		dto.setChanged(entity.getChanged());
		dto.setChangedByUserId(entity.getChangedByUserId());
		dto.setCreated(entity.getCreated());
		dto.setCreatedByUserId(entity.getCreatedByUserId());
		dto.setDeleted(instantFromMillis(entity.getDeleted()));
		dto.setDeletedByUserId(entity.getDeletedByUserId());
		if (entity.getParty() == null) {
			dto.setParty(null);
		} else {
			PartyDto partyDto;
			if (getFetchSet(fetch).contains("party")) {
				partyDto = partyMapper.toDto(entity.getParty());
			} else {
				partyDto = new PartyDto();
				partyDto.setId(requireNonNull(entity.getParty().getId(), "entity.party.id"));
			}
			dto.setParty(partyDto);
		}
	}
}
