package moonlight.ws.business.util;

import java.time.Instant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtil {

	public static final Instant instantFromMillis(long millis) {
		return millis == 0 ? null : Instant.ofEpochMilli(millis);
	}
}
