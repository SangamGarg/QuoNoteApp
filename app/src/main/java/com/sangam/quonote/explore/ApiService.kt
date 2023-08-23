package com.sangam.quonote.explore

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {
    @Headers("Authorization: QlNh4bkVD2hgfQKGRwplXKgQexRsP99HJZwtkFEJ5zLEH5BGL33QxMTF")
    @GET("search")
    fun getData(
        @Query("query") query: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): Call<PexelsResponse>
}
