package moonlight.ws.api.shorturl;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import moonlight.ws.api.RequiresAuthentication;

@Path("short-url")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface ShortUrlRest {

	@GET
	ShortUrlDtoPage getShortUrls(@BeanParam ShortUrlFilter filter);

	@GET
	@Path("{id}")
	@RequiresAuthentication
	ShortUrlDto getShortUrl(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	@Path("by-url/{url:.+}")
	@RequiresAuthentication
	ShortUrlDto getShortUrlByUrl(@NonNull @Encoded @PathParam("url") String url) throws Exception;

	@GET
	@Path("redirect/{code}")
	Response redirect(@NonNull @PathParam("code") String code) throws Exception;

	@POST
	@RequiresAuthentication
	ShortUrlDto createShortUrl(@NonNull ShortUrlDto shortUrl);

	@PUT
	@Path("{id}")
	@RequiresAuthentication
	ShortUrlDto updateShortUrl(@NonNull @PathParam("id") Long id, ShortUrlDto shortUrl);

	@DELETE
	@Path("{id}")
	@RequiresAuthentication
	void deleteShortUrl(@NonNull @PathParam("id") Long id);
}
