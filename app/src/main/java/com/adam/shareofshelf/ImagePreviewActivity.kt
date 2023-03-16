package com.adam.shareofshelf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.adam.shareofshelf.ShareOfShelfExtensions.mBitmapOBj
import com.adam.shareofshelf.ShareOfShelfExtensions.mBitmapOBj2
import kotlinx.android.synthetic.main.activity_image_preview.*

class ImagePreviewActivity : AppCompatActivity() {

    private var isPreview1Selected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)


        //set the title
        supportActionBar?.let {
            it.title = "Image Preview"
            it.setDisplayShowHomeEnabled(false)
            it.setDisplayUseLogoEnabled(false)
        }

        intent?.extras?.let {

            isPreview1Selected = it.getBoolean(INTENT_TYPE_SELECTION)
        }

        if(isPreview1Selected)
        ivImage.setImageBitmap(mBitmapOBj) else  ivImage.setImageBitmap(mBitmapOBj2)

    }
}