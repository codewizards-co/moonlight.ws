package moonlight.ws.liferay;

import static java.util.Objects.*;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.NonNull;

@RequestScoped
public class LiferayResourceFactory {

	@Inject
	protected LiferayConfig liferayConfig;

	protected Map<Class<?>, Object> resourceClass2Resource = new HashMap<>();

	public <R> R getResource(@NonNull Class<R> resourceClass) {
		Object resource = resourceClass2Resource.get(resourceClass);
		if (resource == null) {
			try {
				URL url = new URL(liferayConfig.getUrl());

				Method builderMethod = resourceClass.getMethod("builder");
				Object builder = builderMethod.invoke(null);

				Method endpointMethod = builder.getClass().getMethod("endpoint", String.class, int.class, String.class);
				endpointMethod.invoke(builder, url.getHost(), url.getPort(), url.getProtocol());

				Method contextPathMethod = builder.getClass().getMethod("contextPath", String.class);
				contextPathMethod.invoke(builder, url.getPath());

				String user = requireNonNull(liferayConfig.getUser(),
						"Currently, only user+password-based authentication is supported! You must provide %s!"
								.formatted(LiferayConfig.USER));

				String password = requireNonNull(liferayConfig.getPassword(),
						"Currently, only user+password-based authentication is supported! You must provide %s!"
								.formatted(LiferayConfig.PASSWORD));

				Method authenticationMethod = builder.getClass().getMethod("authentication", String.class, String.class);
				authenticationMethod.invoke(builder, user, password);

				Method buildMethod = builder.getClass().getMethod("build");
				resource = buildMethod.invoke(builder);
				resourceClass2Resource.put(resourceClass, resource);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return resourceClass.cast(requireNonNull(resource, "resource"));
	}
}
