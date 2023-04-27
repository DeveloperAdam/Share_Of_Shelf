package com.adam.shareofshelf.retrofit

import com.adam.shareofshelf.ui.data.BranchDataModel
import com.adam.shareofshelf.ui.data.CustomerDataModel
import com.adam.shareofshelf.ui.data.ImageData
import retrofit2.Call
import retrofit2.http.*

interface DaeemServiceInterface {

    @FormUrlEncoded
    @POST("app/sign_in.php")
    fun authenticateUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<String>

    @FormUrlEncoded
    @POST("app/pos.php")
    fun fetchListOfCustomers(
        @Field("id") id: String
    ): Call<ArrayList<CustomerDataModel>>

    @FormUrlEncoded
    @POST("app/msl.php")
    fun fetchListOfBranches(
        @Field("customer_id") customerID: String,
        @Field("branch_id") branchID: String
    ): Call<ArrayList<BranchDataModel>>

    @FormUrlEncoded
    @POST("app/sos.php")
    fun saveSOS(
        @Field("brand_image") brand_image: String,
        @Field("full_image") full_image: String,
        @Field("customer_id") customer_id: String,
        @Field("brand_id") brand_id: String,
        @Field("branch_id") branch_id: String,
        @Field("total_sos") total_sos: String,
        @Field("brand_sos") brand_sos: String,
        @Field("result") result: String
    ): Call<ImageData>

}