package com.adam.shareofshelf.retrofit

import com.adam.shareofshelf.ui.data.CustomerDataModel
import retrofit2.Call
import retrofit2.http.*

interface DaeemServiceInterface {

    @FormUrlEncoded
    @POST("app/sign_in.php")
    fun authenticateUser(
     @Field("username") username : String,
     @Field("password") password : String
    ): Call<String>
    @FormUrlEncoded
    @POST("app/pos.php")
    fun fetchListOfCustomers(
        @Field("id") id : String,
        @Field("branch_id") branch_id : String
    ): Call<ArrayList<CustomerDataModel>>

}