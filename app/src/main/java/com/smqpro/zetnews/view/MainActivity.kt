package com.smqpro.zetnews.view

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smqpro.zetnews.R
import com.smqpro.zetnews.model.db.NewsDatabase
import com.smqpro.zetnews.model.response.CurrentPage
import com.smqpro.zetnews.view.home.HomeFragment
import com.viven.imagezoom.ImageZoomHelper
import com.smqpro.zetnews.util.TAG
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(R.layout.activity_main),
    BottomNavigationView.OnNavigationItemReselectedListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navController: NavController

    lateinit var imageZoomHelper: ImageZoomHelper
    lateinit var db: NewsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setSupportActionBar(main_toolbar)

        initClassVars()

        main_bottom_nav.setupWithNavController(navController)

        main_bottom_nav.setOnNavigationItemReselectedListener(this)

        setupActionBarWithNavController(navController, appBarConfiguration)

        imageZoomHelper = ImageZoomHelper(this)

        setFullScreen()

    }

    private fun setFullScreen() {
        val decorView: View = window.decorView
        // Hide the status bar.
        val uiOptions: Int = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = uiOptions
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)
    }


    private fun initClassVars() {
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.home_fragment, R.id.account_fragment, R.id.liked_fragment)
        )

        navController = main_host_fragment.findNavController()

        db = NewsDatabase(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.main_host_fragment).navigateUp(appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (main_bottom_nav == null) {
            menuInflater.inflate(R.menu.main_bottom_nav_view, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        when (item.itemId) {
            R.id.home_fragment -> {
                Log.d(TAG, "onNavigationItemReselected: home clicked")
//                (main_host_fragment.childFragmentManager.fragments[0] as HomeFragment).scrollToTop()
            }
        }
    }

    override fun onDestroy() {
        CoroutineScope(Dispatchers.IO).launch {
            db.getNewsDao().truncateCache()
            db.getNewsDao().upsertCurrentPage(CurrentPage(0, 2))
        }
        super.onDestroy()
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//
//        return when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                Log.d(TAG, "Action was DOWN")
//                true
//            }
//            MotionEvent.ACTION_MOVE -> {
//                Log.d(TAG, "Action was MOVE")
//                true
//            }
//            MotionEvent.ACTION_UP -> {
//                Log.d(TAG, "Action was UP")
//                true
//            }
//            MotionEvent.ACTION_CANCEL -> {
//                Log.d(TAG, "Action was CANCEL")
//                true
//            }
//            MotionEvent.ACTION_OUTSIDE -> {
//                Log.d(TAG, "Movement occurred outside bounds of current screen element")
//                true
//            }
//            else -> super.onTouchEvent(event)
//        }
//    }
}