package moonlight.ws.persistence.invoice;

import static moonlight.ws.base.util.StringUtil.*;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import moonlight.ws.api.invoice.InvoiceWorkflow;

@Converter(autoApply = true)
public class InvoiceWorkflowConverter implements AttributeConverter<InvoiceWorkflow, String> {

	@Override
	public String convertToDatabaseColumn(InvoiceWorkflow workflow) {
		return workflow == null ? null : workflow.getDbCode();
	}

	@Override
	public InvoiceWorkflow convertToEntityAttribute(String string) {
		return isEmpty(string) ? null : InvoiceWorkflow.fromDbCode(string);
	}
}
