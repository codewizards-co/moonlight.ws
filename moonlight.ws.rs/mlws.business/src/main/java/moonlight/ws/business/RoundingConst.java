package moonlight.ws.business;

import java.math.RoundingMode;

/**
 * Constants related to rounding.
 */
public interface RoundingConst {

	/**
	 * Default rounding-mode. Used for basically all rounding-operations.
	 */
	RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

	/**
	 * Scale for total prices.
	 */
	int PRICE_TOTAL_SCALE = 2;

	/**
	 * Scale for single prices.
	 */
	int PRICE_SINGLE_SCALE = 4;

	/**
	 * Scale for tax-percentages.
	 */
	int TAX_PERCENT_SCALE = 2;
}
