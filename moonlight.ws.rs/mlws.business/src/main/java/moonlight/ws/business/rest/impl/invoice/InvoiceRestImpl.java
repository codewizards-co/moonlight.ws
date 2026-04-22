package moonlight.ws.business.rest.impl.invoice;

import static java.util.Objects.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.QueryParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.api.invoice.InvoiceDto;
import moonlight.ws.api.invoice.InvoiceDtoPage;
import moonlight.ws.api.invoice.InvoiceFilter;
import moonlight.ws.api.invoice.InvoiceInclude;
import moonlight.ws.api.invoice.InvoiceItemFilter;
import moonlight.ws.api.invoice.InvoiceItemRest;
import moonlight.ws.api.invoice.InvoiceRest;
import moonlight.ws.business.mapper.InvoiceMapper;
import moonlight.ws.persistence.SearchResult;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.invoice.InvoiceDao;
import moonlight.ws.persistence.invoice.InvoiceEntity;
import moonlight.ws.persistence.invoice.InvoiceItemDao;
import moonlight.ws.persistence.invoice.InvoiceItemEntity;
import moonlight.ws.persistence.party.ConsigneeDao;
import moonlight.ws.persistence.party.ConsigneeEntity;
import moonlight.ws.persistence.party.PartyDao;
import moonlight.ws.persistence.party.SupplierDao;
import moonlight.ws.persistence.party.SupplierEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class InvoiceRestImpl implements InvoiceRest {

	@Inject
	private InvoiceDao invoiceDao;

	@Inject
	private InvoiceItemDao invoiceItemDao;

	@Inject
	private InvoiceMapper invoiceMapper;

	@Inject
	protected UserDao userDao;

	@Inject
	protected PartyDao partyDao;

	@Inject
	protected ConsigneeDao consigneeDao;

	@Inject
	protected SupplierDao supplierDao;

	@Inject
	protected InvoiceItemRest invoiceItemRest;

	private Map<Long, SupplierEntity> supplierId2Supplier = new HashMap<>();
	private Map<Long, ConsigneeEntity> warehouseId2Consignee = new HashMap<>();

	/**
	 * Comma-separated list of relation-properties to be fetched in the same
	 * HTTP-request.
	 * <p>
	 * For example, the {@link InvoiceDto#getInvoice() InvoiceDto.party}
	 */
	@QueryParam("fetch")
	protected String fetch;

	@Override
	public InvoiceDto getInvoice(@NonNull Long id) throws Exception {
		invoiceMapper.setFetch(fetch);
		var entity = invoiceDao.getEntity(id);
		if (entity == null) { // || entity.getDeleted() != 0) { // TODO should we filter them out by default
			// and take query-param 'includeDeleted' into account?
			throw new NotFoundException();
		}
		return invoiceMapper.toDto(entity);
	}

	@Override
	public InvoiceDtoPage getInvoices(InvoiceFilter filter) throws Exception {
		invoiceMapper.setFetch(fetch);
		filter = filter != null ? filter : new InvoiceFilter();
		var searchResult = invoiceDao.searchEntities(filter);
		var page = new InvoiceDtoPage();
		page.copyFromFilter(filter);
		page.setItems(invoiceMapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return page;
	}

	@Override
	public InvoiceDto createInvoice(@NonNull InvoiceDto dto) throws Exception {
		validate(null, dto);
		var entity = invoiceMapper.toEntity(dto, null);
		UserEntity user = userDao.currentUser();
		entity.setCreatedByUserId(user.getId());
		entity.setChangedByUserId(user.getId());
		invoiceDao.persistEntity(entity);
		return invoiceMapper.toDto(entity);
	}

	@Override
	public InvoiceDto updateInvoice(@NonNull Long id, @NonNull InvoiceDto dto) throws Exception {
		var entity = invoiceDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			throw new NotFoundException();
		}
		assertNotFinalized(entity);
		validate(id, dto);
		UserEntity user = userDao.currentUser();
		entity.setChanged(Instant.now());
		entity.setChangedByUserId(user.getId());
		entity = invoiceMapper.toEntity(dto, entity);
		return invoiceMapper.toDto(entity);
	}

	@Override
	public void deleteInvoice(@NonNull Long id) throws Exception {
		var entity = invoiceDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			return;
		}
		assertNotFinalized(entity);
		final long now = System.currentTimeMillis();
		forEachInvoiceItem(id, item -> {
			item.setDeleted(now);
			item.setDeletedByUserId(userDao.currentUser().getId());
		});
		entity.setDeleted(now);
		entity.setDeletedByUserId(userDao.currentUser().getId());
	}

	protected void assertNotFinalized(@NonNull InvoiceEntity entity) {
		if (entity.getFinalized() != 0) {
			String msg = "invoice is already finalized!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	@Override
	public InvoiceDto finalize(@NonNull Long id) throws Exception {
		var entity = invoiceDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			throw new NotFoundException();
		}
		if (entity.getFinalized() != 0) {
			return invoiceMapper.toDto(entity);
		}
		assertCanFinalize(entity);
		final long now = System.currentTimeMillis();
		forEachInvoiceItem(id, item -> {
			item.setFinalized(now);
			item.setFinalizedByUserId(userDao.currentUser().getId());
		});
		entity.setFinalized(now);
		entity.setFinalizedByUserId(userDao.currentUser().getId());
		return invoiceMapper.toDto(entity);
	}

	@Override
	public InvoiceDto markPaid(@NonNull Long id, LocalDate paid) throws Exception {
		var entity = invoiceDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			throw new NotFoundException();
		}
		if (Objects.equals(paid, entity.getPaid())) { // prevent changes of markedPaid*, if not really changed.
			return invoiceMapper.toDto(entity);
		}
		assertCanMarkPaid(entity);
		entity.setPaid(paid);
		entity.setMarkedPaid(System.currentTimeMillis());
		entity.setMarkedPaidByUserId(userDao.currentUser().getId());
		return invoiceMapper.toDto(entity);
	}

	private void assertCanMarkPaid(InvoiceEntity entity) {
		if (entity.getFinalized() == 0) {
			String msg = "Invoice[id=%d] is not yet finalized!".formatted(entity.getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (entity.getBooked() == 0) {
			String msg = "Invoice[id=%d] is not yet booked into Liferay!".formatted(entity.getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	protected void forEachInvoiceItem(@NonNull Long invoiceId, @NonNull Consumer<? super InvoiceItemEntity> action) {
		InvoiceItemFilter filter = new InvoiceItemFilter();
		filter.setFilterInvoiceId(invoiceId);
		filter.setPageSize(Filter.MAX_PAGE_SIZE);
		long entitiesDoneCount = 0;
		while (true) {
			SearchResult<InvoiceItemEntity> searchResult = invoiceItemDao.searchEntities(filter);
			if (searchResult.getEntities().isEmpty()) {
				return;
			}
			searchResult.getEntities().forEach(action);
			entitiesDoneCount += searchResult.getEntities().size();
			if (entitiesDoneCount >= searchResult.getTotalSize()) {
				return;
			}
			filter.setPageNumber(filter.getPageNumberOrDefault() + 1);
		}
	}

	private void assertCanFinalize(@NonNull InvoiceEntity invoice) {
		requireNonNull(invoice.getParty().getId(), "invoice.party.id");

		Map<String, BigDecimal> sku2quantity = new HashMap<>();
		Map<String, BigDecimal> sku2priceTotalGross = new HashMap<>();
		Map<String, BigDecimal> sku2priceTotalNet = new HashMap<>();
		final BigDecimal[] priceTotalGross = { BigDecimal.ZERO };
		final BigDecimal[] priceTotalNet = { BigDecimal.ZERO };

		forEachInvoiceItem(invoice.getId(), item -> {
			assertCanFinalize(invoice, item);
			if (item.getInclude() != InvoiceInclude.INCLUDE) {
				return;
			}
			WarehouseItemMovementEntity warehouseItemMovement = item.getWarehouseItemMovement();
			var sku = "_item_" + item.getId();
			if (warehouseItemMovement != null) {
				sku = warehouseItemMovement.getSku();
			}
			sku2quantity.put(sku, sku2quantity.computeIfAbsent(sku, s -> BigDecimal.ZERO).add(item.getQuantity()));
			sku2priceTotalGross.put(sku,
					sku2priceTotalGross.computeIfAbsent(sku, s -> BigDecimal.ZERO).add(item.getPriceTotalGross()));
			sku2priceTotalNet.put(sku,
					sku2priceTotalNet.computeIfAbsent(sku, s -> BigDecimal.ZERO).add(item.getPriceTotalNet()));
			priceTotalGross[0] = priceTotalGross[0].add(item.getPriceTotalGross());
			priceTotalNet[0] = priceTotalNet[0].add(item.getPriceTotalGross());
		});

		switch (invoice.getWorkflow()) {
			case CONSIGNEE:
				if (priceTotalGross[0].compareTo(BigDecimal.ZERO) <= 0) {
					String msg = "Invoice[id=%d] with workflow CONSIGNEE must have a positive total gross, but it is negative (or 0)!"
							.formatted(invoice.getId());
					log.error(msg);
					throw new BadRequestException(msg);
				}
				if (priceTotalNet[0].compareTo(BigDecimal.ZERO) <= 0) {
					String msg = "Invoice[id=%d] with workflow CONSIGNEE must have a positive total net, but it is negative (or 0)!"
							.formatted(invoice.getId());
					log.error(msg);
					throw new BadRequestException(msg);
				}
				sku2quantity.forEach((sku, quantity) -> {
					if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
						String msg = "Invoice[id=%d] with workflow CONSIGNEE and sum of quantity for sku=%s must be positive, but it is negative (or 0)!"
								.formatted(invoice.getId(), sku);
						log.error(msg);
						throw new BadRequestException(msg);
					}
				});
				sku2priceTotalGross.forEach((sku, priceTotal) -> {
					if (priceTotal.compareTo(BigDecimal.ZERO) <= 0) {
						String msg = "Invoice[id=%d] with workflow CONSIGNEE and sum of priceTotalGross for sku=%s must be positive, but it is negative (or 0)!"
								.formatted(invoice.getId(), sku);
						log.error(msg);
						throw new BadRequestException(msg);
					}
				});
				sku2priceTotalNet.forEach((sku, priceTotal) -> {
					if (priceTotal.compareTo(BigDecimal.ZERO) <= 0) {
						String msg = "Invoice[id=%d] with workflow CONSIGNEE and sum of priceTotalNet for sku=%s must be positive, but it is negative (or 0)!"
								.formatted(invoice.getId(), sku);
						log.error(msg);
						throw new BadRequestException(msg);
					}
				});
				break;
			case SUPPLIER:
				if (priceTotalGross[0].compareTo(BigDecimal.ZERO) >= 0) {
					String msg = "Invoice[id=%d] with workflow SUPPLIER must have a negative total gross, but it is positive (or 0)!"
							.formatted(invoice.getId());
					log.error(msg);
					throw new BadRequestException(msg);
				}
				if (priceTotalNet[0].compareTo(BigDecimal.ZERO) >= 0) {
					String msg = "Invoice[id=%d] with workflow SUPPLIER must have a negative total net, but it is positive (or 0)!"
							.formatted(invoice.getId());
					log.error(msg);
					throw new BadRequestException(msg);
				}
				sku2quantity.forEach((sku, quantity) -> {
					if (quantity.compareTo(BigDecimal.ZERO) >= 0) {
						String msg = "Invoice[id=%d] with workflow SUPPLIER and sum of quantity for sku=%s must be negative, but it is positive (or 0)!"
								.formatted(invoice.getId(), sku);
						log.error(msg);
						throw new BadRequestException(msg);
					}
				});
				sku2priceTotalGross.forEach((sku, priceTotal) -> {
					if (priceTotal.compareTo(BigDecimal.ZERO) >= 0) {
						String msg = "Invoice[id=%d] with workflow SUPPLIER and sum of priceTotalGross for sku=%s must be negative, but it is positive (or 0)!"
								.formatted(invoice.getId(), sku);
						log.error(msg);
						throw new BadRequestException(msg);
					}
				});
				sku2priceTotalNet.forEach((sku, priceTotal) -> {
					if (priceTotal.compareTo(BigDecimal.ZERO) >= 0) {
						String msg = "Invoice[id=%d] with workflow SUPPLIER and sum of priceTotalNet for sku=%s must be negative, but it is positive (or 0)!"
								.formatted(invoice.getId(), sku);
						log.error(msg);
						throw new BadRequestException(msg);
					}
				});
				break;
			default:
				throw new IllegalStateException("invoice.workflow is unknown: " + invoice.getWorkflow());
		}
	}

	private void assertCanFinalize(@NonNull InvoiceEntity invoice, @NonNull InvoiceItemEntity invoiceItem) {
		if (invoiceItem.getInclude() == InvoiceInclude.EXCLUDE) {
			return;
		}
		WarehouseItemMovementEntity warehouseItemMovement = invoiceItem.getWarehouseItemMovement();
		if (warehouseItemMovement == null) {
			String msg = "invoice.id=%d, invoiceItem.id=%d: warehouseItemMovement must not be null!"
					.formatted(invoice.getId(), invoiceItem.getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		switch (invoice.getWorkflow()) {
			case CONSIGNEE:
				assertConsigneePartyCorrect(invoice, invoiceItem, warehouseItemMovement);
				break;
			case SUPPLIER:
				assertSupplierPartyCorrect(invoice, invoiceItem, warehouseItemMovement);
				break;
			default:
				throw new IllegalStateException("invoice.workflow is unknown: " + invoice.getWorkflow());
		}
		if (BigDecimal.ZERO.compareTo(invoiceItem.getQuantity()) == 0) {
			String msg = "invoice.id=%d, invoiceItem.id=%d: invoiceItem has quantity 0!".formatted(invoice.getId(),
					invoiceItem.getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (invoiceItem.getPriceTotalGross() == null || invoiceItem.getPriceTotalNet() == null) {
			String msg = "invoice.id=%d, invoiceItem.id=%d: invoiceItem has no price!".formatted(invoice.getId(),
					invoiceItem.getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	private void assertConsigneePartyCorrect(@NonNull InvoiceEntity invoice, @NonNull InvoiceItemEntity invoiceItem,
			@NonNull WarehouseItemMovementEntity warehouseItemMovement) {
		Long invoicePartyId = requireNonNull(invoice.getParty().getId(), "invoice.party.id");
		Long warehouseId = requireNonNull(warehouseItemMovement.getWarehouseId(), "warehouseItemMovement.warehouseId");
		ConsigneeEntity consignee = getConsignee(warehouseItemMovement);
		if (consignee == null) {
			String msg = "invoice.id=%d, invoiceItem.id=%d: No consignee assigned to warehouse.id=%d!"
					.formatted(invoice.getId(), invoiceItem.getId(), warehouseId);
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (!invoicePartyId.equals(consignee.getParty().getId())) {
			String msg = "invoice.id=%d, invoiceItem.id=%d: invoice.party.id=%d does not match consignee.party.id=%d!"
					.formatted(invoice.getId(), invoiceItem.getId(), invoicePartyId, consignee.getParty().getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	private void assertSupplierPartyCorrect(@NonNull InvoiceEntity invoice, @NonNull InvoiceItemEntity invoiceItem,
			@NonNull WarehouseItemMovementEntity warehouseItemMovement) {
		Long invoicePartyId = requireNonNull(invoice.getParty().getId(), "invoice.party.id");
		Long supplierId = warehouseItemMovement.getSupplierId();
		if (supplierId == null) {
			String msg = "invoice.id=%d, invoiceItem.id=%d: warehouseItemMovement.supplierId is null!"
					.formatted(invoice.getId(), invoiceItem.getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		SupplierEntity supplier = getSupplier(warehouseItemMovement);
		requireNonNull(supplier, "Supplier[id=%d] referenced by WarehouseItemMovement[id=%d] not found"
				.formatted(supplierId, warehouseItemMovement.getId()));
		if (!invoicePartyId.equals(supplier.getParty().getId())) {
			String msg = "invoice.id=%d, invoiceItem.id=%d: invoice.party.id=%d does not match supplier.party.id=%d!"
					.formatted(invoice.getId(), invoiceItem.getId(), invoicePartyId, supplier.getParty().getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	protected ConsigneeEntity getConsignee(@NonNull WarehouseItemMovementEntity warehouseItemMovement) {
		var warehouseId = requireNonNull(warehouseItemMovement.getWarehouseId(), "warehouseItemMovement.warehouseId");
		return warehouseId2Consignee.computeIfAbsent(warehouseId, id -> consigneeDao.getConsignee(warehouseId));
	}

	protected SupplierEntity getSupplier(@NonNull WarehouseItemMovementEntity warehouseItemMovement) {
		var supplierId = requireNonNull(warehouseItemMovement.getSupplierId(), "warehouseItemMovement.supplierId");
		return supplierId2Supplier.computeIfAbsent(supplierId, id -> supplierDao.getEntity(supplierId));
	}

	protected void validate(Long id, @NonNull InvoiceDto dto) throws Exception {
		// TODO switch to proper bean-validation
		validateId(id, dto);
		validateParty(dto);
		if (dto.getWorkflow() == null) {
			String msg = "workflow is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	protected void validateId(Long id, @NonNull InvoiceDto dto) {
		if (dto.getId() == null) {
			dto.setId(id);
			return;
		}
		if (!dto.getId().equals(id)) {
			String msg = "invalid id! dto.id=%d, but must be null or %d.".formatted(dto.getId(), id);
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	protected void validateParty(@NonNull InvoiceDto dto) {
		if (dto.getParty() == null) {
			String msg = "party is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getParty().getId() == null) {
			String msg = "party.id is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (partyDao.getEntity(dto.getParty().getId()) == null) {
			String msg = "party.id=%d references non-existent party!".formatted(dto.getParty().getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}
}
