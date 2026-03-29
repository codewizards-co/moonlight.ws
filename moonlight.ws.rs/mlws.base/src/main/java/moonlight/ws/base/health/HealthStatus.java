package moonlight.ws.base.health;

import static moonlight.ws.base.util.StringUtil.*;

import lombok.Getter;

@Getter
public class HealthStatus {

	/**
	 * Gets the unique name of the health-check as returned by
	 * {@link HealthCheck#getName()}.
	 */
	private final String healthCheckName;

	/**
	 * Gets the code of the malady or {@code null}, if health is good. An empty
	 * string is equivalent to {@code null}.
	 */
	private final String maladyCode;

	/**
	 * Gets a message describing the problem in case there is a malady.
	 */
	private final String maladyMessage;

	private final boolean disabled;

	public HealthStatus(String healthCheckName, String maladyCode, String maladyMessage) {
		this.healthCheckName = healthCheckName;
		this.maladyCode = maladyCode;
		this.maladyMessage = maladyMessage;
		this.disabled = false;
	}

	public HealthStatus(String healthCheckName, boolean disabled) {
		this.healthCheckName = healthCheckName;
		this.maladyCode = null;
		this.maladyMessage = null;
		this.disabled = disabled;
	}

	public boolean isHealthy() {
		return isEmpty(getMaladyCode());
	}
}
