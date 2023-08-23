package com.sangam.quonote.profile

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class AllQuotesActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var myAdapter: MyAdapterQuote
    lateinit var databaseReference: DatabaseReference
    lateinit var auth: FirebaseAuth
    lateinit var quoteList: ArrayList<UserQuoteDataClass>
    lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_quotes)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility or
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        getQuotes()
        getCount()
        textView = findViewById(R.id.textshowallquotes)
    }


    private fun getCount() {
        val countReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(auth.currentUser!!.uid).child("Quotes")

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
                Toast.makeText(this@AllQuotesActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        countReference.addValueEventListener(valueEventListener)
    }

    private fun addToFavourite(quote: String? = null) {
        val lottie = findViewById<View>(R.id.lottieAnimationViewFav)
        lottie.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(auth.currentUser!!.uid).child("Favourites").child(quote!!)
            val quote1 = UserQuoteDataClass(quote)
            databaseReference.setValue(quote1).addOnCompleteListener {
                if (it.isSuccessful) {
                    lottie.visibility = View.GONE

                    Toast.makeText(this, "Added To Favourites", Toast.LENGTH_SHORT).show()
                    //     binding.etWrite.text?.clear()
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${it.exception?.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }, 1000)


    }

    private fun deleteQuote(quote: String? = null) {
        val lottie = findViewById<View>(R.id.lottieAnimationViewDelete)
        lottie.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(auth.currentUser!!.uid).child("Quotes").child(quote!!)
            databaseReference.removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    lottie.visibility = View.GONE
                    Toast.makeText(this, "Delete Successfully", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Error: ${it.exception?.message}", Toast.LENGTH_SHORT)
                        .show()

                }
            }
        }, 1000)


    }

    private fun dialogBox(quotename: String? = null) {
        val alertDialog = AlertDialog.Builder(this@AllQuotesActivity)
        val view = layoutInflater.inflate(R.layout.addtofav_dialog_box, null)
        alertDialog.setView(view)
        val fav = view.findViewById<TextView>(R.id.savetofavtxt)
        val delete = view.findViewById<TextView>(R.id.deletequotetxt)
        val share = view.findViewById<TextView>(R.id.sharequotetxt)

        val dialog = alertDialog.create()
        dialog.show()
        share.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, quotename.toString().trim())
            startActivity(intent)
        }



        fav.setOnClickListener {
            addToFavourite(quotename)
            dialog.dismiss()
        }
        delete.setOnClickListener {
            deleteQuote(quotename)
            dialog.dismiss()
        }
    }

    private fun getQuotes() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(auth.currentUser!!.uid).child("Quotes")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("QuoteDataFromFirebase", snapshot.toString())
                if (snapshot.exists()) {
                    quoteList = ArrayList()
                    for (quoteSnapshot in snapshot.children) {
                        val quote1 = quoteSnapshot.getValue(UserQuoteDataClass::class.java)
                        quoteList.add(quote1!!)
                    }
                    myAdapter = MyAdapterQuote(this@AllQuotesActivity, quoteList)
                    recyclerView.adapter = myAdapter
                    progressDialog.dismiss()

                    myAdapter.setOnItemClickListener(object : MyAdapterQuote.onItemClickListener {
                        override fun onItemClick(quote: String?) {
                            Log.d("saregama", quote!!)
//                            Toast.makeText(this@AllQuotesActivity, quote, Toast.LENGTH_SHORT).show()
                            dialogBox(quote)
                        }
                    })

                } else {
                    Toast.makeText(
                        this@AllQuotesActivity,
                        "No Item Added In Quotes",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AllQuotesActivity, "Cannot Load Data", Toast.LENGTH_SHORT)
                    .show()
                progressDialog.dismiss()
            }
        })
    }
}