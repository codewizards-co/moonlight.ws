package moonlight.ws.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("")
public interface WelcomeRest {

    @GET
    @Produces(MediaType.TEXT_HTML)
    String getWelcomePage();

}
