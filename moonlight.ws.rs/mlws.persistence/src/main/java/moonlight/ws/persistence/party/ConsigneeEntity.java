package moonlight.ws.persistence.party;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;

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

/**
 * A consignee is a merchant maintaining a
 * <a href="https://en.wikipedia.org/wiki/Consignment">consignment stock</a> and
 * selling on commission-base. Depending on language and country, this is the
 * same as or very similar to a commission-merchant.
 * <p>
 * Important: This consignee has not much to do with the receiver of a shipment!
 */
@Getter
@Setter
@Entity(name = "Consignee")
public class ConsigneeEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ConsigneeIdSequence")
	@SequenceGenerator(name = "ConsigneeIdSequence", sequenceName = "ConsigneeIdSequence", allocationSize = 1)
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

	/**
	 * References the related {@linkplain Warehouse warehouse} via its
	 * {@link Warehouse#getId() ID}. Must not be {@code null}.
	 */
	private Long warehouseId;
}
