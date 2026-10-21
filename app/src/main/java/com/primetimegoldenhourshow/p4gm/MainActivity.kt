package com.primetimegoldenhourshow.p4gm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.primetimegoldenhourshow.p4gm.data.SettingsRepository
import com.primetimegoldenhourshow.p4gm.databinding.ActivityMainBinding
import com.primetimegoldenhourshow.p4gm.ui.ControlsFragment
import com.primetimegoldenhourshow.p4gm.ui.DashboardFragment
import com.primetimegoldenhourshow.p4gm.ui.GameActivity
import com.primetimegoldenhourshow.p4gm.ui.GameFilesFragment
import com.primetimegoldenhourshow.p4gm.ui.GraphicsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsRepository: SettingsRepository

    private val dashboardFragment = DashboardFragment()
    private val graphicsFragment = GraphicsFragment()
    private val controlsFragment = ControlsFragment()
    private val gameFilesFragment = GameFilesFragment()

    private var activeFragment: Fragment = dashboardFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(this)

        // Setup Fragment Navigation
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, gameFilesFragment, "FILES").hide(gameFilesFragment)
            add(R.id.fragmentContainer, controlsFragment, "CONTROLS").hide(controlsFragment)
            add(R.id.fragmentContainer, graphicsFragment, "GRAPHICS").hide(graphicsFragment)
            add(R.id.fragmentContainer, dashboardFragment, "DASHBOARD")
        }.commit()

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    switchFragment(dashboardFragment)
                    true
                }
                R.id.nav_graphics -> {
                    switchFragment(graphicsFragment)
                    true
                }
                R.id.nav_controls -> {
                    switchFragment(controlsFragment)
                    true
                }
                R.id.nav_files -> {
                    switchFragment(gameFilesFragment)
                    true
                }
                else -> false
            }
        }

        binding.btnHeaderPlay.setOnClickListener {
            val gameDirInfo = settingsRepository.loadGameDirectoryInfo()
            if (gameDirInfo.isValidP4GFolder || gameDirInfo.folderUriString.isNotEmpty()) {
                val intent = Intent(this, GameActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select PS Vita P4G game directory first!", Toast.LENGTH_SHORT).show()
                binding.bottomNav.selectedItemId = R.id.nav_dashboard
            }
        }

        updateHeaderStatus()
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (activeFragment != targetFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(targetFragment)
                .commit()
            activeFragment = targetFragment
        }
    }

    fun updateHeaderStatus() {
        val gameDirInfo = settingsRepository.loadGameDirectoryInfo()
        if (gameDirInfo.isValidP4GFolder || gameDirInfo.folderUriString.isNotEmpty()) {
            binding.tvHeaderStatus.text = "Loaded: ${gameDirInfo.titleId} (${gameDirInfo.titleName})"
            binding.tvHeaderStatus.setTextColor(android.graphics.Color.parseColor("#00E676"))
        } else {
            binding.tvHeaderStatus.text = "Persona 4 Golden Android Recomp Project"
            binding.tvHeaderStatus.setTextColor(android.graphics.Color.parseColor("#B0B3C6"))
        }
    }

    override fun onResume() {
        super.onResume()
        updateHeaderStatus()
    }
}
