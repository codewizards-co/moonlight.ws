package moonlight.ws.api;

import java.util.Collection;

import lombok.NonNull;

public class CommaSeparatedListOfString extends CommaSeparatedList<String> {

	private static final long serialVersionUID = 1L;

	public CommaSeparatedListOfString() {
	}

	public CommaSeparatedListOfString(String string) {
		super(string);
	}

	public CommaSeparatedListOfString(@NonNull Collection<? extends String> elements) {
		super(elements);
	}

	@Override
	protected String deserializeElement(String string) {
		return string;
	}

	@Override
	protected String serializeElement(String element) {
		return element;
	}
}
