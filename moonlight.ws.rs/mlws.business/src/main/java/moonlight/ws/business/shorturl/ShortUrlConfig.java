package moonlight.ws.business.shorturl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import moonlight.ws.api.shorturl.ShortUrlRest;
import moonlight.ws.base.AbstractConfig;
import moonlight.ws.base.MoonlightUiConfig;
import moonlight.ws.business.shorturl.impl.LiferayProductShortUrlStaticMapping;
import moonlight.ws.business.shorturl.impl.MoonlightWarehouseItemShortUrlStaticMapping;
import moonlight.ws.liferay.LiferayConfig;
import moonlight.ws.persistence.shorturl.ShortUrlDao;

/**
 * Configuration for short URLs (used for QR-code-links).
 * <p>
 * A QR-link is a QR-code containing a URL leading to one of our IT-services. So
 * far, there are the following short URLs:
 * <ul>
 * <li>Dynamic short URLs generated for individual URLs:
 * <ul>
 * <li>Short URL: <code>{{{@link #URL SHORTURL_URL}}}/r/{code}</code></li>
 * <li>Service URL:
 * <code>{{moonlight_ws_rs_url}}/short-url/redirect/{code}</code><br/>
 * (see {@link ShortUrlRest#redirect(String)})</li>
 * <li>Real URL: Read {@linkplain ShortUrlDao#getShortUrlByCode(String) from
 * database by code}.
 * </ul>
 * </li>
 * <li>{@linkplain ShortUrlStaticMapping Static short-URL-mappings} for usually
 * regex-based short-URLs:
 * <ul>
 * <li>Liferay-product-URLs (see {@link LiferayProductShortUrlStaticMapping}):
 * <ul>
 * <li>Short URL:
 * <code>{{{@link #URL SHORTURL_URL}}}/p/{productCode}</code></li>
 * <li>Real URL:
 * <code>{{{@link LiferayConfig#URL LIFERAY_URL}}}/web/dkc/p/{productCode}</code></li>
 * </ul>
 * </li>
 * <li>Warehouse-item-URLs (see
 * {@link MoonlightWarehouseItemShortUrlStaticMapping}):
 * <ul>
 * <li>Short URL:
 * <code>{{{@link #URL SHORTURL_URL}}}/warehouse-item/{id}</code></li>
 * <li>Real URL:
 * <code>{{{@link MoonlightUiConfig#URL MOONLIGHTUI_URL}}}/#/warehouse-item?id={id}</code></li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
@ApplicationScoped
@Default
public class ShortUrlConfig extends AbstractConfig {

	/**
	 * Base-URL used for short URLs (and thus QR-code-links). This must be a server
	 * understanding the links and redirecting to the correct URL.
	 */
	public static final String URL = "SHORTURL_URL";

	/**
	 * Gets the value for {@value #URL}.
	 *
	 * @return the value for {@value #URL}. Never {@code null}.
	 */
	public String getUrl() {
		return getValueOrFail(URL);
	}
}
