package com.ethanhua.sample

import android.app.Application

/**
 * Created by ethanhua on 2018/2/26.
 */

class AppContext : Application() {

    companion object {
        lateinit var instance: AppContext
    }

    override fun onCreate() {
        super.onCreate()
        instance =this
    }

}
