package moonlight.ws.business.rest.time;

import static moonlight.ws.base.util.StringUtil.*;

import java.time.Instant;

import jakarta.ws.rs.ext.ParamConverter;

public class InstantConverter implements ParamConverter<Instant> {

	@Override
	public Instant fromString(String value) {
		return isEmpty(value) ? null : Instant.parse(value);
	}

	@Override
	public String toString(Instant value) {
		return value == null ? null : value.toString();
	}
}