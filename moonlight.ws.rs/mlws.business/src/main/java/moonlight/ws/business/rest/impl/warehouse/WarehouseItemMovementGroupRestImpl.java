package moonlight.ws.business.rest.impl.warehouse;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.warehouse.WarehouseItemMovementGroupDto;
import moonlight.ws.api.warehouse.WarehouseItemMovementGroupDtoPage;
import moonlight.ws.api.warehouse.WarehouseItemMovementGroupFilter;
import moonlight.ws.api.warehouse.WarehouseItemMovementGroupRest;
import moonlight.ws.business.mapper.WarehouseItemMovementGroupMapper;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementGroupDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementGroupEntity;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class WarehouseItemMovementGroupRestImpl implements WarehouseItemMovementGroupRest {

	@Inject
	private WarehouseItemMovementGroupMapper mapper;

	@Inject
	private WarehouseItemMovementGroupDao dao;

	@Inject
	private UserDao userDao;

	@Override
	public WarehouseItemMovementGroupDto getWarehouseItemMovementGroup(@NonNull Long id) throws Exception {
		WarehouseItemMovementGroupEntity entity = dao.getEntity(id);
		if (entity == null) {
			throw new NotFoundException();
		}
		return mapper.toDto(entity);
	}

	@Override
	public WarehouseItemMovementGroupDtoPage getWarehouseItemMovementGroups(WarehouseItemMovementGroupFilter filter)
			throws Exception {
		filter = filter == null ? new WarehouseItemMovementGroupFilter() : filter;
		var searchResult = dao.searchEntities(filter);
		var page = new WarehouseItemMovementGroupDtoPage();
		page.copyFromFilter(filter);
		page.setItems(mapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return page;
	}

	@Override
	public WarehouseItemMovementGroupDto createWarehouseItemMovementGroup(@NonNull WarehouseItemMovementGroupDto dto)
			throws Exception {
		if (dto.getDraft() != null && !dto.getDraft().booleanValue()) {
			String msg = "Cannot create a group with draft=false. You could never attach any movement to this group.";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		WarehouseItemMovementGroupEntity entity = mapper.toEntity(dto, null);
		UserEntity user = userDao.currentUser();
		entity.setCreatedByUserId(user.getId());
		entity.setChangedByUserId(user.getId());
		entity.setFinalized(0); // MUST always be a draft

		dao.persistEntity(entity);
		return mapper.toDto(entity);
	}

	@Override
	public WarehouseItemMovementGroupDto finalize(@NonNull Long id) throws Exception {
		WarehouseItemMovementGroupEntity group = dao.getEntity(id);
		if (group == null) {
			throw new NotFoundException();
		}
		EntityManager em = dao.getEntityManager();
		em.refresh(group, LockModeType.PESSIMISTIC_WRITE);
		if (group.getFinalized() != 0) {
			return mapper.toDto(group); // already finalized => immediately return without action
		}
		if (group.getMovements().isEmpty()) {
			String msg = "The group with id=%d does not contain any movement! Cannot finalize empty group." //
					.formatted(group.getId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		for (WarehouseItemMovementEntity movement : group.getMovements()) {
			em.refresh(movement, LockModeType.PESSIMISTIC_WRITE);
		}
		final long now = System.currentTimeMillis();
		group.setFinalized(now);
		for (WarehouseItemMovementEntity movement : group.getMovements()) {
			if (movement.getFinalized() == 0) {
				movement.setFinalized(now);
			}
		}
		return mapper.toDto(group);
	}
}
