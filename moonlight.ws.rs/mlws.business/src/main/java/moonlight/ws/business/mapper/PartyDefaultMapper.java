package moonlight.ws.business.mapper;

import jakarta.enterprise.context.RequestScoped;
import lombok.NonNull;
import moonlight.ws.api.party.PartyDefaultDto;
import moonlight.ws.persistence.party.PartyDefaultEntity;

@RequestScoped
public class PartyDefaultMapper extends AbstractMapper<PartyDefaultEntity, PartyDefaultDto> {

	@Override
	protected void copyPropertiesToEntity(@NonNull PartyDefaultEntity entity, @NonNull PartyDefaultDto dto) {
		// id, created*, changed*, deleted cannot be written by client!
		entity.setTradeDiscountPercent(dto.getTradeDiscountPercent());
		entity.setTaxPercent(dto.getTaxPercent());
		entity.setCatalogName(dto.getCatalogName());
	}

	@Override
	protected void copyPropertiesToDto(@NonNull PartyDefaultDto dto, @NonNull PartyDefaultEntity entity) {
		dto.setId(entity.getId());
		dto.setChanged(entity.getChanged());
		dto.setChangedByUserId(entity.getChangedByUserId());
		dto.setCreated(entity.getCreated());
		dto.setCreatedByUserId(entity.getCreatedByUserId());
		dto.setTradeDiscountPercent(entity.getTradeDiscountPercent());
		dto.setTaxPercent(entity.getTaxPercent());
		dto.setCatalogName(entity.getCatalogName());
	}
}
