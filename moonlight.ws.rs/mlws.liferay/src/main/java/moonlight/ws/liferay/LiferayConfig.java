package moonlight.ws.liferay;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import moonlight.ws.base.AbstractConfig;

/**
 * Configuration for the Liferay integration.
 *
 * @see #URL
 */
@ApplicationScoped
@Default
public class LiferayConfig extends AbstractConfig {

	/**
	 * The base-URL of the Liferay-server.
	 * <p>
	 * Required. This must be specified for communication with Liferay!
	 * <p>
	 * For example: {@code https://dragonkingchocolate.com:1443/liferay}
	 * <p>
	 * It is <b>strongly recommended to use HTTPS!</b> You should <i>never</i> use a
	 * plain-text HTTP-connection productively!
	 */
	public static final String URL = "LIFERAY_URL";

	/**
	 * The service-user for logging in to Liferay.
	 * <p>
	 * Optional. If not specified, the bearer-bearerTokenSha256 is directly forwarded. If a user
	 * is specified, the corresponding {@link #PASSWORD} must be specified, too.
	 */
	public static final String USER = "LIFERAY_USER";

	/**
	 * The password of the service-user for logging in to Liferay.
	 * <p>
	 * Optional. If not specified, the bearer-bearerTokenSha256 is directly forwarded. This
	 * password must be specified together with the {@link #USER}.
	 */
	public static final String PASSWORD = "LIFERAY_PASSWORD";

	public static final String CACHE_EXPIRY_MS = "LIFERAY_CACHE_EXPIRY_MS";

	/**
	 * Gets the value for {@value #URL}, throwing an exception, if it is not
	 * configured.
	 *
	 * @return the value for {@value #URL}. Never {@code null}.
	 */
	public String getUrl() {
		return getValueOrFail(URL);
	}

	/**
	 * Gets the value for {@value #USER}, returning {@code null}, if it is not
	 * configured.
	 *
	 * @return the value for {@value #USER}. May be {@code null}.
	 */
	public String getUser() {
		return getValue(USER, null);
	}

	/**
	 * Gets the value for {@value #PASSWORD}, returning {@code null}, if it is not
	 * configured.
	 *
	 * @return the value for {@value #PASSWORD}. May be {@code null}.
	 */
	public String getPassword() {
		return getValue(PASSWORD, null);
	}

	public long getCacheExpiryMs() {
		return getValueAsLong(CACHE_EXPIRY_MS, 60_000L, AbstractConfig::assertNotNegative);
	}
}
