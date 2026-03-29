package moonlight.ws.business.booking;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.ejb3.annotation.TransactionTimeout;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.invoice.InvoiceFilter;
import moonlight.ws.api.warehouse.WarehouseItemMovementFilter;
import moonlight.ws.persistence.invoice.InvoiceDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementDao;

@RequestScoped
@Slf4j
public class LiferayBookingWorker {

	private static final long TX_HARD_TIMEOUT_MS = 120_000;
	private static final long TX_SOFT_TIMEOUT_MS = 60_000;

	@Inject
	private WarehouseItemMovementDao warehouseItemMovementDao;

	@Inject
	private InvoiceDao invoiceDao;

	@Inject
	private LiferayBookingHelper helper;

	private long workStartTimestamp;

	private Set<Long> processedIds;

	@Transactional(value = TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
	@TransactionTimeout(value = TX_HARD_TIMEOUT_MS, unit = TimeUnit.MILLISECONDS)
	public void work() throws Exception {
		workStartTimestamp = System.currentTimeMillis();

		if (getWorkStartTimestampMinute() % 2 == 0) {
			processInvoices();
			processWarehouseItemMovements();
		} else {
			processWarehouseItemMovements();
			processInvoices();
		}
	}

	protected int getWorkStartTimestampMinute() {
		var calendar = new GregorianCalendar();
		calendar.setTimeInMillis(workStartTimestamp);
		return calendar.get(Calendar.MINUTE);
	}

	protected long getWorkDurationMillis() {
		return System.currentTimeMillis() - workStartTimestamp;
	}

	protected void processWarehouseItemMovements() {
		processedIds = new HashSet<>();
		while (true) {
			if (getWorkDurationMillis() > TX_SOFT_TIMEOUT_MS) {
				log.info("processWarehouseItemMovements: soft-timeout elapsed! duration={} ms; softTimeout={} ms",
						getWorkDurationMillis(), TX_SOFT_TIMEOUT_MS);
				return;
			}

			Long warehouseItemMovementId = nextUnbookedWarehouseItemMovementId();
			if (warehouseItemMovementId == null) {
				log.info("processWarehouseItemMovements: no more unbooked WarehouseItemMovement found! duration={} ms",
						getWorkDurationMillis());
				return;
			}

			try {
				helper.bookWarehouseItemMovement(warehouseItemMovementId);
			} catch (Exception x) {
				log.error("processWarehouseItemMovements: warehouseItemMovementId=%s: failed."
						.formatted(warehouseItemMovementId), x);
			}
		}
	}

	protected void processInvoices() {
		processedIds = new HashSet<>();
		while (true) {
			if (getWorkDurationMillis() > TX_SOFT_TIMEOUT_MS) {
				log.info("processInvoices: soft-timeout elapsed! duration={} ms; softTimeout={} ms",
						getWorkDurationMillis(), TX_SOFT_TIMEOUT_MS);
				return;
			}

			Long invoiceId = nextUnbookedInvoiceId();
			if (invoiceId == null) {
				log.info("processInvoices: no more unbooked Invoice found! duration={} ms", getWorkDurationMillis());
				return;
			}

			try {
				helper.bookInvoice(invoiceId);
			} catch (Exception x) {
				log.error("processInvoices: invoiceId=%s: failed.".formatted(invoiceId), x);
			}
		}
	}

	// The shipment-filter does not work. We cannot query in a meaningful way. This
	// strategy makes no sense.
//	private boolean hasOpenDeliveryNote(WarehouseItemMovementEntity warehouseItemMovement) {
//		ShipmentResource shipmentResource = liferayResourceFactory.getResource(ShipmentResource.class);
//		ShipmentItemResource shipmentItemResource = liferayResourceFactory.getResource(ShipmentItemResource.class);
//
//		shipmentResource.getShipmentsPage(null, null, Pagination.of(0, 0), null)
//
//		shipmentItemResource.getShipmentItemsPage(shipment.id, null);
//
//		return false;
//	}

	protected Long nextUnbookedWarehouseItemMovementId() {
		var filter = new WarehouseItemMovementFilter();
		filter.setPageSize(10);
		filter.setFilterBooked(false);
		filter.setFilterDraft(false); // exclude drafts, only process finalized movements
		// we want to book positive quantities first, so that we prevent negative
		// stock-levels
		filter.setSort("quantity:desc");
		while (true) {
			log.debug("nextUnbookedWarehouseItemMovementId: querying with {}", filter);
			var searchResult = warehouseItemMovementDao.searchEntities(filter);
			if (searchResult.getEntities().isEmpty()) {
				return null;
			}
			for (var entity : searchResult.getEntities()) {
				if (processedIds.add(entity.getId())) {
					return entity.getId();
				}
			}
			if (filter.getPageNumberOrDefault() * filter.getPageSizeOrDefault() >= searchResult.getTotalSize()) {
				return null;
			}
			filter.setPageNumber(filter.getPageNumberOrDefault() + 1);
		}
	}

	protected Long nextUnbookedInvoiceId() {
		var filter = new InvoiceFilter();
		filter.setPageSize(10);
		filter.setFilterBooked(false);
		filter.setFilterDraft(false); // exclude drafts, only process finalized movements
		while (true) {
			log.debug("nextUnbookedInvoiceId: querying with {}", filter);
			var searchResult = invoiceDao.searchEntities(filter);
			if (searchResult.getEntities().isEmpty()) {
				return null;
			}
			for (var entity : searchResult.getEntities()) {
				if (processedIds.add(entity.getId())) {
					return entity.getId();
				}
			}
			if (filter.getPageNumberOrDefault() * filter.getPageSizeOrDefault() >= searchResult.getTotalSize()) {
				return null;
			}
			filter.setPageNumber(filter.getPageNumberOrDefault() + 1);
		}
	}
}
