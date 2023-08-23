package com.sangam.quonote.profile

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sangam.quonote.R
import com.sangam.quonote.UserQuoteDataClass

class FavouriteActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var myAdapter: MyAdapterFavourite
    lateinit var databaseReference: DatabaseReference
    lateinit var auth: FirebaseAuth
    var quoteList: ArrayList<UserQuoteDataClass> = arrayListOf()
    lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite_activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility or
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        getFavQuotes()
        getCount()
        textView = findViewById(R.id.textshowallfav)

    }

    private fun getCount() {
        val countReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(auth.currentUser!!.uid).child("Favourites")

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val count = snapshot.childrenCount
                    textView.text = count.toString()
                } else {
                    textView.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FavouriteActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        countReference.addValueEventListener(valueEventListener)
    }

    private fun deleteFromFavourite(quote: String? = null) {
        val lottie = findViewById<View>(R.id.lottieAnimationViewDelete)
        lottie.visibility = View.VISIBLE

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(auth.currentUser!!.uid).child("Favourites").child(quote!!)
        databaseReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                lottie.visibility = View.GONE
                Toast.makeText(this, "Removed Successfully", Toast.LENGTH_SHORT).show()

                // Remove the quote from the quoteList
                val iterator = quoteList.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    if (item.quote == quote) {
                        iterator.remove()
                        break
                    }
                }

                // Update the RecyclerView adapter
                myAdapter.notifyDataSetChanged()

                // Update the count TextView
                getCount()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun dialogBox(quotename: String? = null) {
        val alertDialog = AlertDialog.Builder(this@FavouriteActivity)
        val view = layoutInflater.inflate(R.layout.removefromfav_dialog_box, null)
        alertDialog.setView(view)
        val delete = view.findViewById<TextView>(R.id.removifromfavtxt)
        val share = view.findViewById<TextView>(R.id.sharequotetxt)

        val dialog = alertDialog.create()
        dialog.show()
        share.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, quotename.toString().trim())
            startActivity(intent)
        }
        delete.setOnClickListener {
            deleteFromFavourite(quotename)
            dialog.dismiss()
        }
    }

    private fun getFavQuotes() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(auth.currentUser!!.uid).child("Favourites")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FavQuoteDataFrmFirebase", snapshot.toString())
                if (snapshot.exists()) {
                    quoteList = ArrayList()
                    for (quoteSnapshot in snapshot.children) {
                        val quote1 = quoteSnapshot.getValue(UserQuoteDataClass::class.java)
                        quoteList.add(quote1!!)
                    }
                    myAdapter = MyAdapterFavourite(this@FavouriteActivity, quoteList)
                    recyclerView.adapter = myAdapter
                    progressDialog.dismiss()
                    myAdapter.setOnItemClickListener(object :
                        MyAdapterFavourite.onItemClickListener {
                        override fun onItemClick(quote: String?) {
                            dialogBox(quote)
                        }
                    })
                } else {
                    Toast.makeText(
                        this@FavouriteActivity,
                        "No Item In Favourites",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FavouriteActivity, "Cannot Load Data", Toast.LENGTH_SHORT)
                    .show()
                progressDialog.dismiss()
            }
        })
    }
}