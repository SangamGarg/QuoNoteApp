package com.sangam.quonote.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sangam.quonote.UserQuoteDataClass
import com.sangam.quonote.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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

        binding.textSaveToMyQuotes.setOnClickListener {
binding.lottieAnimationViewsave?.visibility =View.VISIBLE
            if (binding.txtShow.text.toString().trim().isEmpty()) {
                Toast.makeText(activity, "Add Some Quote On Board First", Toast.LENGTH_SHORT).show()
            } else {
                databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseAuth.currentUser!!.uid).child("Quotes")
                    .child(binding.txtShow.text.trim().toString())

                val quote = UserQuoteDataClass(binding.txtShow.text.trim().toString())

                databaseReference.setValue(quote).addOnCompleteListener {
                    if (it.isSuccessful) {
                        binding.lottieAnimationViewsave?.visibility =View.GONE
                        Toast.makeText(activity, "Added To My Quotes", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("ErrorOfSave", it.exception?.message.toString())
                        Toast.makeText(
                            activity,
                            "Error: ${it.exception?.message.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }
        }

//        binding.textSaveToMyFav.setOnClickListener {
//            if (binding.txtShow.text.toString().trim().isEmpty()) {
//                Toast.makeText(activity, "Add Some Quote On Board First", Toast.LENGTH_SHORT).show()
//            } else {
//                databaseReference = FirebaseDatabase.getInstance().getReference("Users")
//                    .child(firebaseAuth.currentUser!!.uid).child("Favourites")
//                    .child(binding.txtShow.text.trim().toString())
//
//                val quote1 = UserQuoteDataClass(binding.txtShow.text.trim().toString())
//
//                databaseReference.setValue(quote1).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        Toast.makeText(activity, "Added To Favourites", Toast.LENGTH_SHORT).show()
//                        //     binding.etWrite.text?.clear()
//                    } else {
//                        Toast.makeText(
//                            activity,
//                            "Error: ${it.exception?.message.toString()}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            }
//        }



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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}