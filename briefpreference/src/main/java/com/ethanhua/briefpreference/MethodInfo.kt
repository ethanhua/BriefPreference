package com.ethanhua.briefpreference

import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Created by ethanhua on 2018/2/26.
 */

class MethodInfo(method: Method, args: Array<Any>?) {

    val returnType: Type = method.genericReturnType
    val actionType: Int
    val key: String

    init {
        actionType = getActionType(method, args)
        key = getKey(method)
    }

    private fun getActionType(method: Method, args: Array<Any>?): Int {
        var methodType = UNKNOWN
        for (annotation in method.annotations) {
            if (annotation is Remove) {
                methodType = REMOVE
                break
            }
            if (annotation is Clear) {
                methodType = CLEAR
                break
            }
        }
        if (methodType == UNKNOWN) {
            if (args != null && args.size > 1) {
                throw methodError(method, "${method.name} method has more than one parameter")
            }
            var typeToCheck: Any? = null
            if (args != null && args.isNotEmpty()) {
                typeToCheck = args[0]
            }
            val hasReturnType = returnType !== Void.TYPE
            var haveDefaultValue = method.parameterAnnotations.any {
                it.any {
                    it is Default
                }
            }
            if (typeToCheck != null && !haveDefaultValue && hasReturnType) {
                throw methodError(method, "Setter method ${method.name} should not have return value")
            }
            methodType = if (hasReturnType) {
                GET
            } else {
                PUT
            }
        }
        return methodType
    }

    private fun getKey(method: Method): String {
        var keyName = ""
        method.annotations.forEach {
            if (it is Key) {
                keyName = it.value.trim()
            }
        }
        if (keyName.isNullOrEmpty()) {
            keyName = getKeyFromMethod(method)
        }
        return keyName
    }

    private fun getKeyFromMethod(method: Method): String {
        val value = method.name.toLowerCase()
        if (value.startsWith("is") && returnType === Boolean::class.javaPrimitiveType) return value.substring(2)
        if (value.startsWith("get")) return value.substring(3)
        if (value.startsWith("put")) return value.substring(3)
        if (value.startsWith("set")) return value.substring(3)
        if (value.startsWith("remove")) return value.substring(6)
        return value
    }

    private fun methodError(method: Method, message: String, vararg args: Any): RuntimeException {
        var message = message
        if (args.isNotEmpty()) {
            message = String.format(message, *args)
        }
        return IllegalArgumentException("${method.declaringClass.simpleName}.${method.name}:$message")
    }
}
