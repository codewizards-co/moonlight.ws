package moonlight.ws.business.mapper;

import static java.util.Objects.*;
import static moonlight.ws.business.util.TimeUtil.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import moonlight.ws.api.PriceDto;
import moonlight.ws.api.warehouse.WarehouseItemMovementDto;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementGroupDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementGroupEntity;

@ApplicationScoped
public class WarehouseItemMovementMapper extends AbstractMapper<WarehouseItemMovementEntity, WarehouseItemMovementDto> {

	@Inject
	private WarehouseItemMovementGroupDao groupDao;

	@Inject
	private PriceMapper priceMapper;

	@Override
	protected void copyPropertiesToEntity(@NonNull WarehouseItemMovementEntity entity,
			@NonNull WarehouseItemMovementDto dto) {
		// id, created*, changed*, booked cannot be written by client!
		// draft is not mapped and not processed here, but only in the RestImpl.
		// finalized is controlled by the client via the draft-flag -- and also not
		// mapped here.
		entity.setQuantity(dto.getQuantity());
		entity.setUnitOfMeasureKey(dto.getUnitOfMeasureKey()); // overwritten by the server in the RestImpl, but we
																// still copy it here
		entity.setSku(dto.getSku());
		entity.setWarehouseId(dto.getWarehouseId());
		entity.setWarehouseErc(dto.getWarehouseErc());
		entity.setWarehouseItemId(dto.getWarehouseItemId());
		entity.setWarehouseItemErc(dto.getWarehouseItemErc());

		WarehouseItemMovementGroupEntity group = dto.getGroupId() == null ? null
				: requireNonNull(groupDao.getEntity(dto.getGroupId()),
						"WarehouseItemMovementGroup[id=%d]".formatted(dto.getGroupId()));
		entity.setGroup(group);

		entity.setType(dto.getType());
		entity.setOtherWarehouseId(dto.getOtherWarehouseId());
		entity.setOtherWarehouseItemId(dto.getOtherWarehouseItemId()); // overwritten by the server in the RestImpl, but
																		// we still copy it here
		entity.setSupplierId(dto.getSupplierId());

		PriceDto price = priceMapper.calculateMissingProperties(dto.getPrice());
		if (price == null) {
			entity.setPriceTotalNet(null);
			entity.setPriceTotalGross(null);
			entity.setTaxPercent(null);
		} else {
			entity.setPriceTotalNet(requireNonNull(price.getPriceTotalNet(), "price.priceTotalNet"));
			entity.setPriceTotalGross(requireNonNull(price.getPriceTotalGross(), "price.priceTotalGross"));
			entity.setTaxPercent(requireNonNull(price.getTaxPercent(), "price.taxPercent"));
		}
	}

	@Override
	protected void copyPropertiesToDto(@NonNull WarehouseItemMovementDto dto,
			@NonNull WarehouseItemMovementEntity entity) {
		dto.setId(entity.getId());
		dto.setCreated(entity.getCreated());
		dto.setCreatedByUserId(entity.getCreatedByUserId());
		dto.setChanged(entity.getChanged());
		dto.setChangedByUserId(entity.getChangedByUserId());
		dto.setQuantity(entity.getQuantity());
		dto.setUnitOfMeasureKey(entity.getUnitOfMeasureKey());
		dto.setBooked(instantFromMillis(entity.getBooked()));
		dto.setSku(entity.getSku());
		dto.setWarehouseId(entity.getWarehouseId());
		dto.setWarehouseErc(entity.getWarehouseErc());
		dto.setWarehouseItemId(entity.getWarehouseItemId());
		dto.setWarehouseItemErc(entity.getWarehouseItemErc());
		dto.setDraft(entity.getFinalized() == 0);
		dto.setFinalized(instantFromMillis(entity.getFinalized()));
		dto.setGroupId(entity.getGroup() == null ? null : entity.getGroup().getId());
		dto.setType(entity.getType());
		dto.setOtherWarehouseId(entity.getOtherWarehouseId());
		dto.setOtherWarehouseItemId(entity.getOtherWarehouseItemId());
		dto.setSupplierId(entity.getSupplierId());
		dto.setPrice(priceMapper.toDto(entity.getQuantity(), entity.getPriceTotalNet(), entity.getPriceTotalGross(),
				entity.getTaxPercent()));
	}
}
