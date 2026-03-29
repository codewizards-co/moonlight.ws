package moonlight.ws.api;

import java.util.Collection;

import lombok.NonNull;

public class CommaSeparatedListOfLong extends CommaSeparatedList<Long> {

	private static final long serialVersionUID = 1L;

	public CommaSeparatedListOfLong() {
	}

	public CommaSeparatedListOfLong(String string) {
		super(string);
	}

	public CommaSeparatedListOfLong(@NonNull Collection<? extends Long> elements) {
		super(elements);
	}

	@Override
	protected Long deserializeElement(@NonNull String string) {
		if (string.isEmpty()) {
			return null;
		}
		return Long.valueOf(string);
	}

	@Override
	protected String serializeElement(Long element) {
		return element == null ? "" : element.toString();
	}
}
