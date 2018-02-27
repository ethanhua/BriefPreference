package com.ethanhua.briefpreference

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import java.lang.reflect.*
import java.util.*

/**
 * Created by ethanhua on 2018/2/26.
 */
class BriefPreference(private var converterFactory: Converter.Factory = DefaultConverterFactory()) {

    private lateinit var preferences: SharedPreferences
    private lateinit var preferenceChangeObservable: Observable<String>
    private val methodInfoCache = LinkedHashMap<String, MethodInfo>()

    fun <T> create(context: Context, service: Class<T>): T {
        createSharePreference(context, service)
        createPreferenceChangeObservable()
        return createServiceProxy(service)
    }

    private fun getSpName(clazz: Class<*>) = clazz.getAnnotation(SpName::class.java)?.value
            ?: clazz.simpleName

    private fun adapterMethod(methodInfo: MethodInfo, args: Array<Any>?): Any? {
        return when (methodInfo.actionType) {
            GET -> getValue(methodInfo.key, methodInfo.returnType, args?.get(0))
            PUT -> putValue(methodInfo.key, args?.get(0))
            REMOVE -> preferences.edit().remove(methodInfo.key).apply()
            CLEAR -> preferences.edit().clear().apply()
            else -> null
        }
    }

    @Throws(Exception::class)
    private fun putValue(key: String, value: Any?) {
        with(preferences.edit()) {
            when (value) {
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                else -> {
                    value?.let {
                        val converter: Converter<Any, String>? = converterFactory.fromType(value.javaClass)
                        val converterValue: String? = converter?.convert(it)
                        if (converterValue.isNullOrBlank()) {
                            return
                        }
                        putString(key, converterValue)
                    }
                }
            }?.apply()
        }
    }

    @Throws(Exception::class)
    private fun getValue(key: String, type: Type, default: Any?): Any? {
        val returnType = Types.getRawType(type)
        return with(preferences) {
            val res: Any = when (returnType) {
                Long::class.java -> getLong(key, if (default == null) 0L else default as Long)
                String::class.java -> getString(key, if (default == null) "" else default as String)
                Int::class.java -> getInt(key, if (default == null) 0 else default as Int)
                Boolean::class.java -> getBoolean(key, if (default == null) false else default as Boolean)
                Float::class.java -> getFloat(key, if (default == null) 0F else default as Float)
                Observable::class.java -> getObservable(key, type, default)
                Flowable::class.java -> getFlowable(key, type, default)
                else -> {
                    val str = getString(key, "")
                    if (str.isNullOrEmpty() && default != null) {
                        return default
                    }
                    val converter: Converter<String, Any>? = converterFactory.toType(getConverterType(type))
                    return converter?.convert(str)
                }
            }
            res
        }
    }

    private fun getObservable(key: String, type: Type, default: Any?): Observable<Any> {
        if (default == null) {
            throw IllegalArgumentException("default value can not be null")
        }
        return preferenceChangeObservable.filter({
            key == it
        }).startWith(key).map {
            getValue(it, Types.getGenericActualType(type, Observable::class.java), default)
        }
    }

    private fun getFlowable(key: String, type: Type, default: Any?) =
            getObservable(key, type, default).toFlowable(BackpressureStrategy.LATEST)

    private fun getMethodInfo(method: Method, args: Array<Any>?): MethodInfo {
        synchronized(methodInfoCache) {
            val key = method.name
            var methodInfo: MethodInfo? = methodInfoCache[key]
            if (methodInfo == null) {
                methodInfo = MethodInfo(method, args)
                methodInfoCache[key] = methodInfo
            }
            return methodInfo
        }
    }

    private fun getConverterType(type: Type): Type {
        var converterType = type
        when (type) {
            is Class<*> -> {
            }
            is ParameterizedType -> {
                val rawType = Types.getRawType(type)
                converterType = (object : ParameterizedType {
                    override fun getActualTypeArguments(): Array<Type> {
                        return arrayOf(Types.getGenericActualType(type, rawType))
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
                val rawType = Types.getRawType(type)
                converterType = (object : ParameterizedType {
                    override fun getActualTypeArguments(): Array<Type> {
                        return arrayOf(Types.getGenericActualType(type.genericComponentType, rawType))
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

    private fun createSharePreference(context: Context, service: Class<*>) {
        preferences = context.getSharedPreferences(getSpName(service), Context.MODE_PRIVATE)
    }

    private fun createPreferenceChangeObservable() {
        preferenceChangeObservable = Observable.create<String>({ emitter ->
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
                if (preferences == this.preferences) {
                    emitter.onNext(key)
                }
            }
            emitter.setCancellable {
                preferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
            preferences.registerOnSharedPreferenceChangeListener(listener)
        }).share()
    }

    private fun <T> createServiceProxy(service: Class<T>): T = Proxy.newProxyInstance(service.classLoader, arrayOf<Class<*>>(service), object : InvocationHandler {
        @Throws(Throwable::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
            return if (method.declaringClass == Any::class.java) {
                method.invoke(this, args)
            } else adapterMethod(getMethodInfo(method, args), args)
        }
    }) as T

}