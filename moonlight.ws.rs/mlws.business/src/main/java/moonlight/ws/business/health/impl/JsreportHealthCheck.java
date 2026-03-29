package moonlight.ws.business.health.impl;

import lombok.extern.slf4j.Slf4j;
import moonlight.ws.base.health.HealthCheck;
import moonlight.ws.base.health.HealthStatus;

@Slf4j
public class JsreportHealthCheck implements HealthCheck {

	public static final String NAME = "jsreport";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public HealthStatus check() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
