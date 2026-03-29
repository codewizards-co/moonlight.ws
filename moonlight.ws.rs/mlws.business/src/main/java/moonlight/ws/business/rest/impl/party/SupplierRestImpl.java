package moonlight.ws.business.rest.impl.party;

import java.time.Instant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.QueryParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.api.party.SupplierDto;
import moonlight.ws.api.party.SupplierDtoPage;
import moonlight.ws.api.party.SupplierFilter;
import moonlight.ws.api.party.SupplierRest;
import moonlight.ws.business.mapper.SupplierMapper;
import moonlight.ws.persistence.SearchResult;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.party.PartyDao;
import moonlight.ws.persistence.party.SupplierDao;
import moonlight.ws.persistence.party.SupplierEntity;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class SupplierRestImpl implements SupplierRest {

	@Inject
	private SupplierDao supplierDao;

	@Inject
	private SupplierMapper supplierMapper;

	@Inject
	protected UserDao userDao;

	@Inject
	private PartyDao partyDao;

	/**
	 * Comma-separated list of relation-properties to be fetched in the same
	 * HTTP-request.
	 * <p>
	 * For example, the {@link SupplierDto#getParty() Supplier.party}
	 */
	@QueryParam("fetch")
	protected String fetch;

	@Override
	public SupplierDto getSupplier(@NonNull Long id) throws Exception {
		supplierMapper.setFetch(fetch);
		var entity = supplierDao.getEntity(id);
		if (entity == null) { // || entity.getDeleted() != 0) { // TODO should we filter them out by default
			// and take query-param 'includeDeleted' into account?
			throw new NotFoundException();
		}
		return supplierMapper.toDto(entity);
	}

	@Override
	public SupplierDtoPage getSuppliers(SupplierFilter filter) throws Exception {
		supplierMapper.setFetch(fetch);
		filter = filter != null ? filter : new SupplierFilter();
		var searchResult = supplierDao.searchEntities(filter);
		var page = new SupplierDtoPage();
		page.copyFromFilter(filter);
		page.setItems(supplierMapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return page;
	}

	@Override
	public SupplierDto createSupplier(@NonNull SupplierDto dto) throws Exception {
		validate(null, dto);

		SupplierEntity entity = null;
		var filter = new SupplierFilter();
		filter.setIncludeDeleted(true);
		filter.setFilterPartyId(dto.getParty().getId());
		filter.setPageSize(Filter.MAX_PAGE_SIZE);
		SearchResult<SupplierEntity> searchResult = supplierDao.searchEntities(filter);

		entity = searchResult.getEntities().stream().filter(e -> e.getDeleted() == 0).findAny().orElse(null);
		if (entity != null) {
			String msg = "There is already a (non-deleted) supplier for party.id=%d!".formatted(dto.getParty().getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}

		entity = searchResult.getEntities().stream().findAny().orElse(null);
		if (entity != null) {
			entity.setDeleted(0); // UNDELETE
			entity.setDeletedByUserId(null);
		}

		entity = supplierMapper.toEntity(dto, entity);
		UserEntity user = userDao.currentUser();
		if (entity.getId() == null) {
			entity.setCreatedByUserId(user.getId());
		}
		entity.setChangedByUserId(user.getId());
		supplierDao.persistEntity(entity);
		return supplierMapper.toDto(entity);
	}

	@Override
	public SupplierDto updateSupplier(@NonNull Long id, @NonNull SupplierDto dto) throws Exception {
		var entity = supplierDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			throw new NotFoundException();
		}
		validate(id, dto);
		UserEntity user = userDao.currentUser();
		entity.setChanged(Instant.now());
		entity.setChangedByUserId(user.getId());
		entity = supplierMapper.toEntity(dto, entity);
		return supplierMapper.toDto(entity);
	}

	@Override
	public void deleteSupplier(@NonNull Long id) throws Exception {
		SupplierEntity entity = supplierDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			return;
		}
		entity.setDeleted(System.currentTimeMillis());
		entity.setDeletedByUserId(userDao.currentUser().getId());
	}

	protected void validate(Long id, @NonNull SupplierDto dto) throws Exception {
		// TODO switch to proper bean-validation
		validateId(id, dto);
		validateParty(dto);
	}

	protected void validateId(Long id, @NonNull SupplierDto dto) {
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

	protected void validateParty(@NonNull SupplierDto dto) {
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
