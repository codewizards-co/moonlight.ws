package moonlight.ws.api.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.ws.rs.NameBinding;

/**
 * Annotation enabling authentication for a RESTful resource.
 * <p>
 * <b>Warning:</b> A RESTful resource without this annotation is anonymously
 * accessible!
 * <p>
 * When adding this annotation on the class or interface, it applies to all its
 * methods. If fine-grained control is needed, this annotation can be applied to
 * individual methods of a RESTful interface (or class).
 * <p>
 * The default implementation bound to this annotation is the
 * {@code Pac4jAuthenticationFilter}. There still is additionally
 * {@code KeycloakAuthenticationFilter}, but it is not used, anymore. It is only
 * kept for reference for a while.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@NameBinding
public @interface RequiresAuthentication {
}
