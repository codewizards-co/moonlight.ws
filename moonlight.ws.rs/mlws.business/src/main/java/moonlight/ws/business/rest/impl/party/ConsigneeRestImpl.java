package moonlight.ws.business.rest.impl.party;

import java.time.Instant;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.problem.Problem;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.QueryParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.party.ConsigneeDto;
import moonlight.ws.api.party.ConsigneeDtoPage;
import moonlight.ws.api.party.ConsigneeFilter;
import moonlight.ws.api.party.ConsigneeRest;
import moonlight.ws.business.mapper.ConsigneeMapper;
import moonlight.ws.liferay.LiferayResourceFactory;
import moonlight.ws.persistence.SearchResult;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.party.ConsigneeDao;
import moonlight.ws.persistence.party.ConsigneeEntity;
import moonlight.ws.persistence.party.PartyDao;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class ConsigneeRestImpl implements ConsigneeRest {

	@Inject
	private ConsigneeDao consigneeDao;

	@Inject
	private ConsigneeMapper consigneeMapper;

	@Inject
	protected UserDao userDao;

	@Inject
	private PartyDao partyDao;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	/**
	 * Comma-separated list of relation-properties to be fetched in the same
	 * HTTP-request.
	 * <p>
	 * For example, the {@link ConsigneeDto#getParty() Consignee.party}
	 */
	@QueryParam("fetch")
	protected String fetch;

	@Override
	public ConsigneeDto getConsignee(@NonNull Long id) throws Exception {
		consigneeMapper.setFetch(fetch);
		var entity = consigneeDao.getEntity(id);
		if (entity == null) { // || entity.getDeleted() != 0) { // TODO should we filter them out by default
								// and take query-param 'includeDeleted' into account?
			throw new NotFoundException();
		}
		return consigneeMapper.toDto(entity);
	}

	@Override
	public ConsigneeDtoPage getConsignees(ConsigneeFilter filter) throws Exception {
		consigneeMapper.setFetch(fetch);
		filter = filter != null ? filter : new ConsigneeFilter();
		var searchResult = consigneeDao.searchEntities(filter);
		var page = new ConsigneeDtoPage();
		page.copyFromFilter(filter);
		page.setItems(consigneeMapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return page;
	}

	@Override
	public ConsigneeDto createConsignee(@NonNull ConsigneeDto dto) throws Exception {
		validate(null, dto);
		ConsigneeEntity entity = null;
		ConsigneeFilter filter = new ConsigneeFilter();
		filter.setIncludeDeleted(true);
		filter.setFilterPartyId(dto.getParty().getId());
		filter.setFilterWarehouseId(dto.getWarehouseId());
		SearchResult<ConsigneeEntity> searchResult = consigneeDao.searchEntities(filter);

		entity = searchResult.getEntities().stream().filter(e -> e.getDeleted() == 0).findAny().orElse(null);
		if (entity != null) {
			String msg = "There is already a (non-deleted) consignee for party.id=%d and warehouseId=%d!"
					.formatted(dto.getParty().getId(), dto.getWarehouseId());
			log.error(msg);
			throw new BadRequestException(msg);
		}

		entity = searchResult.getEntities().stream().findAny().orElse(null);
		if (entity != null) {
			entity.setDeleted(0); // UNDELETE
			entity.setDeletedByUserId(null);
		}

		entity = consigneeMapper.toEntity(dto, entity);
		UserEntity user = userDao.currentUser();
		entity.setCreatedByUserId(user.getId());
		entity.setChangedByUserId(user.getId());
		consigneeDao.persistEntity(entity);
		return consigneeMapper.toDto(entity);
	}

	@Override
	public ConsigneeDto updateConsignee(@NonNull Long id, @NonNull ConsigneeDto dto) throws Exception {
		var entity = consigneeDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			throw new NotFoundException();
		}
		validate(id, dto);
		UserEntity user = userDao.currentUser();
		entity.setChanged(Instant.now());
		entity.setChangedByUserId(user.getId());
		entity = consigneeMapper.toEntity(dto, entity);
		return consigneeMapper.toDto(entity);
	}

	@Override
	public void deleteConsignee(@NonNull Long id) throws Exception {
		ConsigneeEntity entity = consigneeDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			return;
		}
		entity.setDeleted(System.currentTimeMillis());
		entity.setDeletedByUserId(userDao.currentUser().getId());
	}

	protected void validate(Long id, @NonNull ConsigneeDto dto) throws Exception {
		// TODO switch to proper bean-validation
		validateId(id, dto);
		validateParty(dto);
		validateWarehouseId(dto);
	}

	private void validateWarehouseId(@NonNull ConsigneeDto dto) throws Exception {
		Long warehouseId = dto.getWarehouseId();
		if (warehouseId == null) {
			String msg = "warehouseId is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		WarehouseResource warehouseResource = liferayResourceFactory.getResource(WarehouseResource.class);
		Warehouse warehouse;
		try {
			warehouse = warehouseResource.getWarehouseId(warehouseId);
		} catch (Problem.ProblemException x) {
			if (x.getProblem() != null && "NOT_FOUND".equals(x.getProblem().getStatus())) {
				warehouse = null;
			} else {
				throw x;
			}
		}
		if (warehouse == null) {
			String msg = "warehouseId=%d references non-existent warehouse!".formatted(warehouseId);
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}

	protected void validateId(Long id, @NonNull ConsigneeDto dto) {
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

	protected void validateParty(@NonNull ConsigneeDto dto) {
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
