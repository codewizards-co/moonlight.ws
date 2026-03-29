package moonlight.ws.business.rest.impl;

import static java.util.Objects.*;
import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.base.util.UrlUtil.*;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.shorturl.ShortUrlDto;
import moonlight.ws.api.shorturl.ShortUrlDtoPage;
import moonlight.ws.api.shorturl.ShortUrlFilter;
import moonlight.ws.api.shorturl.ShortUrlRest;
import moonlight.ws.base.util.IdentifierUtil;
import moonlight.ws.business.mapper.ShortUrlMapper;
import moonlight.ws.business.shorturl.ShortUrlConfig;
import moonlight.ws.business.shorturl.ShortUrlStaticMapping;
import moonlight.ws.business.shorturl.ShortUrlStaticMappingRegistry;
import moonlight.ws.business.shorturl.UrlType;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.shorturl.ShortUrlDao;
import moonlight.ws.persistence.shorturl.ShortUrlEntity;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class ShortUrlRestImpl implements ShortUrlRest {

	@Inject
	protected ShortUrlDao shortUrlDao;

	@Inject
	protected ShortUrlMapper shortUrlMapper;

	@Inject
	protected ShortUrlStaticMappingRegistry shortUrlStaticMappingRegistry;

	@Inject
	protected ShortUrlConfig shortUrlConfig;

	@Inject
	protected UserDao userDao;

	@Override
	public ShortUrlDtoPage getShortUrls(ShortUrlFilter filter) {
		filter = filter != null ? filter : new ShortUrlFilter();
		var searchResult = shortUrlDao.searchEntities(filter);
		var page = new ShortUrlDtoPage();
		page.copyFromFilter(filter);
		page.setItems(shortUrlMapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return page;
	}

	@Override
	public ShortUrlDto getShortUrl(@NonNull Long id) throws Exception {
		var entity = shortUrlDao.getEntity(id);
		if (entity == null) {
			throw new NotFoundException();
		}
		return shortUrlMapper.toDto(entity);
	}

	@Override
	public ShortUrlDto getShortUrlByUrl(@NonNull String _url) throws Exception {
		log.info("urlFromClient: " + _url);
		String url = URLDecoder.decode(_url, StandardCharsets.UTF_8);
		log.info("urlDecoded: " + url);
		url = workaroundFixBrokenUrl(url);
		log.info("url: " + url);
		for (ShortUrlStaticMapping shortUrlStaticMapping : shortUrlStaticMappingRegistry.getShortUrlStaticMappings()) {
			UrlType urlType = shortUrlStaticMapping.canMap(url);
			if (urlType != null) {
				ShortUrlDto shortUrlDto = new ShortUrlDto();
				String mappedUrl = shortUrlStaticMapping.map(url);
				switch (urlType) {
					case LONG:
						shortUrlDto.setLongUrl(url);
						shortUrlDto.setShortUrl(mappedUrl);
						break;
					case SHORT:
						shortUrlDto.setShortUrl(url);
						shortUrlDto.setLongUrl(mappedUrl);
						break;
					default:
						throw new IllegalStateException("Unknown urlType: " + urlType);
				}
				return shortUrlDto;
			}
		}

		var shortUrlEntity = shortUrlDao.getShortUrlByLongUrl(url);
		if (shortUrlEntity != null) {
			return shortUrlMapper.toDto(shortUrlEntity);
		}
		shortUrlEntity = shortUrlDao.getShortUrlByShortUrl(url);
		if (shortUrlEntity != null) {
			return shortUrlMapper.toDto(shortUrlEntity);
		}
		throw new NotFoundException();
	}

	@Override
	public Response redirect(@NonNull String code) throws Exception {
		ShortUrlEntity entity = shortUrlDao.getShortUrlByCode(code);
		if (entity == null) {
			throw new NotFoundException();
		}
		URI redirectUri;
		try {
			redirectUri = URI.create(entity.getLongUrl());
		} catch (Exception x) {
			log.debug("redirect: longUrl='%s' is not a legal URI: %s".formatted(entity.getLongUrl(), x), x);
			throw new BadRequestException("longUrl='%s' is not a legal URI!".formatted(entity.getLongUrl()));
		}
		log.info("redirectUri: {}", redirectUri);
		return Response.status(Response.Status.FOUND).location(redirectUri).build();
	}

	@Override
	public ShortUrlDto createShortUrl(@NonNull ShortUrlDto shortUrl) {
		final var longUrl = shortUrl.getLongUrl();
		if (isEmpty(longUrl)) {
			throw new BadRequestException("Property 'longUrl' is missing or empty!");
		}
		if (longUrl.length() > ShortUrlDto.LONG_URL_LENGTH_MAX) {
			throw new BadRequestException(
					"Property 'longUrl' is too long (exceeds %d chars)!".formatted(ShortUrlDto.LONG_URL_LENGTH_MAX));
		}
		if (shortUrl.getShortUrl() != null && shortUrl.getShortUrl().length() > ShortUrlDto.SHORT_URL_LENGTH_MAX) {
			throw new BadRequestException(
					"Property 'shortUrl' is too long (exceeds %d chars)!".formatted(ShortUrlDto.SHORT_URL_LENGTH_MAX));
		}
		validateUrlForDynamicShortUrl(longUrl, UrlType.LONG);
		if (!isEmpty(shortUrl.getShortUrl())) {
			validateUrlForDynamicShortUrl(shortUrl.getShortUrl(), UrlType.SHORT);
		}
		if (longUrl.startsWith(concatUrlSegments(shortUrlConfig.getUrl(), "r/"))) {
			throw new BadRequestException("Property 'longUrl' is already a short URL!");
		}
		if (!isEmpty(shortUrl.getCode())) {
			ShortUrlEntity entity = shortUrlDao.getShortUrlByCode(shortUrl.getCode());
			if (entity != null) {
				if (!entity.getLongUrl().equals(longUrl)) {
					throw new BadRequestException(
							"There exists already a dynamic ShortUrl-instance for the code '%s', but its long-URL '%s' does not match the new long-URL '%s'."
									.formatted(shortUrl.getCode(), entity.getLongUrl(), longUrl));
				}
			}
		}
		ShortUrlEntity entity = shortUrlDao.getShortUrlByLongUrl(longUrl);
		ShortUrlEntity entity2 = isEmpty(shortUrl.getShortUrl()) ? null
				: shortUrlDao.getShortUrlByShortUrl(shortUrl.getShortUrl());
		if (entity != null) {
			Long entity1Id = requireNonNull(entity.getId(), "entity.id");
			Long entity2Id = entity2 == null ? null : entity2.getId();
			if (entity1Id.equals(entity2Id)) {
				entity2 = entity;
			} else {
				if (entity2Id != null) {
					throw new BadRequestException(
							"There exist already 2 different dynamic ShortUrl-instances for the URLs '%s' and '%s'."
									.formatted(longUrl, shortUrl.getShortUrl()));
				}
			}
			if (!isEmpty(shortUrl.getCode())) {
				if (!entity.getCode().equals(shortUrl.getCode())) {
					throw new BadRequestException(
							"There exists already a dynamic ShortUrl-instance for the URLs '%s' and '%s', but its code '%s' does not match the new code '%s'."
									.formatted(longUrl, shortUrl.getShortUrl()));
				}
			}
			if (!isEmpty(shortUrl.getShortUrl())) {
				if (!entity.getShortUrl().equals(shortUrl.getShortUrl())) {
					throw new BadRequestException(
							"There exists already a dynamic ShortUrl-instance for the long-URL '%s', but it has the short-URL '%s' (not matching new short-URL '%s')."
									.formatted(longUrl, entity.getShortUrl(), shortUrl.getShortUrl()));
				}
			}
			// Most likely, this is a retry (we receive valid data matching our previously
			// persisted data).
			return shortUrlMapper.toDto(entity);
		}
		if (entity2 != null && entity != entity2) {
			throw new BadRequestException(
					"There exists already another dynamic ShortUrl-instance for the short-URL '%s', but the long-URL '%s' is still unknown."
							.formatted(shortUrl.getShortUrl(), longUrl));
		}
		entity = shortUrlMapper.toEntity(shortUrl, null);
		if (isEmpty(entity.getCode())) {
			entity.setCode(generateCode());
		}
		if (isEmpty(entity.getShortUrl())) {
			entity.setShortUrl(concatUrlSegments(shortUrlConfig.getUrl(), "r", entity.getCode()));
		}
		UserEntity user = userDao.currentUser();
		entity.setCreatedByUserId(user.getId());
		entity.setChangedByUserId(user.getId());
		shortUrlDao.persistEntity(entity);
		return shortUrlMapper.toDto(entity);
	}

	protected String generateCode() {
		int length = 4;
		for (int i = 0; i < 200; ++i) {
			String code = IdentifierUtil.createRandomID(length);
			ShortUrlEntity entity = shortUrlDao.getShortUrlByCode(code);
			if (entity == null) {
				return code;
			}
			if (i % 10 == 0) {
				length += 1;
			}
		}
		throw new IllegalStateException("Did not find any available code!");
	}

	protected void validateUrlForDynamicShortUrl(@NonNull String url, @NonNull UrlType urlType) {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			String msg = "url='%s' is not a legal URL! It does not start with 'http://' or 'https://'!".formatted(url);
			log.error(msg);
			throw new BadRequestException(msg);
		}
		try {
			URI.create(url);
		} catch (Exception x) {
			log.error("url='%s' is not a legal URI: %s".formatted(url, x), x);
			throw new BadRequestException("url='%s' is not a legal URI!".formatted(url));
		}
		for (ShortUrlStaticMapping shortUrlStaticMapping : shortUrlStaticMappingRegistry.getShortUrlStaticMappings()) {
			if (shortUrlStaticMapping.canMap(url) != null) {
				throw new BadRequestException(
						"The URL '%s' is statically mapped. Cannot create a dynamic ShortUrl-instance.".formatted(url));
			}
		}
	}

	@Override
	public ShortUrlDto updateShortUrl(@NonNull Long id, @NonNull ShortUrlDto shortUrl) {
		throw new NotFoundException("Not yet implemented -- any maybe never will be.");
	}

	@Override
	public void deleteShortUrl(@NonNull Long id) {
		shortUrlDao.deleteEntity(id);
	}

}
