package moonlight.ws.persistence.warehouse;

import java.util.HashSet;
import java.util.Set;

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

@Getter
@Setter
@Entity(name = "WarehouseItemMovementGroup")
public class WarehouseItemMovementGroupEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WarehouseItemMovementGroupIdSequence")
	@SequenceGenerator(name = "WarehouseItemMovementGroupIdSequence", sequenceName = "WarehouseItemMovementGroupIdSequence", allocationSize = 1)
	private Long id;

	private long finalized;

	@OneToMany(mappedBy = "group")
	private Set<WarehouseItemMovementEntity> movements = new HashSet<>();
}
