package com.adam.shareofshelf.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.adam.shareofshelf.R
import kotlinx.android.synthetic.main.activity_list_of_clients.*

class ListOfClientsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_clients)

 
        bottom_nav_view.selectedItemId = R.id.home_fragment
        bottom_nav_view.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.home_fragment -> {
                    gotoHome()
                    true
                }
                else -> {
                    showConfirmation()
                    false
                }
            }
        }
    }

    private fun showConfirmation(){
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_logout))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)
            ) { dialog, id -> finish() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    private fun gotoHome(){
            supportFragmentManager.beginTransaction().replace(R.id.container,HomeFragment()).commit()
    }
}