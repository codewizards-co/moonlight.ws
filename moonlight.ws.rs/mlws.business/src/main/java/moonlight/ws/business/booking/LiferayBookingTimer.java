package moonlight.ws.business.booking;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class LiferayBookingTimer {

	@Inject
	private RequestContextController requestContextController;

	@Inject
	private LiferayBookingWorker worker;

	private final AtomicBoolean busy = new AtomicBoolean(false);

	@Lock(LockType.WRITE)
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void onTimerEvent() {
		if (!busy.compareAndSet(false, true)) {
			log.info("onTimerEvent: still busy => quit immediately");
			return;
		}
		log.debug("onTimerEvent: entered");
		long startTimestamp = System.currentTimeMillis();
		try {
			boolean requestContextActivated = requestContextController.activate();
			try {
				worker.work();
			} finally {
				if (requestContextActivated) {
					requestContextController.deactivate();
				}
			}
		} catch (Throwable x) {
			log.error("onTimerEvent: " + x, x);
		} finally {
			busy.set(false);
			log.debug("onTimerEvent: took {} ms", System.currentTimeMillis() - startTimestamp);
		}
	}
}
