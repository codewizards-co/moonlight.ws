package moonlight.ws.business.shorturl.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.inject.Inject;
import lombok.NonNull;
import moonlight.ws.business.shorturl.ShortUrlConfig;
import moonlight.ws.business.shorturl.ShortUrlStaticMapping;
import moonlight.ws.business.shorturl.UrlType;

public abstract class RegexShortUrlStaticMapping implements ShortUrlStaticMapping {

	private Pattern longUrlPattern;

	private Pattern shortUrlPattern;

	@Inject
	protected ShortUrlConfig shortUrlConfig;

	protected Pattern getLongUrlPattern() {
		if (longUrlPattern == null) {
			longUrlPattern = createLongUrlPattern();
		}
		return longUrlPattern;
	}

	protected Pattern getShortUrlPattern() {
		if (shortUrlPattern == null) {
			shortUrlPattern = createShortUrlPattern();
		}
		return shortUrlPattern;
	}

	@Override
	public UrlType canMap(@NonNull String url) {
		if (getLongUrlPattern().matcher(url).matches()) {
			return UrlType.LONG;
		}
		if (getShortUrlPattern().matcher(url).matches()) {
			return UrlType.SHORT;
		}
		return null;
	}

	@Override
	public String map(@NonNull String url) {
		Matcher matcher = getLongUrlPattern().matcher(url);
		if (matcher.matches()) {
			String code = getCodeFromLongUrlMatcher(matcher);
			return getShortUrl(code);
		}
		matcher = getShortUrlPattern().matcher(url);
		if (matcher.matches()) {
			String code = getCodeFromShortUrlMatcher(matcher);
			return getLongUrl(code);
		}
		throw new IllegalArgumentException("Cannot map: " + url);
	}

	protected abstract Pattern createLongUrlPattern();

	protected String getCodeFromLongUrlMatcher(@NonNull Matcher matcher) {
		if (matcher.groupCount() < 1) {
			return "";
		}
		return matcher.group(1);
	}

	protected abstract String getLongUrl(@NonNull String code);

	protected abstract Pattern createShortUrlPattern();

	protected String getCodeFromShortUrlMatcher(@NonNull Matcher matcher) {
		if (matcher.groupCount() < 1) {
			return "";
		}
		return matcher.group(1);
	}

	protected abstract String getShortUrl(@NonNull String code);
}
