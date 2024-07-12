package com.sangam.quonote

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sangam.quonote.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignInBinding
    lateinit var firebaseAuth: FirebaseAuth
    private val emailpattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private lateinit var client: GoogleSignInClient
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credentials).addOnCompleteListener {
            if (it.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "SignIn Successful", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.textView2.animate().apply {
            duration = 1000
            rotationXBy(360f)
        }
        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        client = GoogleSignIn.getClient(this, gso)
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.TextForgotPass.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.forgot_password_dialog, null)
            builder.setView(view)
            builder.create().show()
            builder.setCancelable(true)
            val UserEmail = view.findViewById<EditText>(R.id.ETResetEmail)
            val button = view.findViewById<Button>(R.id.btnForgotPass)
            button.setOnClickListener {
                val email = UserEmail.text.toString()
                if (email.trim().isEmpty()) {
                    UserEmail.error = "Empty Field"
                } else if (!email.matches(emailpattern.toRegex())) {
                    UserEmail.error = "Enter Valid Email"
                } else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
                            builder.create().dismiss()
                        } else {
                            Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT)
                                .show()
                            builder.create().dismiss()

                        }
                    }
                }
            }
        }
        binding.txtsignin.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.btnSignIn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()
            binding.progreessbarSignIn.visibility = View.VISIBLE

            if (email.trim().isEmpty() || password.trim().isEmpty()) {
                if (email.trim().isEmpty()) binding.emailEt.error = "Empty Field"
                if (password.trim().isEmpty()) binding.passET.error = "Empty Field"
                binding.progreessbarSignIn.visibility = View.GONE
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false

            } else if (!email.matches(emailpattern.toRegex()) || password.length < 6) {
                if (!email.matches(emailpattern.toRegex())) binding.emailEt.error =
                    "Enter Valid Email"
                if (password.length < 6) binding.passET.error =
                    "Enter Password More than 6 characters"
                binding.progreessbarSignIn.visibility = View.GONE
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false

            } else {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "SignIn Successful", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Error ${it.exception?.message}", Toast.LENGTH_SHORT)
                            .show()
                        binding.progreessbarSignIn.visibility = View.GONE
                    }
                }
            }
        }
//        binding.btnSignInGoogle.setOnClickListener {
//            val signInClient = client.signInIntent
//            launcher.launch(signInClient)
//        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}