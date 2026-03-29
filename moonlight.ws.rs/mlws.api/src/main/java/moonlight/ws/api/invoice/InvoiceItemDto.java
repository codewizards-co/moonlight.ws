package moonlight.ws.api.invoice;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.liferay.headless.commerce.admin.order.client.dto.v1_0.OrderItem;

import lombok.Getter;
import lombok.Setter;
import moonlight.ws.api.PriceDto;
import moonlight.ws.api.warehouse.WarehouseItemMovementDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class InvoiceItemDto {

	private Long id;

	@JsonFormat(shape = Shape.STRING)
	private Instant created;

	private Long createdByUserId;

	@JsonFormat(shape = Shape.STRING)
	private Instant changed;

	private Long changedByUserId;

	/**
	 * Either {@code null} or the timestamp when it was deleted.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant deleted;
	private Long deletedByUserId;

	private Boolean draft;

	/**
	 * Either {@code null} or the timestamp when it was finalized.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant finalized;

	private Long finalizedByUserId;

	private InvoiceDto invoice;

	private WarehouseItemMovementDto warehouseItemMovement;

	private InvoiceInclude include;

	/**
	 * The quantity being charged. It is positive, if we charge to the other party
	 * and it is negative, if we refund an item.
	 * <p>
	 * In a normal sale to a customer, both the quantity and the
	 * {@link #getPriceTotalGross() priceTotalGross} are positive.
	 */
	private BigDecimal quantity;

	private PriceDto price;

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
