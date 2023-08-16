package com.sangam.quonote.ui.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sangam.quonote.UserQuoteDataClass
import com.sangam.quonote.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val homeViewModel =
//            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        firebaseAuth = FirebaseAuth.getInstance()
        binding.cardView.animate().apply {
            duration = 1500
            rotationXBy(360f)
        }
        binding.btnshow.setOnClickListener {
            if (binding.etWrite.text.toString().trim().isNotEmpty()) {
                getData()
                binding.txtShow.text = binding.etWrite.text.toString()
                binding.etWrite.text?.clear()
            } else {
                binding.etWrite.error = "This is Empty Field "
            }
        }
        loadData()
        binding.textClearBoard.setOnClickListener {
            if (binding.txtShow.text.toString().trim().isNotEmpty()) {
                binding.lottieAnimationViewClear?.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    clearData()
                    binding.txtShow.text = ""
                    binding.lottieAnimationViewClear?.visibility = View.GONE
                    Toast.makeText(activity, "Board Cleared", Toast.LENGTH_SHORT).show()
                }, 1000)

            } else {
                Toast.makeText(activity, "Board Is Already Empty", Toast.LENGTH_SHORT).show()
            }

        }
        binding.btnShare.setOnClickListener {
            if (binding.txtShow.text.toString().trim().isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.setType("text/plain")
                intent.putExtra(Intent.EXTRA_TEXT, binding.txtShow.text.toString().trim())
                startActivity(intent)
            } else {
                Toast.makeText(activity, "Add Some Quote On Board First", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.textSaveToMyQuotes.setOnClickListener {
            if (binding.txtShow.text.toString().trim().isEmpty()) {
                Toast.makeText(activity, "Add Some Quote On Board First", Toast.LENGTH_SHORT)
                    .show()
            } else {
                binding.lottieAnimationViewsave?.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(firebaseAuth.currentUser!!.uid).child("Quotes")
                        .child(binding.txtShow.text.trim().toString())

                    val quote = UserQuoteDataClass(binding.txtShow.text.trim().toString())

                    databaseReference.setValue(quote).addOnCompleteListener {
                        if (it.isSuccessful) {
                            binding.lottieAnimationViewsave?.visibility = View.GONE
                            Toast.makeText(activity, "Added To My Quotes", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.d("ErrorOfSave", it.exception?.message.toString())
                            binding.lottieAnimationViewsave?.visibility = View.GONE
                            Toast.makeText(
                                activity,
                                "Error: ${it.exception?.message.toString()}",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }

                }, 1000)
            }

        }

        return root
    }

    private fun getData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("sharedpreference", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.apply {
            putString("shared", binding.etWrite.text.toString())
            putBoolean("Finished", true)
        }
        editor.apply()
        Toast.makeText(activity, "Your Quote For The Day is Showed", Toast.LENGTH_SHORT).show()

    }

    private fun loadData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("sharedpreference", Context.MODE_PRIVATE)
        val data: String? = sharedPreferences.getString("shared", null)
        binding.txtShow.text = data
        sharedPreferences.getBoolean("Finished", false)
    }

    private fun clearData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("sharedpreference", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.remove("shared")
        editor.remove("Finished")
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}