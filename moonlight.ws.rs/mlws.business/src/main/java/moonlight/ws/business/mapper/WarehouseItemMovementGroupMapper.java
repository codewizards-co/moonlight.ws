package moonlight.ws.business.mapper;

import static moonlight.ws.business.util.TimeUtil.*;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import moonlight.ws.api.warehouse.WarehouseItemMovementGroupDto;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementGroupEntity;

@ApplicationScoped
public class WarehouseItemMovementGroupMapper
		extends AbstractMapper<WarehouseItemMovementGroupEntity, WarehouseItemMovementGroupDto> {

	@Override
	protected void copyPropertiesToEntity(@NonNull WarehouseItemMovementGroupEntity entity,
			@NonNull WarehouseItemMovementGroupDto dto) {
		// id, created*, changed*, booked cannot be written by client!
		// draft is not mapped and not processed here, but only in the RestImpl.
		// finalized is controlled by the client via the draft-flag -- and also not
		// mapped here.
	}

	@Override
	protected void copyPropertiesToDto(@NonNull WarehouseItemMovementGroupDto dto,
			@NonNull WarehouseItemMovementGroupEntity entity) {
		dto.setId(entity.getId());
		dto.setDraft(entity.getFinalized() == 0);
		dto.setFinalized(instantFromMillis(entity.getFinalized()));
	}
}
