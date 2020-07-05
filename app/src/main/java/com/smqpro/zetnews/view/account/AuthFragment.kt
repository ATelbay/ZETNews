package com.smqpro.zetnews.view.account

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.smqpro.zetnews.R
import com.smqpro.zetnews.util.TAG
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthFragment : Fragment(R.layout.fragment_auth) {
    private lateinit var fAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fAuth = FirebaseAuth.getInstance()
        initLoginUser()
        initSignUpUser()
    }

    private fun initLoginUser() {
        account_login_button.setOnClickListener {
            checkInsertedData {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        fAuth.signInWithEmailAndPassword(
                            account_email_til_et.text.toString(),
                            account_password_til_et.text.toString()
                        ).await()
                        withContext(Dispatchers.Main) {
                            findNavController().popBackStack()
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Auth failed", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context, "Something went wrong. Please, try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

    }

    private fun initSignUpUser() {
        account_sign_up_button.setOnClickListener {
            checkInsertedData {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        fAuth.createUserWithEmailAndPassword(
                            account_email_til_et.text.toString(),
                            account_password_til_et.text.toString()
                        ).await()
                        withContext(Dispatchers.Main) {
                            findNavController().popBackStack()
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Auth failed", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context, "Something went wrong. Please, try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun checkInsertedData(
        block: () -> Unit
    ) {
        if (
            account_email_til_et.text.toString().isNotEmpty() &&
            account_password_til_et.text.toString().length > 5
        ) {
            account_login_til.error = null
            account_password_til.error = null
            block()
        } else {
            if (account_email_til_et.text.toString().isEmpty()) {
                account_login_til.error = "Login can not be empty"
            }
            if (account_password_til_et.text.toString().isEmpty()) {
                account_password_til.error = "Password can not be empty"
            }
            if (account_password_til_et.text.toString().isNotEmpty() &&
                account_password_til_et.text.toString().length < 6) {
                account_password_til.error = "Password must contain more than 6 characters"
            }
        }
    }
}