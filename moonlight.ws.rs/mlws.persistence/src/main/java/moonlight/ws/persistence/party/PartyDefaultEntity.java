package moonlight.ws.persistence.party;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import moonlight.ws.persistence.AbstractEntity;

@Getter
@Setter
@Entity(name = "PartyDefault")
public class PartyDefaultEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	private Long id;

	/**
	 * The discount given to the consignee. It is applied directly and invisibly to
	 * the regular net price.
	 * <p>
	 * Never {@code null}.
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
}
