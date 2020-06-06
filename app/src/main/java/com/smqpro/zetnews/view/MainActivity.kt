package com.smqpro.zetnews.view

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MotionEventCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.smqpro.zetnews.R
import com.smqpro.zetnews.model.db.NewsDatabase
import com.smqpro.zetnews.util.Constants
import com.smqpro.zetnews.util.TAG
import com.smqpro.zetnews.view.home.HomeFragmentDirections
import com.smqpro.zetnews.view.home.HomeRepository
import com.smqpro.zetnews.view.home.HomeViewModel
import com.smqpro.zetnews.view.home.HomeViewModelProviderFactory
import com.viven.imagezoom.ImageZoomHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
//    lateinit var imageZoomHelper: ImageZoomHelper
    lateinit var db: NewsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setSupportActionBar(main_toolbar)
        initClassVars()

        main_nav_view.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
//        imageZoomHelper = ImageZoomHelper(this)

    }

//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)
//    }



    private fun initClassVars() {
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.home_fragment, R.id.account_fragment),
            main_drawer_layout
        )

        navController = main_host_fragment.findNavController()

//        navController.addOnDestinationChangedListener { controller, destination, arguments ->
//            when (destination.id) {
//                R.id.nav_world -> {
//                    HomeFragmentDirections.navWorld(Constants.SECTIONS.WORLD.toString())
//                    Toast.makeText(this, "WORLD", Toast.LENGTH_SHORT).show()
//                }
//
//            }
//
//        }

        db = NewsDatabase(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.main_host_fragment).navigateUp(appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (main_nav_view == null) {
            menuInflater.inflate(R.menu.main_drawer_menu, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
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