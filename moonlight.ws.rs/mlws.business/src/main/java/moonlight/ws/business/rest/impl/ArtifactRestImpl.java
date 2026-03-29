package moonlight.ws.business.rest.impl;

import static java.util.Objects.*;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import moonlight.ws.api.ArtifactDto;
import moonlight.ws.api.ArtifactRest;

@RequestScoped
public class ArtifactRestImpl implements ArtifactRest {

	@Override
	public ArtifactDto getArtifact(@NonNull String groupId, @NonNull String artifactId) throws Exception {
		String pomPropertiesPath = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
		URL pomPropertiesUrl = ArtifactRestImpl.class.getClassLoader().getResource(pomPropertiesPath);
		if (pomPropertiesUrl == null) {
			throw new NotFoundException();
		}
		Properties pomProperties = new Properties();
		try (InputStream in = pomPropertiesUrl.openStream()) {
			pomProperties.load(in);
		}
		return fromPomProperties(pomProperties);
	}

	protected ArtifactDto fromPomProperties(@NonNull Properties pomProperties) {
		ArtifactDto artifact = new ArtifactDto();

		String groupId = pomProperties.getProperty("groupId");
		artifact.setGroupId(requireNonNull(groupId, "pomProperties.groupId"));

		String artifactId = pomProperties.getProperty("artifactId");
		artifact.setArtifactId(requireNonNull(artifactId, "pomProperties.artifactId"));

		String version = pomProperties.getProperty("version");
		artifact.setVersion(requireNonNull(version, "pomProperties.version"));

		return artifact;
	}
}
