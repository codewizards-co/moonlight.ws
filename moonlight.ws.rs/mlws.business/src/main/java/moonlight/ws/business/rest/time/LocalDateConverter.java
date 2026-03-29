package moonlight.ws.business.rest.time;

import static moonlight.ws.base.util.StringUtil.*;

import java.time.LocalDate;

import jakarta.ws.rs.ext.ParamConverter;

public class LocalDateConverter implements ParamConverter<LocalDate> {

	@Override
	public LocalDate fromString(String value) {
		return isEmpty(value) ? null : LocalDate.parse(value);
	}

	@Override
	public String toString(LocalDate value) {
		return value == null ? null : value.toString();
	}
}