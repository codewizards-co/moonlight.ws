package moonlight.ws.business.rest.impl.party;

import static moonlight.ws.base.util.StringUtil.*;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.party.PartyDto;
import moonlight.ws.api.party.PartyDtoPage;
import moonlight.ws.api.party.PartyFilter;
import moonlight.ws.api.party.PartyRest;
import moonlight.ws.api.party.SupplierDto;
import moonlight.ws.business.mapper.PartyMapper;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.party.PartyDao;
import moonlight.ws.persistence.party.PartyEntity;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class PartyRestImpl implements PartyRest {

	@Inject
	private PartyDao partyDao;

	@Inject
	private PartyMapper partyMapper;

	@Inject
	protected UserDao userDao;

	/**
	 * Comma-separated list of relation-properties to be fetched in the same
	 * HTTP-request.
	 * <p>
	 * For example, the {@link SupplierDto#getParty() Supplier.party}
	 */
	@QueryParam("fetch")
	protected String fetch;

	@Override
	public PartyDto getParty(@NonNull Long id) throws Exception {
		partyMapper.setFetch(fetch);
		var entity = partyDao.getEntity(id);
		if (entity == null) { // || entity.getDeleted() != 0) { // TODO should we filter them out by default
			// and take query-param 'includeDeleted' into account?
			throw new NotFoundException();
		}
		return partyMapper.toDto(entity);
	}

	@Override
	public PartyDtoPage getParties(PartyFilter filter) throws Exception {
		partyMapper.setFetch(fetch);
		filter = filter != null ? filter : new PartyFilter();
		var searchResult = partyDao.searchEntities(filter);
		var page = new PartyDtoPage();
		page.copyFromFilter(filter);
		page.setItems(partyMapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return page;
	}

	@Override
	public PartyDto createParty(@NonNull PartyDto dto) throws Exception {
		validate(null, dto);
		var entity = partyMapper.toEntity(dto, null);
		UserEntity user = userDao.currentUser();
		entity.setCreatedByUserId(user.getId());
		entity.setChangedByUserId(user.getId());
		partyDao.persistEntity(entity);
		return partyMapper.toDto(entity);
	}

	@Override
	public PartyDto updateParty(@NonNull Long id, @NonNull PartyDto dto) throws Exception {
		var entity = partyDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			throw new NotFoundException();
		}
		validate(id, dto);
		UserEntity user = userDao.currentUser();
		entity.setChanged(Instant.now());
		entity.setChangedByUserId(user.getId());
		entity = partyMapper.toEntity(dto, entity);
		return partyMapper.toDto(entity);
	}

	@Override
	public void deleteParty(@NonNull Long id) throws Exception {
		var entity = partyDao.getEntity(id);
		if (entity == null || entity.getDeleted() != 0) {
			return;
		}
		entity.setDeleted(System.currentTimeMillis());
		entity.setDeletedByUserId(userDao.currentUser().getId());
	}

	protected void validate(Long id, @NonNull PartyDto dto) throws Exception {
		// TODO switch to proper bean-validation
		validateId(id, dto);
		validateCode(dto);
		validateName(dto);
	}

	protected void validateId(Long id, @NonNull PartyDto dto) {
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

	protected void validateCode(@NonNull PartyDto dto) throws Exception {
		if (isEmpty(dto.getCode())) {
			String msg = "code is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (Pattern.compile("\\s").matcher(dto.getCode()).find()) {
			String msg = "code contains white-space!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		var filter = new PartyFilter();
		filter.setFilterCode(dto.getCode());
		Optional<PartyEntity> otherPartyEntity = partyDao.searchEntities(filter).getEntities().stream() //
				.filter(e -> !e.getId().equals(dto.getId())).findAny();
		if (otherPartyEntity.isPresent()) {
			String msg = "Duplicate code '%s'!".formatted(dto.getCode());
			log.error(msg);
			throw new ClientErrorException(msg, Response.Status.CONFLICT);
		}
	}

	protected void validateName(@NonNull PartyDto dto) throws Exception {
		if (isEmpty(dto.getName())) {
			String msg = "name is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (!trim(dto.getName()).equals(dto.getName())) {
			String msg = "name begins or ends with white-spaces!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}
}
