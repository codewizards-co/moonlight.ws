package moonlight.ws.api;

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
 * The default (and currently only) implementation bound to this annotation is
 * the {@code KeycloakAuthenticationFilter}.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@NameBinding
public @interface RequiresAuthentication {
}
