package moonlight.ws.api.party;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moonlight.ws.api.RestConst;

/**
 * A party is an individual or organisation taking part in a business
 * transaction or any other legal contract.
 * <p>
 * Important: A party has nothing to do with drinking beer or otherwise having
 * fun!
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class PartyDto {

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

	private Boolean active;

	private String code;
	private String name;

	private String email;
	private String phone;

	private String street1;
	private String street2;
	private String street3;
	private String zip;
	private String city;
	private String regionCode;
	private String countryIsoCode;

	private String taxNo;

	private String website;

	private String description;

	/**
	 * The consignee-relations of this party, if the client passed
	 * {@code consignees} in its {@link RestConst#QUERY_FETCH fetch}. Otherwise,
	 * this is {@code null}. If {@code fetch} contains {@code consignees}, but there
	 * is none existing for this party, this is an empty list.
	 */
	private Set<ConsigneeDto> consignees;

	/**
	 * The supplier-relation of this party, if there is one assigned to this party.
	 * If {@link RestConst#QUERY_FETCH fetch} contained {@code supplier}, it is
	 * fully loaded. Otherwise it is a hollow object containing solely the
	 * {@link SupplierDto#getId() id}.
	 * <p>
	 * If there is no supplier-instance associated with this party, this property is
	 * {@code null}, meaning that this party is not a supplier.
	 */
	private SupplierDto supplier;

	public PartyDto(Long id) {
		this.id = id;
	}
}
