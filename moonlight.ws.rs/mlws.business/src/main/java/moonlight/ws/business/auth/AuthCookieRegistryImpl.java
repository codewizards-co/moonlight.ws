package moonlight.ws.business.auth;

import static java.util.Objects.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.base.auth.AuthCookie;
import moonlight.ws.base.auth.AuthCookieRegistry;

@ApplicationScoped
@Slf4j
public class AuthCookieRegistryImpl implements AuthCookieRegistry {
	private static final long EVICT_PERIOD_MS = 15L * 60_000L;

	private final Map<String, AuthCookie> bearerTokenSha256AuthCookie = new HashMap<>();

	private Timer timer;
	private TimerTask timerTask;

	@Override
	public synchronized AuthCookie getAuthCookieByBearerTokenSha256(@NonNull String bearerTokenSha256) {
		return bearerTokenSha256AuthCookie.get(bearerTokenSha256);
	}

	@Override
	public synchronized AuthCookie getOrCreateAuthCookie(@NonNull AuthInfo authInfo) {
		final AuthCookie newAuthCookie = new AuthCookie(authInfo); // creation is faster than calculating the sha256
																	// => do it only once
		final AuthCookie oldAuthCookie = bearerTokenSha256AuthCookie.get(newAuthCookie.bearerTokenSha256);
		if (oldAuthCookie == null || oldAuthCookie.isNearlyExpired()) {
			putAuthCookie(newAuthCookie);
			return newAuthCookie;
		}
		return oldAuthCookie;
	}

	protected synchronized void putAuthCookie(@NonNull AuthCookie authCookie) {
		String bearerTokenSha256 = requireNonNull(authCookie.bearerTokenSha256, "authCookie.bearerTokenSha256");
		bearerTokenSha256AuthCookie.put(bearerTokenSha256, authCookie);
		initTimerTask();
	}

	protected synchronized void evictExpiredAuthCookies() {
		for (Iterator<Map.Entry<String, AuthCookie>> it = bearerTokenSha256AuthCookie.entrySet().iterator(); it
				.hasNext();) {
			Map.Entry<String, AuthCookie> me = it.next();
			if (me.getValue().isExpired()) {
				it.remove();
			}
		}
	}

	private synchronized void initTimerTask() {
		if (timer == null) {
			timer = new Timer("AuthCookieRegistryImpl.timer", true);
		}
		if (timerTask == null) {
			timerTask = new TimerTask() {

				@Override
				public void run() {
					evictExpiredAuthCookies();
				}
			};
			timer.schedule(timerTask, EVICT_PERIOD_MS, EVICT_PERIOD_MS);
		}
	}
}
