package com.adam.shareofshelf.ui.data

import com.google.gson.annotations.SerializedName

data class SubmitDataRequest(
    @SerializedName("full_image")
    val fullImage: String,
    @SerializedName("brand_image")
    val brandImage: String,
    @SerializedName("total_sos")
    val totalSos: String,
    @SerializedName("brand_sos")
    val brandSos: String,
    @SerializedName("brand_id")
    val brandId: String,
    @SerializedName("customer_id")
    val customerId: String,
    @SerializedName("branch_id")
    val branchId: String
)
