package moonlight.ws.api.warehouse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moonlight.ws.api.PriceDto;
import moonlight.ws.api.RestConst;
import moonlight.ws.api.party.SupplierDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class WarehouseItemMovementDto {

	private Long id;

	@JsonFormat(shape = Shape.STRING)
	private Instant created;

	private Long createdByUserId;

	@JsonFormat(shape = Shape.STRING)
	private Instant changed;

	private Long changedByUserId;

	/**
	 * References {@link WarehouseItem#getId() WarehouseItem.id}.
	 */
	private Long warehouseItemId;

	/**
	 * References {@link WarehouseItem#getExternalReferenceCode()
	 * WarehouseItem.externalReferenceCode}.
	 */
	private String warehouseItemErc;

	/**
	 * References {@link WarehouseItem#getSku() WarehouseItem.sku}.
	 */
	private String sku;

	/**
	 * The list of products related to the SKU of this warehouse-item's movement.
	 * Resolved only when {@link RestConst#QUERY_FETCH fetch} contains
	 * {@code products}.
	 */
	private List<WarehouseItemProductDto> products;

	/**
	 * References {@link Warehouse#getId() Warehouse.id}.
	 */
	private Long warehouseId;

	/**
	 * References {@link Warehouse#getExternalReferenceCode()
	 * Warehouse.externalReferenceCode}.
	 */
	private String warehouseErc;

	/**
	 * The quantity being moved in or out of a warehouse. It is positive, if it's
	 * moved in (increasing {@link WarehouseItem#getQuantity()
	 * WarehouseItem.quantity}) and it is negative, if it's moved out (decreasing
	 * {@code WarehouseItem.quantity}).
	 */
	private BigDecimal quantity;

	private String unitOfMeasureKey;

	/**
	 * Either {@code null} or the timestamp when it was booked into Liferay.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant booked;

	private Boolean draft;

	/**
	 * Either {@code null} or the timestamp when it was finalized.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant finalized;

	private Long groupId;

	/**
	 * The type of this movement.
	 */
	private WarehouseItemMovementType type;

	/**
	 * References {@link Warehouse#getId() Warehouse.id} of the other warehouse, if
	 * {@link #getType() type} is {@link WarehouseItemMovementType#TRANSFER
	 * TRANSFER}. For other movement-types, this is {@code null}.
	 * <p>
	 * If {@link #getQuantity() quantity} is positive, the goods are transferred
	 * from the other warehouse to this warehouse (referenced by
	 * {@link #getWarehouseId()}).
	 * <p>
	 * If {@link #getQuantity() quantity} is negative, the goods are transferred to
	 * the other warehouse from this warehouse (referenced by
	 * {@link #getWarehouseId()}).
	 * <p>
	 * In such a transfer, there is also a {@link #getGroup() group} existing,
	 * grouping the other warehouse's movement with this instance. If this
	 * instance's quantity is positive, the other movement's quantity is negative
	 * (and vice versa). The sum of both quantities is 0.
	 *
	 * @see WarehouseItemMovementType#TRANSFER
	 */
	private Long otherWarehouseId;

	/**
	 * References {@link WarehouseItem#getId() WarehouseItem.id} of the other
	 * warehouse, if {@link #getType() type} is
	 * {@link WarehouseItemMovementType#TRANSFER TRANSFER}. For other
	 * movement-types, this is {@code null}.
	 * <p>
	 * The same product having the same SKU has a different warehouse-item-ID in
	 * each warehouse.
	 */
	private Long otherWarehouseItemId;

	/**
	 * References {@link SupplierDto#getId() SupplierDto.id} of the supplier, if
	 * {@link #getType() type} is {@link WarehouseItemMovementType#SUPPLY SUPPLY}.
	 * For other movement-types, this is {@code null}.
	 */
	private Long supplierId;

	/**
	 * The supplier referenced by {@link #supplierId}, if that is not {@code null}
	 * and {@link RestConst#QUERY_FETCH fetch} contains {@code supplier} or
	 * {@code supplier.party}.
	 */
	private SupplierDto supplier;

	/**
	 * The price to be paid for this movement, if {@link #getType() type} is
	 * {@link WarehouseItemMovementType#SUPPLY SUPPLY}. For other movement-types,
	 * this is {@code null}.
	 * <p>
	 * Even if {@code type} is {@code SUPPLY}, this price is still optional.
	 * <p>
	 * {@link PriceDto#getQuantity() price.quantity} always equals
	 * {@link #getQuantity() this.quantity}! When posting/putting data,
	 * {@code price.quantity} can be omitted (optional).
	 */
	private PriceDto price;

	public WarehouseItemMovementDto(Long id) {
		this.id = id;
	}
}
