package com.ethanhua.sample

import com.ethanhua.briefpreference.Clear
import com.ethanhua.briefpreference.Default
import com.ethanhua.briefpreference.Key
import com.ethanhua.briefpreference.Remove
import io.reactivex.Observable

/**
 * Created by ethanhua on 2018/2/26.
 */
interface UserService {

    @Key("testName")
    fun putName(value: String)

    @Key("testName")
    fun getName(@Default value: String = "ethanhua"): String

    @Remove
    fun removeUser()

    @Clear
    fun clear()

    @Key("user")
    fun putUser(value: User)
//
//    @Key("user")
//    fun getUser():User?

    fun putPUser(value: PUser)

    fun getPUser(): PUser?

    @Key("user")
    fun getUser(@Default user: User = User("ethanhua", "avatar")): Observable<User>

    @Key("listUser")
    fun listUser(@Default listUser: MutableList<User> = mutableListOf()): Observable<List<User>>

    @Key("listUser")
    fun updateUser(listUser: MutableList<User>)
}