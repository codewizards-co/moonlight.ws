package moonlight.ws.business.rest.impl.auth;

import java.time.Instant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.auth.UserDto;
import moonlight.ws.api.auth.UserDtoPage;
import moonlight.ws.api.auth.UserFilter;
import moonlight.ws.api.auth.UserRest;
import moonlight.ws.business.auth.UserMapper;
import moonlight.ws.persistence.SearchResult;
import moonlight.ws.persistence.auth.UserDao;
import moonlight.ws.persistence.auth.UserEntity;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class UserRestImpl implements UserRest {

	@Inject
	private UserDao userDao;

	@Inject
	private UserMapper userMapper;

	@Override
	public UserDto getUser(@NonNull Long id) throws Exception {
		var entity = userDao.getEntity(id);
		if (entity == null) {
			throw new NotFoundException();
		}
		return userMapper.toDto(entity);
	}

	@Override
	public UserDtoPage getUsers(UserFilter filter) throws Exception {
		filter = filter != null ? filter : new UserFilter();
		var searchResult = userDao.searchEntities(filter);
		var page = new UserDtoPage();
		page.copyFromFilter(filter);
		page.setItems(userMapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return page;
	}

	@Override
	public UserDto createUser(@NonNull UserDto dto) throws Exception {
		validate(null, dto);
		UserEntity entity = userMapper.toEntity(dto, null);
		UserEntity user = userDao.currentUser();
		entity.setCreatedByUserId(user.getId());
		entity.setChangedByUserId(user.getId());
		userDao.persistEntity(entity);
		return userMapper.toDto(entity);
	}

	@Override
	public UserDto updateUser(@NonNull Long id, @NonNull UserDto dto) throws Exception {
		var entity = userDao.getEntity(id);
		if (entity == null) {
			throw new NotFoundException();
		}
		validate(id, dto);
		UserEntity user = userDao.currentUser();
		entity.setChanged(Instant.now());
		entity.setChangedByUserId(user.getId());
		entity = userMapper.toEntity(dto, entity);
		return userMapper.toDto(entity);
	}

	protected void validate(Long id, @NonNull UserDto dto) throws Exception {
		// TODO switch to proper bean-validation
		validateId(id, dto);
		validateUsername(id, dto);
	}

	protected void validateId(Long id, @NonNull UserDto dto) {
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

	protected void validateUsername(Long id, @NonNull UserDto dto) {
		UserEntity entity = null;
		UserFilter filter = new UserFilter();
		filter.setFilterUsername(dto.getUsername());
		SearchResult<UserEntity> searchResult = userDao.searchEntities(filter);
		entity = searchResult.getEntities().isEmpty() ? null : searchResult.getEntities().get(0);
		if (entity != null && !entity.getId().equals(id)) {
			String msg = "There is already a user with username='%s'!".formatted(dto.getUsername());
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}
}
