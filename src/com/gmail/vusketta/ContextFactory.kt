package com.gmail.vusketta

import com.gmail.vusketta.exceptions.NoSuchGenericException
import com.gmail.vusketta.maps.EmptyMap
import com.gmail.vusketta.maps.IgnoreMap
import com.gmail.vusketta.wrappers.WildcardWrapper

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

import java.util.*

fun create(type: Class<*>): Context = create(type, resolveRawGenerics(type)) /* entry point */

private fun create(type: Class<*>, rootGenerics: LinkedHashMap<String, Type?>): Context =
    Context(type, resolve(type, rootGenerics, emptyMap()))

private fun resolve(
    type: Class<*>,
    rootGenerics: LinkedHashMap<String, Type?>,
    knownGenerics: Map<Class<*>, LinkedHashMap<String, Type?>>
): Map<Class<*>, LinkedHashMap<String, Type?>> {
    val generics: MutableMap<Class<*>, LinkedHashMap<String, Type?>> = HashMap()
    generics[type] = rootGenerics
    type.analyzeClass(generics, knownGenerics)
    return generics
}

private fun resolveGenerics(type: Type, generics: LinkedHashMap<String, Type?>): LinkedHashMap<String, Type?> {
    var actual = type
    if (type.isParameterized()) {
        actual = ResolverUtils.resolveTypeVariable(type, generics)
    }
    val resolved: LinkedHashMap<String, Type?>
    if (actual is ParameterizedType) {
        val genericTypes: Array<Type> = actual.actualTypeArguments
        val raw = actual.rawType as Class<*>
        val genericNames = raw.typeParameters
        resolved = fillOuterGenerics(actual, LinkedHashMap())
        val cnt = genericNames.size
        for (i in 0 until cnt) {
            resolved[genericNames[i].name] = genericTypes[i]
        }
    } else {
        resolved = resolveRawGenerics(ResolverUtils.erase(actual, generics))
    }
    return resolved
}

private fun resolveRawGenerics(type: Class<*>): LinkedHashMap<String, Type?> =
    fillOuterGenerics(type, resolveDirectRawGenerics(type))


private fun resolveDirectRawGenerics(type: Class<*>): LinkedHashMap<String, Type?> =
    resolveRawGenericsChain(type.typeParameters)

private fun resolveRawGeneric(variable: TypeVariable<*>, generics: LinkedHashMap<String, Type?>?): Type {
    if (variable.bounds.size <= 1) return ResolverUtils.resolveTypeVariable(variable.bounds[0], generics)
    val types: MutableList<Type> = ArrayList()
    for (bound in variable.bounds) {
        val actual = ResolverUtils.resolveTypeVariable(bound, generics)
        types.add(actual)
    }
    types.remove(OBJECT)
    return if (types.size > 1) {
        WildcardWrapper.upperBounded(*types.toTypedArray<Type>())
    } else {
        if (types.isEmpty()) OBJECT else types[0]
    }
}

private fun fillOuterGenerics(
    type: Type,
    generics: LinkedHashMap<String, Type?>,
    knownGenerics: Map<Class<*>, LinkedHashMap<String, Type?>>? = null
): LinkedHashMap<String, Type?> {
    if (!isInner(type)) return generics

    val outer = getOuter(type)!!
    val outerGenerics: LinkedHashMap<String, Type?> =
        if (outer.isParameterized()) resolveGenerics(outer, IgnoreMap(generics))
        else {
            val erased = ResolverUtils.erase(outer, generics)
            if (Objects.nonNull(knownGenerics) && knownGenerics!!.containsKey(erased)) LinkedHashMap(knownGenerics[erased])
            else resolveRawGenerics(erased)
        }
    for (variable in ResolverUtils.erase(type, generics).typeParameters) {
        outerGenerics.remove(variable.name)
    }
    return if (generics is EmptyMap) outerGenerics
    else {
        generics.putAll(outerGenerics)
        generics
    }
}

private fun resolveRawGenericsChain(declaredGenerics: Array<out TypeVariable<out Class<out Any>>>): LinkedHashMap<String, Type?> {
    if (declaredGenerics.isEmpty()) return EmptyMap.instance

    val contextGenerics = LinkedHashMap<String, Type?>()
    val res = LinkedHashMap<String, Type?>()
    for (variable in declaredGenerics) {
        var resolved: Type? = null
        try {
            resolved = resolveRawGeneric(variable, contextGenerics)
        } catch (e: NoSuchGenericException) {
            if (e.genericName == variable.name && e.genericDeclaration == variable.genericDeclaration) {
                resolved = ResolverUtils.erase(variable.bounds[0], contextGenerics)
            }
        }
        res[variable.name] = resolved
        contextGenerics[variable.name] = resolved
    }
    return res
}

private fun Class<*>.analyzeClass(
    types: MutableMap<Class<*>, LinkedHashMap<String, Type?>>,
    knownTypes: Map<Class<*>, LinkedHashMap<String, Type?>>
) {
    var supertype = this
    while (true) {
        supertype.analyzeInterfaces(types, knownTypes)
        val next = supertype.superclass
        if (Objects.isNull(next) || next == OBJECT) break
        val nextGenerics = if (knownTypes.containsKey(next)) knownTypes[next]!!
        else supertype.analyzeParent(types[supertype]!!)
        types[next] = fillOuterGenerics(next, nextGenerics, knownTypes)
        supertype = next
    }
}

private fun Class<*>.analyzeInterfaces(
    types: MutableMap<Class<*>, LinkedHashMap<String, Type?>>,
    knownTypes: Map<Class<*>, LinkedHashMap<String, Type?>>,
) {
    genericInterfaces.forEach { interfaze ->
        interfaze.analyzeInterface(types, knownTypes, this)
    }
}

private fun Type.analyzeInterface(
    types: MutableMap<Class<*>, LinkedHashMap<String, Type?>>,
    knownTypes: Map<Class<*>, LinkedHashMap<String, Type?>>,
    hostType: Class<*>
) {
    val interfaze = (if (this is ParameterizedType) rawType else this) as Class<*>
    if (knownTypes.containsKey(interfaze)) {
        types[interfaze] = knownTypes[interfaze]!!
    } else if (this is ParameterizedType) {
        types[interfaze] = resolveGenerics(this, types[hostType]!!)
    } else if (interfaze.typeParameters.isNotEmpty()) {
        types[interfaze] = resolveRawGenerics(interfaze)
    }
    interfaze.analyzeClass(types, knownTypes)
}

private fun Class<*>.analyzeParent(generics: LinkedHashMap<String, Type?>): LinkedHashMap<String, Type?> {
    var resolved: LinkedHashMap<String, Type?>? = null
    val parent = superclass
    val genericParent = genericSuperclass
    if (!isInterface && parent.hasSuperclass() && genericParent.isParameterized()) {
        resolved = resolveGenerics(genericParent, generics)
    } else if (Objects.nonNull(parent) && parent.typeParameters.isNotEmpty()) {
        resolved = resolveRawGenerics(parent)
    }
    return if (Objects.isNull(resolved)) EmptyMap.instance else resolved!!
}
