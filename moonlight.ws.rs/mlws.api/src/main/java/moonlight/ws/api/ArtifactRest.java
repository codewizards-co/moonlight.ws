package moonlight.ws.api;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Path("artifact")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
public interface ArtifactRest {

	@GET
	@Path("{groupId}/{artifactId}")
	ArtifactDto getArtifact(@PathParam("groupId") String groupId, @PathParam("artifactId") String artifactId)
			throws Exception;
}
