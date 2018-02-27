package com.ethanhua.briefpreference

import java.io.IOException
import java.lang.reflect.Type

/**
 * Created by ethanhua on 2018/2/26.
 */
interface Converter<F, T> {

    @Throws(IOException::class)
    fun convert(value: F?): T?

    interface Factory {

        fun <F> fromType(fromType: Type): Converter<F, String>

        fun <T> toType(toType: Type): Converter<String, T>
    }
}