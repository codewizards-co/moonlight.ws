package moonlight.ws.rs;

import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import moonlight.ws.base.GlobalConfig;
import moonlight.ws.persistence.init.DataInitializer;
import moonlight.ws.persistence.liquibase.LiquibaseInitializer;

public class MoonlightWsServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// There is no API to set the timeouts in the Liferay-client-lib. We have to set
		// the global timeouts. It works this way, but isn't nice. For now, we keep it
		// this way, though.
		// *Warning!* Even when changing these values, it has not immediately an effect.
		// Java's socket-/connection-framework seems to cache or pool connections or
		// sth. like this.
		// *Warning 2!* This must never be set anywhere else.
		GlobalConfig globalConfig = CDI.current().select(GlobalConfig.class).get();
		Long defaultConnectTimeout = globalConfig.getDefaultConnectTimeout();
		if (defaultConnectTimeout != null) {
			System.setProperty(GlobalConfig.SYS_PROP_DEFAULT_CONNECT_TIMEOUT, defaultConnectTimeout.toString());
		}
		Long defaultReadTimeout = globalConfig.getDefaultReadTimeout();
		if (defaultReadTimeout != null) {
			System.setProperty(GlobalConfig.SYS_PROP_DEFAULT_READ_TIMEOUT, defaultReadTimeout.toString());
		}

		runInitializer(LiquibaseInitializer.class);
		runInitializer(DataInitializer.class);
	}

	protected <I extends Runnable> void runInitializer(Class<I> intializerClass) {
		RequestContextController requestContextController = getRequestContextController();
		boolean requestManagedHere = requestContextController.activate();
		try {
			I initializer = CDI.current().select(intializerClass).get();
			initializer.run();
		} finally {
			if (requestManagedHere) {
				requestContextController.deactivate();
			}
		}
	}

	protected RequestContextController getRequestContextController() {
		return CDI.current().select(RequestContextController.class).get();
	}
}
