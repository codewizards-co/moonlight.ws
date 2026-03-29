package moonlight.ws.business.rest.impl.invoice;

import static java.util.Objects.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.QueryParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.PriceDto;
import moonlight.ws.api.invoice.AutoCreateInvoiceItemsRequest;
import moonlight.ws.api.invoice.InvoiceItemDto;
import moonlight.ws.api.invoice.InvoiceItemDtoPage;
import moonlight.ws.api.invoice.InvoiceItemFilter;
import moonlight.ws.api.invoice.InvoiceItemRest;
import moonlight.ws.business.mapper.InvoiceItemMapper;
import moonlight.ws.business.mapper.PriceMapper;
import moonlight.ws.persistence.SearchResult;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.invoice.InvoiceDao;
import moonlight.ws.persistence.invoice.InvoiceEntity;
import moonlight.ws.persistence.invoice.InvoiceItemDao;
import moonlight.ws.persistence.invoice.InvoiceItemEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class InvoiceItemRestImpl implements InvoiceItemRest {

	@Inject
	private InvoiceItemDao invoiceItemDao;

	@Inject
	private InvoiceItemMapper invoiceItemMapper;

	@Inject
	private UserDao userDao;

	@Inject
	private InvoiceDao invoiceDao;

	@Inject
	private WarehouseItemMovementDao warehouseItemMovementDao;

	@Inject
	private AutoCreateInvoiceItemsForConsignee autoCreateInvoiceItemsForConsignee;

	@Inject
	private AutoCreateInvoiceItemsForSupplier autoCreateInvoiceItemsForSupplier;

	@Inject
	private PriceMapper priceMapper;

	/**
	 * Comma-separated list of relation-properties to be fetched in the same
	 * HTTP-request.
	 * <p>
	 * For example, the {@link InvoiceItemDto#getInvoice() InvoiceItemDto.invoice}
	 */
	@QueryParam("fetch")
	protected String fetch;

	@Override
	public InvoiceItemDto getInvoiceItem(@NonNull Long id) throws Exception {
		invoiceItemMapper.setFetch(fetch);
		var entity = invoiceItemDao.getEntity(id);
		if (entity == null) { // || entity.getDeleted() != 0) { // TODO should we filter them out by default
			// and take query-param 'includeDeleted' into account?
			throw new NotFoundException();
		}
		return invoiceItemMapper.toDto(entity);
	}

	@Override
	public InvoiceItemDtoPage getInvoiceItems(InvoiceItemFilter filter) throws Exception {
		invoiceItemMapper.setFetch(fetch);
		filter = filter != null ? filter : new InvoiceItemFilter();
		var searchResult = invoiceItemDao.searchEntities(filter);
		var page = new InvoiceItemDtoPage();
		page.copyFromFilter(filter);
		page.setItems(invoiceItemMapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return page;
	}

	@Override
	public InvoiceItemDto createInvoiceItem(@NonNull InvoiceItemDto dto) throws Exception {
		invoiceItemMapper.setFetch(fetch);
		validate(null, dto);
		var entity = invoiceItemMapper.toEntity(dto, null);
		invoiceDao.lock(entity.getInvoice());
		assertNotFinalized(entity.getInvoice());
		UserEntity user = userDao.currentUser();
		entity.setCreatedByUserId(user.getId());
		entity.setChangedByUserId(user.getId());
		invoiceItemDao.persistEntity(entity);
		copyPriceToWarehouseItemMovement(entity);
		updateTotal(entity.getInvoice());
		return invoiceItemMapper.toDto(entity);
	}

	@Override
	public InvoiceItemDto updateInvoiceItem(@NonNull Long id, @NonNull InvoiceItemDto dto) throws Exception {
		invoiceItemMapper.setFetch(fetch);
		var entity = invoiceItemDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			throw new NotFoundException();
		}
		validate(id, dto);
		invoiceDao.lock(entity.getInvoice());
		assertNotFinalized(entity);
		assertNotFinalized(entity.getInvoice());
		UserEntity user = userDao.currentUser();
		entity.setChanged(Instant.now());
		entity.setChangedByUserId(user.getId());
		entity = invoiceItemMapper.toEntity(dto, entity);
		copyPriceToWarehouseItemMovement(entity);
		updateTotal(entity.getInvoice());
		return invoiceItemMapper.toDto(entity);
	}

	protected void assertNotFinalized(@NonNull InvoiceItemEntity invoiceItem) {
		if (invoiceItem.getFinalized() != 0) {
			String msg = "invoice-item already finalized!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	protected void assertNotFinalized(@NonNull InvoiceEntity invoice) {
		if (invoice.getFinalized() != 0) {
			String msg = "invoice already finalized!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	@Override
	public void deleteInvoiceItem(@NonNull Long id) throws Exception {
		var entity = invoiceItemDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			return;
		}
		invoiceDao.lock(entity.getInvoice());
		assertNotFinalized(entity.getInvoice());
		entity.setDeleted(System.currentTimeMillis());
		entity.setDeletedByUserId(userDao.currentUser().getId());
		// note: this method is not called by InvoiceRestImpl.deleteInvoice(...), i.e.
		// when deleting an invoice implicitly deletes all invoice-items.
		updateTotal(entity.getInvoice());
	}

	@Override
	public List<InvoiceItemDto> autoCreateInvoiceItems(@NonNull AutoCreateInvoiceItemsRequest request)
			throws Exception {
		invoiceItemMapper.setFetch(fetch);
		if (request.getInvoice() == null) {
			String msg = "invoice is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (request.getInvoice().getId() == null) {
			String msg = "invoice.id is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		InvoiceEntity invoice = invoiceDao.getEntity(request.getInvoice().getId());
		if (invoice == null || invoice.getDeleted() != 0) {
			String msg = "invoice.id=%d references non-existent invoice!".formatted(request.getInvoice().getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		invoiceDao.lock(invoice);
		List<InvoiceItemEntity> invoiceItems;
		switch (invoice.getWorkflow()) {
			case CONSIGNEE:
				invoiceItems = autoCreateInvoiceItemsForConsignee.createInvoiceItems(invoice);
				break;
			case SUPPLIER:
				invoiceItems = autoCreateInvoiceItemsForSupplier.createInvoiceItems(invoice);
				break;
			default:
				throw new IllegalStateException("Unknown workflow: " + invoice.getWorkflow());
		}
		updateTotal(invoice);
		return invoiceItemMapper.toDtos(invoiceItems);
	}

	protected void validate(Long id, @NonNull InvoiceItemDto dto) throws Exception {
		// TODO switch to proper bean-validation
		validateId(id, dto);
		validateInvoice(dto);
		if (dto.getInclude() == null) {
			String msg = "include is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getQuantity() == null && dto.getPrice() != null && dto.getPrice().getQuantity() != null) {
			dto.setQuantity(dto.getPrice().getQuantity());
		}

		validateWarehouseItemMovement(dto);

		if (dto.getPrice() != null) {
			PriceDto price = dto.getPrice();
			if (price.getQuantity() == null) {
				price.setQuantity(dto.getQuantity());
			} else if (price.getQuantity().compareTo(dto.getQuantity()) != 0) {
				String msg = "quantity=%s differs from price.quantity=%s!".formatted(dto.getQuantity().toPlainString(),
						price.getQuantity().toPlainString());
				log.error(msg);
				throw new BadRequestException(msg);
			}
			try {
				priceMapper.calculateMissingProperties(price);
			} catch (Exception x) {
				String msg = "price is too incomplete to calculate missing properties!";
				log.error(msg, x);
				throw new BadRequestException(msg);
			}
		}
	}

	protected void validateId(Long id, @NonNull InvoiceItemDto dto) {
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

	protected void validateInvoice(@NonNull InvoiceItemDto dto) {
		if (dto.getInvoice() == null) {
			String msg = "invoice is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getInvoice().getId() == null) {
			String msg = "invoice.id is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (invoiceDao.getEntity(dto.getInvoice().getId()) == null) {
			String msg = "invoice.id=%d references non-existent invoice!".formatted(dto.getInvoice().getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	protected void validateWarehouseItemMovement(@NonNull InvoiceItemDto dto) {
		if (dto.getWarehouseItemMovement() == null) {
			return; // may be null
		}
		// but if it exists, it must be valid
		if (dto.getWarehouseItemMovement().getId() == null) {
			String msg = "warehouseItemMovement.id is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		var warehouseItemMovement = warehouseItemMovementDao.getEntity(dto.getWarehouseItemMovement().getId());
		if (warehouseItemMovement == null) {
			String msg = "warehouseItemMovement.id=%d references non-existent warehouse-item-movement!"
					.formatted(dto.getWarehouseItemMovement().getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getQuantity() == null) {
			dto.setQuantity(warehouseItemMovement.getQuantity().negate());
		} else if (dto.getQuantity().compareTo(warehouseItemMovement.getQuantity().negate()) != 0) {
			String msg = "quantity=%s does not match negated WarehouseItemMovement[id=%d].quantity=%s!".formatted(
					dto.getQuantity().toPlainString(), warehouseItemMovement.getId(),
					warehouseItemMovement.getQuantity().toPlainString());
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	protected void copyPriceToWarehouseItemMovement(@NonNull InvoiceItemEntity invoiceItem) {
		WarehouseItemMovementEntity warehouseItemMovement = invoiceItem.getWarehouseItemMovement();
		if (warehouseItemMovement != null) {
			warehouseItemMovement.setPriceTotalGross(invoiceItem.getPriceTotalGross());
			warehouseItemMovement.setPriceTotalNet(invoiceItem.getPriceTotalNet());
			warehouseItemMovement.setTaxPercent(invoiceItem.getTaxPercent());
		}
	}

	private void updateTotal(InvoiceEntity invoice) {
		invoiceItemDao.flush(); // write everything so that the query returns a correct result
		InvoiceItemFilter filter = new InvoiceItemFilter();
		filter.setFilterInvoiceId(requireNonNull(invoice.getId(), "invoice.id"));
		invoice.setIncludedTotalGross(BigDecimal.ZERO);
		invoice.setIncludedTotalNet(BigDecimal.ZERO);
		invoice.setExcludedTotalGross(BigDecimal.ZERO);
		invoice.setExcludedTotalNet(BigDecimal.ZERO);
		while (true) {
			SearchResult<InvoiceItemEntity> searchResult = invoiceItemDao.searchEntities(filter);
			if (searchResult.getEntities().isEmpty()) {
				break;
			}
			for (InvoiceItemEntity item : searchResult.getEntities()) {
				switch (item.getInclude()) {
					case INCLUDE:
						invoice.setIncludedTotalGross(
								invoice.getIncludedTotalGross().add(nullTo0(item.getPriceTotalGross())));
						invoice.setIncludedTotalNet(
								invoice.getIncludedTotalNet().add(nullTo0(item.getPriceTotalNet())));
						break;
					case EXCLUDE:
						invoice.setExcludedTotalGross(
								invoice.getExcludedTotalGross().add(nullTo0(item.getPriceTotalGross())));
						invoice.setExcludedTotalNet(
								invoice.getExcludedTotalNet().add(nullTo0(item.getPriceTotalNet())));
						break;
					default:
						throw new IllegalStateException("Unknown include: " + item.getInclude());
				}
			}
			filter.setPageNumber(filter.getPageNumberOrDefault() + 1);
			if (filter.getPageNumber() * filter.getPageSizeOrDefault() > searchResult.getTotalSize()) {
				break;
			}
		}
	}

	private static BigDecimal nullTo0(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}
}
