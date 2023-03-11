package com.adam.shareofshelf

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        bindViews()
    }

    private fun bindViews() {
        btnContinue.setOnClickListener {
            startActivity(Intent(application, DashboardActivity::class.java))
        }
    }
}