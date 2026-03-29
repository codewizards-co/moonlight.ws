package moonlight.ws.api.party;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import moonlight.ws.api.RestConst;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SupplierDto {

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
	private Instant deleted;
	private Long deletedByUserId;

	/**
	 * The related party. Must not be {@code null}. If the query-parameter
	 * {@link RestConst#QUERY_FETCH fetch} contained {@code party} during a
	 * GET-request, this is the fully loaded DTO, otherwise it is hollow and
	 * contains nothing but the {@link PartyDto#getId() id}.
	 */
	private PartyDto party;
}
