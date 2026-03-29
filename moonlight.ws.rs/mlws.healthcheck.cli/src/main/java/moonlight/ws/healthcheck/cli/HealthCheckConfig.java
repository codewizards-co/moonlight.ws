package moonlight.ws.healthcheck.cli;

import moonlight.ws.base.AbstractConfig;

public class HealthCheckConfig extends AbstractConfig {

	/**
	 * Health-check-URL.
	 * <p>
	 * Example: {@code https://dragonkingchocolate.com/moonlight.ws.rs/health}
	 */
	public static final String URL = "HEALTHCHECK_URL";

	/**
	 * Connect-timeout in milliseconds.
	 */
	public static final String CONNECT_TIMEOUT = "HEALTHCHECK_CONNECT_TIMEOUT";

	/**
	 * Read-timeout in milliseconds.
	 */
	public static final String READ_TIMEOUT = "HEALTHCHECK_READ_TIMEOUT";

	/**
	 * Time in milliseconds before retrying after an error.
	 */
	public static final String HEALTHCHECK_ERROR_WAIT_BEFORE_RETRY = "ERROR_WAIT_BEFORE_RETRY";

	/**
	 * Retries after the first error, before sending an e-mail. 0 means to never
	 * retry (send an e-mail immediately after the first GET-request on the
	 * {@link #URL} failed). 1 means to retry once, i.e. to perform a maximum of 2
	 * GET-requests in total.
	 */
	public static final String HEALTHCHECK_ERROR_RETRY_COUNT = "ERROR_RETRY_COUNT";

	/**
	 * E-mail-address to be notified on error and also on recovery.
	 */
	public static final String HEALTHCHECK_EMAIL_TO = "EMAIL_TO";

	/**
	 * Gets the value for {@value #URL}.
	 *
	 * @return the value for {@value #URL}. Never {@code null}.
	 */
	public String getUrl() {
		return getValueOrFail(URL);
	}

	/**
	 * Gets the value for {@value #CONNECT_TIMEOUT} (milliseconds).
	 *
	 * @return the value for {@value #CONNECT_TIMEOUT}. May be {@code null}
	 *         (equivalent to an empty string).
	 */
	public Long getConnectTimeout() {
		return getValueAsLong(CONNECT_TIMEOUT, null, AbstractConfig::assertNotNegative);
	}

	/**
	 * Gets the value for {@value #READ_TIMEOUT} (milliseconds).
	 *
	 * @return the value for {@value #READ_TIMEOUT}. May be {@code null} (equivalent
	 *         to an empty string).
	 */
	public Long getReadTimeout() {
		return getValueAsLong(READ_TIMEOUT, null, AbstractConfig::assertNotNegative);
	}
}
