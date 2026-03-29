package moonlight.ws.api;

import java.time.Instant;

import lombok.NonNull;

public class DateTime {

	private final Instant instant;

	public DateTime(@NonNull String string) {
		this.instant = Instant.parse(string);
	}

	public DateTime(@NonNull Instant instant) {
		this.instant = instant;
	}

	@Override
	public String toString() {
		return instant.toString();
	}

	public Instant getInstant() {
		return instant;
	}
}
