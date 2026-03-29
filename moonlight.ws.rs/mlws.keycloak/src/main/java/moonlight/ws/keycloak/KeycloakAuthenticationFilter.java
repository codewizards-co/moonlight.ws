package moonlight.ws.keycloak;

import static java.util.Objects.*;
import static moonlight.ws.api.RestConst.*;
import static moonlight.ws.base.util.StringUtil.*;

import java.io.IOException;
import java.util.List;

import org.keycloak.adapters.BearerTokenRequestAuthenticator;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.representations.AccessToken;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.api.RequiresAuthentication;
import moonlight.ws.base.auth.AuthCookie;
import moonlight.ws.base.auth.AuthCookieRegistry;
import moonlight.ws.base.internal.AuthInfoAccessor;

/**
 * Request-filter-implementation bound to {@link RequiresAuthentication},
 * securing RESTful services in a fine-grained way (by interface/class or
 * method).
 */
@Provider
@RequiresAuthentication
@Slf4j
public class KeycloakAuthenticationFilter implements ContainerRequestFilter, ContainerResponseFilter {

	/**
	 * Authentication-type indicating an OpenID-based authentication using the
	 * "Bearer" header following the {@value #HEADER_AUTH} header key.
	 * <p>
	 * If this authentication-mode is used (i.e. the client sends this in the
	 * {@value #HEADER_AUTH} header), then the {@link KeycloakDeploymentFactory}
	 * must be able to provide a
	 */
	private static final String AUTH_TYPE_BEARER = "Bearer";

	private static final List<String> SUPPORTED_AUTH_TYPES = List.of(AUTH_TYPE_BEARER);

	@Context
	private HttpServletRequest servletRequest;
	@Context
	private HttpServletResponse servletResponse;
	@Inject
	private IKeycloakDeploymentFactory keycloakDeploymentFactory;
	@Inject
	private ServletContext servletContext;
	@Inject
	private OpenIdConfig openIdConfig;
	@Inject
	private AuthCookieRegistry authCookieRegistry;

	@Override
	public void filter(@NonNull ContainerRequestContext requestContext) throws IOException {
		// BEFORE request
		authenticate(requestContext);
	}

	@Override
	public void filter(@NonNull ContainerRequestContext requestContext,
			@NonNull ContainerResponseContext responseContext) throws IOException {
		// AFTER request

		// Clearing of AuthInfo is done in:
		//
		// moonlight.logistics.webservice.MoonlightWsAuthCookieFilter
		//
		// This was necessary for the AuthAndRedirectRest-service, which does not use
		// @RequiresAuthentication and thus does not trigger this
		// KeycloakAuthenticationFilter. But it does perform a login (= binding AuthInfo
		// to the current thread) and thus needs logging out -- and cookie-handling.
	}

	private void authenticate(@NonNull ContainerRequestContext requestContext) throws IOException {
		requireNonNull(servletRequest, "servletRequest");
		requireNonNull(servletResponse, "servletResponse");
		if (authenticateOpenId(requestContext)) {
			return;
		}
		if (authenticateAuthCookie()) {
			return;
		}
		throw createNotAuthorizedException();
	}

	private boolean authenticateOpenId(@NonNull ContainerRequestContext requestContext) {
		String authHeaderValue = requestContext.getHeaderString(HEADER_AUTH);
		if (authHeaderValue != null) {
			int spaceIndex = authHeaderValue.indexOf(' ');
			if (spaceIndex >= 0) {
				final String authType = authHeaderValue.substring(0, spaceIndex);
				if (AUTH_TYPE_BEARER.equalsIgnoreCase(authType)) {
					_authenticateOpenId();
					return true;
				}
				log.error("Auth-type '{}' not supported! Supported auth-types are: {}", authType,
						SUPPORTED_AUTH_TYPES);
			}
		}
		return false;
	}

	private boolean authenticateAuthCookie() {
		requireNonNull(servletRequest, "servletRequest");
		Cookie cookie = getAuthCookieFromServletRequest();
		if (cookie != null) {
			String authCookieToken = cookie.getValue();
			if (!isEmpty(authCookieToken)) {
				AuthCookie authCookie = authCookieRegistry.getAuthCookieByBearerTokenSha256(authCookieToken);
				if (authCookie == null || authCookie.isExpired()) {
					log.error("AUTH_TOKEN '{}' unknown or expired!", authCookieToken);
				} else {
					setAuthInfo(authCookie.authInfo);
					log.info("Authenticated user via auth-cookie: {}, {}", authCookie.authInfo.getUsername(),
							authCookieToken);
					return true;
				}
			}
		}
		return false;
	}

	private Cookie getAuthCookieFromServletRequest() {
		Cookie[] cookies = servletRequest.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (COOKIE_AUTH_TOKEN.equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		return null;
	}

	private NotAuthorizedException createNotAuthorizedException() {
		return new NotAuthorizedException(AUTH_TYPE_BEARER + " realm=\"" + openIdConfig.getRealm() + "\"");
	}

	/**
	 * Authenticate the current user via OpenID, if the {@value #AUTH_TYPE_BEARER}
	 * header was specified.
	 */
	private void _authenticateOpenId() {
		requireNonNull(servletContext, "servletContext");
		BearerTokenRequestAuthenticator authenticator = (BearerTokenRequestAuthenticator) servletContext
				.getAttribute(BearerTokenRequestAuthenticator.class.getName());
		if (authenticator == null) {
			KeycloakDeployment keycloakDeployment;
			try {
				keycloakDeployment = keycloakDeploymentFactory.getKeycloakDeployment();
			} catch (Exception x) {
				log.error("KeycloakDeployment is not properly configured: " + x, x);
				throw createNotAuthorizedException();
			}
			authenticator = new BearerTokenRequestAuthenticator(keycloakDeployment);
			servletContext.setAttribute(BearerTokenRequestAuthenticator.class.getName(), authenticator);
		}
		HttpFacade httpFacade = new HttpFacadeImpl(servletResponse, servletRequest);
		AuthOutcome authOutcome;
		synchronized (authenticator) {
			authOutcome = authenticator.authenticate(httpFacade);
			if (AuthOutcome.AUTHENTICATED != authOutcome) {
				log.error("Unexpected authOutcome: {}", authOutcome);
				throw createNotAuthorizedException();
			}
			AccessToken token = requireNonNull(authenticator.getToken(), "authenticator.token");
			String username = token.getPreferredUsername();
			AuthInfo authInfo = new AuthInfo(true, requireNonNull(username, "username"),
					requireNonNull(authenticator.getTokenString(), "authenticator.tokenString"));
			setAuthInfo(authInfo);
			log.info("Authenticated user via OpenID: {}", username);
		}
	}

	@SuppressWarnings("deprecation")
	private void setAuthInfo(AuthInfo authInfo) {
		AuthInfoAccessor.setAuthInfo(authInfo);
	}
};