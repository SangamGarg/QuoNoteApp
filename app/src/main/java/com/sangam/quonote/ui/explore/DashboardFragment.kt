package com.sangam.quonote.ui.explore

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.sangam.quonote.databinding.FragmentDashboardBinding
import com.sangam.quonote.explore.ImageShowActivity
import com.sangam.quonote.explore.MyAdapterImage
import com.sangam.quonote.explore.PexelsResponse
import com.sangam.quonote.explore.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    lateinit var myAdapter: MyAdapterImage
    private var currentPage = 1

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.recyclerView.layoutManager = GridLayoutManager(activity, 2)
        binding.btnSearchQuote.setOnClickListener {
            getQuotes(1)
        }

        binding.btnRefreshQuote.setOnClickListener {
            currentPage++
            if (currentPage > 6) {
                currentPage = 1
            }
            getQuotes(currentPage)
        }
        return root
    }

    private fun getQuotes(pageNo: Int) {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val quoteSearch = binding.ETSearchQuote.text.toString()
        if (quoteSearch.trim().isEmpty()) {
            binding.ETSearchQuote.error = "Empty Field"
            progressDialog.dismiss()
        } else {

            RetrofitInstance.apiInterface.getData(
                binding.ETSearchQuote.text.toString(),
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
                            binding.recyclerView.adapter = myAdapter
                            progressDialog.dismiss()
                            Toast.makeText(
                                activity,
                                "You Searched For $quoteSearch Images",
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
                                        binding.ETSearchQuote.text.toString()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}