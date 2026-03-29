package moonlight.logistics.liferay;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;
import moonlight.ws.liferay.LiferayConfig;
import moonlight.ws.liferay.LiferayResourceFactory;

@ExtendWith(WeldJunit5Extension.class)
public class LiferayResourceFactoryTest {

	@WeldSetup
	public WeldInitiator weld = WeldInitiator //
			.from(LiferayResourceFactory.class) //
			.addBeans(createLiferayConfigBean()) //
			.activate(RequestScoped.class) //
			.build();

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	private Bean<?> createLiferayConfigBean() {
		return MockBean.builder().types(LiferayConfig.class).scope(ApplicationScoped.class)
				.create((creationalContext) -> {
					LiferayConfig liferayConfig = mock(LiferayConfig.class);
					when(liferayConfig.getUrl()).thenReturn("https://my-host.my-domain.com/liferay");
					when(liferayConfig.getUser()).thenReturn("my-daemon");
					when(liferayConfig.getPassword()).thenReturn("my-secret");
					return liferayConfig;
				}).build();
	}

	@Test
	void getWarehouseResource() throws Exception {
		WarehouseResource resource = liferayResourceFactory.getResource(WarehouseResource.class);
		assertThat(resource).isNotNull();
	}
}
