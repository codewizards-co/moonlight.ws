package moonlight.ws.api.party;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;

import lombok.Getter;
import lombok.Setter;
import moonlight.ws.api.RestConst;

/**
 * A consignee is a merchant maintaining a
 * <a href="https://en.wikipedia.org/wiki/Consignment">consignment stock</a> and
 * selling on commission-base. Depending on language and country, this is the
 * same as or very similar to a commission-merchant.
 * <p>
 * Important: This consignee has not much to do with the receiver of a shipment!
 * <p>
 * The existance of an instance of {@code Consignee} makes the related
 * {@code Party} a consignee (a.k.a. commission-merchant). The corresponding
 * consignor is the company running moonlight.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ConsigneeDto {

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

	/**
	 * References the related {@linkplain Warehouse warehouse} via its
	 * {@link Warehouse#getId() ID}. Must not be {@code null}.
	 */
	private Long warehouseId;
}
