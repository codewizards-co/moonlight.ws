package moonlight.ws.business.rest.impl.party;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;
import moonlight.ws.api.party.PartyDefaultDto;
import moonlight.ws.api.party.PartyDefaultDtoPage;
import moonlight.ws.api.party.PartyDefaultRest;
import moonlight.ws.business.mapper.PartyDefaultMapper;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.party.PartyDefaultDao;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class PartyDefaultRestImpl implements PartyDefaultRest {

	@Inject
	private PartyDefaultDao partyDefaultDao;

	@Inject
	private PartyDefaultMapper partyDefaultMapper;

	@Inject
	protected UserDao userDao;

	@Override
	public PartyDefaultDto getPartyDefault(@NonNull Long id) throws Exception {
		var entity = partyDefaultDao.getEntity(id);
		if (entity == null) {
			throw new NotFoundException();
		}
		return partyDefaultMapper.toDto(entity);
	}

	@Override
	public PartyDefaultDtoPage getPartyDefaults() throws Exception {
		var entity = partyDefaultDao.getEntity();
		var page = new PartyDefaultDtoPage();
		page.copyFromFilter(new Filter());
		page.setItems(partyDefaultMapper.toDtos(Collections.singletonList(entity)));
		page.setTotalSize(1);
		return page;
	}

	@Override
	public PartyDefaultDto updatePartyDefault(@NonNull Long id, @NonNull PartyDefaultDto dto) throws Exception {
		var entity = partyDefaultDao.getEntity(id);
		if (entity == null) {
			throw new NotFoundException();
		}
		validate(id, dto);
		UserEntity user = userDao.currentUser();
		entity.setChanged(Instant.now());
		entity.setChangedByUserId(user.getId());
		entity = partyDefaultMapper.toEntity(dto, entity);
		return partyDefaultMapper.toDto(entity);
	}

	protected void validate(@NonNull Long id, @NonNull PartyDefaultDto dto) throws Exception {
		// TODO switch to proper bean-validation
		validateId(id, dto);
		validateTradeDiscountPercent(dto);
	}

	protected void validateId(Long id, @NonNull PartyDefaultDto dto) {
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

	protected void validateTradeDiscountPercent(@NonNull PartyDefaultDto dto) throws Exception {
		if (dto.getTradeDiscountPercent() == null) {
			String msg = "tradeDiscountPercent is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getTradeDiscountPercent().compareTo(BigDecimal.valueOf(100L)) > 0) {
			String msg = "tradeDiscountPercent is greater than 100!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getTradeDiscountPercent().compareTo(BigDecimal.ZERO) < 0) {
			String msg = "tradeDiscountPercent is negative!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
	}
}
