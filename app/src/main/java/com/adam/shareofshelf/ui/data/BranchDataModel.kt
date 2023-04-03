package com.adam.shareofshelf.ui.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class BranchDataModel(
    @SerializedName("brand_id") val brandId: String? = null,
    @SerializedName("sub_brand_id") val subBrandId: String? = null,
    @SerializedName("product_id") val productId: String? = null,
    @SerializedName("brand_name") val brandName: String? = null,
    @SerializedName("brand_name_arabic") val brandNameArabic: String? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("product_name_arabic") val productNameArabic: String? = null,
    @SerializedName("code") val code: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
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
        parcel.writeString(brandId)
        parcel.writeString(subBrandId)
        parcel.writeString(productId)
        parcel.writeString(brandName)
        parcel.writeString(brandNameArabic)
        parcel.writeString(productName)
        parcel.writeString(productNameArabic)
        parcel.writeString(code)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BranchDataModel> {
        override fun createFromParcel(parcel: Parcel): BranchDataModel {
            return BranchDataModel(parcel)
        }

        override fun newArray(size: Int): Array<BranchDataModel?> {
            return arrayOfNulls(size)
        }
    }
}
