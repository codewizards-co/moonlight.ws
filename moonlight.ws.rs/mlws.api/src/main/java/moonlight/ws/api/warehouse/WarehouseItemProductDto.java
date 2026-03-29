package moonlight.ws.api.warehouse;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WarehouseItemProductDto {

	private Long productId;
	private Map<String, String> productName;
}
