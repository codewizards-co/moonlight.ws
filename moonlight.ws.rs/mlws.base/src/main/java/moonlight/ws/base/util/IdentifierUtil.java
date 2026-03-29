package moonlight.ws.base.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

import lombok.experimental.UtilityClass;

/**
 * Utility class for identifiers.
 * <p>
 * Copied from <a href=
 * "http://cumulus4j.org/1.1.1/apidocs/index.html?org/cumulus4j/keymanager/back/shared/IdentifierUtil.html">org.cumulus4j.keymanager.back.shared.IdentifierUtil</a>.
 * This is allowed, because we (<a href="http://www.nightlabs.de">NightLabs</a>)
 * are the authors of Cumulus4j.
 */
@UtilityClass
public class IdentifierUtil {

	private static SecureRandom random = new SecureRandom();

	private static double log(final double base, final double value) {
		return Math.log10(value) / Math.log10(base);
	}

	/**
	 * <p>
	 * Create a random <code>String</code> identifier with a sufficiently unique
	 * length.
	 * </p>
	 * <p>
	 * This method calls {@link #createRandomID(int)} with a <code>length</code> of
	 * 25.
	 * </p>
	 * <p>
	 * The <code>length</code> of 25 is chosen, because it produces an identifier
	 * which has about the same uniqueness as {@link UUID#randomUUID()}. This is
	 * because the String has 36 ^ 25 (approximately equals 2 ^ 129) possible values
	 * while a UUID has 2 ^ 128 possible values and both identifiers are created
	 * using the same method ({@link SecureRandom#nextBytes(byte[])}).
	 * </p>
	 *
	 * @return a random <code>String</code>.
	 * @see #createRandomID(int)
	 */
	public static String createRandomID() {
		return createRandomID(25);
	}

	/**
	 * <p>
	 * Create a random <code>String</code> identifier with a specified length.
	 * </p>
	 * <p>
	 * The generated identifier will contain only the characters '0'...'9' and
	 * 'a'...'z' and will have the specified <code>length</code>. This method uses a
	 * {@link SecureRandom} (just like {@link UUID#randomUUID()}). With a length of
	 * 25, the identifier will have about the same uniqueness as a <code>UUID</code>
	 * - see {@link #createRandomID()}.
	 * </p>
	 *
	 * @param length the number of <code>char</code>s in the result.
	 * @return a random <code>String</code> with the given <code>length</code>.
	 * @see #createRandomID()
	 */
	public static String createRandomID(final int length) {
		final int byteArrayLength = (int) log(256, Math.pow(36, length)) + 2;

		final byte[] val = new byte[byteArrayLength];
		random.nextBytes(val);
		val[0] = (byte) (val[0] & 0x7F); // ensure a positive value
		final BigInteger bi = new BigInteger(val);
		String result = bi.toString(36);
		while (result.length() > length) {
			result = result.substring(1); // cut the first characters, because their range may be limited (never reaches
											// 'z')
		}

		if (result.length() < length) { // prepend with '0' to reach a fixed length.
			final StringBuilder sb = new StringBuilder(length);
			for (int i = result.length(); i < length; ++i) {
				sb.append('0');
			}

			sb.append(result);
			result = sb.toString();
		}

		if (result.length() > length + 1) {
			throw new IllegalStateException(
					"Why is result.length == " + result.length() + " > " + length + "+1 chars?!");
		}

		if (result.length() > length) {
			result = result.substring(result.length() - length);
		}

		if (result.length() != length) {
			throw new IllegalStateException("Why is result.length != " + length + " chars?!");
		}

		return result;
	}
}
