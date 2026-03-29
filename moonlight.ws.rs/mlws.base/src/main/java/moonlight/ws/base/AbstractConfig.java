package moonlight.ws.base;

import static java.util.Objects.*;
import static moonlight.ws.base.util.StringUtil.*;

import java.util.function.Consumer;

/**
 * Abstract base for configurations.
 */
public abstract class AbstractConfig {

	/**
	 * Gets a value or fail, if it is not present.
	 *
	 * @param name the name of the system-property. Must not be {@code null}.
	 * @return the system-property's value. Never {@code null}. An
	 *         {@link IllegalStateException} is thrown instead of returning
	 *         {@code null}.
	 */
	public String getValueOrFail(final String name) {
		requireNonNull(name, "name");
		String value = getValue(name, null);
		if (isEmpty(value)) {
			throw new IllegalStateException(String.format("Env-var '%s' missing!", name));
		}
		return value;
	}

	/**
	 * Gets a value or the specified {@code defaultValue}.
	 *
	 * @param name         the name of the env-var. Must not be {@code null}.
	 * @param defaultValue the default-value to be returned in case the property is
	 *                     undefined. May be s{@code null}.
	 * @return the system-property's value or the {@code defaultValue}. Is
	 *         {@code null}, iff the property is undefined and
	 *         {@code defaultValue == null}.
	 */
	public String getValue(final String name, final String defaultValue) {
		requireNonNull(name, "name");
		String value = System.getenv(name);
		if (!isEmpty(value)) {
			return value;
		}
		return defaultValue;
	}

	protected Long getValueAsLong(final String name, final Long defaultValue, Consumer<Long> validator) {
		requireNonNull(name, "name");
		String s = trim(getValue(name, null));
		if (isEmpty(s)) {
			return defaultValue;
		}
		try {
			Long l = Long.valueOf(s);
			if (validator != null) {
				validator.accept(l);
			}
			return l;
		} catch (Exception x) {
			throw (IllegalStateException) new IllegalStateException(
					"Env-var '%s' contains the illegal value '%s': %s".formatted(name, s, x.getMessage())).initCause(x);
		}
	}

	protected static void assertNotNegative(long value) {
		if (value < 0) {
			throw new IllegalStateException("value must not be negative");
		}
	}
}
