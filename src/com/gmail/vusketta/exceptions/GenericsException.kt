package com.gmail.vusketta.exceptions

open class GenericsException : RuntimeException {
    constructor() : super()
    constructor(s: String?) : super(s)
}
