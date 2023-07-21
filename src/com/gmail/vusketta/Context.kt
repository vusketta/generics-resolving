package com.gmail.vusketta

import java.lang.reflect.Type

class Context(val rootClass: Class<*>, private val types: Map<Class<*>, LinkedHashMap<String, Type?>>) {
    fun getTypeGenerics(type: Class<*>): Map<String, Type?> {
        require(types.containsKey(type))
        return LinkedHashMap(types[type])
    }

    val composingTypes: Set<Class<*>>
        get() = HashSet(types.keys)
    val typesMap: Map<Class<*>, LinkedHashMap<String, Type?>>
        get() = HashMap(types)
}
