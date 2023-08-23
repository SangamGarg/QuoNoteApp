package com.sangam.quonote.explore2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofitInstance by lazy {
        Retrofit.Builder().baseUrl("https://api.api-ninjas.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiInterface by lazy {
        retrofitInstance.create(ApiService::class.java)
    }
}