package ru.netology.nework.activity

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.NavOptions
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.constraintlayout.motion.widget.MotionLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Location permission is required for map functionality", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val motionLayoutBottomNav: MotionLayout = findViewById(R.id.motion_layout_bottom_nav)
        val navView: BottomNavigationView = findViewById(R.id.bottom_nav)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        val touchTarget: FrameLayout = findViewById(R.id.touch_target)

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "The app needs location access to show maps.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_account)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination?.id ?: 0
            if (item.itemId == currentDestination) {
                return@setOnItemSelectedListener false
            }

            val builder = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.graph.startDestinationId, false)


            val options = builder.build()
            when (item.itemId) {
                R.id.postsFragment -> {
                    navController.navigate(R.id.postsFragment, null, options)
                    true
                }
                R.id.eventsFragment -> {
                    navController.navigate(R.id.eventsFragment, null, options)
                    true
                }
                R.id.usersFragment -> {
                    navController.navigate(R.id.usersFragment, null, options)
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.postsFragment, R.id.eventsFragment -> {
                    supportActionBar?.title = getString(R.string.app_name)
                    motionLayoutBottomNav.visibility = View.VISIBLE
                    motionLayoutBottomNav.transitionToState(R.id.expanded)
                    fab.setImageResource(R.drawable.ic_add)
                }
                R.id.profile -> {
                    val userId = arguments?.getLong("userId")
                    supportActionBar?.title = if (userId == appAuth.authState.value.id) "Profile" else "User"
                    motionLayoutBottomNav.visibility = View.VISIBLE
                    motionLayoutBottomNav.transitionToState(R.id.expanded)
                    fab.setImageResource(R.drawable.ic_add)
                }
                R.id.newPostFragment, R.id.newEventFragment, R.id.usersFragment, R.id.mapPickerFragment-> {
                    supportActionBar?.title =  if (destination.id == R.id.newPostFragment) "New Post" else if (destination.id == R.id.newEventFragment) "New Event" else ""
                    motionLayoutBottomNav.visibility = View.GONE
                    motionLayoutBottomNav.transitionToState(R.id.collapsed)
                    fab.visibility = View.GONE
                    touchTarget.visibility = View.GONE
                }
                else -> {
                    motionLayoutBottomNav.visibility = View.VISIBLE
                    motionLayoutBottomNav.transitionToState(R.id.expanded)
                    fab.visibility = View.GONE
                    touchTarget.visibility = View.VISIBLE
                }
            }
            invalidateOptionsMenu()
        }

        fab.setOnClickListener {
            val isAuthenticated = appAuth.authState.value.token != null
            if (!isAuthenticated) {
                navController.navigate(R.id.sign_In)
                return@setOnClickListener
            }

            when (navController.currentDestination?.id) {
                R.id.postsFragment, R.id.eventsFragment -> {
                    navController.navigate(R.id.chooserFragment)
                }
                R.id.profile -> {
                    navController.navigate(R.id.newJobFragment)
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                appAuth.authState.collect {
                    invalidateOptionsMenu()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        val currentDestinationId = navController.currentDestination?.id

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)

        if (currentDestinationId == R.id.postDetailsFragment) {
            toolbar.overflowIcon = null
        } else {
            toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_account)
        }

        val hideMenuItems = currentDestinationId in setOf(
            R.id.mapPickerFragment,
            R.id.newPostFragment,
            R.id.newEventFragment
        )

        if (hideMenuItems) {
            menu.findItem(R.id.sign_In)?.isVisible = false
            menu.findItem(R.id.sign_Up)?.isVisible = false
            menu.findItem(R.id.logout)?.isVisible = false
            menu.findItem(R.id.profile)?.isVisible = false
        } else {
            val isAuthenticated = appAuth.authState.value.token != null
            menu.findItem(R.id.sign_In)?.isVisible = !isAuthenticated
            menu.findItem(R.id.sign_Up)?.isVisible = !isAuthenticated
            menu.findItem(R.id.logout)?.isVisible = isAuthenticated
            menu.findItem(R.id.profile)?.isVisible = isAuthenticated
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_In -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.sign_In)
                true
            }

            R.id.sign_Up -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.sign_Up)
                true
            }

            R.id.logout -> {
                appAuth.removeAuth()
                true
            }

            R.id.profile -> {
                val userId = appAuth.authState.value.id
                findNavController(R.id.nav_host_fragment).navigate(
                    R.id.profile,
                    bundleOf("userId" to userId)
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}