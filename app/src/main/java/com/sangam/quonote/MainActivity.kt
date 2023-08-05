package com.sangam.quonote

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sangam.quonote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(100)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
       // showWelcome()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun showWelcome() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(auth.currentUser!!.uid).child("User Profile")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val profile = snapshot.getValue(UserDataClass::class.java)
                    val username = profile?.name.toString()
                    val toast =
                        Toast.makeText(this@MainActivity, "Welcome ${username}", Toast.LENGTH_SHORT)
                    toast.show()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onBackPressed() {


        val currentFragmentId =
            findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id

        if (currentFragmentId == R.id.navigation_home ||
            currentFragmentId == R.id.navigation_dashboard ||
            currentFragmentId == R.id.navigation_notifications
        ) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Close The App")
            alertDialog.setCancelable(false)
            alertDialog.setMessage("Are you sure you want to quit?")
            alertDialog.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                finishAffinity()
            }
            alertDialog.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create().show()
        } else {
            super.onBackPressed()
        }
    }
}