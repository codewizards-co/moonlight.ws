package moonlight.ws.keycloak;

import org.keycloak.adapters.KeycloakDeployment;

/**
 * Factory instantiating a configured {@link KeycloakDeployment} from the
 * JEE/web-server's configuration.
 * <p>
 * There is currently only one single implementation:
 * {@link KeycloakDeploymentFactory}
 */
public interface IKeycloakDeploymentFactory {

	/**
	 * Gets a completely configured and readily usable {@link KeycloakDeployment}
	 * instance.
	 *
	 * @return a completely configured and readily usable
	 *         {@link KeycloakDeployment}. Never {@code null}.
	 */
	KeycloakDeployment getKeycloakDeployment();
}
