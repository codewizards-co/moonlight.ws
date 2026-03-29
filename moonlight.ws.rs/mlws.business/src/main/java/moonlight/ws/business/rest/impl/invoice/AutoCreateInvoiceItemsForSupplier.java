package moonlight.ws.business.rest.impl.invoice;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import lombok.NonNull;
import moonlight.ws.persistence.invoice.InvoiceItemEntity;
import moonlight.ws.persistence.party.SupplierEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;

@RequestScoped
public class AutoCreateInvoiceItemsForSupplier extends AutoCreateInvoiceItemsForX {

	@Override
	protected @NonNull List<WarehouseItemMovementEntity> getWarehouseItemMovementsToProcess() {
		SupplierEntity supplier = party.getSupplierExcludingDeleted();
		requireNonNull(supplier, "party[id=%d].supplier".formatted(party.getId()));
		return new ArrayList<>(
				warehouseItemMovementDao.getWarehouseItemMovementsWithoutInvoiceItemForSupply(supplier.getId()));
	}

	@Override
	protected @NonNull InvoiceItemEntity createInvoiceItem(@NonNull WarehouseItemMovementEntity warehouseItemMovement) {
		var invoiceItem = super.createInvoiceItem(warehouseItemMovement);
		invoiceItem.setPriceTotalGross(warehouseItemMovement.getPriceTotalGross().negate());
		invoiceItem.setPriceTotalNet(warehouseItemMovement.getPriceTotalNet().negate());
		invoiceItem.setTaxPercent(warehouseItemMovement.getTaxPercent());
		return invoiceItem;
	}
}
