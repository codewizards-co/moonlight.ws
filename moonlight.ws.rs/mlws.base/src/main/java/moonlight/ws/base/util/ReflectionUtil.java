package moonlight.ws.base.util;

import static java.util.Objects.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtil {

	/**
	 * Resolves the actual type arguments of a base-class declared in a concrete
	 * sub-class.
	 * <p>
	 * This is a convenience method delegating to
	 * {@link #resolveActualTypeArguments(Class, Class)} passing
	 * {@code concreteObject.getClass()} as {@code concreteClass}.
	 * <p>
	 * Copied from:
	 * <a href="https://github.com/cloudstore/cloudstore">cloudstore</a>, class
	 * {@code co.codewizards.cloudstore.core.util.ReflectionUtil}.
	 *
	 * @param baseClass      the base class. Must not be <code>null</code>.
	 * @param concreteObject an instance of a sub-class of the generic
	 *                       {@code baseClass}.
	 * @return the resolved type arguments. Never <code>null</code> (empty array for
	 *         a non-generic base-class).
	 */
	public static final <T> Type[] resolveActualTypeArguments(final Class<T> baseClass, final T concreteObject) {
		requireNonNull(baseClass, "baseClass");
		requireNonNull(concreteObject, "concreteObject");
		@SuppressWarnings("unchecked")
		final Class<? extends T> concreteClass = (Class<? extends T>) concreteObject.getClass();
		return resolveActualTypeArguments(baseClass, concreteClass);
	}

	/**
	 * Resolves the actual type arguments of a base-class declared in a concrete
	 * sub-class.
	 * <p>
	 * The length as well as the order of the resolved type arguments matches the
	 * declaration order in the base-class. If a type argument could successfully be
	 * resolved, it is usually an instance of {@link Class}. If it could not be
	 * resolved (because the sub-class does not specify the generic type info -
	 * directly or indirectly), it is an instance of {@link TypeVariable}.
	 * <p>
	 * A typical use-case is this:
	 *
	 * <pre>
	 * public abstract class MyBase&lt;A, B, C&gt; {
	 * 	final Class&lt;A&gt; actualTypeArgumentA;
	 * 	final Class&lt;B&gt; actualTypeArgumentB;
	 * 	final Class&lt;C&gt; actualTypeArgumentC;
	 *
	 * 	public MyBase() {
	 * 		final Type[] actualTypeArguments = resolveActualTypeArguments(MyBase.class, this);
	 *
	 * 		// The following assignments fail - of course -, if the concrete class
	 * 		// lacks
	 * 		// generic type info - like the example class "MyFail" below.
	 * 		actualTypeArgumentA = (Class&lt;A&gt;) actualTypeArguments[0];
	 * 		actualTypeArgumentB = (Class&lt;B&gt;) actualTypeArguments[1];
	 * 		actualTypeArgumentC = (Class&lt;C&gt;) actualTypeArguments[2];
	 * 	}
	 * }
	 *
	 * public class MyConcrete extends MyBase&lt;Long, Boolean, String&gt; {
	 * }
	 *
	 * public class MyFail extends MyBase {
	 * }
	 * </pre>
	 * <p>
	 * Copied from:
	 * <a href="https://github.com/cloudstore/cloudstore">cloudstore</a>, class
	 * {@code co.codewizards.cloudstore.core.util.ReflectionUtil}.
	 *
	 * @param baseClass     the base class. Must not be <code>null</code>.
	 * @param concreteClass a sub-class of the generic {@code baseClass}.
	 * @return the resolved type arguments. Never <code>null</code> (empty array for
	 *         a non-generic base-class).
	 */
	public static final <T> Type[] resolveActualTypeArguments(final Class<T> baseClass,
			final Class<? extends T> concreteClass) {
		return _resolveActualTypeArgs(baseClass, concreteClass);
	}

	private static final <T> Type[] _resolveActualTypeArgs(final Class<T> baseClass,
			final Class<? extends T> concreteClass, final Type... actualArgs) {
		requireNonNull(baseClass, "baseClass");
		requireNonNull(concreteClass, "concreteClass");
		requireNonNull(actualArgs, "actualArgs");

		if (actualArgs.length != 0 && actualArgs.length != concreteClass.getTypeParameters().length) {
			throw new IllegalArgumentException(
					"actualArgs.length != 0 && actualArgs.length != concreteClass.typeParameters.length");
		}

		final Type[] _actualArgs = actualArgs.length == 0 ? concreteClass.getTypeParameters() : actualArgs;

		// map type parameters into the actual types
		Map<String, Type> typeVariables = new HashMap<String, Type>();
		for (int i = 0; i < _actualArgs.length; i++) {
			TypeVariable<?> typeVariable = concreteClass.getTypeParameters()[i];
			typeVariables.put(typeVariable.getName(), _actualArgs[i]);
		}

		// Find direct ancestors (superclass, interfaces)
		List<Type> ancestors = new LinkedList<Type>();
		if (concreteClass.getGenericSuperclass() != null) {
			ancestors.add(concreteClass.getGenericSuperclass());
		}
		for (Type t : concreteClass.getGenericInterfaces()) {
			ancestors.add(t);
		}

		// Recurse into ancestors (superclass, interfaces)
		for (Type type : ancestors) {
			if (type instanceof Class<?>) {
				// ancestor is non-parameterized. Recurse only if it matches the base class.
				Class<?> ancestorClass = (Class<?>) type;
				if (baseClass.isAssignableFrom(ancestorClass)) {
					Type[] result = _resolveActualTypeArgs(baseClass, (Class<? extends T>) ancestorClass);
					if (result != null) {
						return result;
					}
				}
			}
			if (type instanceof ParameterizedType) {
				// ancestor is parameterized. Recurse only if the raw type matches the base
				// class.
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type rawType = parameterizedType.getRawType();
				if (rawType instanceof Class<?>) {
					Class<?> rawTypeClass = (Class<?>) rawType;
					if (baseClass.isAssignableFrom(rawTypeClass)) {

						// loop through all type arguments and replace type variables with the actually
						// known types
						List<Type> resolvedTypes = new LinkedList<Type>();
						for (Type t : parameterizedType.getActualTypeArguments()) {
							if (t instanceof TypeVariable<?>) {
								Type resolvedType = typeVariables.get(((TypeVariable<?>) t).getName());
								resolvedTypes.add(resolvedType != null ? resolvedType : t);
							} else {
								resolvedTypes.add(t);
							}
						}

						Type[] result = _resolveActualTypeArgs(baseClass, (Class<? extends T>) rawTypeClass,
								resolvedTypes.toArray(new Type[] {}));
						if (result != null) {
							return result;
						}
					}
				}
			}
		}

		// we have a result if we reached the base class.
		return concreteClass.equals(baseClass) ? _actualArgs : null;
	}

}
