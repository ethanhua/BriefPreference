package com.ethanhua.briefpreference

import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import java.io.*
import java.lang.reflect.Type

/**
 * Created by ethanhua on 2018/2/26.
 */
class DefaultConverterFactory : Converter.Factory {

    private val mFromSerializableConverter by lazy {
        object : Converter<Serializable, String> {
            override fun convert(value: Serializable?): String? {
                val byteOutputStream = ByteArrayOutputStream()
                if (value == null) {
                    return null
                }
                try {
                    val out = ObjectOutputStream(byteOutputStream)
                    out.writeObject(value)
                    out.close()
                    return Base64.encodeToString(byteOutputStream.toByteArray(), Base64.DEFAULT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }

    private val mToSerializableConverter by lazy {
        object : Converter<String, Serializable> {
            override fun convert(value: String?): Serializable? {
                if (value.isNullOrEmpty()) {
                    return null
                }
                try {
                    val inStream = ObjectInputStream(ByteArrayInputStream(Base64.decode(value, Base64.DEFAULT)))
                    val obj = inStream.readObject() ?: return null
                    return obj as Serializable
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }

    private val mFromParcelableConverter by lazy {
        object : Converter<Parcelable, String> {
            override fun convert(value: Parcelable?): String? {
                if (value == null) {
                    return null
                }
                val parcel = Parcel.obtain()
                value.writeToParcel(parcel, 0)
                val marshall = parcel.marshall()
                parcel.recycle()
                return Base64.encodeToString(marshall, Base64.DEFAULT)
            }
        }
    }

    override fun <F> fromType(fromType: Type): Converter<F, String> {
        if (fromType is Class<*>) {
            if (Serializable::class.java.isAssignableFrom(fromType)) {
                return mFromSerializableConverter as Converter<F, String>
            }
            if (Parcelable::class.java.isAssignableFrom(fromType)) {
                return mFromParcelableConverter as Converter<F, String>
            }
        }
        throw IllegalArgumentException("DefaultConverterFactory supports only Serializable and Parcelable")
    }

    override fun <T> toType(toType: Type): Converter<String, T> {
        if (toType is Class<*>) {
            if (Serializable::class.java.isAssignableFrom(toType)) {
                return mToSerializableConverter as Converter<String, T>
            }
            if (Parcelable::class.java.isAssignableFrom(toType)) {
                try {
                    return ToParcelableConverter(toType.getField("CREATOR").get(null) as Parcelable.Creator<T>)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: NoSuchFieldException) {
                    e.printStackTrace()
                }

            }
        }
        throw IllegalArgumentException("DefaultConverterFactory supports only Serializable and Parcelable")
    }

    private class ToParcelableConverter<T>(var creator: Parcelable.Creator<T>) : Converter<String, T> {

        override fun convert(value: String?): T? {
            if (value.isNullOrEmpty()) {
                return null
            }
            val bytes = Base64.decode(value, Base64.DEFAULT)
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            return creator.createFromParcel(parcel)
        }
    }

}