
export interface Price {

    quantity?: number;
    priceSingleNet?: number;
    priceSingleGross?: number;
    priceTotalNet?: number;
    priceTotalGross?: number;
    taxPercent?: number;
}

export function negatePriceIfNeeded(quantity: number, price?: Price): Price | undefined {
    if (price === undefined || price.quantity === undefined || quantity === price.quantity) {
        return price;
    }
    if (quantity === -1 * price.quantity) {
        return {
            quantity: negate(price.quantity),
            priceSingleNet: price.priceSingleNet, // single prices must not be negated
            priceSingleGross: price.priceSingleGross, // single prices must not be negated
            priceTotalNet: negate(price.priceTotalNet),
            priceTotalGross: negate(price.priceTotalGross),
            taxPercent: price.taxPercent
        };
    }
    throw new Error(`quantity = ${quantity} neither matches price.quantity = ${price.quantity} nor its negation!`);
}

function negate(value: number | undefined): number | undefined {
    return value === undefined ? undefined : -1 * value;
}