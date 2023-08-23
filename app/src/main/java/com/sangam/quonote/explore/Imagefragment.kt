package com.sangam.quonote.explore

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sangam.quonote.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Imagefragment : Fragment() {
    lateinit var myAdapter: MyAdapterImage
    private var currentPage = 1
    lateinit var recyclerView: RecyclerView
    lateinit var editText: EditText
    lateinit var button: Button
    lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_imagefragment, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        button = view.findViewById(R.id.btnSearchImage)
        textView = view.findViewById(R.id.btnRefreshImage)
        editText = view.findViewById(R.id.ETSearchImage)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        button.setOnClickListener {
            getImages(1)
        }

        textView.setOnClickListener {
            currentPage++
            if (currentPage > 6) {
                currentPage = 1
            }
            getImages(currentPage)
        }
        return view
    }

    private fun getImages(pageNo: Int) {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val imageSearch = editText.text.toString()
        if (imageSearch.trim().isEmpty()) {
            editText.error = "Empty Field"
            progressDialog.dismiss()
        } else {

            RetrofitInstance.apiInterface.getData(
                editText.text.toString(),
                500,
                pageNo
            )
                .enqueue(object : Callback<PexelsResponse?> {
                    override fun onResponse(
                        call: Call<PexelsResponse?>,
                        response: Response<PexelsResponse?>
                    ) {
                        if (response.isSuccessful) {

                            Log.d("ApiQuote", response.body().toString())
                            val data = response.body()?.photos ?: emptyList()
                            myAdapter = MyAdapterImage(data)
                            recyclerView.adapter = myAdapter
                            progressDialog.dismiss()
                            editText.text.clear()
                            Toast.makeText(
                                activity,
                                "You Searched For $imageSearch Images",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            myAdapter.setOnItemClickListener(object :
                                MyAdapterImage.OnItemClickListener {
                                override fun onItemClick(photoUrl: String) {
                                    val intent = Intent(activity, ImageShowActivity::class.java)
                                    intent.putExtra("ImageUrl", photoUrl)
                                    intent.putExtra(
                                        "SearchImage",
                                        editText.text.toString()
                                    )
                                    startActivity(intent)
                                }

                            })


                        } else {
                            Toast.makeText(
                                activity,
                                "Error Something Went Wrong: ${response.errorBody().toString()}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            progressDialog.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<PexelsResponse?>, t: Throwable) {
                        Toast.makeText(
                            activity, "Error! Something Went Wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialog.dismiss()

                    }
                })
        }
    }

}