package com.ethanhua.sample

import com.ethanhua.briefpreference.BriefPreference


/**
 * Created by ethanhua on 2018/2/26.
 */

object UserRepository {

    private val localService: UserService by lazy {
        BriefPreference(GsonConverterFactory()).create(AppContext.instance, UserService::class.java)
    }

    fun putUserName(name: String) = localService.putName(name)

    fun getUserName() = localService.getName()

    fun clear() = localService.clear()

    fun remove() = localService.removeUser()

    fun putUser(user: User) = localService.putUser(user)

    fun getUser() = localService.getUser()

    fun putPUser(pUser: PUser) = localService.putPUser(pUser)

    fun getPUser() = localService.getPUser()

    fun listUser() = localService.listUser()

    fun updateListUser(list: MutableList<User>) = localService.updateUser(list)
}