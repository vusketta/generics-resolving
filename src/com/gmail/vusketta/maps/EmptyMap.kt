package com.gmail.vusketta.maps

import java.lang.reflect.Type

class EmptyMap : LinkedHashMap<String, Type?>(0) {
    override fun put(key: String, value: Type?): Type? {
        throw UnsupportedOperationException()
    }

    override fun putAll(from: Map<out String, Type?>) {
        throw UnsupportedOperationException()
    }

    companion object {
        val instance = EmptyMap()
    }
}
