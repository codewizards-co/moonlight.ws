package moonlight.ws.healthcheck.cli;

public class MoonlightWsHealthCheck {

	public static void main(String[] args) throws Exception {
		// TODO we should implement this:
		// do a GET on the $HEALTHCHECK_URL (e.g.
		// https://dragonkingchocolate.com/moonlight.ws.rs/health).
		// We should use the timeouts $HEALTHCHECK_CONNECT_TIMEOUT and
		// $HEALTHCHECK_READ_TIMEOUT when querying. Default should be 30000 and 60000
		// ms.
		// If it fails or _all_.healthy is not true, it should wait for
		// $HEALTHCHECK_ERROR_WAIT_BEFORE_RETRY (e.g. 120000 ms),
		// then try again. If it failed (or was not healthy) for
		// $HEALTHCHECK_ERROR_RETRY_COUNT (e.g. 4) times, then it
		// should send an email to $HEALTHCHECK_EMAIL_TO (e.g.
		// webmaster@dragonkingchocolate.com).
	}

	public MoonlightWsHealthCheck() {
	}

}
