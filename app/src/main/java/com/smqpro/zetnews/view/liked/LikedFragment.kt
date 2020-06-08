package com.smqpro.zetnews.view.liked

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.smqpro.zetnews.R
import com.smqpro.zetnews.util.TAG

class LikedFragment : Fragment(R.layout.fragment_liked) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: $tag")
    }
}