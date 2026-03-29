package moonlight.ws.persistence;

import static java.util.Objects.*;
import static moonlight.ws.base.util.ReflectionUtil.*;
import static moonlight.ws.base.util.SortUtil.*;
import static moonlight.ws.base.util.StringUtil.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.Filter;

@Transactional(TxType.MANDATORY)
@Slf4j
public abstract class AbstractDao<E extends AbstractEntity> {

	private final Class<E> entityClass;

	@SuppressWarnings("unchecked")
	public AbstractDao() {
		entityClass = (Class<E>) resolveActualTypeArguments(AbstractDao.class, this)[0];
	}

	@PersistenceContext(unitName = "MoonlightPU")
	protected EntityManager entityManager;

	public Class<E> getEntityClass() {
		return entityClass;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public String getEntityName() {
		Class<E> clazz = requireNonNull(getEntityClass(), "getEntityClass()");
		Entity annotation = clazz.getAnnotation(Entity.class);
		if (annotation == null) {
			throw new IllegalStateException("Entity-class %s lacks annotation '@Entity'!".formatted(clazz.getName()));
		}
		String name = annotation.name();
		if (isEmpty(name)) {
			return clazz.getName();
		}
		return name;
	}

	public E getEntity(@NonNull Long id) {
		return entityManager.find(getEntityClass(), id);
	}

	/**
	 * Gets the entities for the given {@code ids}. If there is no entity for an ID,
	 * it is silently excluded from the result-{@code Set}.
	 * <p>
	 * Even if you pass a {@code Collection} supporting duplicates (like
	 * {@link List}), the result-set contains every entity only exactly once.
	 *
	 * @param ids a collection of entity-IDs. Must not be {@code null}.
	 * @return a {@code Set} of entities. Never {@code null}. Sorted by {@code id}
	 *         (ascending, lowest first).
	 */
	public SortedSet<E> getEntities(@NonNull Collection<Long> ids) {
		// TODO implement more efficiently! We should do bulk-selects with 100 IDs per
		// query or so...
		Set<Long> idSet = ids instanceof Set ? (Set<Long>) ids : new HashSet<>(ids);
		SortedSet<E> result = new TreeSet<>();
		idSet.forEach(id -> {
			E entity = getEntity(id);
			if (entity != null) {
				result.add(entity);
			}
		});
		return result;
	}

	public void persistEntity(@NonNull E entity) {
		entityManager.persist(entity);
	}

	public E deleteEntity(@NonNull Long id) {
		E entity = getEntity(id);
		if (entity != null) {
			entityManager.remove(entity);
		}
		return entity;
	}

	public void lock(@NonNull E entity) {
		entityManager.refresh(entity, LockModeType.PESSIMISTIC_WRITE);
	}

	public void flush() {
		entityManager.flush();
	}

	protected SearchResult<E> searchEntities(@NonNull String jpqlCriteria, @NonNull HashMap<String, Object> jpqlParams,
			Filter filter) {
		if (filter == null) {
			filter = new Filter();
		}
		String entityName = getEntityName();
		SearchResult<E> searchResult = new SearchResult<>();

		String countJqpl = "select count(e) from %s e where 1 = 1 %s" //
				.formatted(entityName, jpqlCriteria);
		TypedQuery<Long> countQuery = entityManager.createQuery(countJqpl, Long.class);
		for (Map.Entry<String, Object> param : jpqlParams.entrySet()) {
			countQuery.setParameter(param.getKey(), param.getValue());
		}
		searchResult.setTotalSize(countQuery.getSingleResult());

		if (filter.getPageSizeOrDefault() > 0) { // no need to query, if the client wants the count only.
			String entityJqpl = "select e from %s e where 1 = 1 %s order by %s" //
					.formatted(entityName, jpqlCriteria, getOrderByColumns(filter));
			TypedQuery<E> entityQuery = entityManager.createQuery(entityJqpl, getEntityClass());
			for (Map.Entry<String, Object> param : jpqlParams.entrySet()) {
				entityQuery.setParameter(param.getKey(), param.getValue());
			}
			applyPagination(entityQuery, filter);
			searchResult.setEntities(entityQuery.getResultList());
		}
		return searchResult;
	}

	protected String getOrderByColumns(@NonNull Filter filter) {
		Map<String, Boolean> propName2Descending = getSortPropName2DescendingMap(filter);
		if (!propName2Descending.containsKey("id")) {
			// we *must* always sort in a stable way due to pagination, hence we add the
			// unique id at the end, if it isn't part of the sort-declaration, yet.
			propName2Descending.put("id", false);
		}
		return propName2Descending.entrySet().stream() //
				.map(me -> "e." + me.getKey() + (me.getValue() ? " desc" : " asc")) //
				.collect(Collectors.joining(", "));
	}

	private void applyPagination(@NonNull TypedQuery<?> entityQuery, @NonNull Filter filter) {
		entityQuery.setFirstResult((int) getFirstResult(filter)); // this is IMHO a bug in the JPA-API ;-)
		entityQuery.setMaxResults(filter.getPageSizeOrDefault());
	}

	protected long getFirstResult(@NonNull Filter filter) {
		return (filter.getPageNumberOrDefault() - 1) * filter.getPageSizeOrDefault();
	}

	protected static String prepareLikeCriterion(String criterion) {
		if (criterion == null) {
			return null;
		}
		RegexWithFlags rwf = getRegexWithFlagsIfRegex(criterion);
		if (rwf == null) { // criterion is not a regex => exact matching
			return criterion;
		}
		// very limited support -- but sufficient for now
		return rwf.regex.replaceAll(Pattern.quote(".*"), "%");
	}

	private static RegexWithFlags getRegexWithFlagsIfRegex(String criterion) {
		if (criterion == null || criterion.length() < 3) {
			return null;
		}
		if (!criterion.startsWith("/")) {
			return null;
		}
		String regex = criterion.substring(1);
		int lastSlashIndex = regex.lastIndexOf('/');
		if (lastSlashIndex < 0) {
			return null;
		}
		String flagsString = regex.substring(lastSlashIndex + 1);
		regex = regex.substring(0, lastSlashIndex);
		int flags = 0;
		for (int i = 0; i < flagsString.length(); ++i) {
			char flagChar = flagsString.charAt(i);
			switch (flagChar) {
				case 'i':
					flags |= Pattern.CASE_INSENSITIVE;
					break;
				case 'm':
					flags |= Pattern.MULTILINE;
					break;
				case 's':
					flags |= Pattern.DOTALL;
				default:
					throw new IllegalArgumentException("Unknown regex-flag: " + flagChar);
			}
		}
		return new RegexWithFlags(regex, flags);
	}

	private static record RegexWithFlags(String regex, int flags) {
	}
}
