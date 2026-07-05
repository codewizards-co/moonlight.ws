package moonlight.ws.api.auth;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;

public enum Role {

	/**
	 * User having basic access to moonlight. When a user is automatically enlisted
	 * via OpenID, he does not have any role at all. Only when granted this role, he
	 * can at least use some of the functionality.
	 */
	BASIC(1L << 0),

	/**
	 * User moving stuff into / out of stock.
	 */
	LOGISTICIAN(1L << 1, BASIC),

	/**
	 * User charging invoices.
	 */
	ACCOUNTANT(1L << 2, BASIC),

	/**
	 * Administrator having all permissions to do everything.
	 */
	ADMIN(Role.BIT_MAX, LOGISTICIAN, ACCOUNTANT);

	/**
	 * The maximum bit-value is the bit at index 59. We thus support a maximum of 60
	 * roles (index 0...59) avoiding the quirks of the sign-bit at index 63 and
	 * keeping it at an easily human-understandable limit.
	 * <p>
	 * We most likely never need more than a handful of roles, hence the limit of 60
	 * is totally sufficient. If we ever need more, we should likely refactor this
	 * entirely.
	 */
	public static final long BIT_MAX = 1L << 59;

	/**
	 * The minimum bit-value is the bit at index 0.
	 */
	public static final long BIT_MIN = 1L;

	private final long bit;
	private Role[] _containedRoles;
	private Set<Role> containedRoles;
	private Set<Role> allContainedRoles;

	private static Map<Long, Role> bit2Role;

	private Role(long bit, Role... containedRoles) {
		if (bit > BIT_MAX || bit < BIT_MIN) {
			throw new IllegalArgumentException("bit=%d exceeds range!".formatted(bit));
		}
		if (Long.bitCount(bit) != 1) {
			throw new IllegalArgumentException(
					"bit=%d has bitCount=%d, but must be 1!".formatted(bit, Long.bitCount(bit)));
		}
		this.bit = bit;
		this._containedRoles = containedRoles == null ? new Role[0] : containedRoles;
	};

	private static synchronized Map<Long, Role> getBit2Role() {
		if (bit2Role == null) {
			Map<Long, Role> b2r = new HashMap<Long, Role>(Role.values().length);
			for (Role role : Role.values()) {
				if (b2r.put(role.getBit(), role) != null) {
					throw new IllegalStateException("Duplicate bit: " + role.getBit());
				}
			}
			bit2Role = b2r;
		}
		return bit2Role;
	}

	/**
	 * Gets a single role from a single bit-value. If there is no such role, an
	 * {@code IllegalArgumentException} is thrown.
	 *
	 * @param bit the {@linkplain Role#getBit() bit} of the role to be mapped.
	 *
	 * @throws IllegalArgumentException if the given {@code bit} cannot be mapped to
	 *                                  a {@link Role}.
	 */
	private static Role fromBit(long bit) { // making this private to avoid accidental use instead of fromBits(...)
		Role role = getBit2Role().get(bit);
		if (role != null) {
			return role;
		}
		if (bit > BIT_MAX || bit < BIT_MIN) {
			throw new IllegalArgumentException("bit=%d exceeds range!".formatted(bit));
		}
		if (Long.bitCount(bit) != 1) {
			throw new IllegalArgumentException(
					"bit=%d has bitCount=%d, but must be 1!".formatted(bit, Long.bitCount(bit)));
		}
		throw new IllegalArgumentException("There is no Role with bit=%d!".formatted(bit));
	}

	/**
	 * Gets a writable {@code Set} with the roles from the given {@code bits}.
	 *
	 * @param bits the bits encoding the roles.
	 *
	 * @see #toBits(Set)
	 */
	public static Set<Role> fromBits(long bits) {
		return Stream.iterate(bits, v -> v != 0, v -> v ^ Long.lowestOneBit(v)) //
				.map(Long::lowestOneBit) //
				.map(Role::fromBit) //
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
	}

	/**
	 * Gets a long with all {@linkplain Role#getBit() bits} (ORed) from all
	 * {@code roles}.
	 *
	 * @param roles the roles to be mapped to a bit-mask. Must not be {@code null},
	 *              but may be empty.
	 *
	 * @see #fromBits(long)
	 */
	public static long toBits(@NonNull Set<Role> roles) {
		long result = 0;
		for (Role role : roles) {
			result |= role.getBit();
		}
		return result;
	}

	/**
	 * Gets the single bit to be stored efficiently as a bit-mask in the database.
	 */
	public long getBit() {
		return bit;
	}

	/**
	 * Gets the roles <i>directly</i> contained in this role. A user having this
	 * role automatically has these contained roles and all the roles transitively
	 * contained.
	 * <p>
	 * Note: The result of this method does not contain this role. This method
	 * reflects what was declared and is not resolved.
	 * <p>
	 * Usually, you should use {@link #getAllContainedRoles()} instead, which
	 * contains the resolved effective set of roles.
	 *
	 * @see #getAllContainedRoles()
	 */
	public synchronized Set<Role> getContainedRoles() {
		if (containedRoles == null) {
			containedRoles = _containedRoles.length == 0 ? Collections.emptySet()
					: Collections.unmodifiableSet(EnumSet.of(_containedRoles[0], _containedRoles));
		}
		return containedRoles;
	}

	/**
	 * Gets all roles directly or indirectly (transitively) contained in this role.
	 * A user having this role automatically has all the roles returned by this
	 * method.
	 * <p>
	 * Note: The result of this method also contains this role.
	 *
	 * @see #getContainedRoles()
	 */
	public synchronized Set<Role> getAllContainedRoles() {
		if (allContainedRoles == null) {
			Set<Role> acr = EnumSet.of(this);
			for (Role role : this.getContainedRoles()) {
				acr.add(role);
				acr.addAll(role.getAllContainedRoles());
			}
			allContainedRoles = acr;
		}
		return allContainedRoles;
	}
}
