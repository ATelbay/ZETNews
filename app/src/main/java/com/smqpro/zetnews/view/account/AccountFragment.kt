package com.smqpro.zetnews.view.account

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.smqpro.zetnews.R
import com.smqpro.zetnews.util.TAG
import com.smqpro.zetnews.util.load
import kotlinx.android.synthetic.main.fragment_account.*


class AccountFragment : Fragment(R.layout.fragment_account) {
    private lateinit var fAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fAuth = FirebaseAuth.getInstance()
        initAuthUser()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: triggered")
        updateUI(fAuth.currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        currentUser?.let {
            signed_in_login_tv.text = it.displayName
            signed_in_mail_tv.text = it.email
            signed_in_picture_iv.load(it.photoUrl.toString())
            signed_in_appbar.visibility = View.VISIBLE
            initUpdateProfileButton()
            signed_in_auth_button.text = getString(R.string.log_out)
        }
    }

    private fun initAuthUser() {
        signed_in_auth_button.setOnClickListener {
            if (fAuth.currentUser != null) {
                fAuth.signOut()
                reloadFragment()
            } else {
                findNavController().navigate(AccountFragmentDirections.toAuthFragment())
            }
        }

    }

    private fun initSwitchTheme() {
        signed_in_switch_theme_button.setOnClickListener {

        }
    }

    private fun initUpdateProfileButton() {
        signed_in_update_profile_button.setOnClickListener {
            findNavController().navigate(AccountFragmentDirections.toUpdateProfileFragment())
        }
    }

    private fun reloadFragment() {
        findNavController().navigate(AccountFragmentDirections.reloadAccountFragment())
    }

}