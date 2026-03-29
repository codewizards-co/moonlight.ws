package moonlight.ws.persistence.shorturl;

import java.util.HashMap;

import jakarta.enterprise.context.RequestScoped;
import lombok.NonNull;
import moonlight.ws.api.shorturl.ShortUrlFilter;
import moonlight.ws.persistence.AbstractDao;
import moonlight.ws.persistence.SearchResult;

@RequestScoped
public class ShortUrlDao extends AbstractDao<ShortUrlEntity> {

	public ShortUrlEntity getShortUrlByCode(@NonNull String code) {
		var params = new HashMap<String, Object>();
		var jpqlCriteria = " and e.code = :code";
		params.put("code", code);
		SearchResult<ShortUrlEntity> searchResult = searchEntities(jpqlCriteria, params, null);
		if (searchResult.getTotalSize() == 1) {
			return searchResult.getEntities().get(0);
		} else if (searchResult.getTotalSize() == 0) {
			return null;
		} else {
			throw new IllegalStateException(
					"Found %d rows for code='%s'! Expected 0 or 1 rows.".formatted(searchResult.getTotalSize(), code));
		}
	}

	public ShortUrlEntity getShortUrlByLongUrl(@NonNull String longUrl) {
		var params = new HashMap<String, Object>();
		var jpqlCriteria = " and e.longUrl = :longUrl";
		params.put("longUrl", longUrl);
		SearchResult<ShortUrlEntity> searchResult = searchEntities(jpqlCriteria, params, null);
		if (searchResult.getTotalSize() == 1) {
			return searchResult.getEntities().get(0);
		} else if (searchResult.getTotalSize() == 0) {
			return null;
		} else {
			throw new IllegalStateException("Found %d rows for longUrl='%s'! Expected 0 or 1 rows."
					.formatted(searchResult.getTotalSize(), longUrl));
		}
	}

	public ShortUrlEntity getShortUrlByShortUrl(@NonNull String shortUrl) {
		var params = new HashMap<String, Object>();
		var jpqlCriteria = " and e.shortUrl = :shortUrl";
		params.put("shortUrl", shortUrl);
		SearchResult<ShortUrlEntity> searchResult = searchEntities(jpqlCriteria, params, null);
		if (searchResult.getTotalSize() == 1) {
			return searchResult.getEntities().get(0);
		} else if (searchResult.getTotalSize() == 0) {
			return null;
		} else {
			throw new IllegalStateException("Found %d rows for shortUrl='%s'! Expected 0 or 1 rows."
					.formatted(searchResult.getTotalSize(), shortUrl));
		}
	}

	public SearchResult<ShortUrlEntity> searchEntities(ShortUrlFilter filter) {
		var params = new HashMap<String, Object>();
		var jpqlCriteria = "";
		if (filter != null) {
			if (filter.getFilterCode() != null) {
				jpqlCriteria += " and lower(e.code) like lower(:code)";
				params.put("code", prepareLikeCriterion(filter.getFilterCode()));
			}
			if (filter.getFilterLongUrl() != null) {
				jpqlCriteria += " and lower(e.longUrl) like lower(:longUrl)";
				params.put("longUrl", prepareLikeCriterion(filter.getFilterLongUrl()));
			}
			if (filter.getFilterShortUrl() != null) {
				jpqlCriteria += " and lower(e.shortUrl) like lower(:shortUrl)";
				params.put("shortUrl", prepareLikeCriterion(filter.getFilterShortUrl()));
			}

			if (filter.getFilterCreatedFromIncl() != null) {
				jpqlCriteria += " and e.created >= :createdFromIncl";
				params.put("createdFromIncl", filter.getFilterCreatedFromIncl().getInstant());
			}
			if (filter.getFilterCreatedToExcl() != null) {
				jpqlCriteria += " and e.created < :createdToExcl";
				params.put("createdToExcl", filter.getFilterCreatedToExcl().getInstant());
			}

			if (filter.getFilterChangedFromIncl() != null) {
				jpqlCriteria += " and e.changed >= :changedFromIncl";
				params.put("changedFromIncl", filter.getFilterChangedFromIncl().getInstant());
			}
			if (filter.getFilterChangedToExcl() != null) {
				jpqlCriteria += " and e.changed < :changedToExcl";
				params.put("changedToExcl", filter.getFilterChangedToExcl().getInstant());
			}
		}
		return searchEntities(jpqlCriteria, params, filter);
	}
}
