package com.ethanhua.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        UserRepository.getUser().subscribe {
            it?.let {
                text.text = it.userName
            }
        }

        btn.setOnClickListener({
            UserRepository.putUser(User(edit.text.toString(), "test"))
            UserRepository.putUserName(edit.text.toString())
        })
        btnRemove.setOnClickListener {
            UserRepository.removeUser()
        }
        btnClean.setOnClickListener {
            UserRepository.clear()
        }
    }
}
