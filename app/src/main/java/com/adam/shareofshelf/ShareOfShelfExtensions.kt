package com.adam.shareofshelf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import java.io.ByteArrayOutputStream


object ShareOfShelfExtensions {

    var mBitmapOBj : Bitmap? = null

    fun View.takeScreenShot() : Bitmap{
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }

    fun Bitmap.convertToByteArray(): ByteArray{
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
        this.recycle()
        return stream.toByteArray()
    }

    fun ByteArray.convertByteArrayToBitmap(): Bitmap{
        return  BitmapFactory.decodeByteArray(this,0,this.size)
    }
}