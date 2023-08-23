package com.sangam.quonote.explore2

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.sangam.quonote.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuoteFragment : Fragment() {
    lateinit var spinner: Spinner

    lateinit var textView: TextView
    lateinit var textView2: TextView
    lateinit var btnRefreshQuote: Button
    lateinit var buttonCopy: Button
    lateinit var selectedItem: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_quote, container, false)
        spinner = view.findViewById(R.id.spinner)
        textView = view.findViewById(R.id.textViewQuote)
        textView2 = view.findViewById(R.id.textViewAuthor)
        btnRefreshQuote = view.findViewById(R.id.btnRefreshQuote)
        buttonCopy = view.findViewById(R.id.btnCopyquote)


        val clipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem != "Select a category") {
                        getQuotes()
                    Toast.makeText(
                        activity,
                        "You Searched For $selectedItem Quotes",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(activity, "Select a Category first", Toast.LENGTH_SHORT).show()

            }
        }


        buttonCopy.setOnClickListener {
            val textToCopy = textView.text.toString()

            if (textToCopy.trim().isEmpty()) {
                Toast.makeText(activity, "No Text To Copy", Toast.LENGTH_SHORT).show()

            } else {

                val clipData = ClipData.newPlainText("Quote", textToCopy)
                clipboardManager.setPrimaryClip(clipData)

                Toast.makeText(activity, "Quote Copied", Toast.LENGTH_SHORT).show()
            }

        }

        btnRefreshQuote.setOnClickListener {
                getQuotes()
        }
        return view
    }

        fun getQuotes() {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val quoteSearch = selectedItem
        if (selectedItem == "Select a category") {
            progressDialog.dismiss()
            Toast.makeText(activity, "Select a Category first", Toast.LENGTH_SHORT).show()

        } else {

            RetrofitInstance.apiInterface.getData(quoteSearch)
                .enqueue(object : Callback<List<QuoteResponseItem>> {
                    override fun onResponse(
                        call: Call<List<QuoteResponseItem>>,
                        response: Response<List<QuoteResponseItem>>
                    ) {
                        if (response.isSuccessful) {
                            val quoteItems = response.body() ?: emptyList()
                            val quotes = quoteItems.map { it.quote }
                            val author = quoteItems.map { it.author }

                            Log.d("ApiQuotes", quotes.toString())

                            textView.text = quotes.toString()
                            textView2.text = "Author: ${author.toString()}"
                            progressDialog.dismiss()
                        } else {
                            Log.d("ApiQuotes", response.errorBody().toString())

                            Toast.makeText(
                                activity,
                                response.errorBody().toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            progressDialog.dismiss()
                        }
                        progressDialog.dismiss()
                    }

                    override fun onFailure(call: Call<List<QuoteResponseItem>>, t: Throwable) {
                        Toast.makeText(
                            activity,
                            t.localizedMessage!!.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialog.dismiss()
                    }
                })
        }


    }

}