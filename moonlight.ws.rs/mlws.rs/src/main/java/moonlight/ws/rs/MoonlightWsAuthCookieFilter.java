package moonlight.ws.rs;

import static moonlight.ws.api.RestConst.COOKIE_AUTH_TOKEN;

import java.io.IOException;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.base.auth.AuthCookie;
import moonlight.ws.base.auth.AuthCookieRegistry;
import moonlight.ws.base.internal.AuthInfoAccessor;

@Slf4j
public class MoonlightWsAuthCookieFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(request, response);

		// AFTER request
		AuthInfo authInfo = getAuthInfo();
		if (authInfo != null && authInfo.isAuthenticated()) {
			log.info("authInfo.username: {}", authInfo.getUsername());
			AuthCookieRegistry authCookieRegistry = CDI.current().select(AuthCookieRegistry.class).get();
			AuthCookie authCookie = authCookieRegistry.getOrCreateAuthCookie(authInfo);
			log.info("bearerTokenSha256: {}", authCookie.bearerTokenSha256);
			Cookie clientAuthCookie = getAuthCookieFromServletRequest(request);
			String clientAuthCookieValue = clientAuthCookie != null ? clientAuthCookie.getValue() : null;
			log.info("clientAuthCookieValue: {}", clientAuthCookieValue);
			if (clientAuthCookieValue == null || !clientAuthCookieValue.equals(authCookie.bearerTokenSha256)) {
//				Cookie cookie = new Cookie(COOKIE_AUTH_TOKEN, authCookie.bearerTokenSha256);
//				cookie.setMaxAge((int) (AuthCookie.EXPIRY_MS / 1000));
//				cookie.setPath("/");
//				// SameSite=None is needed for showing images from
//				// https://dragonkingchocolate.com/xyz.png on https://dkc.bio/ui
//				cookie.setAttribute("SameSite", "None");
//				((HttpServletResponse) response).addCookie(cookie);
				// The above code does not work! Directly setting the HTTP-header instead works:
				((HttpServletResponse) response).addHeader("Set-Cookie", //
						COOKIE_AUTH_TOKEN + "=" + authCookie.bearerTokenSha256 //
								+ "; path=/; Max-Age=" + (AuthCookie.EXPIRY_MS / 1000) //
								+ "; Secure; HttpOnly; SameSite=None");
			}
		}
		setAuthInfo(null);
	}

	private Cookie getAuthCookieFromServletRequest(ServletRequest request) {
		Cookie[] cookies = ((HttpServletRequest) request).getCookies();
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
	private AuthInfo getAuthInfo() {
		return AuthInfoAccessor.getAuthInfo();
	}

	@SuppressWarnings("deprecation")
	private void setAuthInfo(AuthInfo authInfo) {
		AuthInfoAccessor.setAuthInfo(authInfo);
	}
}
