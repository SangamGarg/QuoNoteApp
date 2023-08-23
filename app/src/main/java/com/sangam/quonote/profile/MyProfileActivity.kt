package com.sangam.quonote.profile

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.sangam.quonote.R
import com.sangam.quonote.SignInActivity
import com.sangam.quonote.UserDataClass
import com.sangam.quonote.databinding.ActivityMyProfileBinding
import java.io.File

class MyProfileActivity : AppCompatActivity() {

    lateinit var imageUri: Uri
    private var uri: Uri? = null
    private var storageRef = Firebase.storage
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var binding: ActivityMyProfileBinding
    private val contract =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                binding.imageViewProfile.setImageURI(imageUri)
                uri = imageUri

            } else {
                binding.imageViewProfile.setImageURI(uri)
                uri = null
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    private val requestForPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                contract.launch(imageUri)
            } else {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    showRationaleDialog()
                } else {
                    val message =
                        "You've denied camera permission twice. To enable it, open app settings."
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()
        getImage()
        getData()

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    binding.imageViewProfile.setImageURI(uri)
                    this.uri = uri
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }
        binding.imageViewProfile.setOnClickListener {
            openProfilePhoto()
        }
        imageUri = createImageUri()!!
        binding.textChoose.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_choose_image, null)
            alertDialog.setView(view)
            alertDialog.create().show()
            val gallery = view.findViewById<TextView>(R.id.textOpenGallery)
            gallery.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

            }
            val camera = view.findViewById<TextView>(R.id.textOpenCamera)
            camera.setOnClickListener {
                if (checkPermission()) {
                    contract.launch(imageUri)
                } else {
                    requestForPermission.launch(android.Manifest.permission.CAMERA)
                }

            }

        }

        binding.UploadImage.setOnClickListener {

            uploadImage()
        }

        binding.editNAmeAndPhone.setOnClickListener {
            editNameAndPhone()
        }
        binding.textDeleteAccount.setOnClickListener {
            deleteAlertDialog()
        }
        binding.textUpdatePassword.setOnClickListener {
            updatePassword()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showRationaleDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Camera Permission")
            .setMessage("This app requires camera permission to take profile photos. If you deny this time you have to manually go to app setting to allow permission.")
            .setPositiveButton("Ok") { _, _ ->
                requestForPermission.launch(android.Manifest.permission.CAMERA)
            }
        builder.create().show()
    }

    private fun checkPermission(): Boolean {
        val permission = android.Manifest.permission.CAMERA
        return ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createImageUri(): Uri? {
        val image = File(applicationContext.filesDir, "profile_photos.png")
        return FileProvider.getUriForFile(
            applicationContext, "com.sangam.quonote.fileProvider", image
        )
    }

    private fun openProfilePhoto() {
        val alertDialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_open, null)
        alertDialog.setView(view)
        alertDialog.setCancelable(true)

        val profile = view.findViewById<ImageView>(R.id.imgShowProfile)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseAuth.currentUser!!.uid).child("User Image")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val imageUrl = snapshot.child("url").getValue(String::class.java)
                    Glide.with(this@MyProfileActivity).load(imageUrl!!).into(profile)
                } else {
                    Toast.makeText(
                        this@MyProfileActivity, "No Profile Photo uploaded", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MyProfileActivity, "Cannot Load Profile Image", Toast.LENGTH_SHORT
                ).show()
            }
        })
        alertDialog.create().show()
    }

    //This is the function to update the user password
    private fun updatePassword() {
        val alertDialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.password_update_dialog, null)
        alertDialog.setView(view)
        alertDialog.setCancelable(true)
        alertDialog.setTitle("Update Password")
        alertDialog.create().show()
        val password = view.findViewById<EditText>(R.id.ETUpdatePassword)
        val button = view.findViewById<Button>(R.id.btnPassUpdate)
        button.setOnClickListener {
            val userPass = password.text.toString()
            if (userPass.trim().isEmpty()) {
                password.error = "Empty Field"
            } else if (userPass.length < 6) {
                password.error = "Enter Password More than 6 characters"
            } else {
                val user = Firebase.auth.currentUser
                user?.updatePassword(userPass)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this@MyProfileActivity,
                            "Password Updated Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        alertDialog.create().dismiss()
                        val firebaseAuth = FirebaseAuth.getInstance()
                        firebaseAuth.signOut()
                        Toast.makeText(
                            this@MyProfileActivity, "Please Login Again", Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@MyProfileActivity, SignInActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@MyProfileActivity,
                            "Error: ${it.exception?.message.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        alertDialog.create().dismiss()

                    }
                }
            }
        }
    }

    private fun deleteAlertDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setCancelable(false)
        alertDialog.setTitle("Delete Account")
        alertDialog.setMessage("Are You Sure?")


        alertDialog.setPositiveButton("yes") { _, _ ->
            deleteAccount()
        }
        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.create().show()

    }

    //This is the function to delete the user account
    private fun deleteAccount() {
        val user = Firebase.auth.currentUser
        user?.delete()?.addOnCompleteListener {
            if (it.isSuccessful) {
                clearData()
                Toast.makeText(
                    this@MyProfileActivity, "Account Deleted Successfully", Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            } else {
                Log.e("DeleteAccount", it.exception.toString())
                Toast.makeText(
                    this@MyProfileActivity,
                    "Error: ${it.exception?.message.toString()} ",
                    Toast.LENGTH_SHORT
                ).show()

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
    //This is the function to get the image of user and show in image view

    private fun getImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading Profile")
        progressDialog.setCancelable(false)
        progressDialog.show()


        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseAuth.currentUser!!.uid).child("User Image")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val imageUrl = snapshot.child("url").getValue(String::class.java)
                    Glide.with(this@MyProfileActivity).load(imageUrl!!)
                        .into(binding.imageViewProfile)
                    progressDialog.dismiss()
                    binding.imageViewProfile.animate().apply {
                        duration = 1500
                        rotationYBy(360f)
                    }
                } else {
                    Toast.makeText(
                        this@MyProfileActivity, "No Profile Photo uploaded", Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MyProfileActivity, "Cannot Load Profile Image", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    //This is the function to edit the name and phone of user

    private fun editNameAndPhone() {
        val alertDialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.edit_dialogbox, null)
        alertDialog.setView(view)
        alertDialog.setCancelable(true)
        val dialog = alertDialog.create()
        dialog.show()
        val name = view.findViewById<EditText>(R.id.ETEditName)
        val phone = view.findViewById<EditText>(R.id.ETEditPhone)
        val button = view.findViewById<Button>(R.id.savebutton)

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseAuth.currentUser!!.uid).child("User Profile")

        button.setOnClickListener {
            val username = name.text.toString().trim()
            val userphone = phone.text.toString().trim()

            if (username.isEmpty() || userphone.isEmpty()) {
                if (username.isEmpty()) {
                    name.error = "Empty Field"
                }
                if (userphone.isEmpty()) {
                    phone.error = "Empty Field"
                }
            } else if (userphone.length != 10) {
                phone.error = "Enter Valid Phone No."
            } else {
                val updateInfo = mapOf(
                    "name" to username, "phone" to userphone
                )

                databaseReference.updateChildren(updateInfo).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Data Updated Successfully", Toast.LENGTH_SHORT)
                            .show()
                        binding.textName.text = username
                        binding.textPhoneNo.text = userphone
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            this, "Error : ${task.exception?.message}", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    //This is the function to upload the image of user

    private fun uploadImage() {
        if (uri == null) {
            Toast.makeText(this, "Please choose an image first", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Uploading")
        progressDialog.show()

        val imageRef =
            storageRef.reference.child("images").child(System.currentTimeMillis().toString())
        imageRef.putFile(uri!!).addOnSuccessListener { task ->
            task.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val mapImage = mapOf(
                    "url" to uri.toString()
                )
                databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseAuth.currentUser!!.uid).child("User Image")
                databaseReference.setValue(mapImage).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT)
                            .show()
                        progressDialog.dismiss()
                        reloadActivity()
                    } else {
                        Toast.makeText(
                            this, "${task.exception?.message}", Toast.LENGTH_SHORT
                        ).show()
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun reloadActivity() {
        recreate()
    }

    //This is the function to get the profile data of user
    private fun getData() {
//        val progressDialog = ProgressDialog(this)
//        progressDialog.setMessage("Loading Profile")
//        progressDialog.setCancelable(false)
//        progressDialog.show()

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(firebaseAuth.currentUser!!.uid).child("User Profile")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ProfileFromFirebase", snapshot.toString())
                if (snapshot.exists()) {
                    val profile = snapshot.getValue(UserDataClass::class.java)
                    binding.textEmail.text = profile?.email.toString()
                    binding.textPhoneNo.text = profile?.phone.toString()
                    binding.textName.text = profile?.name.toString()
//                    progressDialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}