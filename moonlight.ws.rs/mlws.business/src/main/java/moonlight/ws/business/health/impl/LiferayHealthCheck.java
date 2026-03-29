package moonlight.ws.business.health.impl;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.base.health.HealthCheck;
import moonlight.ws.base.health.HealthStatus;
import moonlight.ws.liferay.LiferayResourceFactory;

@Slf4j
public abstract class LiferayHealthCheck implements HealthCheck {

	public static final String MALADY_CODE_CONNECT_EXCEPTION = "CONNECT_EXCEPTION";
	public static final String MALADY_CODE_TIMEOUT = "TIMEOUT";
	public static final String MALADY_CODE_NO_WAREHOUSE_FOUND = "NO_WAREHOUSE_FOUND";
	public static final String MALADY_CODE_NO_SKU_FOUND = "NO_SKU_FOUND";

	@Inject
	protected LiferayResourceFactory liferayResourceFactory;

	@Override
	public final HealthStatus check() throws Exception {
		log.info("[{}].check: entered.", getName());
		final long startTimestamp = System.currentTimeMillis();
		try {
			return _check();
		} catch (Exception x) {
			log.error("[%s].check: %s".formatted(getName(), x), x);

			ConnectException connectException = ExceptionUtils.throwableOfType(x, ConnectException.class);
			if (connectException != null) {
				return new HealthStatus(getName(), MALADY_CODE_CONNECT_EXCEPTION, connectException.toString());
			}

			SocketTimeoutException socketTimeoutException = ExceptionUtils.throwableOfType(x,
					SocketTimeoutException.class);
			if (socketTimeoutException != null) {
				return new HealthStatus(getName(), MALADY_CODE_TIMEOUT, socketTimeoutException.toString());
			}

			throw x;
		} finally {
			log.info("[{}].check: done in {} ms.", getName(), System.currentTimeMillis() - startTimestamp);
		}
	}

	protected abstract HealthStatus _check() throws Exception;
}
