package moonlight.ws.api.party;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.NonNull;
import moonlight.ws.api.RequiresAuthentication;

/**
 * RESTful API for managing {@linkplain PartyDto party-instances}.
 * <p>
 * A party is an individual or organisation taking part in a business
 * transaction or any other legal contract.
 * <p>
 * Important: A party has nothing to do with drinking beer or otherwise having
 * fun!
 */
@Path("party")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface PartyRest {

	@GET
	@Path("{id}")
	PartyDto getParty(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	PartyDtoPage getParties(@BeanParam PartyFilter filter) throws Exception;

	@POST
	PartyDto createParty(@NonNull PartyDto dto) throws Exception;

	@PUT
	@Path("{id}")
	PartyDto updateParty(@NonNull @PathParam("id") Long id, @NonNull PartyDto dto) throws Exception;

	@DELETE
	@Path("{id}")
	void deleteParty(@NonNull @PathParam("id") Long id) throws Exception;
}
