package com.gmail.vusketta.wrappers

import java.lang.reflect.Type

class MultiType(vararg types: Type) : Type {
    private val types: Array<out Type>

    init {
        require(types.size >= 2) { "Impossible to create multi type with 0 or 1 type provided." }
        this.types = types
    }
}
