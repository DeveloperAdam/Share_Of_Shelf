package com.adam.shareofshelf.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.adam.shareofshelf.R
import com.adam.shareofshelf.utils.Constants.INTENT_ID
import kotlinx.android.synthetic.main.activity_list_of_clients.*

class ListOfClientsActivity : AppCompatActivity() {

    private var id = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_clients)

        intent?.let {
            id = it.getStringExtra(INTENT_ID) ?: ""
        }
        bottom_nav_view.selectedItemId = R.id.home_fragment
        bottom_nav_view.setOnItemSelectedListener { item ->
            when (item.itemId) {
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

    private fun showConfirmation() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_logout))
            .setCancelable(false)
            .setPositiveButton(
                getString(R.string.yes)
            ) { dialog, id -> finish() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun gotoHome() {
        supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment.newInstance(id)).commit()
    }
}