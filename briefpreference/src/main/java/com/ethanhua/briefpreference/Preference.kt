package com.ethanhua.briefpreference

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by ethanhua on 2018/3/13.
 */
class Preference(spName: String,
                 context: Context,
                 private var converterFactory: Converter.Factory) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
    private val keyChangeObservable: Observable<String>
    private lateinit var observableEmitter: ObservableEmitter<String>

    init {
        keyChangeObservable = Observable.create<String>({ emitter ->
            observableEmitter = emitter
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
                if (preferences == sharedPreferences) {
                    emitter.onNext(key)
                }
            }
            emitter.setCancellable {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        }).share()
    }

    @Throws(Exception::class)
    fun putValue(key: String, value: Any?) {
        with(sharedPreferences.edit()) {
            when (value) {
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                else -> putObject(key, value)
            }?.apply()
        }
    }

    @Throws(Exception::class)
    fun getValue(key: String, type: Type, default: Any?): Any? {
        val returnType = Utils.getRawType(type)
        return with(sharedPreferences) {
            val res: Any? = when (returnType) {
                Long::class.java -> getLong(key, if (default == null) 0L else default as Long)
                String::class.java -> getString(key, if (default == null) "" else default as String)
                Int::class.java -> getInt(key, if (default == null) 0 else default as Int)
                Boolean::class.java -> getBoolean(key, if (default == null) false else default as Boolean)
                Float::class.java -> getFloat(key, if (default == null) 0F else default as Float)
                Observable::class.java -> getObservable(key, type, default)
                Flowable::class.java -> getFlowable(key, type, default)
                else -> getObject(key, type, default)
            }
            res
        }
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clear() {
        val keyList = sharedPreferences.all.keys.toList()
        sharedPreferences.edit().clear().apply()
        keyList.forEach { observableEmitter.onNext(it) }
    }

    private fun getObject(key: String, type: Type, default: Any?): Any? {
        val str = sharedPreferences.getString(key, "")
        if (str.isNullOrEmpty() && default != null) {
            return default
        }
        val converter: Converter<String, Any>? = converterFactory.toType(getConverterType(type))
        return converter?.convert(str)
    }

    private fun putObject(key: String, value: Any?): SharedPreferences.Editor? {
        return value?.let {
            val converter: Converter<Any, String>? = converterFactory.fromType(value.javaClass)
            val converterValue: String? = converter?.convert(it)
            if (converterValue.isNullOrBlank()) {
                return null
            }
            sharedPreferences.edit().putString(key, converterValue)
        }
    }

    private fun getObservable(key: String, type: Type, default: Any?): Observable<Any> {
        if (default == null) {
            throw IllegalArgumentException("default value can not be null")
        }
        return keyChangeObservable.filter({
            key == it
        }).startWith(key).map {
            getValue(it, Utils.getGenericActualType(type, Observable::class.java), default)
        }
    }

    private fun getFlowable(key: String, type: Type, default: Any?) =
            getObservable(key, type, default).toFlowable(BackpressureStrategy.LATEST)

    private fun getConverterType(type: Type): Type {
        var converterType = type
        when (type) {
            is Class<*> -> {
            }
            is ParameterizedType -> {
                val rawType = Utils.getRawType(type)
                converterType = (object : ParameterizedType {
                    override fun getActualTypeArguments(): Array<Type> {
                        return arrayOf(Utils.getGenericActualType(type, rawType))
                    }

                    override fun getRawType(): Type {
                        return rawType
                    }

                    override fun getOwnerType(): Type? {
                        return null
                    }
                })
            }
            is GenericArrayType -> {
                val rawType = Utils.getRawType(type)
                converterType = (object : ParameterizedType {
                    override fun getActualTypeArguments(): Array<Type> {
                        return arrayOf(Utils.getGenericActualType(type.genericComponentType, rawType))
                    }

                    override fun getRawType(): Type {
                        return rawType
                    }

                    override fun getOwnerType(): Type? {
                        return null
                    }
                })
            }
            else -> throw IllegalArgumentException("not support value type")
        }
        return converterType
    }

}