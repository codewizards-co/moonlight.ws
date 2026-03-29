package moonlight.ws.persistence.invoice;

import java.math.BigDecimal;

import com.liferay.headless.commerce.admin.order.client.dto.v1_0.OrderItem;

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
import moonlight.ws.api.invoice.InvoiceInclude;
import moonlight.ws.persistence.AbstractEntity;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;

@Getter
@Setter
@Entity(name = "InvoiceItem")
public class InvoiceItemEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "InvoiceItemIdSequence")
	@SequenceGenerator(name = "InvoiceItemIdSequence", sequenceName = "InvoiceItemIdSequence", allocationSize = 1)
	private Long id;

	/**
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was deleted. This is never {@code null} for better & easier
	 * indexability.
	 */
	private long deleted;
	private Long deletedByUserId;

	/**
	 * Either {@code 0} or the {@linkplain System#currentTimeMillis() timestamp}
	 * when it was finalized. This is never {@code null} for better & easier
	 * indexability.
	 */
	private long finalized;

	private Long finalizedByUserId;

	@ManyToOne
	@JoinColumn(name = "invoiceId")
	private InvoiceEntity invoice;

	@ManyToOne
	@JoinColumn(name = "warehouseItemMovementId")
	private WarehouseItemMovementEntity warehouseItemMovement;

	private InvoiceInclude include;

	/**
	 * The quantity being charged. It is positive, if we charge to the other party
	 * and it is negative, if we refund an item.
	 * <p>
	 * In a normal sale to a customer, both the quantity and the
	 * {@link #getPriceTotalGross() priceTotalGross} are positive.
	 */
	private BigDecimal quantity;

	private BigDecimal priceTotalNet;
	private BigDecimal priceTotalGross;
	private BigDecimal taxPercent;

// TODO we need to introduce manual items, too.
//	/**
//	 * If there is no {@link #getWarehouseItemMovement() warehouseItemMovement},
//	 * i.e. this is a manual position, this is the manual name. This property has no
//	 * effect on invoice-items referencing a warehouse-item-movement.
//	 */
//	private String name;

	/**
	 * References {@link OrderItem#getId() OrderItem.id} in Liferay.
	 * <p>
	 * Important: Multiple instances of {@code InvoiceItemEntity} may reference the
	 * same Liferay-order-item, because they may be summed up.
	 */
	private Long orderItemId;

	/**
	 * References {@link ShipmentItem#getId() ShipmentItem.id} in Liferay.
	 * <p>
	 * Important: Multiple instances of {@code InvoiceItemEntity} may reference the
	 * same Liferay-order-item and thus also the same shipment-item, because they
	 * may be summed up.
	 */
	private Long shipmentItemId;
}
