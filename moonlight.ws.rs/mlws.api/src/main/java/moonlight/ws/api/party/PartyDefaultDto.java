package moonlight.ws.api.party;

import java.math.BigDecimal;
import java.time.Instant;

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
public class PartyDefaultDto {

	/**
	 * There is exactly one entity and this is its ID.
	 */
	public static final Long PARTY_DEFAULT_ID = 1L;

	private Long id;

	@JsonFormat(shape = Shape.STRING)
	private Instant created;

	private Long createdByUserId;

	@JsonFormat(shape = Shape.STRING)
	private Instant changed;

	private Long changedByUserId;

	/**
	 * The discount given to the consignee. It is applied directly and invisibly to
	 * the regular net price.
	 */
	private BigDecimal tradeDiscountPercent;

	/**
	 * The tax-percentage to be applied when charging an invoice.
	 */
	private BigDecimal taxPercent;

	/**
	 * The product-catalog used for determining the price of consignment-sales.
	 */
	private String catalogName;

	public PartyDefaultDto(Long id) {
		this.id = id;
	}
}
