package moonlight.ws.business.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.auth.RequiresRole;
import moonlight.ws.api.auth.Role;

@Provider
@Priority(Priorities.AUTHORIZATION) // runs early, but after AUTHENTICATION
@Slf4j
public class RequiresRoleFilterProvider implements DynamicFeature {

	public RequiresRoleFilterProvider() {
		log.debug("RequiresRoleFilterProvider created.");
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		// Check if the method or the class/interface has the annotation
		RequiresRole annotation = resourceInfo.getResourceMethod().getAnnotation(RequiresRole.class);
		if (annotation == null) {
			annotation = resourceInfo.getResourceClass().getAnnotation(RequiresRole.class);
		}

		// If found, register a new instance of our filter with the extracted value
		if (annotation != null) {
			Role role = annotation.value();
			context.register(new RequiresRoleFilter(role));
		}
	}
}
