package com.ethanhua.sample

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by ethanhua on 2018/2/26.
 */
class PUser(val name: String, val avatar: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(avatar)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PUser> {
        override fun createFromParcel(parcel: Parcel): PUser {
            return PUser(parcel)
        }

        override fun newArray(size: Int): Array<PUser?> {
            return arrayOfNulls(size)
        }
    }
}