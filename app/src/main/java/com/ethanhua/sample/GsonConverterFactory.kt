package com.ethanhua.sample

import com.ethanhua.briefpreference.Converter
import com.google.gson.Gson
import java.lang.reflect.Type


/**
 * Created by ethanhua on 2018/2/27.
 */
class GsonConverterFactory : Converter.Factory {

    private val gson = Gson()

    override fun <F> fromType(fromType: Type): Converter<F, String> {
        return object : Converter<F, String> {
            override fun convert(value: F?): String? {
                return gson.toJson(value)
            }
        }
    }

    override fun <T> toType(toType: Type): Converter<String, T> {
        return object : Converter<String, T> {
            override fun convert(value: String?): T? {
                return gson.fromJson(value, toType)
            }
        }
    }

}