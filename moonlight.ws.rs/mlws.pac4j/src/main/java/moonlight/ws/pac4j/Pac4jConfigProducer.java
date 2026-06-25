package moonlight.ws.pac4j;

import static moonlight.ws.api.RestConst.*;
import static moonlight.ws.base.util.UrlUtil.*;

import java.util.UUID;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jee.context.JEEContextFactory;
import org.pac4j.jee.context.session.JEESessionStoreFactory;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oidc.config.OidcConfiguration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.base.auth.OpenIdConfig;

@ApplicationScoped
@Slf4j
public class Pac4jConfigProducer {

	public static final String pac4jClientName = "moonlight_" + UUID.randomUUID().toString();

	@Inject
	private OpenIdConfig openIdConfig;

	@Inject
	private NotAuthorizedExceptionFactory notAuthorizedExceptionFactory;

	@Produces
	@ApplicationScoped
	public Config createConfig() {
		// 1. Pass config-values from our OpenIdConfig into the pac4j-OidcConfiguration
		OidcConfiguration oidcConfig = new OidcConfiguration();
		oidcConfig.setClientId(openIdConfig.getClientId());
		oidcConfig.setSecret(openIdConfig.getClientSecret());
		oidcConfig.setDiscoveryURI(concatUrlSegments(openIdConfig.getUrl(), "auth/realms", openIdConfig.getRealm(),
				".well-known/openid-configuration"));

		// 2. Create an authenticator that verifies incoming JWT access tokens
		JwtAuthenticator authenticator = new JwtAuthenticator();

		// 3. Define a HeaderClient to look for the Bearer token in headers
		HeaderClient headerClient = new HeaderClient(HEADER_AUTH, HEADER_AUTH_BEARER_PREFIX, authenticator);
		headerClient.setName(pac4jClientName); // client-name for supporting multiple clients (not supported by us)

		Config config = new Config(headerClient);
		config.setWebContextFactory(JEEContextFactory.INSTANCE);
		config.setSessionStoreFactory(JEESessionStoreFactory.INSTANCE);
		config.setProfileManagerFactory(ProfileManagerFactory.DEFAULT);

		// 4. NOT using JEEHttpActionAdapter.INSTANCE, but custom hardened
		// exception-handling
		HttpActionAdapter httpActionAdapter = new HttpActionAdapter() {
			@Override
			public Object adapt(HttpAction action, WebContext context) {
				if (action == null) { // should never happen!
					log.error("httpActionAdapter.adapt: action is null!");
					throw notAuthorizedExceptionFactory.createNotAuthorizedException();
				}

				int code = action.getCode();

				log.error("httpActionAdapter.adapt: action.code={}", code);

				if (action instanceof WithLocationAction withLocationAction) {
					log.error("httpActionAdapter.adapt: withLocationAction.location={}",
							withLocationAction.getLocation());
				} else if (action instanceof WithContentAction withContentAction) {
					log.error("httpActionAdapter.adapt: withContentAction.content={}", withContentAction.getContent());
				}

				// Handle 403 Forbidden specifically (Valid user, but explicitly blocked)
				if (code == Response.Status.FORBIDDEN.getStatusCode()) {
					throw new ForbiddenException();
				}

				// Default Fallback: Always return a generic 401 Unauthorized for everything
				// else. This includes missing tokens, invalid signatures, expired tokens, or
				// bad headers. We do not reveal any info to the client who may be an attacker!
				throw notAuthorizedExceptionFactory.createNotAuthorizedException();
			}
		};

		config.setHttpActionAdapter(httpActionAdapter);
		return config;
	}
}
