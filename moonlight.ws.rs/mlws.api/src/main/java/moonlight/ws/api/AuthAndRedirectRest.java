package moonlight.ws.api;

import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("auth-and-redirect")
public interface AuthAndRedirectRest {

	@GET
	@Path("{bearerTokenSha256}/{redirectUrl:.+}")
	@Encoded // the decoding-problem seems to originate from the nginx -- but we still use
				// manual en-/decoding
	Response performGet(@PathParam("bearerTokenSha256") String bearerTokenSha256,
			@PathParam("redirectUrl") String redirectUrl) throws Exception;
}
