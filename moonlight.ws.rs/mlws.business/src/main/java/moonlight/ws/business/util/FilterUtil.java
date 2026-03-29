package moonlight.ws.business.util;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class FilterUtil {

	public static <E> boolean equalsFilterValue(@NonNull E entity, @NonNull Function<E, Object> getter,
			Object filterValue) {
		if (filterValue == null) {
			return true;
		}
		Object value = getter.apply(entity);
		return filterValue.equals(value);
	}

	public static <E> boolean containsFilterValue(@NonNull E entity, @NonNull Function<E, String> getter,
			String filterValue) {
		if (filterValue == null || filterValue.isEmpty()) {
			return true;
		}
		String value = getter.apply(entity);
		if (value == null) {
			return false;
		}
		return value.toLowerCase(Locale.UK).contains(filterValue.toLowerCase(Locale.UK));
	}

	public static <E> boolean matchesFilterValue(E entity, Function<E, String> getter, Pattern filterValue) {
		if (filterValue == null) {
			return true;
		}
		String value = getter.apply(entity);
		if (value == null) {
			return false;
		}
		return filterValue.matcher(value).matches();
	}

	/**
	 * Gets a {@link Pattern} from the given {@code filterValue}, if this is a
	 * regex. A regex starts and ends with '/'. After the final '/', a list of flags
	 * can be specified:
	 * <ul>
	 * <li>{@code i}: insensitive: case-insensitive match,
	 * {@link Pattern#CASE_INSENSITIVE}
	 * <li>{@code m}: multi-line: ^ and $ match start/end of line,
	 * {@link Pattern#MULTILINE}
	 * <li>{@code s}: single-line: dot matches newline, {@link Pattern#DOTALL}
	 * </ul>
	 *
	 * @param filterValue the filter-value to be converted into a regex, if it is
	 *                    marked as one (with '/'). May be {@code null}.
	 * @return an instance of {@code Pattern} equivalent to the given
	 *         {@code filterValue} or {@code null}.
	 */
	public static Pattern getPatternIfRegex(String filterValue) {
		if (filterValue == null || filterValue.length() < 3) {
			return null;
		}
		if (!filterValue.startsWith("/")) {
			return null;
		}
		String regex = filterValue.substring(1);
		int lastSlashIndex = regex.lastIndexOf('/');
		if (lastSlashIndex < 0) {
			return null;
		}
		String flagsString = regex.substring(lastSlashIndex + 1);
		regex = regex.substring(0, lastSlashIndex);
		int flags = 0;
		for (int i = 0; i < flagsString.length(); ++i) {
			char flagChar = flagsString.charAt(i);
			switch (flagChar) {
				case 'i':
					flags |= Pattern.CASE_INSENSITIVE;
					break;
				case 'm':
					flags |= Pattern.MULTILINE;
					break;
				case 's':
					flags |= Pattern.DOTALL;
				default:
					throw new IllegalArgumentException("Unknown regex-flag: " + flagChar);
			}
		}
		return Pattern.compile(regex, flags);
	}
}
