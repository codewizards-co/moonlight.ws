package moonlight.ws.business.auth;

import jakarta.enterprise.context.RequestScoped;
import lombok.NonNull;
import moonlight.ws.api.auth.Role;
import moonlight.ws.api.auth.UserDto;
import moonlight.ws.business.mapper.AbstractMapper;
import moonlight.ws.persistence.auth.UserEntity;

@RequestScoped
public class UserMapper extends AbstractMapper<UserEntity, UserDto> {

	@Override
	protected void copyPropertiesToEntity(@NonNull UserEntity entity, @NonNull UserDto dto) {
		// id, created*, changed*, deleted cannot be written by client!
		entity.setUsername(dto.getUsername());
		if (dto.getRoles() != null) {
			entity.setRoleBits(Role.toBits(dto.getRoles()));
		}
	}

	@Override
	protected void copyPropertiesToDto(@NonNull UserDto dto, @NonNull UserEntity entity) {
		dto.setId(entity.getId());
		dto.setUsername(entity.getUsername());
		dto.setChanged(entity.getChanged());
		dto.setChangedByUserId(entity.getChangedByUserId());
		dto.setCreated(entity.getCreated());
		dto.setCreatedByUserId(entity.getCreatedByUserId());
//		dto.setDeleted(instantFromMillis(entity.getDeleted()));
//		dto.setDeletedByUserId(entity.getDeletedByUserId());
		dto.setRoles(Role.fromBits(entity.getRoleBits()));
	}
}
