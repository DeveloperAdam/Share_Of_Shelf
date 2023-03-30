package com.adam.shareofshelf.retrofit

import retrofit2.Call
import retrofit2.http.*

interface DaeemServiceInterface {

    @FormUrlEncoded
    @POST("app/sign_in.php")
    fun authenticateUser(
     @Field("username") username : String,
     @Field("password") password : String
    ): Call<String>
}