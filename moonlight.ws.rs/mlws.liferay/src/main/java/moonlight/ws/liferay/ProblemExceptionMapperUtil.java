package moonlight.ws.liferay;

import static moonlight.ws.base.util.StringUtil.*;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;
import lombok.experimental.UtilityClass;
import moonlight.ws.api.ErrorDto;

@UtilityClass
public class ProblemExceptionMapperUtil {

	/**
	 * Handles the {@code Problem.status}.
	 *
	 * @param status             the {@code Problem.status}. May be {@code null}.
	 * @param exceptionClassName the exception-class-name. May be {@code null}.
	 * @param exceptionMessage   the exception-message. May be {@code null}.
	 */
	public static final Response problemToResponse(String status, String exceptionClassName, String exceptionMessage) {
		if ("NOT_FOUND".equals(status)) {
			return createResponse(Status.NOT_FOUND, exceptionClassName, exceptionMessage);
		}
		return createResponse(Status.INTERNAL_SERVER_ERROR, exceptionClassName, exceptionMessage);
	}

	private static Response createResponse(StatusType status, String exceptionClassName, String exceptionMessage) {
		return Response.status(status).entity(new ErrorDto(status.getStatusCode(), exceptionClassName,
				isEmpty(exceptionMessage) ? status.getReasonPhrase() : exceptionMessage)).build();
	}
}
