package moonlight.ws.api.auth;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.NonNull;

/**
 * RESTful API for managing {@linkplain UserDto user-instances}.
 */
@Path("user")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequiresAuthentication
@RequiresRole(Role.BASIC)
public interface UserRest {

	@GET
	@Path("{id}")
	UserDto getUser(@NonNull @PathParam("id") Long id) throws Exception;

	@GET
	UserDtoPage getUsers(@BeanParam UserFilter filter) throws Exception;

	@POST
	@RequiresRole(Role.ADMIN)
	UserDto createUser(@NonNull UserDto dto) throws Exception;

	@PUT
	@Path("{id}")
	@RequiresRole(Role.ADMIN)
	UserDto updateUser(@NonNull @PathParam("id") Long id, @NonNull UserDto dto) throws Exception;

//	@DELETE // TODO should we implement this at all?!??
//	@Path("{id}")
//	@RequiresRole(Role.ADMIN)
//	void deleteUser(@NonNull @PathParam("id") Long id) throws Exception;
}
