package moonlight.ws.business.shorturl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class ShortUrlStaticMappingRegistry {

	private List<ShortUrlStaticMapping> shortUrlStaticMappings;

	@Inject
	@Any
	private Instance<ShortUrlStaticMapping> shortUrlStaticMappingInstance;

	public synchronized List<ShortUrlStaticMapping> getShortUrlStaticMappings() {
		if (shortUrlStaticMappings == null) {
			ArrayList<ShortUrlStaticMapping> l = new ArrayList<ShortUrlStaticMapping>();
			for (var susm : shortUrlStaticMappingInstance) {
				l.add(susm);
			}
			l.trimToSize();
			Collections.sort(l, (a, b) -> {
				var res = Integer.compare(a.getOrderHint(), b.getOrderHint());
				if (res != 0) {
					return res;
				}
				res = a.getClass().getName().compareTo(b.getClass().getName());
				return res;
			});
			this.shortUrlStaticMappings = Collections.unmodifiableList(l);
		}
		return shortUrlStaticMappings;
	}
}
