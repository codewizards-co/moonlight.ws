package moonlight.ws.business.rest.impl.warehouse;

import static java.util.Objects.*;
import static moonlight.ws.base.util.StringUtil.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseItemResource;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;
import lombok.NonNull;
import moonlight.ws.api.ErrorCorrectionLevel;
import moonlight.ws.api.warehouse.WarehouseItemLabelDto;
import moonlight.ws.api.warehouse.WarehouseItemLabelFilter;
import moonlight.ws.api.warehouse.WarehouseItemLabelRest;
import moonlight.ws.base.util.IOUtil;
import moonlight.ws.business.rest.impl.liferay.SkuCache;
import moonlight.ws.business.shorturl.ShortUrlConfig;
import moonlight.ws.liferay.LiferayResourceFactory;

@RequestScoped
public class WarehouseItemLabelRestImpl implements WarehouseItemLabelRest {

	@Inject
	private UriInfo uriInfo;

	@Inject
	private ShortUrlConfig shortUrlConfig;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Inject
	private SkuCache skuCache;

	private WarehouseResource warehouseResource;
	private WarehouseItemResource warehouseItemResource;
	private Locale locale;
	private Map<Long, Warehouse> warehouseId2warehouse;

	@Override
	public String getWarehouseItemLabelHtml(WarehouseItemLabelFilter filter) throws Exception {
		if (!isEmpty(filter.getLocale())) {
			String filterLocale = filter.getLocale();
			for (Locale l : Locale.getAvailableLocales()) {
				if (filterLocale.equalsIgnoreCase(l.toString())) {
					locale = l;
					break;
				}
			}
		}
		if (locale == null) {
			locale = Locale.US; // TODO check browser-preferences in some HTTP-header?!?!!!
		}

		List<Long> warehouseItemIds = filter.getWarehouseItemIds().stream().filter(id -> id != null).toList();
		if (warehouseItemIds.isEmpty()) {
			throw new BadRequestException("warehouseItemIds is empty!");
		}
		initLiferayResources();

		List<WarehouseItemLabelDto> warehouseItemLabels = warehouseItemIds.parallelStream() //
				.map(this::getWarehouseItem) //
				.map(this::toWarehouseItemLabel) //
				.toList();

		Set<Long> warehouseIds = warehouseItemLabels.stream() //
				.map(wil -> wil.getWarehouseItem().getWarehouseId()) //
				.filter(id -> id != null) //
				.collect(Collectors.toSet());

		warehouseId2warehouse = warehouseIds.parallelStream().map(this::getWarehouse)
				.collect(Collectors.toMap(Warehouse::getId, w -> w));

		StringBuilder html = new StringBuilder();
		html.append("<html><body>");

		html.append("<hr/>");

		for (var warehouseItemLabel : warehouseItemLabels) {
			html.append(toHtml(warehouseItemLabel));
			html.append("<hr/>");
		}

		html.append("</body></html>");

		return html.toString();
	}

	private String toHtml(@NonNull WarehouseItemLabelDto warehouseItemLabel) {
		String qrContent = shortUrlConfig.getUrl();
		if (!qrContent.endsWith("/")) {
			qrContent += "/";
		}
		WarehouseItem warehouseItem = warehouseItemLabel.getWarehouseItem();
		qrContent += "warehouse-item/" + warehouseItem.getId();
		qrContent = URLEncoder.encode(qrContent, StandardCharsets.UTF_8);

		String qrCodeUrl = uriInfo.getBaseUriBuilder().path("qr-code").path("{qrContent}").buildFromEncoded(qrContent)
				.toString() + "?errorCorrectionLevel=" + ErrorCorrectionLevel.H;

		Sku sku0 = warehouseItemLabel.getSkus().isEmpty() ? null : warehouseItemLabel.getSkus().get(0);

		String productNames = warehouseItemLabel.getSkus().stream().map(sku -> getL10n(sku.getProductName()))
				.collect(Collectors.joining(", "));

		Warehouse warehouse = requireNonNull(warehouseId2warehouse.get(warehouseItem.getWarehouseId()),
				"warehouseId2warehouse.get(%d)".formatted(warehouseItem.getWarehouseId()));

		Map<String, Object> variables = Map.of( //
				"warehouseName", getL10n(warehouse.getName()), //
				"warehouseCountryISOCode", warehouse.getCountryISOCode(), //
				"warehouseRegionISOCode", warehouse.getRegionISOCode(), //
				"warehouseCity", warehouse.getCity(), //
				"sku", warehouseItem.getSku(), //
				"productName0", sku0 == null ? "" : getL10n(sku0.getProductName()), //
				"productNames", productNames, //
				"qrCodeUrl", qrCodeUrl //
		);

		String template = """
				<table>
					<tr>
						<td>
							<img src="${qrCodeUrl}" />
						</td>
						<td style="font-size: 6mm;">
							<b style="font-size: 10mm; margin-right: 0.5em;">${sku}</b> ${warehouseName} (${warehouseCountryISOCode}, ${warehouseCity})<br/>
							${productNames}
						</td>
					</tr>
				</table>
				""";
		return IOUtil.replaceTemplateVariables(template, variables);
	}

	protected Warehouse getWarehouse(@NonNull Long id) {
		try {
			return requireNonNull(warehouseResource.getWarehouseId(id),
					"warehouseResource.getWarehouseId(%d)".formatted(id));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected WarehouseItem getWarehouseItem(@NonNull Long id) {
		try {
			return requireNonNull(warehouseItemResource.getWarehouseItem(id),
					"warehouseItemResource.getWarehouseItem(%d)".formatted(id));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected WarehouseItemLabelDto toWarehouseItemLabel(@NonNull WarehouseItem warehouseItem) {
		try {
			WarehouseItemLabelDto warehouseItemLabel = new WarehouseItemLabelDto();
			warehouseItemLabel.setWarehouseItem(warehouseItem);
			String wisku = warehouseItem.getSku();
			if (!isEmpty(wisku)) {
				List<Sku> skus = skuCache.getSkus().stream().filter(sku -> wisku.equals(sku.getSku())).toList();
				warehouseItemLabel.setSkus(skus);
			}
			return warehouseItemLabel;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void initLiferayResources() {
		warehouseResource = liferayResourceFactory.getResource(WarehouseResource.class);
		warehouseItemResource = liferayResourceFactory.getResource(WarehouseItemResource.class);
	}

	protected String getL10n(Map<String, String> i18n) {
		// TODO find out preferred language!
		if (i18n == null || i18n.isEmpty()) {
			return "";
		}
		String value = i18n.get(locale.toString());
		if (value == null) {
			value = i18n.get(Locale.US.toString());
		}
		if (value == null) {
			value = i18n.values().iterator().next().toString();
		}
		return value;
	}
}
