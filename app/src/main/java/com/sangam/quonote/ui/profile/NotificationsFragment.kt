package com.sangam.quonote.ui.profile

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sangam.quonote.R
import com.sangam.quonote.SignInActivity
import com.sangam.quonote.databinding.FragmentNotificationsBinding
import com.sangam.quonote.profile.AllQuotesActivity
import com.sangam.quonote.profile.FavouriteActivity
import com.sangam.quonote.profile.MyProfileActivity

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var database: FirebaseDatabase

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        firebaseAuth = FirebaseAuth.getInstance()

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Logging Out")
        progressDialog.setCancelable(false)

        binding.TextMyFavourites.setOnClickListener {
            val intent = Intent(activity, FavouriteActivity::class.java)
            startActivity(intent)
        }
        binding.TextMyProfile.setOnClickListener {
            val intent = Intent(activity, MyProfileActivity::class.java)
            startActivity(intent)
        }
        binding.TextLogout.setOnClickListener {
            progressDialog.show()
            firebaseAuth.signOut()
            progressDialog.dismiss()
            Toast.makeText(activity, "Logged Out Successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.TextAboutUs.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.about_us_dialogbox, null)
            alertDialog.setView(view).setCancelable(true)

            alertDialog.create().show()
        }
        binding.textAllQuotes.setOnClickListener {
            val intent = Intent(activity, AllQuotesActivity::class.java)
            startActivity(intent)
        }
        binding.TextFeedback.setOnClickListener {
            giveFeedback()
        }
        binding.TextRateUs.setOnClickListener {
            openPlayStoreForRating()
        }

        return root
    }

    private fun openPlayStoreForRating() {
        val uri = Uri.parse("market://details?id=com.sangam.quonote")
        val playStoreIntent = Intent(Intent.ACTION_VIEW, uri)
        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(playStoreIntent)
        } catch (e: ActivityNotFoundException) {
            // If Play Store app is not available, open the link in the browser
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.sangam.quonote")
            )
            startActivity(webIntent)
        }
    }

    private fun giveFeedback() {
        val alertDialog = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_feedback, null)
        alertDialog.setView(view)
        alertDialog.setCancelable(true)
        val dialog = alertDialog.create()
        dialog.show()
        val feedback = view.findViewById<EditText>(R.id.ETFeedback)
        val button = view.findViewById<Button>(R.id.submitButton)
        button.setOnClickListener {
            dialog.dismiss()
            val userfeed = feedback.text.toString()
            database = FirebaseDatabase.getInstance()
            val dbreference =
                database.reference.child("Feedbacks").child(firebaseAuth.currentUser!!.uid)
                    .setValue(userfeed).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(activity, "Feedback Given", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                activity,
                                "Error : ${it.exception?.message.toString()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}