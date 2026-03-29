package moonlight.ws.business.shorturl.impl;

import static moonlight.ws.base.util.UrlUtil.*;

import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import moonlight.ws.business.shorturl.ShortUrlConfig;
import moonlight.ws.business.shorturl.ShortUrlStaticMapping;
import moonlight.ws.liferay.LiferayConfig;

/**
 * Static mapping of short-URLs for Liferay-products.
 * <p>
 * <ul>
 * <li>Short URL:
 * <code>{{{@link ShortUrlConfig#URL SHORTURL_URL}}}/p/{productCode}</code></li>
 * <li>Real URL:
 * <code>{{{@link LiferayConfig#URL LIFERAY_URL}}}/web/dkc/p/{productCode}</code></li>
 * </ul>
 */
@ApplicationScoped
public class LiferayProductShortUrlStaticMapping extends RegexShortUrlStaticMapping implements ShortUrlStaticMapping {

	@Inject
	protected ShortUrlConfig shortUrlConfig;

	@Inject
	protected LiferayConfig liferayConfig;

	@Override
	protected Pattern createLongUrlPattern() {
		String liferayUrl = liferayConfig.getUrl();
		while (liferayUrl.endsWith("/")) {
			liferayUrl = liferayUrl.substring(0, liferayUrl.length() - 1);
		}
		return Pattern.compile(Pattern.quote(liferayUrl) + "(?:\\/[a-z]{2})?\\/web\\/dkc\\/p\\/(.+)");
	}

	@Override
	protected String getLongUrl(@NonNull String code) {
		return concatUrlSegments(liferayConfig.getUrl(), "web/dkc/p", code);
	}

	@Override
	protected Pattern createShortUrlPattern() {
		return Pattern.compile(Pattern.quote(concatUrlSegments(shortUrlConfig.getUrl(), "p/")) + "(.+)");
	}

	@Override
	protected String getShortUrl(@NonNull String code) {
		return concatUrlSegments(shortUrlConfig.getUrl(), "p", code);
	}
}
