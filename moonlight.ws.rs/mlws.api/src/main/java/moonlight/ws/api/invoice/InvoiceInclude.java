package moonlight.ws.api.invoice;

import static java.util.function.Function.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import moonlight.ws.api.liferay.WarehouseItemDto;

@Getter
public enum InvoiceInclude {

	/**
	 * Include this item and thus the referenced {@link WarehouseItemDto} in the
	 * invoice. This is the normal use-case.
	 */
	INCLUDE("I"),

	/**
	 * Exclude this item and thus the referenced {@link WarehouseItemDto} from
	 * invoicing. This is used to permanently exclude a warehouse-item from all
	 * future runs of the invoice-item-auto-generation. It usually means that this
	 * warehouse-item was somehow invoiced outside the system.
	 */
	EXCLUDE("X");

	/**
	 * The alphanumeric code of this type as stored in the database. Maximum length
	 * is 1 character.
	 */
	private final String dbCode;

	private static final Map<String, InvoiceInclude> dbCode2Enum;

	static {
		dbCode2Enum = Collections.unmodifiableMap(
				Arrays.asList(values()).stream().collect(Collectors.toMap(InvoiceInclude::getDbCode, identity())));
	}

	private InvoiceInclude(@NonNull String code) {
		this.dbCode = code;
	}

	public static final InvoiceInclude fromDbCode(@NonNull String dbCode) {
		var mode = dbCode2Enum.get(dbCode);
		if (mode == null) {
			throw new IllegalArgumentException("There is no InvoiceInclude with this dbCode: " + dbCode);
		}
		return mode;
	}
}
