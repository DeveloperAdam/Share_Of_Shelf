package com.adam.shareofshelf.ui.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CustomerDataModel(
    @SerializedName("branch_id") val branchId: String? = null,
    @SerializedName("retailer_id") val retailerId: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("city_id") val cityId: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("branch_name") val branchName: String? = null,
    @SerializedName("coordinates") val coordinates: String? = null,
    @SerializedName("class") val branchClass: String? = null,
    @SerializedName("customer_name_english") val customerName: String? = null,
    @SerializedName("customer_id") val customerId: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(branchId)
        parcel.writeString(retailerId)
        parcel.writeString(region)
        parcel.writeString(cityId)
        parcel.writeString(categoryId)
        parcel.writeString(branchName)
        parcel.writeString(coordinates)
        parcel.writeString(branchClass)
        parcel.writeString(customerName)
        parcel.writeString(customerId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CustomerDataModel> {
        override fun createFromParcel(parcel: Parcel): CustomerDataModel {
            return CustomerDataModel(parcel)
        }

        override fun newArray(size: Int): Array<CustomerDataModel?> {
            return arrayOfNulls(size)
        }
    }
}

