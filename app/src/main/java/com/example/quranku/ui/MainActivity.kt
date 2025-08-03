package com.example.quranku.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.quranku.R
import com.example.quranku.databinding.ActivityMainBinding
import com.example.quranku.ui.history.HistoryFragment
import com.example.quranku.ui.home.HomeFragment
import com.example.quranku.ui.info.InfoFragment
import com.example.quranku.util.FirstTimeSetupHelper

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding by lazy { _binding!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Perform first-time setup if needed
        val setupHelper = FirstTimeSetupHelper(this)
        if (setupHelper.isFirstTimeSetup()) {
            android.util.Log.d("MainActivity", "First time setup detected, initializing demo files...")
            setupHelper.performFirstTimeSetup()
        } else {
            android.util.Log.d("MainActivity", "App already set up, demo files status: ${setupHelper.getDemoFilesStatus()}")
        }
        
        // Debug first-time setup
        debugFirstTimeSetup()

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        // Bottom navigation listener
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.nav_info -> {
                    replaceFragment(InfoFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    
    // Debug method to test first-time setup (can be called from adb)
    private fun debugFirstTimeSetup() {
        val setupHelper = FirstTimeSetupHelper(this)
        android.util.Log.d("MainActivity", "Demo files status: ${setupHelper.getDemoFilesStatus()}")
        android.util.Log.d("MainActivity", "Demo files exist: ${setupHelper.checkDemoFilesExist()}")
        android.util.Log.d("MainActivity", "Raw resources exist: ${setupHelper.checkRawResourcesExist()}")
        android.util.Log.d("MainActivity", "Is first time setup: ${setupHelper.isFirstTimeSetup()}")
        
        // Reset first-time setup for testing
        setupHelper.resetFirstTimeSetup()
        
        // Trigger first-time setup again
        if (setupHelper.isFirstTimeSetup()) {
            android.util.Log.d("MainActivity", "Triggering first-time setup after reset...")
            setupHelper.performFirstTimeSetup()
        }
    }
}
