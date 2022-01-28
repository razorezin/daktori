package com.redapps.tabib.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.clovertech.autolib.utils.PrefUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.redapps.tabib.R
import com.redapps.tabib.databinding.ActivityMainBinding
import com.redapps.tabib.utils.MenuUtils

class PatientActivity : AppCompatActivity() {

    val TAG = "LOG TAG"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top account image on click
        binding.imageAccountMain.setOnClickListener(View.OnClickListener {
            MenuUtils.showAccountDialog(this)
        })

        // setups
        initNavigation()

        // temp
        Glide.with(this)
            .load(R.drawable.patient)
            .into(binding.imageAccountMain)
    }

    override fun onResume() {
        super.onResume()
        saveFCMToken()
    }

    private fun initNavigation(){
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_booking, R.id.navigation_treatment, R.id.navigation_appointment
            )
        )
        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener{ _, destination, _ ->
            if(destination.id == R.id.nav_doctor_detail) {
                binding.textAppTitle.visibility = View.GONE
                binding.imageAccountMain.visibility = View.GONE
                binding.navView.visibility = View.GONE
            } else {
                binding.textAppTitle.visibility = View.VISIBLE
                binding.imageAccountMain.visibility = View.VISIBLE
                binding.navView.visibility = View.VISIBLE
            }
        }
    }

    private fun saveFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            if (token != null){
                PrefUtils.with(this).save(PrefUtils.Keys.FCM_TOKEN, token)
            }

            // Log and toast
            Log.d(TAG, "FCM token : " + token)
        })
    }


}