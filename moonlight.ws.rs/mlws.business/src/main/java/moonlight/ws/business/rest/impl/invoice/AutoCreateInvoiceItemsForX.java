package moonlight.ws.business.rest.impl.invoice;

import static java.util.Objects.*;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import moonlight.ws.api.invoice.InvoiceInclude;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.invoice.InvoiceEntity;
import moonlight.ws.persistence.invoice.InvoiceItemEntity;
import moonlight.ws.persistence.party.PartyEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;

public abstract class AutoCreateInvoiceItemsForX {

	@Inject
	private UserDao userDao;

	@Inject
	protected WarehouseItemMovementDao warehouseItemMovementDao;

	protected EntityManager entityManager;

	protected InvoiceEntity invoice;
	protected PartyEntity party;

	public @NonNull List<InvoiceItemEntity> createInvoiceItems(@NonNull InvoiceEntity invoice) {
		this.invoice = invoice;
		this.party = requireNonNull(invoice.getParty(), "invoice.party");
		this.entityManager = warehouseItemMovementDao.getEntityManager();
		var warehouseItemMovements = getWarehouseItemMovementsToProcess();
		return warehouseItemMovements.stream().map(wim -> persisteInvoiceItem(createInvoiceItem(wim))).toList();
	}

	protected @NonNull InvoiceItemEntity createInvoiceItem(@NonNull WarehouseItemMovementEntity warehouseItemMovement) {
		var invoiceItem = new InvoiceItemEntity();
		invoiceItem.setCreatedByUserId(userDao.currentUser().getId());
		invoiceItem.setChangedByUserId(invoiceItem.getCreatedByUserId());
		invoiceItem.setInvoice(requireNonNull(invoice, "invoice"));
		invoiceItem.setWarehouseItemMovement(warehouseItemMovement);
		invoiceItem.setInclude(InvoiceInclude.INCLUDE);

		// We must negate the quantity for both: consignees and suppliers.
		// The normal use-cases look like this:
		//
		// CONSIGNEE
		// A consignee sells from his warehouse (reducing his quantity), hence the
		// warehouse-item-movement has a negative quantity for a regular sale from the
		// consignee to the end-customer. But the invoice from the factory to the
		// consignee must charge the money with a positive quantity and a positive total
		// price.
		//
		// SUPPLIER
		// The factory purchases from a supplier into the factory's warehouse
		// (increasing the quantity), hence the warehouse-item-movement
		// has a positive quantity for a regular purchase from a supplier. But the
		// invoice from the factory to the supplier must reimburse money, thus have a
		// negative total amount. The best approach for this would be to have a negative
		// quantity, a positive single price and a negative total price (quantity *
		// single).
		//
		invoiceItem.setQuantity(
				requireNonNull(warehouseItemMovement.getQuantity(), "warehouseItemMovement.quantity").negate());
		return invoiceItem;
	}

	protected InvoiceItemEntity persisteInvoiceItem(@NonNull InvoiceItemEntity invoiceItem) {
		entityManager.persist(invoiceItem);
		return invoiceItem;
	}

	protected abstract @NonNull List<WarehouseItemMovementEntity> getWarehouseItemMovementsToProcess();

}
