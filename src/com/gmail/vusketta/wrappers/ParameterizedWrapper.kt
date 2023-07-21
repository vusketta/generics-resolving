package com.gmail.vusketta.wrappers

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class ParameterizedWrapper(actualTypeArguments: Array<Type>, private val raw: Type, private val owner: Type?) :
    ParameterizedType {
    private val actualTypeArguments: Array<Type> = actualTypeArguments.copyOf()

    companion object {
        @JvmStatic
        fun hasOwnerType(parameterized: ParameterizedType): Boolean = Objects.nonNull(parameterized.ownerType)
    }

    constructor(parameterized: ParameterizedType) : this(
        parameterized.actualTypeArguments,
        parameterized.rawType,
        parameterized.ownerType
    )

    override fun getActualTypeArguments(): Array<Type> = actualTypeArguments.copyOf()

    override fun getRawType(): Type = raw

    override fun getOwnerType(): Type? = owner
}
