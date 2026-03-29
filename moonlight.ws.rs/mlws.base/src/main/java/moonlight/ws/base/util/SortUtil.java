package moonlight.ws.base.util;

import static moonlight.ws.base.util.StringUtil.*;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;

@Slf4j
@UtilityClass
public class SortUtil {

	/**
	 * Gets the {@link Filter#getSort() sort}-clause from the filter and parses it
	 * into a map. The map's keys are the property-names and their values indicate
	 * whether the sorting is descending. {@code false} means ascending,
	 * {@code true} means descending.
	 *
	 * @param filter the filter or {@code null}.
	 * @return the map of properties with their sort-direction. Never {@code null},
	 *         but maybe empty.
	 */
	public static final Map<String, Boolean> getSortPropName2DescendingMap(Filter filter) {
		final var sort = filter == null ? null : trim(filter.getSort());
		final var propName2descending = new LinkedHashMap<String, Boolean>();
		if (!isEmpty(sort)) {
			for (String sortElement : sort.split("\\s*,\\s*")) {
				var propNameAndOrderArray = sortElement.split("\\s*:\\s*");
				var propName = propNameAndOrderArray[0];
				var order = propNameAndOrderArray.length > 1 ? propNameAndOrderArray[1] : null;
				boolean descending = isEmpty(order) ? false : order.equalsIgnoreCase("desc");
				if (!descending && !isEmpty(order) && !order.equalsIgnoreCase("asc")) {
					log.warn(
							"sort '{}' contains invalid order '{}' for property '{}'! The order must be 'asc', 'desc' or empty.",
							sort, order, propName);
				}
				if (propName2descending.containsKey(propName)) {
					log.warn("sort '{}' contains duplicate property '{}'! All but the first occurrence are ignored.",
							sort, propName);
				} else {
					propName2descending.put(propName, descending);
				}
			}
		}
		return propName2descending;
	}

}
