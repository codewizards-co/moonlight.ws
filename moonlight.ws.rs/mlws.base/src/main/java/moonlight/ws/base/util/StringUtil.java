package moonlight.ws.base.util;

import static java.util.Objects.*;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {

	public static final boolean isEmpty(final String string) {
		return string == null || string.isEmpty();
	}

	public static final String trim(final String string) {
		return string == null ? null : string.trim();
	}

	public static final String nullToEmpty(final String string) {
		return string == null ? "" : string;
	}

	public static final String requireNonEmpty(final String string, final String name) {
		if (requireNonNull(string, name).isEmpty()) {
			throw new IllegalArgumentException("'%s' must not be empty!".formatted(name));
		}
		return string;
	}
}
