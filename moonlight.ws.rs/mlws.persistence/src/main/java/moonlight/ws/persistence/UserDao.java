package moonlight.ws.persistence;

import static java.util.Objects.*;

import java.util.HashMap;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.NonNull;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.api.UserConst;
import moonlight.ws.api.UserFilter;

@RequestScoped
public class UserDao extends AbstractDao<UserEntity> {

	@Inject
	private AuthInfo authInfo;

	@Transactional(value = TxType.REQUIRED, rollbackOn = Throwable.class)
	public UserEntity currentUser() {
		if (!authInfo.isAuthenticated()) {
			UserEntity entity = getEntity(UserConst.ANONYMOUS_USERID);
			return requireNonNull(entity, "anonymousUser"); // must exist due to UserInitializer!
		}

		UserEntity entity = getEntity(authInfo.getUsername());
		if (entity == null) {
			entity = new UserEntity();
			entity.setUsername(authInfo.getUsername());
			entity.setCreatedByUserId(UserConst.SYSTEM_USERID);
			entity.setChangedByUserId(UserConst.SYSTEM_USERID);
			persistEntity(entity);

			entity.setCreatedByUserId(requireNonNull(entity.getId(), "entity.id"));
			entity.setChangedByUserId(requireNonNull(entity.getId(), "entity.id"));
		}
		return entity;
	}

	public UserEntity getEntity(@NonNull String username) {
		UserFilter filter = new UserFilter();
		filter.setFilterUsername(username);
		SearchResult<UserEntity> searchResult = searchEntities(filter);
		List<UserEntity> entities = searchResult.getEntities();
		if (entities.size() == 1) {
			return entities.get(0);
		}
		if (entities.isEmpty()) {
			return null;
		}
		throw new IllegalStateException("Found %d entities, but should be exactly 0 or 1!".formatted(entities.size()));
	}

	public SearchResult<UserEntity> searchEntities(UserFilter filter) {
		var params = new HashMap<String, Object>();
		var jpqlCriteria = "";
		if (filter != null) {
			if (filter.getFilterUsername() != null) {
				jpqlCriteria += " and lower(e.username) like lower(:username)";
				params.put("username", prepareLikeCriterion(filter.getFilterUsername()));
			}

		}
		return searchEntities(jpqlCriteria, params, filter);
	}
}
