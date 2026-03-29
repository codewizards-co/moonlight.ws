package moonlight.ws.api.invoice;

import static java.util.function.Function.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;

@Getter
public enum InvoiceWorkflow {

	CONSIGNEE("C"), SUPPLIER("S");

	/**
	 * The alphanumeric code of this type as stored in the database. Maximum length
	 * is 1 character.
	 */
	private final String dbCode;

	private static final Map<String, InvoiceWorkflow> dbCode2Enum;

	static {
		dbCode2Enum = Collections.unmodifiableMap(
				Arrays.asList(values()).stream().collect(Collectors.toMap(InvoiceWorkflow::getDbCode, identity())));
	}

	private InvoiceWorkflow(@NonNull String code) {
		this.dbCode = code;
	}

	public static final InvoiceWorkflow fromDbCode(@NonNull String dbCode) {
		var type = dbCode2Enum.get(dbCode);
		if (type == null) {
			throw new IllegalArgumentException("There is no InvoiceWorkflow with this dbCode: " + dbCode);
		}
		return type;
	}
}
