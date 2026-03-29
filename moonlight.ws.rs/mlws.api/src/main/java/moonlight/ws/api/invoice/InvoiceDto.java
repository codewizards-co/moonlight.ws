package moonlight.ws.api.invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.liferay.headless.commerce.admin.order.client.dto.v1_0.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moonlight.ws.api.party.PartyDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class InvoiceDto {

	/**
	 * Internal ID. Not to be confused with the official invoice-ID printed on
	 * invoices. The official invoice-ID printed on invoices is assigned *after* an
	 * invoice was finalized.
	 * <p>
	 * Also, we are currently using Liferay's order management and thus an
	 * invoice-ID is assigned to the custom field {@code moonlight_invoiceId} of the
	 * order referenced by {@link #getLiferayOrderId() liferayOrderId}.
	 */
	private Long id;

	@JsonFormat(shape = Shape.STRING)
	private Instant created;

	private Long createdByUserId;

	@JsonFormat(shape = Shape.STRING)
	private Instant changed;

	private Long changedByUserId;

	/**
	 * Either {@code null} or the timestamp when it was deleted.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant deleted;
	private Long deletedByUserId;

	private Boolean draft;

	/**
	 * Either {@code null} or the timestamp when it was finalized.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant finalized;

	private Long finalizedByUserId;
	/**
	 * Either {@code null} or the timestamp when it was booked into Liferay.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant booked;

	private InvoiceWorkflow workflow;

	private PartyDto party;

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
	 * When was the invoice marked as being paid in our system. Either {@code null}
	 * or the timestamp when it was marked as paid.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant markedPaid;

	private Long markedPaidByUserId;

	/**
	 * Either {@code null} or the date when it was paid. If there are multiple
	 * payments, this is the date of the last payment, i.e. when there is no open
	 * amount left for this invoice, anymore.
	 */
	@JsonFormat(shape = Shape.STRING)
	private LocalDate paid;

	public InvoiceDto(Long id) {
		this.id = id;
	}
}
