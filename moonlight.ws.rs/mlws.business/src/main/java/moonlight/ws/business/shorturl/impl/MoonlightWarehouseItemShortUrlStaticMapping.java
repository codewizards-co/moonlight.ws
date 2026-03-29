package moonlight.ws.business.shorturl.impl;

import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.base.util.UrlUtil.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import moonlight.ws.base.MoonlightUiConfig;
import moonlight.ws.business.shorturl.ShortUrlConfig;
import moonlight.ws.business.shorturl.ShortUrlStaticMapping;
import moonlight.ws.business.shorturl.UrlType;

/**
 * Static mapping of short-URLs for Liferay-warehouse-items managed in
 * Moonlight-WS-UI.
 * <p>
 * <ul>
 * <li>Short URL:
 * <code>{{{@link ShortUrlConfig#URL SHORTURL_URL}}}/warehouse-item/{id}</code></li>
 * <li>Real URL:
 * <code>{{{@link MoonlightUiConfig#URL MOONLIGHTUI_URL}}}/#/warehouse-item?id={id}</code></li>
 * </ul>
 */
@ApplicationScoped
public class MoonlightWarehouseItemShortUrlStaticMapping extends RegexShortUrlStaticMapping
		implements ShortUrlStaticMapping {

	private Pattern longUrlPattern2;

	@Inject
	protected ShortUrlConfig shortUrlConfig;

	@Inject
	protected MoonlightUiConfig moonlightUiConfig;

	@Override
	protected Pattern createLongUrlPattern() {
		return Pattern.compile(
				Pattern.quote(concatUrlSegments(moonlightUiConfig.getUrl(), "#/warehouse-item?") + "id=") + "(.+)");
	}

	@Override
	protected String getLongUrl(@NonNull String code) {
		return concatUrlSegments(moonlightUiConfig.getUrl(), "#/warehouse-item?", "id=" + code);
	}

	@Override
	protected Pattern createShortUrlPattern() {
		return Pattern.compile(Pattern.quote(concatUrlSegments(shortUrlConfig.getUrl(), "warehouse-item/")) + "(.+)");
	}

	@Override
	protected String getShortUrl(@NonNull String code) {
		return concatUrlSegments(shortUrlConfig.getUrl(), "warehouse-item", code);
	}

	protected Pattern getLongUrlPattern2() {
		if (longUrlPattern2 == null) {
			URL moonlightUiUrl;
			try {
				moonlightUiUrl = new URL(moonlightUiConfig.getUrl());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			String path = moonlightUiUrl.getPath();
			if (isEmpty(path) || path.equals("/")) {
				path = null;
			}
			longUrlPattern2 = Pattern.compile(
					Pattern.quote(concatUrlSegments(shortUrlConfig.getUrl(), path, "#/warehouse-item?") + "id=") //
							+ "(.+)");
		}
		return longUrlPattern2;
	}

	@Override
	public UrlType canMap(@NonNull String url) {
		if (getLongUrlPattern2().matcher(url).matches()) {
			return UrlType.LONG;
		}
		return super.canMap(url);
	}

	@Override
	public String map(@NonNull String url) {
		Matcher matcher = getLongUrlPattern2().matcher(url);
		if (matcher.matches()) {
			String code = getCodeFromLongUrlMatcher(matcher);
			return getShortUrl(code);
		}
		return super.map(url);
	}
}
