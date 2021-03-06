package com.sergioteso.conecta4.activities

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.sergioteso.conecta4.R
import com.sergioteso.conecta4.firebase.FRDataBase
import com.sergioteso.conecta4.models.RoundRepository
import com.sergioteso.conecta4.models.RoundRepositoryFactory
import kotlinx.android.synthetic.main.activity_loginc4.*


class LoginActivityC4 : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    override fun onLoadFinished(p0: Loader<Cursor>, p1: Cursor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoaderReset(p0: Loader<Cursor>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Cursor> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private var mAuthTask: UserLoginTask? = null
    private fun attemptC4(type: String) {
        val repository = RoundRepositoryFactory.createRepository(this)
        val loginRegisterCallback = object : RoundRepository.LoginRegisterCallback {
            override fun onLogin(playerUuid: String) {
                SettingsActivityC4.setPlayerUUID(this@LoginActivityC4, playerUuid)
                SettingsActivityC4.setPlayerName(
                    this@LoginActivityC4,
                    email.text.toString()
                )
                val intent = RoundListActivity.newIntent(this@LoginActivityC4)
                startActivity(intent)
                finish()
            }

            override fun onError(error: String) {
                when (error) {
                    FRDataBase.LOGIN_CREDENTIALS_ERROR -> {
                        email.error = getString(R.string.error_invalid_email)
                        password.error = getString(R.string.error_incorrect_password)
                        password.requestFocus()
                    }
                    FRDataBase.REGISTER_ALREADY_EXISTS -> {
                        Toast.makeText(applicationContext, "User Already Exists", Toast.LENGTH_SHORT).show()
                    }
                    FRDataBase.REGISTER_ERROR -> {
                        Toast.makeText(applicationContext, "DB error User couldn't be registered", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        email.error = getString(R.string.error_invalid_email)
                        password.error = getString(R.string.error_incorrect_password)
                        password.requestFocus()
                    }
                }

            }
        }
        when (type) {
            "login" -> repository?.login(
                email.text.toString(),
                password.text.toString(), loginRegisterCallback
            )
            "register" -> repository?.register(
                email.text.toString(),
                password.text.toString(), loginRegisterCallback
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginc4)

        tv_local.text = if (RoundRepositoryFactory.LOCAL) "LOCAL" else "ONLINE"

        val configuration = getResources().getConfiguration()
        val screenWidthDp = configuration.screenWidthDp
        if (screenWidthDp >= 900) {
            email.setText("antonio@gmail.com")
            password.setText("antonio")
        } else {
            email.setText("natasha@gmail.com")
            password.setText("natasha")
        }
        email_sign_in_button.setOnClickListener { attemptLogin("login") }
        email_register_button.setOnClickListener { attemptLogin("register") }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin(type: String) {
//        if (mAuthTask != null) {
//            return
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView?.requestFocus()
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            //showProgress(true)
//            //mAuthTask = UserLoginTask(emailStr, passwordStr)
//            //mAuthTask!!.execute(null as Void?)
//            attemptC4(type)
//        }
        attemptC4(type)
    }
}