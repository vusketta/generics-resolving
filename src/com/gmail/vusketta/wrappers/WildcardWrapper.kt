package com.gmail.vusketta.wrappers

import com.gmail.vusketta.OBJECT

import java.lang.reflect.Type
import java.lang.reflect.WildcardType

import java.util.*

class WildcardWrapper(upperBounds: Array<out Type>, lowerBounds: Array<out Type>) : WildcardType {
    private val upperBounds: Array<out Type>
    private val lowerBounds: Array<out Type>

    companion object {
        @JvmStatic
        fun upperBounded(vararg bounds: Type): WildcardType {
            return WildcardWrapper(bounds, arrayOf())
        }

        @JvmStatic
        fun lowerBounded(vararg bounds: Type): WildcardType {
            return WildcardWrapper(arrayOf(OBJECT), bounds)
        }

        @JvmStatic
        fun hasLowerBounds(wildcard: WildcardType): Boolean {
            return wildcard.lowerBounds.any { Objects.nonNull(it) }
        }
    }

    constructor(wildcard: WildcardType) : this(wildcard.upperBounds, wildcard.lowerBounds)

    init {
        this.upperBounds = upperBounds.copyOf()
        this.lowerBounds = lowerBounds.copyOf()
    }

    override fun getUpperBounds(): Array<out Type> = upperBounds.copyOf()

    override fun getLowerBounds(): Array<out Type> = lowerBounds.copyOf()
}
