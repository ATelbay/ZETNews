package com.smqpro.zetnews.view.account

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.smqpro.zetnews.R
import com.smqpro.zetnews.util.TAG
import com.smqpro.zetnews.util.load
import kotlinx.android.synthetic.main.fragment_update_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception


class UpdateProfileFragment : Fragment(R.layout.fragment_update_profile) {
    private val fAuth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun initUpdatePicture() {
        update_profile_button.setOnClickListener {

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.update_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_update_profile -> {
                initUpdateProfile()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initUpdateProfile() {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(update_profile_login_et.text.toString())
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            try {

                fAuth.currentUser?.updateProfile(profileUpdates)?.await()
            } catch (e: Exception) {
                Log.d(TAG, "initUpdateProfile: $e")
            }
            withContext(Dispatchers.Main) {
                findNavController().navigate(UpdateProfileFragmentDirections.toAccountFragment())
            }

        }
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    private fun updateUI() {
        fAuth.currentUser?.let { user ->
//            update_profile_picture_iv.load(user.photoUrl.toString())
            update_profile_login_et.setText(user.displayName)
            update_profile_mail_tv.text = user.email
        }
    }
}