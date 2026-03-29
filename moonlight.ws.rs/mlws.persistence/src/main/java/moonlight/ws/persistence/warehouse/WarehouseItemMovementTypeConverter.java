package moonlight.ws.persistence.warehouse;

import static moonlight.ws.base.util.StringUtil.*;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import moonlight.ws.api.warehouse.WarehouseItemMovementType;

@Converter(autoApply = true)
public class WarehouseItemMovementTypeConverter implements AttributeConverter<WarehouseItemMovementType, String> {

	@Override
	public String convertToDatabaseColumn(WarehouseItemMovementType type) {
		return type == null ? null : type.getDbCode();
	}

	@Override
	public WarehouseItemMovementType convertToEntityAttribute(String string) {
		return isEmpty(string) ? null : WarehouseItemMovementType.fromDbCode(string);
	}
}
