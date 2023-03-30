package com.adam.shareofshelf.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val baseUrl = "https://www.daaemsolutions.com/daaem/"
    fun getInstance(): Retrofit {


        val mOkHttpClient = OkHttpClient
            .Builder()
            .build()


        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(mOkHttpClient)
            .build()
    }
}