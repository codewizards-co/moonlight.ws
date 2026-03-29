package moonlight.ws.api.warehouse;

import java.util.ArrayList;
import java.util.List;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseItemLabelDto {

	private WarehouseItem warehouseItem;
	private List<Sku> skus;

	public List<Sku> getSkus() {
		if (skus == null) {
			return new ArrayList<>();
		}
		return skus;
	}
}
