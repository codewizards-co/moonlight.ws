package moonlight.ws.base.auth;

import static java.util.Objects.*;
import static moonlight.ws.base.util.HashUtil.*;

import lombok.NonNull;
import moonlight.ws.api.AuthInfo;

public class AuthCookie {
	public static final long EXPIRY_MS = 3600L * 1000L;
	public static final long NEARLY_EXPIRY_MS = EXPIRY_MS * 2 / 3;

	public final String bearerTokenSha256;
	public final AuthInfo authInfo;
	public final long createdTimestamp;

	public AuthCookie(@NonNull AuthInfo authInfo) {
		this.bearerTokenSha256 = calculateBearerTokenSha256(authInfo);
		this.authInfo = authInfo;
		createdTimestamp = System.currentTimeMillis();
	}

	public boolean isExpired() {
		return System.currentTimeMillis() - createdTimestamp > EXPIRY_MS;
	}

	public boolean isNearlyExpired() {
		return System.currentTimeMillis() - createdTimestamp > NEARLY_EXPIRY_MS;
	}

	public static final String calculateBearerTokenSha256(@NonNull AuthInfo authInfo) {
		if (!authInfo.isAuthenticated()) {
			throw new IllegalArgumentException("authInfo.authenticated == false");
		}
		String bearerToken = requireNonNull(authInfo.getBearerToken(), "authInfo.bearerToken");
		String bearerTokenSha256 = sha256(bearerToken);
		return bearerTokenSha256;
	}
}
