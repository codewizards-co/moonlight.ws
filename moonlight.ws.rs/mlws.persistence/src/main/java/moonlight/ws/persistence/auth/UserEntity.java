package moonlight.ws.persistence.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import moonlight.ws.api.auth.Role;
import moonlight.ws.persistence.AbstractEntity;

@Getter
@Setter
@Entity(name = "User")
public class UserEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "UserIdSequence")
	@SequenceGenerator(name = "UserIdSequence", sequenceName = "UserIdSequence", allocationSize = 1)
	private Long id;

	private String username;

	/**
	 * Bitmask of roles granted to this user. Can be mapped from/to a {@code Set<Role>} via {@link Role#toBits(long)}/{@link Role#fromBits(long)}.
	 */
	private long roleBits;
}
