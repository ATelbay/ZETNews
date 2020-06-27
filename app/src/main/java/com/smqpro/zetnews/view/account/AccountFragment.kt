package com.smqpro.zetnews.view.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.smqpro.zetnews.R
import com.smqpro.zetnews.util.TAG
import com.smqpro.zetnews.util.load
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account_signed_in.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class AccountFragment : Fragment() {
    private lateinit var fAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fAuth = FirebaseAuth.getInstance()
        Log.d(TAG, "onCreateView: user is ${fAuth.currentUser}")
        return if (fAuth.currentUser != null)
            inflater.inflate(R.layout.fragment_account_signed_in, container, false)
        else
            inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (fAuth.currentUser != null) {
            initLogoutUser()
        } else {
            initSignUpUser()
            initLoginUser()
        }


    }

    override fun onStart() {
        super.onStart()
        updateUI(fAuth.currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        currentUser?.let {
            signed_in_login_tv.text = it.displayName
            signed_in_mail_tv.text = it.email
            signed_in_picture_iv.load(it.photoUrl.toString())
        }
    }

    private fun initLogoutUser() {
        signed_in_logout_button.setOnClickListener {
            fAuth.signOut()
            reloadFragment()
        }
    }

    private fun initSwitchTheme() {
        signed_in_switch_theme_button.setOnClickListener {

        }
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
                            reloadFragment()
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
                            findNavController().navigate(AccountFragmentDirections.reloadFragment())
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
            account_password_til_et.text.toString().isNotEmpty()
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
        }
    }

    private fun reloadFragment() {
        findNavController().navigate(AccountFragmentDirections.reloadFragment())
    }

}