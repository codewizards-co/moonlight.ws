package moonlight.ws.api.liferay;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;

public class WarehouseItemDto extends WarehouseItem {

	private static final long serialVersionUID = 1L;

	@JsonProperty("externalReferenceCode")
	@Override
	public void setExternalReferenceCode(String externalReferenceCode) {
		super.setExternalReferenceCode(externalReferenceCode);
	}

	@JsonProperty("id")
	@Override
	public void setId(Long id) {
		super.setId(id);
	}

	@JsonProperty("modifiedDate")
	@Override
	public void setModifiedDate(Date modifiedDate) {
		super.setModifiedDate(modifiedDate);
	}

	@JsonProperty("quantity")
	@Override
	public void setQuantity(BigDecimal quantity) {
		super.setQuantity(quantity);
	}

	@JsonProperty("reservedQuantity")
	@Override
	public void setReservedQuantity(BigDecimal reservedQuantity) {
		super.setReservedQuantity(reservedQuantity);
	}

	@JsonProperty("sku")
	@Override
	public void setSku(String sku) {
		super.setSku(sku);
	}

	@JsonProperty("unitOfMeasureKey")
	@Override
	public void setUnitOfMeasureKey(String unitOfMeasureKey) {
		super.setUnitOfMeasureKey(unitOfMeasureKey);
	}

	@JsonProperty("warehouseExternalReferenceCode")
	@Override
	public void setWarehouseExternalReferenceCode(String warehouseExternalReferenceCode) {
		super.setWarehouseExternalReferenceCode(warehouseExternalReferenceCode);
	}

	@JsonProperty("warehouseId")
	@Override
	public void setWarehouseId(Long warehouseId) {
		super.setWarehouseId(warehouseId);
	}

//	@JsonIgnore
//	@Override
//	public void setExternalReferenceCode(UnsafeSupplier<String, Exception> externalReferenceCodeUnsafeSupplier) {
//		super.setExternalReferenceCode(externalReferenceCodeUnsafeSupplier);
//	}
//
//	@JsonIgnore
//	@Override
//	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
//		super.setId(idUnsafeSupplier);
//	}
//
//	@JsonIgnore
//	@Override
//	public void setModifiedDate(UnsafeSupplier<Date, Exception> modifiedDateUnsafeSupplier) {
//		super.setModifiedDate(modifiedDateUnsafeSupplier);
//	}
//
//	@JsonIgnore
//	@Override
//	public void setQuantity(UnsafeSupplier<BigDecimal, Exception> quantityUnsafeSupplier) {
//		super.setQuantity(quantityUnsafeSupplier);
//	}
//
//	@JsonIgnore
//	@Override
//	public void setReservedQuantity(UnsafeSupplier<BigDecimal, Exception> reservedQuantityUnsafeSupplier) {
//		super.setReservedQuantity(reservedQuantityUnsafeSupplier);
//	}
//
//	@JsonIgnore
//	@Override
//	public void setSku(UnsafeSupplier<String, Exception> skuUnsafeSupplier) {
//		super.setSku(skuUnsafeSupplier);
//	}
//
//	@JsonIgnore
//	@Override
//	public void setUnitOfMeasureKey(UnsafeSupplier<String, Exception> unitOfMeasureKeyUnsafeSupplier) {
//		super.setUnitOfMeasureKey(unitOfMeasureKeyUnsafeSupplier);
//	}
//
//	@JsonIgnore
//	@Override
//	public void setWarehouseExternalReferenceCode(
//			UnsafeSupplier<String, Exception> warehouseExternalReferenceCodeUnsafeSupplier) {
//		super.setWarehouseExternalReferenceCode(warehouseExternalReferenceCodeUnsafeSupplier);
//	}
//
//	@JsonIgnore
//	@Override
//	public void setWarehouseId(UnsafeSupplier<Long, Exception> warehouseIdUnsafeSupplier) {
//		super.setWarehouseId(warehouseIdUnsafeSupplier);
//	}
}
