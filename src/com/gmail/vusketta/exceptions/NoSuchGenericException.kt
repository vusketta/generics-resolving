package com.gmail.vusketta.exceptions

import java.lang.reflect.GenericDeclaration
import java.lang.reflect.TypeVariable

class NoSuchGenericException(private val type: TypeVariable<*>) :
    GenericsException("They're no such $type in generics map.") {
    val genericName: String
        get() = type.typeName

    val genericDeclaration: GenericDeclaration
        get() = type.genericDeclaration
}
