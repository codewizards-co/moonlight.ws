package moonlight.ws.persistence.party;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import moonlight.ws.persistence.AbstractEntity;

@Getter
@Setter
@Entity(name = "Supplier")
public class SupplierEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SupplierIdSequence")
	@SequenceGenerator(name = "SupplierIdSequence", sequenceName = "SupplierIdSequence", allocationSize = 1)
	private Long id;

	/**
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was deleted. This is never {@code null} for better & easier
	 * indexability.
	 */
	private long deleted;
	private Long deletedByUserId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "partyId")
	private PartyEntity party;
}
