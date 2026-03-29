package moonlight.ws.business.mapper;

import static java.util.Objects.*;
import static moonlight.ws.base.util.ReflectionUtil.*;

import java.lang.reflect.Type;
import java.util.List;

import lombok.NonNull;
import moonlight.ws.persistence.AbstractEntity;

public abstract class AbstractMapper<E extends AbstractEntity, D> {

	protected final Class<E> entityClass;
	protected final Class<D> dtoClass;

	@SuppressWarnings("unchecked")
	public AbstractMapper() {
		Type[] actualTypeArguments = resolveActualTypeArguments(AbstractMapper.class, this);
		this.entityClass = requireNonNull((Class<E>) actualTypeArguments[0], "entityClass");
		this.dtoClass = requireNonNull((Class<D>) actualTypeArguments[1], "dtoClass");
	}

	protected D newDto() {
		try {
			return dtoClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	protected E newEntity() {
		try {
			return entityClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public D toDto(@NonNull E entity) {
		D dto = newDto();
		copyPropertiesToDto(dto, entity);
		return dto;
	}

	public E toEntity(@NonNull D dto, E entity) {
		if (entity == null) {
			entity = newEntity();
		} else {
			requireNonNull(entity.getId(), "entity.id");
		}
		copyPropertiesToEntity(entity, dto);
		return entity;
	}

	protected abstract void copyPropertiesToEntity(@NonNull E entity, @NonNull D dto);

	protected abstract void copyPropertiesToDto(@NonNull D dto, @NonNull E entity);

	public List<D> toDtos(List<E> entities) {
		requireNonNull(entities, "entities");
		return entities.stream().map(e -> toDto(e)).toList();
	}
}
