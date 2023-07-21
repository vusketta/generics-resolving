package com.gmail.vusketta;

import com.gmail.vusketta.exceptions.NoSuchGenericException;
import com.gmail.vusketta.maps.IgnoreMap;
import com.gmail.vusketta.wrappers.ParameterizedWrapper;
import com.gmail.vusketta.wrappers.WildcardWrapper;

import java.lang.reflect.*;
import java.util.Map;
import java.util.Objects;

import static com.gmail.vusketta.ReflectionKt.isObject;
import static com.gmail.vusketta.ReflectionKt.toArrayClass;

public final class ResolverUtils {
    private ResolverUtils() {
        /* preventing instance creation */
    }

    public static Class<?> eraseVariables(Type type) {
        return erase(type, IgnoreMap.Companion.getInstance());
    }

    public static Class<?> erase(Type type, Map<String, Type> generics) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterized -> erase(parameterized.getRawType(), generics);
            case TypeVariable<?> variable -> erase(getDeclaredGeneric(variable, generics), generics);
            case WildcardType wildcard -> erase(wildcard.getUpperBounds()[0], generics);
            case GenericArrayType genericArray -> toArrayClass(erase(genericArray.getGenericComponentType(), generics));
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private static Type getDeclaredGeneric(TypeVariable<?> variable, Map<String, Type> generics) {
        Type declared = generics.get(variable.getName());
        if (Objects.isNull(declared)) {
            throw new NoSuchGenericException(variable);
        }
        return declared;
    }

    static Type resolveTypeVariable(final Type type, final Map<String, Type> generics) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterized -> resolveParameterizedTypeVariable(parameterized, generics);
            case TypeVariable<?> variable -> getDeclaredGeneric(variable, generics);
            case WildcardType wildcard -> resolveWildcardTypeVariable(wildcard, generics);
            case GenericArrayType genericArray -> resolveGenericArrayTypeVariable(genericArray, generics);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    static Type[] resolveTypeVariables(final Type[] types, final Map<String, Type> generics) {
        final Type[] resolved = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            resolved[i] = resolveTypeVariable(types[i], generics);
        }
        return resolved;
    }

    private static Type resolveParameterizedTypeVariable(ParameterizedType parameterized, Map<String, Type> generics) {
        final Type owner = !ParameterizedWrapper.hasOwnerType(parameterized) ? null :
                resolveTypeVariable(parameterized.getOwnerType(), new IgnoreMap(generics)); /* add owner class generics */
        assert owner != null;
        return new ParameterizedWrapper(
                resolveTypeVariables(parameterized.getActualTypeArguments(), generics),
                parameterized.getRawType(),
                owner
        );
    }

    private static Type resolveWildcardTypeVariable(WildcardType wildcard, Map<String, Type> generics) {
        if (WildcardWrapper.hasLowerBounds(wildcard)) {
            final Type lowerBound = resolveTypeVariable(wildcard.getLowerBounds()[0], generics);
            return isObject(lowerBound) ? lowerBound : WildcardWrapper.lowerBounded(lowerBound);
        }
        final Type[] upperBounds = resolveTypeVariables(wildcard.getUpperBounds(), generics);
        return upperBounds.length == 1 ? upperBounds[0] : WildcardWrapper.upperBounded(upperBounds);
    }

    private static Type resolveGenericArrayTypeVariable(GenericArrayType genericArray, Map<String, Type> generics) {
        return toArrayClass(resolveTypeVariable(genericArray.getGenericComponentType(), generics));
    }
}
