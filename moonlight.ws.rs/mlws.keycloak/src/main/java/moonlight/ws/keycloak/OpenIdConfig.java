package moonlight.ws.keycloak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import moonlight.ws.base.AbstractConfig;

/**
 * Configuration for the OIDC (OpenID Connect) integration.
 *
 * @see #URL
 * @see #REALM
 * @see #CLIENT_ID
 * @see #CLIENT_SECRET
 */
@ApplicationScoped
@Default
public class OpenIdConfig extends AbstractConfig {

	/**
	 * The base-URL of the Keycloak-server.
	 * <p>
	 * Required. This must be specified for openID-authentication to work!
	 * <p>
	 * For example: {@code https://codewizards.co:4443}
	 * <p>
	 * It is <b>strongly recommended to use HTTPS!</b> You should <i>never</i> use a
	 * plain-text HTTP-connection productively!
	 */
	public static final String URL = "OPENID_URL";

	/**
	 * The realm used for authentication.
	 * <p>
	 * Required. This must be specified for openID-authentication to work!
	 */
	public static final String REALM = "OPENID_REALM";

	/**
	 * The application's client-ID.
	 * <p>
	 * Required. This must be specified for openID-authentication to work!
	 */
	public static final String CLIENT_ID = "OPENID_CLIENT_ID";

	/**
	 * The application's client-secret.
	 * <p>
	 * Optional. If the client in keycloak is configured to require
	 * client-authentication, this must be provided.
	 */
	public static final String CLIENT_SECRET = "OPENID_CLIENT_SECRET";

	/**
	 * User-name for health-check of OpenID-server.
	 * <p>
	 * Optional. Used for health-check, only. If not specified or empty, the
	 * health-check is disabled.
	 * <p>
	 * This should be a user without privileges, known to the OpenID-server, but
	 * having no (relevant) roles assigned.
	 */
	public static final String HEALTH_CHECK_USER = "OPENID_HEALTH_CHECK_USER";

	/**
	 * Password for health-check of OpenID-server.
	 * <p>
	 * Optional. Used for health-check, only. If not specified or empty, the
	 * health-check is disabled.
	 */
	public static final String HEALTH_CHECK_PASSWORD = "OPENID_HEALTH_CHECK_PASSWORD";

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
	 * Gets the value for {@value #REALM}, throwing an exception, if it is not
	 * configured.
	 *
	 * @return the value for {@value #REALM}. Never {@code null}.
	 */
	public String getRealm() {
		return getValueOrFail(REALM);
	}

	/**
	 * Gets the value for {@value #CLIENT_ID}, taking the default-value-strategy
	 * into account.
	 *
	 * @return the value for {@value #CLIENT_ID}. Never {@code null}.
	 */
	public String getClientId() {
		return getValueOrFail(CLIENT_ID);
	}

	public String getClientSecret() {
		return getValue(CLIENT_SECRET, null);
	}

	public String getHealthCheckUser() {
		return getValue(HEALTH_CHECK_USER, null);
	}

	public String getHealthCheckPassword() {
		return getValue(HEALTH_CHECK_PASSWORD, null);
	}
}
