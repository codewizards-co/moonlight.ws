package moonlight.ws.business.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import moonlight.ws.api.shorturl.ShortUrlDto;
import moonlight.ws.persistence.shorturl.ShortUrlEntity;

@ApplicationScoped
public class ShortUrlMapper extends AbstractMapper<ShortUrlEntity, ShortUrlDto> {

	@Override
	protected void copyPropertiesToEntity(@NonNull ShortUrlEntity entity, @NonNull ShortUrlDto dto) {
		// id, created*, changed*, booked cannot be written by client!
		entity.setCode(dto.getCode());
		entity.setShortUrl(dto.getShortUrl());
		entity.setLongUrl(dto.getLongUrl());
	}

	@Override
	protected void copyPropertiesToDto(@NonNull ShortUrlDto dto, @NonNull ShortUrlEntity entity) {
		dto.setId(entity.getId());
		dto.setChanged(entity.getChanged());
		dto.setChangedByUserId(entity.getChangedByUserId());
		dto.setCreated(entity.getCreated());
		dto.setCreatedByUserId(entity.getCreatedByUserId());
		dto.setCode(entity.getCode());
		dto.setShortUrl(entity.getShortUrl());
		dto.setLongUrl(entity.getLongUrl());
	}
}
