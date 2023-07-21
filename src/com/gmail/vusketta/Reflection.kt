package com.gmail.vusketta

import java.lang.reflect.Array
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

import java.util.*

fun isObject(type: Type): Boolean = type == OBJECT

fun Type?.isParameterized(): Boolean = this is ParameterizedType

fun isInner(type: Type): Boolean {
    if (type is ParameterizedType) {
        return Objects.nonNull(type.ownerType)
    }
    val erased = ResolverUtils.eraseVariables(type)
    return erased.isMemberClass && !erased.isInterface && !Modifier.isStatic(erased.modifiers)
}

fun getOuter(type: Type): Type? {
    if (!isInner(type)) return null
    return if (type is ParameterizedType) type.ownerType else type.javaClass.enclosingClass
}

fun toArray(type: Type): Any {
    return Array.newInstance(type.javaClass, 0)
}

fun toArrayClass(type: Type): Class<*> {
    return toArray(type).javaClass
}

fun toReference(clazz: Class<*>): Class<*> = if (clazz.isPrimitive) PRIMITIVES[clazz]!! else clazz

/* returns true if the type has super class except Object */
fun Class<*>.hasSuperclass(): Boolean = Objects.nonNull(superclass) && !isObject(superclass)
