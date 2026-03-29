package moonlight.ws.base;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

/**
 * Configuration for global settings.
 *
 * @see #DEFAULT_CONNECT_TIMEOUT
 * @see #DEFAULT_READ_TIMEOUT
 */
@ApplicationScoped
@Default
public class GlobalConfig extends AbstractConfig {

	public static final String SYS_PROP_DEFAULT_CONNECT_TIMEOUT = "sun.net.client.defaultConnectTimeout";
	public static final String SYS_PROP_DEFAULT_READ_TIMEOUT = "sun.net.client.defaultReadTimeout";

	/**
	 * Global connect-timeout in milliseconds translated to system-property
	 * {@value #SYS_PROP_DEFAULT_CONNECT_TIMEOUT}.
	 * <p>
	 * Optional. If not set, the system-property is not set.
	 * <p>
	 * This exists, because Liferay's client-lib does not provide an API to set its
	 * timeouts (specifically for Liferay). The only way to control the timeouts for
	 * Liferay-communication is to set the JVM's default-timeouts. This seems to
	 * work fine when doing it immediately during deployment of our WAR.
	 */
	public static final String DEFAULT_CONNECT_TIMEOUT = "DEFAULT_CONNECT_TIMEOUT";

	/**
	 * Global read-timeout in milliseconds translated to system-property
	 * {@value #SYS_PROP_DEFAULT_READ_TIMEOUT}.
	 * <p>
	 * Optional. If not set, the system-property is not set.
	 * <p>
	 * This exists, because Liferay's client-lib does not provide an API to set its
	 * timeouts (specifically for Liferay). The only way to control the timeouts for
	 * Liferay-communication is to set the JVM's default-timeouts. This seems to
	 * work fine when doing it immediately during deployment of our WAR.
	 */
	public static final String DEFAULT_READ_TIMEOUT = "DEFAULT_READ_TIMEOUT";

	/**
	 * Gets the value for {@value #DEFAULT_CONNECT_TIMEOUT} (milliseconds).
	 *
	 * @return the value for {@value #DEFAULT_CONNECT_TIMEOUT}. May be {@code null}
	 *         (equivalent to an empty string).
	 */
	public Long getDefaultConnectTimeout() {
		return getValueAsLong(DEFAULT_CONNECT_TIMEOUT, null, AbstractConfig::assertNotNegative);
	}

	/**
	 * Gets the value for {@value #DEFAULT_READ_TIMEOUT} (milliseconds).
	 *
	 * @return the value for {@value #DEFAULT_READ_TIMEOUT}. May be {@code null}
	 *         (equivalent to an empty string).
	 */
	public Long getDefaultReadTimeout() {
		return getValueAsLong(DEFAULT_READ_TIMEOUT, null, AbstractConfig::assertNotNegative);
	}
}
