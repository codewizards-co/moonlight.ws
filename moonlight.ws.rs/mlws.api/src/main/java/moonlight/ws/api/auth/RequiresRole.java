package moonlight.ws.api.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.ws.rs.NameBinding;

/**
 * Annotation requiring a certain role for a RESTful resource.
 * <p>
 * <b>Warning:</b> A RESTful resource without this annotation is accessible to
 * every authenticated user!
 * <p>
 * When adding this annotation on the class or interface, it applies to all its
 * methods. If fine-grained control is needed, this annotation can be applied to
 * individual methods of a RESTful interface (or class).
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@NameBinding
public @interface RequiresRole {

	Role value();

}
