package moonlight.ws.business.auth;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.AuthInfo;
import moonlight.ws.api.auth.Role;
import moonlight.ws.persistence.auth.UserDao;
import moonlight.ws.persistence.auth.UserEntity;

@Slf4j
public class RequiresRoleFilter implements ContainerRequestFilter {

	private final Role requiredRole;
	private final AuthInfo authInfo;
	private final UserDao userDao;

	public RequiresRoleFilter(@NonNull Role requiredRole) {
		log.debug("RequiresRoleFilter created for requiredRole={}.", requiredRole);
		this.requiredRole = requiredRole;
		CDI<Object> cdi = CDI.current();
		authInfo = cdi.select(AuthInfo.class).get();
		userDao = cdi.select(UserDao.class).get();
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if (!authInfo.isAuthenticated()) {
			log.error("authInfo.isAuthenticated() returned false! Maybe @RequiresRole was used without @RequiresAuthentication?!");
			throw new ForbiddenException();
		}
		UserEntity user = userDao.currentUser();
		Set<Role> roles = Role.fromBits(user.getRoleBits());
		Set<Role> allContainedRoles = EnumSet.noneOf(Role.class);
		for (Role role : roles) {
			allContainedRoles.addAll(role.getAllContainedRoles());
		}
		if (!allContainedRoles.contains(requiredRole)) {
			log.error("User %s does not have the role %s! The user has the roles %s (resolved to all contained roles: %s).".formatted(user.getUsername(), requiredRole, roles, allContainedRoles));
			throw new ForbiddenException();
		}
	}
}
