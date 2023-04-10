package com.adam.shareofshelf.retrofit

import com.adam.shareofshelf.ui.data.BranchDataModel
import com.adam.shareofshelf.ui.data.CustomerDataModel
import com.adam.shareofshelf.ui.data.ImageData
import com.adam.shareofshelf.ui.data.SubmitDataRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
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

    @Headers("Content-Type: application/json")
    @POST("app/sos.php")
    fun saveSOS(
        @Body dataModel: SubmitDataRequest
    ): Call<ImageData>

}