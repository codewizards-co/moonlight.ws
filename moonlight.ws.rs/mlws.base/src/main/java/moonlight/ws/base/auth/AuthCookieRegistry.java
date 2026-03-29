package moonlight.ws.base.auth;

import lombok.NonNull;
import moonlight.ws.api.AuthInfo;

public interface AuthCookieRegistry {

	AuthCookie getAuthCookieByBearerTokenSha256(@NonNull String bearerTokenSha256);

	AuthCookie getOrCreateAuthCookie(@NonNull AuthInfo authInfo);

}
