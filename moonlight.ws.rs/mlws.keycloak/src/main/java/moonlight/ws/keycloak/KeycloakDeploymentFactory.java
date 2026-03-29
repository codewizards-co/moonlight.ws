package moonlight.ws.keycloak;

import static moonlight.ws.base.util.StringUtil.*;

import java.util.Map;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;

import jakarta.ejb.Stateless;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

/**
 * Factory instantiating a configured {@link KeycloakDeployment} from the
 * configuration in the environment-variables.
 * <p>
 * See: <a href=
 * "https://www.keycloak.org/docs/latest/securing_apps/index.html#_java_adapter_config">OpenID
 * Connect / Java Adapter Config</a>
 *
 * @see OpenIdConfig
 */
@Stateless
@Default
public class KeycloakDeploymentFactory implements IKeycloakDeploymentFactory {

	@Inject
	private OpenIdConfig openIdConfig;

	public KeycloakDeploymentFactory() {
	}

	@Override
	public KeycloakDeployment getKeycloakDeployment() {
		AdapterConfig adapterConfig = new AdapterConfig();
		adapterConfig.setAuthServerUrl(openIdConfig.getUrl());
		adapterConfig.setRealm(openIdConfig.getRealm());
		adapterConfig.setResource(openIdConfig.getClientId());

		String clientSecret = openIdConfig.getClientSecret();
		if (!isEmpty(clientSecret)) {
			// https://docs.wildfly.org/31/Admin_Guide.html#Elytron_OIDC_Client
			adapterConfig.setCredentials(Map.of("secret", clientSecret));
		}
		KeycloakDeployment keycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig);
		return keycloakDeployment;
	}
}
