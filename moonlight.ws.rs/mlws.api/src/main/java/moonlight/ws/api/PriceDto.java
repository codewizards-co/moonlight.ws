package moonlight.ws.api;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceDto implements Cloneable {

	public static final int PRICE_TOTAL_SCALE = 2;
	public static final int PRICE_SINGLE_SCALE = 4;

	private BigDecimal priceSingleNet;
	private BigDecimal priceSingleGross;
	private BigDecimal priceTotalNet;
	private BigDecimal priceTotalGross;
	private BigDecimal quantity;
	private BigDecimal taxPercent;

	@Override
	public PriceDto clone() {
		try {
			return (PriceDto) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
