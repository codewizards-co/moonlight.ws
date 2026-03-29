package moonlight.ws.persistence.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.liferay.headless.commerce.admin.order.client.dto.v1_0.Order;

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
import moonlight.ws.api.invoice.InvoiceWorkflow;
import moonlight.ws.persistence.AbstractEntity;
import moonlight.ws.persistence.party.PartyEntity;

@Getter
@Setter
@Entity(name = "Invoice")
public class InvoiceEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "InvoiceIdSequence")
	@SequenceGenerator(name = "InvoiceIdSequence", sequenceName = "InvoiceIdSequence", allocationSize = 1)
	private Long id;

	/**
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was deleted. This is never {@code null} for better & easier
	 * indexability.
	 */
	private long deleted;
	private Long deletedByUserId;

	/**
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was finalized. This is never {@code null} for better & easier
	 * indexability.
	 */
	private long finalized;

	private Long finalizedByUserId;

	/**
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was booked into Liferay. This is never {@code null} for better &
	 * easier indexability.
	 */
	private long booked;

	private InvoiceWorkflow workflow;

	@ManyToOne
	@JoinColumn(name = "partyId")
	private PartyEntity party;

	/**
	 * References {@link Order#getId() Order.id} in Liferay.
	 */
	private Long orderId;

	/**
	 * References {@link Shipment#getId() Shipment.id} in Liferay.
	 */
	private Long shipmentId;

	private BigDecimal includedTotalNet;
	private BigDecimal includedTotalGross;

	private BigDecimal excludedTotalNet;
	private BigDecimal excludedTotalGross;

	/**
	 * When was the invoice marked as being paid in our system. Either {@code 0} or
	 * the {@linkplain System#currentTimeMillis() timestamp} when it was marked as
	 * paid. This is never {@code null} for better & easier indexability.
	 * <p>
	 * If the invoice is marked as not paid after it was already marked as paid,
	 * this is the timestamp of when the payment was registered as undone. It is
	 * always the last timestamp of when the property {@link #getPaid() paid} was
	 * touched.
	 */
	private long markedPaid;

	private Long markedPaidByUserId;

	/**
	 * When was the invoice paid in real life?
	 */
	private LocalDate paid;
}
