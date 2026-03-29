package moonlight.ws.liferay;

import static moonlight.ws.liferay.ProblemExceptionMapperUtil.*;

import com.liferay.headless.commerce.admin.shipment.client.problem.Problem;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class InventoryProblemExceptionMapper implements ExceptionMapper<Problem.ProblemException> {

	@Override
	public Response toResponse(Problem.ProblemException exception) {
		log.error(exception.toString(), exception);
		Problem problem = exception.getProblem();
		return problemToResponse(problem == null ? null : problem.getStatus(), exception.getClass().getName(),
				exception.getMessage());
	}
}
