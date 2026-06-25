package moonlight.ws.pac4j;

import static java.util.Objects.*;
import static moonlight.ws.api.RestConst.*;
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

	@Context
	private HttpServletRequest httpRequest;

	@Context
	private HttpServletResponse httpResponse;

	public Pac4jAuthenticationFilter() {
		log.debug("Pac4jAuthenticationFilter created.");
		CDI<Object> cdi = CDI.current();
		config = cdi.select(Config.class).get();
		notAuthorizedExceptionFactory = cdi.select(NotAuthorizedExceptionFactory.class).get();
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// Grab the raw token from the auth-header
		String _tokenString = null;
		String authHeader = httpRequest.getHeader(HEADER_AUTH);
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
			FrameworkParameters parameters = new JEEFrameworkParameters(httpRequest, httpResponse);

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

	@SuppressWarnings("deprecation")
	private void setAuthInfo(AuthInfo authInfo) {
		AuthInfoAccessor.setAuthInfo(authInfo);
	}
}
