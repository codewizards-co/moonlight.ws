package moonlight.ws.api;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("health")
public interface HealthRest {

	@GET
	@Produces(TEXT_PLAIN)
	String getHealth();
}
