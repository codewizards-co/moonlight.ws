package moonlight.ws.base.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FetchUtil {

	public static final Set<String> getFetchSet(String fetch) {
		if (fetch == null) {
			return Collections.emptySet();
		}
		return Arrays.asList(fetch.trim().split("\\s*,\\s*")).stream().filter(elem -> !elem.isEmpty())
				.collect(Collectors.toSet());
	}

//	public static final Set<String> getFetchSet(Filter filter) {
//		return getFetchSet(filter == null ? null : filter.getFetch());
//	}
}
