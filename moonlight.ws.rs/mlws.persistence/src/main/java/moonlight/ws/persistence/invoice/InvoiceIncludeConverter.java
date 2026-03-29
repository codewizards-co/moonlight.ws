package moonlight.ws.persistence.invoice;

import static moonlight.ws.base.util.StringUtil.*;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import moonlight.ws.api.invoice.InvoiceInclude;

@Converter(autoApply = true)
public class InvoiceIncludeConverter implements AttributeConverter<InvoiceInclude, String> {

	@Override
	public String convertToDatabaseColumn(InvoiceInclude include) {
		return include == null ? null : include.getDbCode();
	}

	@Override
	public InvoiceInclude convertToEntityAttribute(String string) {
		return isEmpty(string) ? null : InvoiceInclude.fromDbCode(string);
	}

}
