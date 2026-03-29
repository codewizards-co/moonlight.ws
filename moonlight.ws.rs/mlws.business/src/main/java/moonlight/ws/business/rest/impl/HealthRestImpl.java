package moonlight.ws.business.rest.impl;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Objects.*;
import static moonlight.ws.base.health.HealthCheck.*;
import static moonlight.ws.base.util.StringUtil.*;

import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.HealthRest;
import moonlight.ws.base.health.HealthCheck;
import moonlight.ws.base.health.HealthStatus;

@RequestScoped
@Slf4j
public class HealthRestImpl implements HealthRest {

	@Inject
	protected RequestContextController requestContextController;

	@Inject
	protected Instance<HealthCheck> healthCheckInstance;

	protected static final ExecutorService executorService = Executors.newFixedThreadPool(10);

	@Override
	public String getHealth() {
		List<Future<HealthStatus>> healthStatusFutures = getHealthChecks().stream() //
				.map(hc -> executorService.submit(() -> check(hc))) //
				.toList();

		List<HealthStatus> healthStatuses = new ArrayList<>(healthStatusFutures.stream() //
				.map(future -> getFromFuture(future)) //
				.toList());

		Collections.sort(healthStatuses, (hs0, hs1) -> hs0.getHealthCheckName().compareTo(hs1.getHealthCheckName()));

		HealthStatus allHealthStatus = healthStatuses.stream().reduce( //
				new HealthStatus(NAME_ALL, null, null), //
				(hs0, hs1) -> hs0.isHealthy() && hs1.isHealthy() ? new HealthStatus(NAME_ALL, null, null)
						: new HealthStatus(NAME_ALL, "sick", "At least one sub-system is not healthy.") //
		);
		healthStatuses.add(allHealthStatus);

		return toHealthString(healthStatuses);
	}

	protected HealthStatus getFromFuture(@NonNull Future<HealthStatus> future) {
		try {
			return future.get();
		} catch (Throwable x) { // should never happen -- maybe only if the JVM is terminated
			log.error("getFromFuture: " + x, x);
			if (x instanceof Error) {
				throw (Error) x;
			}
			throw new RuntimeException(x);
		}
	}

	protected HealthStatus check(@NonNull HealthCheck healthCheck) {
		final boolean requestContextActivated = requestContextController.activate();
		try {
			String healthCheckName = requireNonNull(healthCheck.getName(), "healthCheck.getName()");
			HealthStatus healthStatus;
			try {
				healthStatus = healthCheck.check();
			} catch (Throwable error) {
				log.error("check: healthCheckName='%s': %s".formatted(healthCheckName, error), error);
				return new HealthStatus(healthCheckName, "ERROR_" + error.getClass().getName(), error.toString());
			}
			return healthStatus == null ? new HealthStatus(healthCheckName, null, null)
					: validateHealthStatus(healthCheck, healthStatus);
		} finally {
			if (requestContextActivated) {
				requestContextController.deactivate();
			}
		}
	}

	protected String toHealthString(@NonNull List<HealthStatus> healthStatuses) {
		StringBuilder sb = new StringBuilder();
		sb.append("_timestamp_=").append(Instant.now().toString()).append('\n');
		for (HealthStatus healthStatus : healthStatuses) {
			String healthCheckNamePrefix = urlEncode(healthStatus.getHealthCheckName()) + '.';
			sb.append(healthCheckNamePrefix);
			if (healthStatus.isDisabled()) {
				sb.append("disabled=").append(true).append('\n');
				continue;
			}
			boolean healthy = healthStatus.isHealthy();
			sb.append("healthy=").append(healthy).append('\n');
			if (!healthy) {
				sb.append(healthCheckNamePrefix).append("maladyCode=").append(urlEncode(healthStatus.getMaladyCode()))
						.append('\n');
				sb.append(healthCheckNamePrefix).append("maladyMessage=")
						.append(urlEncode(healthStatus.getMaladyMessage())).append('\n');
			}
		}
		return sb.toString();
	}

	protected List<HealthCheck> getHealthChecks() {
		Set<String> healthCheckNames = new HashSet<>();
		List<HealthCheck> healthChecks = new ArrayList<HealthCheck>();
		for (HealthCheck healthCheck : healthCheckInstance) {
			healthChecks.add(requireNonNull(healthCheck, "healthCheck"));
			String healthCheckName = healthCheck.getName();
			if (healthCheckName == null) {
				throw new IllegalStateException(
						"HealthCheck-implementation %s returned name=null! The name must not be null!"
								.formatted(healthCheck.getClass().getName()));
			}
			if (healthCheckName.isEmpty()) {
				throw new IllegalStateException(
						"HealthCheck-implementation %s returned name=''! The name must not be empty!"
								.formatted(healthCheck.getClass().getName()));
			}
			if (healthCheckName.startsWith("_")) {
				throw new IllegalStateException(
						"HealthCheck-implementation %s returned name='%s'! The name must not start with an underscore (reserved character)."
								.formatted(healthCheck.getClass().getName()));
			}
			if (!healthCheckNames.add(healthCheckName)) {
				throw new IllegalStateException(
						"Name not unique! Multiple HealthCheck-implementations returned name='%s'!"
								.formatted(healthCheckName));
			}
		}
		return healthChecks;
	}

	private static HealthStatus validateHealthStatus(@NonNull HealthCheck healthCheck,
			@NonNull HealthStatus healthStatus) {
		String healthCheckName = requireNonNull(healthCheck.getName(), "healthCheck.getName()");
		if (!healthCheckName.equals(healthStatus.getHealthCheckName())) {
			throw new IllegalStateException(
					"Name-mismatch! The implementation of HealthCheck with name='%s' returned healthStatus.healthCheckName='%s'!"
							.formatted(healthCheckName, healthStatus.getHealthCheckName()));
		}
		return healthStatus;
	}

	private static final String urlEncode(String string) {
		return isEmpty(string) ? "" : URLEncoder.encode(string, UTF_8);
	}
}
