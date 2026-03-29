package moonlight.ws.business.mapper;

import static java.util.Objects.*;
import static moonlight.ws.api.PriceDto.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import moonlight.ws.api.PriceDto;

@ApplicationScoped
public class PriceMapper {

	private static final BigDecimal _100 = BigDecimal.valueOf(100L);
	private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

	public PriceDto toDto(BigDecimal quantity, BigDecimal priceTotalNet, BigDecimal priceTotalGross,
			BigDecimal taxPercent) {
		if (quantity == null) {
			return null;
		}
		if (priceTotalNet == null && priceTotalGross == null) {
			return null;
		}
		if (priceTotalNet == null && taxPercent == null) {
			return null;
		}
		if (priceTotalGross == null && taxPercent == null) {
			return null;
		}
		PriceDto dto = new PriceDto();
		dto.setQuantity(quantity);
		dto.setPriceTotalGross(priceTotalGross);
		dto.setPriceTotalNet(priceTotalNet);
		dto.setTaxPercent(taxPercent);
		return calculateMissingProperties(dto);
	}

	public PriceDto calculateMissingProperties(PriceDto price) {
		if (price == null) {
			return null;
		}
		price = price.clone();
		BigDecimal quantity = requireNonNull(price.getQuantity(), "price.quantity");
		for (int i = 0; i < 2; ++i) {
			BigDecimal priceSingleNet = price.getPriceSingleNet();
			BigDecimal priceSingleGross = price.getPriceSingleGross();
			BigDecimal priceTotalNet = price.getPriceTotalNet();
			BigDecimal priceTotalGross = price.getPriceTotalGross();
			BigDecimal taxPercent = price.getTaxPercent();
			if (priceTotalNet == null && priceSingleNet != null) {
				priceTotalNet = priceSingleNet.multiply(quantity);
			}
			if (priceTotalGross == null && priceSingleGross != null) {
				priceTotalGross = priceSingleGross.multiply(quantity);
			}
			if (priceTotalNet == null && priceTotalGross != null && taxPercent != null) {
				priceTotalNet = calculateNetFromGross(priceTotalGross, taxPercent, PRICE_TOTAL_SCALE);
			}
			if (priceTotalGross == null && priceTotalNet != null && taxPercent != null) {
				priceTotalGross = calculateGrossFromNet(priceTotalNet, taxPercent, PRICE_TOTAL_SCALE);
			}
			if (priceSingleNet == null && priceTotalNet != null) {
				priceSingleNet = priceTotalNet.divide(quantity, PRICE_SINGLE_SCALE, ROUNDING_MODE);
			}
			if (priceSingleGross == null && priceTotalGross != null) {
				priceSingleGross = priceTotalGross.divide(quantity, PRICE_SINGLE_SCALE, ROUNDING_MODE);
			}
			if (priceSingleNet == null && priceSingleGross != null && taxPercent != null) {
				priceSingleNet = calculateNetFromGross(priceSingleGross, taxPercent, PRICE_SINGLE_SCALE);
			}
			if (priceSingleGross == null && priceSingleNet != null && taxPercent != null) {
				priceSingleGross = calculateGrossFromNet(priceSingleNet, taxPercent, PRICE_SINGLE_SCALE);
			}
			if (taxPercent == null && priceTotalGross != null && priceTotalNet != null) {
				taxPercent = priceTotalGross.multiply(_100).divide(priceTotalNet, 1, ROUNDING_MODE).subtract(_100);
			}
			if (taxPercent == null && priceSingleGross != null && priceSingleNet != null) {
				taxPercent = priceSingleGross.multiply(_100).divide(priceSingleNet, 1, ROUNDING_MODE).subtract(_100);
			}
			price.setPriceSingleNet(priceSingleNet);
			price.setPriceSingleGross(priceSingleGross);
			price.setPriceTotalNet(priceTotalNet);
			price.setPriceTotalGross(priceTotalGross);
			price.setTaxPercent(taxPercent);
		}
		requireNonNull(price.getPriceSingleNet(), "priceSingleNet");
		requireNonNull(price.getPriceSingleGross(), "priceSingleGross");
		requireNonNull(price.getPriceTotalNet(), "priceTotalNet");
		requireNonNull(price.getPriceTotalGross(), "priceTotalGross");
		requireNonNull(price.getTaxPercent(), "taxPercent");
		return price;
	}

	public BigDecimal calculateNetFromGross(@NonNull BigDecimal grossPrice, @NonNull BigDecimal taxPercent, int scale) {
		return grossPrice.divide( //
				BigDecimal.ONE.add(taxPercent.divide(_100, 2, RoundingMode.UNNECESSARY)), //
				scale, ROUNDING_MODE);
	}

	public BigDecimal calculateGrossFromNet(@NonNull BigDecimal netPrice, @NonNull BigDecimal taxPercent, int scale) {
		return netPrice.multiply( //
				BigDecimal.ONE.add(taxPercent.divide(_100, 2, RoundingMode.UNNECESSARY))) //
				.setScale(scale, ROUNDING_MODE);
	}

}
