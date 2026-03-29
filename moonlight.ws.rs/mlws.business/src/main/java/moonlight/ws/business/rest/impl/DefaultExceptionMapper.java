package moonlight.ws.business.rest.impl;

import static moonlight.ws.base.util.StringUtil.*;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.ErrorDto;

@Provider
@Slf4j
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable exception) {
		log.error(exception.toString(), exception);
		if (exception instanceof WebApplicationException wae) {
			Response response = wae.getResponse();
			if (response.getEntity() != null) {
				return response;
			}
			return Response.fromResponse(response).entity(new ErrorDto( //
					response.getStatus(), //
					exception.getClass().getName(), //
					isEmpty(exception.getMessage()) //
							? response.getStatusInfo().getReasonPhrase() //
							: exception.getMessage() //
			)) //
					.build();
		}
		return createResponse(Status.INTERNAL_SERVER_ERROR, exception.getClass().getName(), exception.getMessage());
	}

	private static Response createResponse(StatusType status, String exceptionClassName, String exceptionMessage) {
		return Response.status(status)
				.entity(new ErrorDto(status.getStatusCode(), exceptionClassName, exceptionMessage)).build();
	}
}
