package com.ethanhua.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        UserRepository.listUser().subscribe({
            it?.let {
                if(it.isNotEmpty()){
                    text.text = it[0].userName
                }
            }
        }, {
            it.printStackTrace()
        })

        btn.setOnClickListener({
            val name = edit.text.toString()
            val user = User(name, "avatar")
            UserRepository.updateListUser(mutableListOf(user))
        })
        btnRemove.setOnClickListener {
            UserRepository.remove()
        }
        btnClean.setOnClickListener {
            UserRepository.clear()
        }
    }
}
