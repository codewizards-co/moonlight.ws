package moonlight.ws.business.rest.time;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TimeParamConverterProvider implements ParamConverterProvider {

	@SuppressWarnings("unchecked")
	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
		if (Instant.class == rawType) {
			return (ParamConverter<T>) new InstantConverter();
		}
		if (LocalDate.class == rawType) {
			return (ParamConverter<T>) new LocalDateConverter();
		}
		return null;
	}
}