package moonlight.ws.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity implements Comparable<AbstractEntity> {

	public abstract Long getId();

	@Column(nullable = false)
	private Instant created = Instant.now();

	@Column(nullable = false) // TODO switch to real relation to User
	private Long createdByUserId;

	@Column(nullable = false)
	private Instant changed = Instant.now();

	@Column(nullable = false) // TODO switch to real relation to User
	private Long changedByUserId;

	@Override
	public int compareTo(@NonNull AbstractEntity other) {
		int res = this.getClass().getName().compareTo(other.getClass().getName());
		if (res != 0) {
			return res;
		}
		Long thisId = this.getId();
		Long otherId = other.getId();
		if (thisId == null) {
			if (otherId != null) {
				return 1; // should be nulls last -- but didn't test :-D
			}
			return Integer.compare(System.identityHashCode(this), System.identityHashCode(other));
		}
		if (otherId == null) {
			return -1; // should be nulls last -- but didn't test :-D
		}
		return thisId.compareTo(otherId);
	}

	@Override
	public int hashCode() {
		Long id = getId();
		return id == null ? 0 : id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		Long id = getId();
		if (id == null) { // not yet persistent means only instance-identity is considered equal
			return false;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		var other = (AbstractEntity) obj;
		return id.equals(other.getId());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + '[' + toString_properties() + ']';
	}

	protected String toString_properties() {
		return "id=" + getId();
	}
}
