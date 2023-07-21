package com.gmail.vusketta.maps

import com.gmail.vusketta.OBJECT
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.LinkedHashMap

class IgnoreMap : LinkedHashMap<String, Type?> {
    constructor()
    constructor(map: Map<out String, Type?>) : super(map)

    companion object {
        val instance = IgnoreMap()
    }

    override operator fun get(key: String): Type {
        val type = super.get(key)
        return if (Objects.isNull(type)) OBJECT else type!!
    }

    override fun put(key: String, value: Type?): Type? {
        throw UnsupportedOperationException()
    }

    override fun putAll(from: Map<out String, Type?>) {
        throw UnsupportedOperationException()
    }
}
