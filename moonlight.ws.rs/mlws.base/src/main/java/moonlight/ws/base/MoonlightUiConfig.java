package moonlight.ws.base;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

/**
 * Configuration for <b>moonlight.ws.ui</b>.
 */
@ApplicationScoped
@Default
public class MoonlightUiConfig extends AbstractConfig {

	/**
	 * Base-URL used for <b>moonlight.ws.ui</b>.
	 * <p>
	 * Example: {@code https://dragonkingchocolate.com/moonlight.ws.ui}
	 */
	public static final String URL = "MOONLIGHTUI_URL";

	/**
	 * Gets the value for {@value #URL}.
	 *
	 * @return the value for {@value #URL}. Never {@code null}.
	 */
	public String getUrl() {
		return getValueOrFail(URL);
	}
}
