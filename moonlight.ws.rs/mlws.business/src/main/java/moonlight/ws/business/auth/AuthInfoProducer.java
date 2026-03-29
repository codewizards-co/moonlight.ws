package moonlight.ws.business.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.base.internal.AuthInfoAccessor;

@RequestScoped
public class AuthInfoProducer {

	private static final AuthInfo NOT_AUTHENTICATED = new AuthInfo(false, null, null);

	@Produces
	@RequestScoped
	public AuthInfo getAuthInfo() {
		@SuppressWarnings("deprecation")
		AuthInfo authInfo = AuthInfoAccessor.getAuthInfo();
		return authInfo == null ? NOT_AUTHENTICATED : authInfo;
	}
}
