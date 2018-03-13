package com.ethanhua.briefpreference

import android.content.Context
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by ethanhua on 2018/2/26.
 */
class BriefPreference(private var converterFactory: Converter.Factory = DefaultConverterFactory()) {

    private val methodInfoCache = ConcurrentHashMap<Method, MethodInfo>()

    fun <T> create(context: Context, service: Class<T>): T {
        Utils.validateServiceInterface(service)
        return createServiceProxy(service, Preference(getSpName(service), context, converterFactory))
    }

    private fun <T> createServiceProxy(service: Class<T>, preference: Preference): T =
            Proxy.newProxyInstance(service.classLoader, arrayOf<Class<*>>(service), object : InvocationHandler {
                @Throws(Throwable::class)
                override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
                    return if (method.declaringClass == Any::class.java) {
                        method.invoke(this, args)
                    } else adapterMethod(preference, getMethodInfo(method, args), args)
                }
            }) as T

    private fun getSpName(clazz: Class<*>) = clazz.getAnnotation(SpName::class.java)?.value
            ?: clazz.simpleName

    private fun adapterMethod(preference: Preference, methodInfo: MethodInfo, args: Array<Any>?): Any? {
        return when (methodInfo.actionType) {
            GET -> preference.getValue(methodInfo.key, methodInfo.returnType, args?.get(0))
            PUT -> preference.putValue(methodInfo.key, args?.get(0))
            REMOVE -> preference.remove(methodInfo.key)
            CLEAR -> preference.clear()
            else -> null
        }
    }

    private fun getMethodInfo(method: Method, args: Array<Any>?): MethodInfo {
        var methodInfo: MethodInfo? = methodInfoCache[method]
        if (methodInfo == null) {
            methodInfo = MethodInfo(method, args)
            methodInfoCache[method] = methodInfo
        }
        return methodInfo
    }

}