package moonlight.ws.api.auth;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class UserDto {

	private Long id;

	private String username;

	@JsonFormat(shape = Shape.STRING)
	private Instant created;

	private Long createdByUserId;

	@JsonFormat(shape = Shape.STRING)
	private Instant changed;

	private Long changedByUserId;

//	/** Users cannot be deleted. They can be disabled by removing all their roles.
//	 * Either {@code null} or the timestamp when it was deleted.
//	 */
//	private Instant deleted;
//	private Long deletedByUserId;

	private Set<Role> roles;
}
