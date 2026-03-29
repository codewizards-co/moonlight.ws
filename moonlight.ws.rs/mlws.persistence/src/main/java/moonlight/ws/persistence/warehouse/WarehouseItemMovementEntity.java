package moonlight.ws.persistence.warehouse;

import java.math.BigDecimal;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import moonlight.ws.api.warehouse.WarehouseItemMovementType;
import moonlight.ws.persistence.AbstractEntity;
import moonlight.ws.persistence.party.SupplierEntity;

@Getter
@Setter
@Entity(name = "WarehouseItemMovement")
public class WarehouseItemMovementEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WarehouseItemMovementIdSequence")
	@SequenceGenerator(name = "WarehouseItemMovementIdSequence", sequenceName = "WarehouseItemMovementIdSequence", allocationSize = 1)
	private Long id;

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
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was booked into Liferay. This is never {@code null} for better &
	 * easier indexability.
	 */
	private long booked;

	/**
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was finalized. This is never {@code null} for better & easier
	 * indexability.
	 */
	private long finalized;

	@ManyToOne
	@JoinColumn(name = "groupId")
	private WarehouseItemMovementGroupEntity group;

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
	 * References {@link SupplierEntity#getId() SupplierEntity.id} of the supplier,
	 * if {@link #getType() type} is {@link WarehouseItemMovementType#SUPPLY
	 * SUPPLY}. For other movement-types, this is {@code null}.
	 */
	private Long supplierId;

	private BigDecimal priceTotalNet;
	private BigDecimal priceTotalGross;
	private BigDecimal taxPercent;
}
