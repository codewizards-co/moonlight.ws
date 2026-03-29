package moonlight.ws.persistence;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResult<E extends AbstractEntity> {

	private List<E> entities;
	private long totalSize;

	public List<E> getEntities() {
		if (entities == null) {
			entities = new ArrayList<>();
		}
		return entities;
	}
}
