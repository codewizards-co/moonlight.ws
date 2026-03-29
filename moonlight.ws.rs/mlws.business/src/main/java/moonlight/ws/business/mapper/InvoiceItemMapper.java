package moonlight.ws.business.mapper;

import static java.util.Objects.*;
import static moonlight.ws.base.util.FetchUtil.*;
import static moonlight.ws.business.util.TimeUtil.*;

import java.time.Instant;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import moonlight.ws.api.PriceDto;
import moonlight.ws.api.invoice.InvoiceDto;
import moonlight.ws.api.invoice.InvoiceItemDto;
import moonlight.ws.api.warehouse.WarehouseItemMovementDto;
import moonlight.ws.persistence.invoice.InvoiceDao;
import moonlight.ws.persistence.invoice.InvoiceItemEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementDao;

@ApplicationScoped
public class InvoiceItemMapper extends AbstractMapper<InvoiceItemEntity, InvoiceItemDto> {

	@Inject
	private InvoiceDao invoiceDao;

	@Inject
	private InvoiceMapper invoiceMapper;

	@Inject
	private WarehouseItemMovementDao warehouseItemMovementDao;

	@Inject
	private WarehouseItemMovementMapper warehouseItemMovementMapper;

	@Inject
	private PriceMapper priceMapper;

	@Getter
	@Setter
	private String fetch;

	@Override
	protected void copyPropertiesToEntity(@NonNull InvoiceItemEntity entity, @NonNull InvoiceItemDto dto) {
		// id, created*, changed*, deleted cannot be written by client!

		Long invoiceId = dto.getInvoice() == null ? null : dto.getInvoice().getId();
		var invoice = invoiceId == null ? null
				: requireNonNull(invoiceDao.getEntity(invoiceId), "Invoice[id=%d]".formatted(invoiceId));
		entity.setInvoice(invoice);

		Long warehouseItemMovementId = dto.getWarehouseItemMovement() == null ? null
				: dto.getWarehouseItemMovement().getId();
		var warehouseItemMovement = warehouseItemMovementId == null ? null
				: requireNonNull(warehouseItemMovementDao.getEntity(warehouseItemMovementId),
						"WarehouseItemMovement[id=%d]".formatted(warehouseItemMovementId));
		entity.setWarehouseItemMovement(warehouseItemMovement);

		entity.setInclude(dto.getInclude());
		entity.setQuantity(dto.getQuantity());

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
		// orderItemId, shipmentItemId not writable by REST-client
	}

	@Override
	protected void copyPropertiesToDto(@NonNull InvoiceItemDto dto, @NonNull InvoiceItemEntity entity) {
		Set<String> fetchSet = getFetchSet(fetch);

		dto.setId(entity.getId());
		dto.setCreated(entity.getCreated());
		dto.setCreatedByUserId(entity.getCreatedByUserId());
		dto.setChanged(entity.getChanged());
		dto.setChangedByUserId(entity.getChangedByUserId());
		dto.setDeleted(instantFromMillis(entity.getDeleted()));
		dto.setDeletedByUserId(entity.getDeletedByUserId());
		dto.setDraft(entity.getFinalized() == 0);
		dto.setFinalized(entity.getFinalized() == 0 ? null : Instant.ofEpochMilli(entity.getFinalized()));
		dto.setFinalizedByUserId(entity.getFinalizedByUserId());

		if (entity.getInvoice() != null) {
			if (fetchSet.contains("invoice")) {
				dto.setInvoice(invoiceMapper.toDto(entity.getInvoice()));
			} else {
				dto.setInvoice(new InvoiceDto(entity.getInvoice().getId()));
			}
		}
		if (entity.getWarehouseItemMovement() != null) {
			if (fetchSet.contains("warehouseItemMovement")) {
				dto.setWarehouseItemMovement(warehouseItemMovementMapper.toDto(entity.getWarehouseItemMovement()));
			} else {
				var warehouseItemMovementDto = new WarehouseItemMovementDto();
				warehouseItemMovementDto.setId(entity.getWarehouseItemMovement().getId());
				dto.setWarehouseItemMovement(warehouseItemMovementDto);
			}
		}
		dto.setInclude(entity.getInclude());
		dto.setQuantity(entity.getQuantity());
		dto.setPrice(priceMapper.toDto(entity.getQuantity(), entity.getPriceTotalNet(), entity.getPriceTotalGross(),
				entity.getTaxPercent()));
		dto.setOrderItemId(entity.getOrderItemId());
		dto.setShipmentItemId(entity.getShipmentItemId());
	}

}
