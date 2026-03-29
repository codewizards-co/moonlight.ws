package moonlight.ws.base.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtil {

	public static final String jsonEscape(String json) {
		return json == null ? null : json.replaceAll("\\\"", "\\\\\"").replaceAll("\\n", "\\\\n");
	}

	public static final String jsonUnescape(String jsonEscaped) {
		// warning: never tested, never used this method, so far ;-)
		return jsonEscaped == null ? null : jsonEscaped.replaceAll("\\\\\"", "\\\"").replaceAll("\\\\n", "\\n");
	}
}
