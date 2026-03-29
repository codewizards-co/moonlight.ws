package moonlight.ws.persistence.party;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import moonlight.ws.persistence.AbstractEntity;

/**
 * A party is an individual or a legal entity (organisation).
 */
@Getter
@Setter
@Entity(name = "Party")
public class PartyEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PartyIdSequence")
	@SequenceGenerator(name = "PartyIdSequence", sequenceName = "PartyIdSequence", allocationSize = 1)
	private Long id;

	/**
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was deleted. This is never {@code null} for better & easier
	 * indexability.
	 */
	private long deleted;
	private Long deletedByUserId;

	private boolean active;

	private String code;
	private String name;

	private String email;
	private String phone;

	private String street1;
	private String street2;
	private String street3;
	private String zip;
	private String city;
	private String regionCode;
	private String countryIsoCode;

	private String taxNo;

	private String website;

	private String description;

	@OneToMany(mappedBy = "party")
	private Set<ConsigneeEntity> consignees = new HashSet<>();

	@OneToMany(mappedBy = "party")
	private Set<SupplierEntity> suppliers = new HashSet<>();

	public Set<ConsigneeEntity> getConsigneesExcludingDeleted() {
		return consignees.stream().filter(s -> s.getDeleted() == 0).collect(Collectors.toSet());
	}

	public SupplierEntity getSupplierExcludingDeleted() {
		List<SupplierEntity> suppliers = this.suppliers.stream().filter(s -> s.getDeleted() == 0).toList();
		if (suppliers.size() == 1) {
			return suppliers.get(0);
		}
		if (suppliers.isEmpty()) {
			return null;
		}
		throw new IllegalStateException("There is more than 1 non-deleted Supplier associated to this Party!");
	}
}
