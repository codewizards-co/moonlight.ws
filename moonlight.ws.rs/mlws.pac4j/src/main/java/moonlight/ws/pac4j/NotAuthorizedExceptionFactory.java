package moonlight.ws.pac4j;

import static moonlight.ws.api.RestConst.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import moonlight.ws.base.auth.OpenIdConfig;

@ApplicationScoped
public class NotAuthorizedExceptionFactory {

	@Inject
	private OpenIdConfig openIdConfig;

	public NotAuthorizedException createNotAuthorizedException() {
		return new NotAuthorizedException(HEADER_AUTH_BEARER_PREFIX + "realm=\"" + openIdConfig.getRealm() + "\"");
	}
}
