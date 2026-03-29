package moonlight.ws.base.internal;

import org.slf4j.MDC;

import lombok.experimental.UtilityClass;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.api.RestConst;

/**
 * Accessor for the {@link AuthInfo}.
 *
 * @deprecated Do not directly access this class! It is an <b>internal
 *             utility-class</b> and <b>not API!</b> Instead, you should inject
 *             {@link AuthInfo}.
 */
@Deprecated
@UtilityClass
public class AuthInfoAccessor {

	private static final ThreadLocal<AuthInfo> authInfoThreadLocal = new ThreadLocal<AuthInfo>();

	public static AuthInfo getAuthInfo() {
		return authInfoThreadLocal.get();
	}

	public static void setAuthInfo(AuthInfo authInfo) {
		if (authInfo == null) {
			authInfoThreadLocal.remove();
			MDC.remove(RestConst.MDC_USERNAME);
		} else {
			authInfoThreadLocal.set(authInfo);
			MDC.put(RestConst.MDC_USERNAME, authInfo.getUsername());
		}
	}
}
