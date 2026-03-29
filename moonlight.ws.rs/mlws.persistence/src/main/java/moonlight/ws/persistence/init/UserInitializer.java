package moonlight.ws.persistence.init;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.NonNull;
import moonlight.ws.api.UserConst;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;

@RequestScoped
public class UserInitializer {

	@Inject
	private UserDao userDao;

	@Transactional(value = TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
	public void run() {
		UserEntity user = userDao.getEntity(UserConst.SYSTEM_USERNAME);
		if (user == null) {
			user = createUser(UserConst.SYSTEM_USERNAME);
//			user.setId(UserConst.SYSTEM_USERID); // this doesn't work, because Hibernate sucks (rofl) -- but fortunately, it isn't necessary.
			userDao.persistEntity(user);
			if (!UserConst.SYSTEM_USERID.equals(user.getId())) {
				throw new IllegalStateException(
						"systemUser.id != %d, but: %d".formatted(UserConst.SYSTEM_USERID, user.getId()));
			}
		}

		user = userDao.getEntity(UserConst.ANONYMOUS_USERNAME);
		if (user == null) {
			user = createUser(UserConst.ANONYMOUS_USERNAME);
//			user.setId(UserConst.ANONYMOUS_USERID);
			userDao.persistEntity(user);
			if (!UserConst.ANONYMOUS_USERID.equals(user.getId())) {
				throw new IllegalStateException(
						"anoymousUser.id != %d, but: %d".formatted(UserConst.ANONYMOUS_USERID, user.getId()));
			}
		}
	}

	private UserEntity createUser(@NonNull String username) {
		var entity = new UserEntity();
		entity.setUsername(username);
		entity.setChangedByUserId(UserConst.SYSTEM_USERID);
		entity.setCreatedByUserId(UserConst.SYSTEM_USERID);
		return entity;
	}
}
