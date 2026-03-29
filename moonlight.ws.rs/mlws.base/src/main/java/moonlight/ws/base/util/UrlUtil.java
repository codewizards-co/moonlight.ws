package moonlight.ws.base.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlUtil {

	public static final String concatUrlSegments(Object... path) {
		String result = "";
		for (Object p : path) {
			if (p == null || (p instanceof String s && s.isEmpty())) {
				// ignore empty strings, null
			} else if (result.endsWith("/")
					|| (p instanceof String s && (s.startsWith("/") || s.startsWith("?") || s.startsWith("&")))) {
				result += p;
			} else {
				// determine, whether it's a path-segment or a query-param
				if (result.indexOf("?") >= 0) {
					// we are in the query-part
					if (result.endsWith("?")) {
						result += p;
					} else {
						result += "&" + p;
					}
				} else { // path-segment
					if (result.isEmpty()) {
						result += p;
					} else {
						result += "/" + p;
					}
				}
			}
		}
		return result;
	}

	public static String workaroundFixBrokenUrl(final String url) {
//		if (url == null) {
//			return null;
//		}
//		if (url.startsWith("https:/") && !url.startsWith("https://")) {
//			String fixedUrl = "https://" + url.substring("https:/".length());
//			logger.warn("workaround: fixed url: {} => {}", url, fixedUrl);
//			return fixedUrl;
//		}
//		if (url.startsWith("http:/") && !url.startsWith("http://")) {
//			String fixedUrl = "http://" + url.substring("http:/".length());
//			logger.warn("workaround: fixed url: {} => {}", url, fixedUrl);
//			return fixedUrl;
//		}
//		if (url.startsWith("https%3A%2F%2F") || url.startsWith("http%3A%2F%2F") //
//				|| url.startsWith("qr-code%2F") || url.startsWith("warehouse-item-label%3F")) {
//			return URLDecoder.decode(url, StandardCharsets.UTF_8);
//		}
		return url;
	}

}
