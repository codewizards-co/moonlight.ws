package moonlight.ws.business.shorturl;

import lombok.NonNull;

public interface ShortUrlStaticMapping {

	default int getOrderHint() {
		return 1000;
	};

	/**
	 * Checks, if the given {@code url} can be mapped, and if so, what type it is.
	 *
	 * @param url the URL to be checked. Must not be {@code null}.
	 * @return {@code null}, if the given {@code url} cannot be mapped. Otherwise
	 *         the type of the given {@code url}.
	 */
	UrlType canMap(@NonNull String url);

	String map(@NonNull String url);
}
