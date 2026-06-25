package moonlight.ws.pac4j;

import static java.util.Objects.*;
import static moonlight.ws.api.RestConst.*;
import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.pac4j.Pac4jConfigProducer.*;

import java.io.IOException;

import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.FrameworkParameters;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEFrameworkParameters;
import org.pac4j.oidc.profile.OidcProfileDefinition;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.api.RequiresAuthentication;
import moonlight.ws.base.auth.AuthCookie;
import moonlight.ws.base.auth.AuthCookieRegistry;
import moonlight.ws.base.internal.AuthInfoAccessor;
import moonlight.ws.base.util.StringUtil;

@Provider
@RequiresAuthentication
@Priority(Priorities.AUTHENTICATION) // runs early in the chain
@Slf4j
public class Pac4jAuthenticationFilter implements ContainerRequestFilter {

	// DefaultSecurityLogic handles the authentication flow internally
	private final SecurityLogic securityLogic = new DefaultSecurityLogic();

	private final Config config;

	private final NotAuthorizedExceptionFactory notAuthorizedExceptionFactory;

	private final AuthCookieRegistry authCookieRegistry;

	@Context
	private HttpServletRequest servletRequest;

	@Context
	private HttpServletResponse servletResponse;

	public Pac4jAuthenticationFilter() {
		log.debug("Pac4jAuthenticationFilter created.");
		CDI<Object> cdi = CDI.current();
		config = cdi.select(Config.class).get();
		notAuthorizedExceptionFactory = cdi.select(NotAuthorizedExceptionFactory.class).get();
		authCookieRegistry = cdi.select(AuthCookieRegistry.class).get();
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		try {
			authenticateOpenId();
		} catch (Exception openIdAuthFailed) {
			if (authenticateAuthCookie()) {
				return;
			}
			throw openIdAuthFailed;
		}
	}

	private void authenticateOpenId() {
		// Grab the raw token from the auth-header
		String _tokenString = null;
		String authHeader = servletRequest.getHeader(HEADER_AUTH);
		if (authHeader != null && authHeader.startsWith(HEADER_AUTH_BEARER_PREFIX)) {
			_tokenString = authHeader.substring(HEADER_AUTH_BEARER_PREFIX.length()).trim(); // removes "Bearer "
		}
		final String tokenString = _tokenString;
		if (StringUtil.isEmpty(tokenString)) {
			log.error("filter: tokenString is empty or missing!");
			throw notAuthorizedExceptionFactory.createNotAuthorizedException();
		}

		try {
			// Wrap JEE request/response into the pac4j parameters abstraction
			FrameworkParameters parameters = new JEEFrameworkParameters(servletRequest, servletResponse);

			SecurityGrantedAccessAdapter securityGrantedAccessAdapter = (webContext, sessionStore, profiles) -> {
				// This block executes ONLY if authentication succeeds
				UserProfile userProfile = profiles.iterator().next();
				String username = (String) userProfile.getAttribute(OidcProfileDefinition.PREFERRED_USERNAME);

				requireNonNull(username, "userProfile.getAttribute(OidcProfileDefinition.PREFERRED_USERNAME)");
				setAuthInfo(new AuthInfo(true, username, tokenString));
				log.info("Authenticated user via OpenID: {}", username);
				return null;
			};

			final String matchers = null; // not needed

			securityLogic.perform(config, securityGrantedAccessAdapter, pac4jClientName,
					DefaultAuthorizers.IS_AUTHENTICATED, matchers, parameters);
		} catch (WebApplicationException x) {
			throw x;
		} catch (Exception e) {
			log.error("filter: " + e, e);
//			requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
			throw notAuthorizedExceptionFactory.createNotAuthorizedException();
		}
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

	@SuppressWarnings("deprecation")
	private void setAuthInfo(AuthInfo authInfo) {
		AuthInfoAccessor.setAuthInfo(authInfo);
	}
}
