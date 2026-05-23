package moonlight.ws.base.util;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import moonlight.ws.api.liferay.LiferayDtoPage;

@UtilityClass
public class JsonUtil {

	// An ObjectMapper-instance is thread-safe after configuration.
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final String jsonEscape(String json) {
		return json == null ? null : json.replaceAll("\\\"", "\\\\\"").replaceAll("\\n", "\\\\n");
	}

	public static final String jsonUnescape(String jsonEscaped) {
		// warning: never tested, never used this method, so far ;-)
		return jsonEscaped == null ? null : jsonEscaped.replaceAll("\\\\\"", "\\\"").replaceAll("\\\\n", "\\n");
	}

	public static final <T> T jsonClone(T object) {
		if (object == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) object.getClass();
		return jsonClone(object, clazz);
	}

	public static final <I, O extends I> O jsonClone(I object,  @NonNull Class<O> type) {
		if (object == null) {
			return null;
		}
		try {
			String string = objectMapper.writeValueAsString(object);
			return objectMapper.readValue(string, type);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <I, O extends I> LiferayDtoPage<O> jsonClone(@NonNull LiferayDtoPage<I> inputPage,  @NonNull Class<O> outputItemClass) {
		LiferayDtoPage<O> outputPage = new LiferayDtoPage<O>();
		outputPage.setPageNumber(inputPage.getPageNumber());
		outputPage.setPageSize(inputPage.getPageSize());
		outputPage.setTotalSize(inputPage.getTotalSize());
		outputPage.setLastPageNumber(inputPage.getLastPageNumber());
		if (inputPage.getItems() != null) {
			List<O> outputItems = new ArrayList<O>(inputPage.getItems().size());
			for (I inputItem : inputPage.getItems()) {
				outputItems.add(jsonClone(inputItem, outputItemClass));
			}
			outputPage.setItems(outputItems);
		}
		outputPage.setActions(inputPage.getActions());
//		result.setFacets(inputPage.getFacets());
		return outputPage;
	}
}
