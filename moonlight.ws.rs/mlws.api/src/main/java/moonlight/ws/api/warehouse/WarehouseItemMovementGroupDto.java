package moonlight.ws.api.warehouse;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseItemMovementGroupDto {

	private Long id;

	@JsonFormat(shape = Shape.STRING)
	private Instant created;

	private Long createdByUserId;

	@JsonFormat(shape = Shape.STRING)
	private Instant changed;

	private Long changedByUserId;

	private Boolean draft;

	/**
	 * Either {@code null} or the timestamp when it was finalized.
	 */
	@JsonFormat(shape = Shape.STRING)
	private Instant finalized;
}
