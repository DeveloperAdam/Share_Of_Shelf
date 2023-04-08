package com.adam.shareofshelf.retrofit

import com.adam.shareofshelf.ui.data.BranchDataModel
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
        @Field("id") id : String
    ): Call<ArrayList<CustomerDataModel>>

    @FormUrlEncoded
    @POST("app/msl.php")
    fun fetchListOfBranches(
        @Field("customer_id") customerID : String,
        @Field("branch_id") branchID : String
    ): Call<ArrayList<BranchDataModel>>

    @FormUrlEncoded
    @POST("app/sos.php")
    fun saveSOS(
        @Field("customer_id") customerID : String,
        @Field("branch_id") branchID : String,
        @Field("image") imageBase64 : String,
        @Field("brand_id") brandId : String
    ): Call<String>

}