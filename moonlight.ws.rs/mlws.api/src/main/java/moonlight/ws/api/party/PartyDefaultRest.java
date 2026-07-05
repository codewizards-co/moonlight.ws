package moonlight.ws.api.party;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.NonNull;
import moonlight.ws.api.auth.RequiresAuthentication;
import moonlight.ws.api.auth.RequiresRole;
import moonlight.ws.api.auth.Role;

/**
 * RESTful API for managing {@linkplain PartyDto party-instances}.
 * <p>
 * A party is an individual or organisation taking part in a business
 * transaction or any other legal contract.
 * <p>
 * Important: A party has nothing to do with drinking beer or otherwise having
 * fun!
 */
@Path("party-default")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
@RequiresRole(Role.BASIC)
public interface PartyDefaultRest {

	@GET
	@Path("{id}")
	PartyDefaultDto getPartyDefault(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	PartyDefaultDtoPage getPartyDefaults() throws Exception;

	@PUT
	@Path("{id}")
	PartyDefaultDto updatePartyDefault(@NonNull @PathParam("id") Long id, @NonNull PartyDefaultDto dto)
			throws Exception;
}
