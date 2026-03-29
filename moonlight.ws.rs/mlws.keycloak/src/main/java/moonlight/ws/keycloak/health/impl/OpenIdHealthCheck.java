package moonlight.ws.keycloak.health.impl;

import static java.nio.charset.StandardCharsets.*;
import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.base.util.UrlUtil.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.base.health.HealthCheck;
import moonlight.ws.base.health.HealthStatus;
import moonlight.ws.keycloak.OpenIdConfig;

@RequestScoped
@Slf4j
public class OpenIdHealthCheck implements HealthCheck {

	public static final String NAME = "openid";

	public static final String MALADY_CODE_CONNECT_EXCEPTION = "CONNECT_EXCEPTION";
	public static final String MALADY_CODE_TIMEOUT = "TIMEOUT";
	public static final String MALADY_CODE_INVALID_RESPONSE_CODE = "INVALID_RESPONSE_CODE";

	@Inject
	private OpenIdConfig openIdConfig;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public HealthStatus check() throws Exception {
		if (isEmpty(openIdConfig.getUrl())) {
			log.info("check: {} is empty or missing => health-check disabled.", OpenIdConfig.URL);
			return new HealthStatus(NAME, true);
		}
		if (isEmpty(openIdConfig.getRealm())) {
			log.info("check: {} is empty or missing => health-check disabled.", OpenIdConfig.REALM);
			return new HealthStatus(NAME, true);
		}
		if (isEmpty(openIdConfig.getClientId())) {
			log.info("check: {} is empty or missing => health-check disabled.", OpenIdConfig.CLIENT_ID);
			return new HealthStatus(NAME, true);
		}
		if (isEmpty(openIdConfig.getHealthCheckUser())) {
			log.info("check: {} is empty or missing => health-check disabled.", OpenIdConfig.HEALTH_CHECK_USER);
			return new HealthStatus(NAME, true);
		}
		if (isEmpty(openIdConfig.getHealthCheckPassword())) {
			log.info("check: {} is empty or missing => health-check disabled.", OpenIdConfig.HEALTH_CHECK_PASSWORD);
			return new HealthStatus(NAME, true);
		}
		log.info("check: entered.");
		final long startTimestamp = System.currentTimeMillis();

		if (!isEmpty(openIdConfig.getClientSecret())) {
			log.warn(
					"check: {} is specified (and not empty), but not supported! Authentication is likely going to fail.",
					OpenIdConfig.CLIENT_SECRET);
		}

		StringBuilder formUrlEncoded = new StringBuilder();
		formUrlEncoded //
				.append("grant_type=password") //
				.append("&client_id=").append(URLEncoder.encode(openIdConfig.getClientId(), UTF_8)) //
				.append("&username=").append(URLEncoder.encode(openIdConfig.getHealthCheckUser(), UTF_8)) //
				.append("&password=").append(URLEncoder.encode(openIdConfig.getHealthCheckPassword(), UTF_8));

		String urlString = concatUrlSegments(openIdConfig.getUrl(), "realms", openIdConfig.getRealm(),
				"protocol/openid-connect/token");
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true); // sets method = POST
//		connection.setRequestMethod("POST"); // not necessary, because setDoOutput(true) already does this.
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		try (OutputStream out = connection.getOutputStream()) {
			out.write(formUrlEncoded.toString().getBytes(UTF_8));
		}
		int responseCode = connection.getResponseCode();
		StringBuilder responseJson = new StringBuilder();
		try {
			try (InputStream in = getInputStream(connection)) {
				if (in != null) {
					try (Reader reader = new InputStreamReader(in, UTF_8)) {
						char[] cbuf = new char[1024 * 16];
						int count;
						while ((count = reader.read(cbuf)) >= 0) {
							responseJson.append(cbuf, 0, count);
						}
					}
				}
			}
		} catch (Exception x) {
			log.error("check: " + x, x);
		}
		if (responseCode < 200 || responseCode > 299) {
			log.error("check: responseCode={}!", responseCode);
			return new HealthStatus(NAME, MALADY_CODE_INVALID_RESPONSE_CODE,
					"The response-code %d is invalid!\n\n%s".formatted(responseCode, responseJson));
		}
		log.info("check: done in {} ms.", System.currentTimeMillis() - startTimestamp);
		return null;
	}

	protected InputStream getInputStream(@NonNull HttpURLConnection connection) {
		InputStream result = null;
		try {
			result = connection.getInputStream();
		} catch (Exception x) {
			try {
				result = connection.getErrorStream();
			} catch (Exception y) {
				log.error("getInputStream: Could not get regular stream: " + x, x);
				log.error("getInputStream: Could not get error-stream: " + y, y);
			}
		}
		return result;
	}
}
