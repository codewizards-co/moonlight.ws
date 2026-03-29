package moonlight.ws.api;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import lombok.NonNull;

public abstract class CommaSeparatedList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 1L;

	protected static final String COMMA = ",";

	public CommaSeparatedList() {
	}

	public CommaSeparatedList(String string) {
		if (string == null) {
			return;
		}
		boolean lastTokenWasComma = false;
		StringTokenizer st = new StringTokenizer(string, COMMA, true);
		while (st.hasMoreTokens()) {
			String token = requireNonNull(st.nextToken(), "stringTokenizer.nextToken()");
			if (COMMA.equals(token)) {
				if (lastTokenWasComma) {
					this.add(null);
				}
				lastTokenWasComma = true;
				continue;
			}
			E element = deserializeElement(token);
			this.add(element);
			lastTokenWasComma = false;
		}
	}

	public CommaSeparatedList(@NonNull Collection<? extends E> elements) {
		super(elements);
	}

	protected abstract E deserializeElement(@NonNull String string);

	protected abstract String serializeElement(E element);

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (E element : this) {
			String string = serializeElement(element);
			if (string != null) {
				if (first) {
					first = false;
				} else {
					sb.append(COMMA);
				}
				sb.append(string);
			}
		}
		return sb.toString();
	}
}
