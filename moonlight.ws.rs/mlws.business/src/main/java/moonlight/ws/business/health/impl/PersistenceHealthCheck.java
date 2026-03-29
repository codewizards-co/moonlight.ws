package moonlight.ws.business.health.impl;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.UserConst;
import moonlight.ws.base.health.HealthCheck;
import moonlight.ws.base.health.HealthStatus;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.shorturl.ShortUrlDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementDao;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class PersistenceHealthCheck implements HealthCheck {

	public static final String NAME = "persistence";

	public static final String MALADY_CODE_NO_USER_FOUND = "NO_USER_FOUND";
	public static final String MALADY_CODE_SYSTEM_USER_NOT_FOUND = "SYSTEM_USER_NOT_FOUND";
	public static final String MALADY_CODE_ANONYMOUS_USER_NOT_FOUND = "ANONYMOUS_USER_NOT_FOUND";

	@Inject
	private UserDao userDao;

	@Inject
	private ShortUrlDao shortUrlDao;

	@Inject
	private WarehouseItemMovementDao warehouseItemMovementDao;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public HealthStatus check() throws Exception {
		log.info("check: entered.");
		final long startTimestamp = System.currentTimeMillis();
		var userSearchResult = userDao.searchEntities(null);
		if (userSearchResult.getEntities().isEmpty()) {
			return new HealthStatus(NAME, MALADY_CODE_NO_USER_FOUND, "Not a single user found!");
		}
		if (userDao.getEntity(UserConst.SYSTEM_USERNAME) == null) {
			return new HealthStatus(NAME, MALADY_CODE_SYSTEM_USER_NOT_FOUND,
					"User '%s' not found!".formatted(UserConst.SYSTEM_USERNAME));
		}
		if (userDao.getEntity(UserConst.ANONYMOUS_USERNAME) == null) {
			return new HealthStatus(NAME, MALADY_CODE_ANONYMOUS_USER_NOT_FOUND,
					"User '%s' not found!".formatted(UserConst.ANONYMOUS_USERNAME));
		}

		shortUrlDao.searchEntities(null);
		warehouseItemMovementDao.searchEntities(null);

		log.info("check: done in {} ms.", System.currentTimeMillis() - startTimestamp);
		return null;
	}
}
