package com.sangam.quonote.explore2

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {
    @Headers("X-Api-Key: b+iPwAySewJoY0fQMO1A4w==7jd2HY04eYkJxypI")
    @GET("quotes")
    fun getData(@Query("category") category: String): Call<List<QuoteResponseItem>>
}