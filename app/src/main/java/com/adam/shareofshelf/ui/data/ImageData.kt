package com.adam.shareofshelf.ui.data

import com.google.gson.annotations.SerializedName

data class ImageData(
    @SerializedName("full_image") val fullImage: String? = null,
    @SerializedName("brand_id") val brandId: String? = null,
    @SerializedName("customer_id") val customerId: String? = null
)
