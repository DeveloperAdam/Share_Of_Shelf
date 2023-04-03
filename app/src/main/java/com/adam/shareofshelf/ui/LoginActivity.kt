package com.adam.shareofshelf.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adam.shareofshelf.R
import com.adam.shareofshelf.retrofit.DaeemServiceInterface
import com.adam.shareofshelf.retrofit.RetrofitClient
import com.adam.shareofshelf.utils.Constants.INTENT_ID
import com.adam.shareofshelf.utils.Constants.PREFS_NAME
import com.adam.shareofshelf.utils.Constants.PREF_UNAME
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var id = ""
    private var strUsername = ""
    private var strPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        icon.setOnClickListener(this)
        tvSignIn.setOnClickListener(this)
        layoutButton.setOnClickListener(this)
    }

    private fun gotoDashboard() {
        if (cbRememberMe.isChecked)
            savePreferences()
        startActivity(Intent(this@LoginActivity, HostActivity::class.java).apply {
            putExtra(INTENT_ID, id)
        })
        finish()
    }

    private fun authenticateUser() {
        progress.visibility = View.VISIBLE
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(DaeemServiceInterface::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiInterface.authenticateUser(strUsername, strPassword).enqueue(
                    object : Callback<String> {
                        override fun onResponse(
                            call: Call<String>,
                            response: Response<String>
                        ) {
                            progress.visibility = View.GONE
                            if (response.body().equals("not found", true))
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Authentication Failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            else {
                                id = response.body().toString()
                                gotoDashboard()
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            progress.visibility = View.GONE
                            Toast.makeText(this@LoginActivity, t.toString(), Toast.LENGTH_LONG)
                                .show()
                        }

                    }
                )
            } catch (Ex: Exception) {
                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE
                }
                Ex.localizedMessage?.let { Log.e("Error", it) }
            }
        }
    }

    private fun validate(): Boolean {

        var isValid = true
        if (etUsername.text.toString().isEmpty()) {
            isValid = false
            etUsername.setError(getString(R.string.validate_username), null)
        } else
            strUsername = etUsername.text.toString()

        if (etPassword.text.toString().isEmpty()) {
            isValid = false
            etPassword.setError(getString(R.string.validate_password), null)
        } else
            strPassword = etPassword.text.toString()

        return isValid

    }

    private fun savePreferences() {
        val settings = getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val editor = settings.edit()
        editor.putString(PREF_UNAME, strUsername)
        //editor.putString(PREF_PASSWORD, PasswordValue)
        editor.apply()
    }

    private fun loadPreferences() {
        val settings = getSharedPreferences(
            PREFS_NAME,
            MODE_PRIVATE
        )
        // Get value
        strUsername = settings.getString(PREF_UNAME, "Username") ?: "Username"
        if (!strUsername.equals("username", true))
        {
            etUsername.setText(strUsername)
            cbRememberMe.isChecked = true
        }
        else
        {
            cbRememberMe.isChecked = false
            return
        }
    }


    override fun onResume() {
        super.onResume()
        loadPreferences()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.layoutButton, R.id.icon, R.id.tvSignIn -> if (validate())
                authenticateUser()
        }
    }
}