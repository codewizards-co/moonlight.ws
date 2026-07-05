package mlws.apimoonlight.ws.api.auth;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import moonlight.ws.api.auth.Role;

public class RoleTest {

	@Test
	void adminContainsAllRoles() {
		for (Role role : Role.values()) {
			assertThat(Role.ADMIN.getAllContainedRoles()).contains(role);
		}
	}

	@Test
	void fromBits_0() {
		Set<Role> roles = Role.fromBits(0);
		assertThat(roles).isNotNull();
		assertThat(roles).isEmpty();
	}

	@Test
	void toBits_admin() {
		assertThat(Role.ADMIN.getBit()).isEqualTo(576460752303423488L);
		assertThat(Role.toBits(Set.of(Role.ADMIN))).isEqualTo(576460752303423488L);
	}

	@Test
	void fromBits_admin() {
		assertThat(Role.fromBits(576460752303423488L)).isEqualTo(Set.of(Role.ADMIN));
	}

	@Test
	void toBits_BASIC() {
		assertThat(Role.toBits(Set.of(Role.BASIC))).isEqualTo(1L);
	}

	@Test
	void toBits_LOGISTICIAN() {
		assertThat(Role.toBits(Set.of(Role.LOGISTICIAN))).isEqualTo(2L);
	}

	@Test
	void toBits_ACCOUNTANT() {
		assertThat(Role.toBits(Set.of(Role.ACCOUNTANT))).isEqualTo(4L);
	}

	@Test
	void toBits_ACCOUNTANT_LOGISTICIAN() {
		assertThat(Role.toBits(Set.of(Role.ACCOUNTANT, Role.LOGISTICIAN))).isEqualTo(6L);
	}

	@Test
	void fromBits_ACCOUNTANT_LOGISTICIAN() {
		assertThat(Role.fromBits(6L)).isEqualTo(Set.of(Role.ACCOUNTANT, Role.LOGISTICIAN));
	}
}
