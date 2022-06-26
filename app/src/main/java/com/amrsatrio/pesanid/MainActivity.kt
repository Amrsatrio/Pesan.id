package com.amrsatrio.pesanid

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.amrsatrio.pesanid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val navView = binding.navView

		val navController = findNavController(R.id.nav_host_fragment_activity_main)
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val topLevelDestinationIds = setOf(R.id.home, R.id.orders, R.id.profile)
		val appBarConfiguration = AppBarConfiguration(topLevelDestinationIds)
		//setupActionBarWithNavController(navController, appBarConfiguration)
		navView.setupWithNavController(navController)

		// hide nav bar if not on top level
		navController.addOnDestinationChangedListener { _, destination, _ ->
			navView.visibility = if (destination.id in topLevelDestinationIds) View.VISIBLE else View.GONE
		}
	}
}