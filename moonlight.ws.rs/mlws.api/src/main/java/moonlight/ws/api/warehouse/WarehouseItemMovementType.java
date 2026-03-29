package moonlight.ws.api.warehouse;

import static java.util.function.Function.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;

@Getter
public enum WarehouseItemMovementType {

	/**
	 * The inventory was taken initially or a wrong quantity was corrected when
	 * taking inventory again, later. It basically means that we don't know where a
	 * product has come from (positive quantity) or where it has gone (negative
	 * quantity).
	 * <p>
	 * Even while taking inventory, another (specific) movement-type should be
	 * chosen, if it is known. This is a fallback-type to be used only, if no
	 * specific type is known.
	 */
	INVENTORY("INVE"),

	/**
	 * A product or raw material was <b>purchased from a supplier</b> and added to
	 * stock.
	 * <p>
	 * The <b>quantity is normally positive</b>, but it may be negative in order to
	 * correct a previous wrong booking.
	 */
	SUPPLY("SUPP"),

	/**
	 * An intermediate product or raw material was taken from stock to be consumed
	 * in the process of producing another product.
	 * <p>
	 * The raw material may be transformed into sth. else or it may just be used and
	 * thrown away. For example, cacao-beans are transformed into chocolate, but
	 * cleaning agents are simply washed away.
	 * <p>
	 * The <b>quantity is normally negative</b>, but it may be positive in order to
	 * correct a previous wrong booking.
	 */
	CONSUMPTION("CONS"),

	/**
	 * An intermediate or final product was produced and added to stock.
	 * <p>
	 * The <b>quantity is normally positive</b>, but it may be negative in order to
	 * correct a previous wrong booking.
	 */
	PRODUCTION("PROD"),

	/**
	 * Goods were transferred from/to another warehouse.
	 * <p>
	 * When performing a transfer, 2 movements are created and persisted, both with
	 * this type. The sum of their quantities is 0 as one side has a negative
	 * quantity to remove the goods from stock while the other side has an
	 * equivalent positive quantity to add the goods to stock.
	 *
	 * @see WarehouseItemMovementEntity#getOtherWarehouseId()
	 */
	TRANSFER("TRAN"),

	/**
	 * Goods were sold.
	 * <p>
	 * Since we normally handle all of our own sales directly in Liferay (and there
	 * exists no warehouse-item-movement in Moonlight), this type is usually used
	 * for shops selling on a commission basis, only.
	 */
	SALE("SALE"),

	/**
	 * Goods were damaged so that they cannot be sold or processed, anymore. The
	 * cause of this may be just time or some moulder or any events like a flood.
	 */
	DAMAGED("DAMA");

	/**
	 * The alphanumeric code of this type as stored in the database. Maximum length
	 * is 4 characters.
	 */
	private final String dbCode;

	private static final Map<String, WarehouseItemMovementType> dbCode2Enum;

	static {
		dbCode2Enum = Collections.unmodifiableMap(Arrays.asList(values()).stream()
				.collect(Collectors.toMap(WarehouseItemMovementType::getDbCode, identity())));
	}

	private WarehouseItemMovementType(@NonNull String code) {
		this.dbCode = code;
	}

	public static final WarehouseItemMovementType fromDbCode(@NonNull String dbCode) {
		var type = dbCode2Enum.get(dbCode);
		if (type == null) {
			throw new IllegalArgumentException("There is no WarehouseItemMovementType with this dbCode: " + dbCode);
		}
		return type;
	}
}
