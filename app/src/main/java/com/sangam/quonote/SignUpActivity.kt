package com.sangam.quonote

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sangam.quonote.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    private val emailpattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.textView2.animate().apply {
            duration =1000
            rotationXBy(360f)
        }

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.txtsignup.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()
            val confirmpassword = binding.confirmPassEt.text.toString()
            val name = binding.nameET.text.toString()
            val phoneno = binding.phoneEt.text.toString()

            binding.progreessbarSignUp.visibility = View.VISIBLE
            binding.passwordLayout.isPasswordVisibilityToggleEnabled = true
            binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = true


            if (email.trim().isEmpty() || password.trim().isEmpty() || confirmpassword.trim()
                    .isEmpty() || name.trim().isEmpty() || phoneno.trim().isEmpty()
            ) {
                if (email.isEmpty()) binding.emailEt.error = "Empty Field"
                if (name.isEmpty()) binding.nameET.error = "Empty Field"
                if (phoneno.isEmpty()) binding.phoneEt.error = "Empty Field"
                if (password.isEmpty()) {
                    binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
                    binding.passET.error = "Empty Field"
                }
                if (confirmpassword.isEmpty()) {
                    binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
                    binding.confirmPassEt.error = "Empty Field"
                }
                Toast.makeText(this, "Enter Valid Details", Toast.LENGTH_SHORT).show()
                binding.progreessbarSignUp.visibility = View.GONE

            } else if (!email.matches(emailpattern.toRegex())) {
                binding.emailEt.error = "Enter Valid Email"
                binding.progreessbarSignUp.visibility = View.GONE

            } else if (phoneno.length !=10) {
                binding.phoneEt.error = "Enter Valid Phone No"
                binding.progreessbarSignUp.visibility = View.GONE


            } else if (password.length < 6) {
                binding.passET.error = "Enter Password more than 6 Characters"
                binding.progreessbarSignUp.visibility = View.GONE
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false

            } else if (password != confirmpassword) {
                binding.confirmPassEt.error = "Password is Not Matching"
                binding.progreessbarSignUp.visibility = View.GONE
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
                binding.confirmPasswordLayout.isPasswordVisibilityToggleEnabled = false
            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        clearData()
                        val databaseReference =
                            database.reference.child("Users").child(auth.currentUser!!.uid)
                                .child("User Profile")
                        val users: UserDataClass =
                            UserDataClass(name, email, phoneno, auth.currentUser!!.uid)
                        databaseReference.setValue(users).addOnCompleteListener {
                            if (it.isSuccessful) {
                                val intent = Intent(this, SignInActivity::class.java)
                                startActivity(intent)
                                Toast.makeText(this, "SignUp Successful", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Error ${it.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.progreessbarSignUp.visibility = View.GONE

                            }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "SignUp Failed ${it.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.progreessbarSignUp.visibility = View.GONE

                    }
                }
            }
        }
    }
    private fun clearData() {
        val sharedPreferences = getSharedPreferences("sharedpreference", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.remove("shared")
        editor.remove("Finished")
        editor.apply()
    }
}