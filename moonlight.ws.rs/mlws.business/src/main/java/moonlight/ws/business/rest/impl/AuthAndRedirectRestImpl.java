package moonlight.ws.business.rest.impl;

import static moonlight.ws.base.util.UrlUtil.*;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.AuthAndRedirectRest;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.base.auth.AuthCookie;
import moonlight.ws.base.auth.AuthCookieRegistry;
import moonlight.ws.base.internal.AuthInfoAccessor;

@RequestScoped
@Slf4j
public class AuthAndRedirectRestImpl implements AuthAndRedirectRest {

	@Inject
	private UriInfo uriInfo;

	@Inject
	private AuthCookieRegistry authCookieRegistry;

	@Override
	public Response performGet(String bearerTokenSha256, String redirectUrl) throws Exception {
		log.info("bearerTokenSha256: {}", bearerTokenSha256);
		log.info("redirectUrlFromClient: {}", redirectUrl);
		redirectUrl = URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8);
		log.info("redirectUrlDecoded: {}", redirectUrl);
		redirectUrl = workaroundFixBrokenUrl(redirectUrl);
		log.info("redirectUrl: {}", redirectUrl);
		int protocolEndSeparatorIndex = redirectUrl.indexOf("://");
		if (protocolEndSeparatorIndex < 0 || protocolEndSeparatorIndex > 9) {
			String baseUrl = uriInfo.getBaseUri().toString();
			if (!baseUrl.endsWith("/") && !redirectUrl.startsWith("/")) {
				baseUrl += "/";
			}
			redirectUrl = baseUrl + redirectUrl;
			log.debug("made relative redirectUrl absolute: {}", redirectUrl);
		}
		URI redirectUri = URI.create(redirectUrl);
		log.info("redirectUri: {}", redirectUri);
		AuthCookie authCookie = authCookieRegistry.getAuthCookieByBearerTokenSha256(bearerTokenSha256);
		if (authCookie == null) {
			log.error("bearerTokenSha256='{}' is not known.", bearerTokenSha256);
			throw new NotFoundException();
		}
		setAuthInfo(authCookie.authInfo);
		return Response.status(Response.Status.FOUND).location(redirectUri).build();
	}

	@SuppressWarnings("deprecation")
	private void setAuthInfo(AuthInfo authInfo) {
		AuthInfoAccessor.setAuthInfo(authInfo);
	}
}
