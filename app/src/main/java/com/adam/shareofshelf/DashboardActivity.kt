package com.adam.shareofshelf

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.adam.shareofshelf.ShareOfShelfExtensions.mBitmapOBj
import com.adam.shareofshelf.ShareOfShelfExtensions.takeScreenShot
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


const val TAG = "Device Support"
const val INTENT_RECEIVE_DATA = 0
const val PERMISSION_REQUEST_CODE = 1100
const val INTENT_TYPE_SELECTION = "1000"

class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    //Variables

    private var permissionsGranted = false
    private var sosValue = 0.0
    private var fullCategorySpace = ""
    private var customerCategorySpace = ""
    private val MinOpenGlVersin = 3.0
    private var isFullCategorySelected = false

    //Views
    private lateinit var btnClear: Button
    private lateinit var btnSave: Button
    private lateinit var tvFullCategoryValue: TextView
    private lateinit var tvCustomerCategoryValue: TextView
    private lateinit var ivFullCategoryCamera: ImageView
    private lateinit var ivCustomerCategoryCamera: ImageView

    val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        supportActionBar?.hide()
        if (!checkIsSupportedDeviceOrFinish()) {
            Toast.makeText(
                applicationContext,
                getString(R.string.device_not_supported),
                Toast.LENGTH_LONG
            )
                .show()
        } else {
            bindViews()
        }

    }

    private fun bindViews() {

        //set the title
        supportActionBar?.let {
            it.setTitle(R.string.app_name)
            it.setDisplayShowHomeEnabled(false)
            it.setDisplayUseLogoEnabled(false)
        }

        btnSave = findViewById(R.id.btnSave)
        btnClear = findViewById(R.id.btnClear)
        tvFullCategoryValue = findViewById(R.id.tvFullCategoryValue)
        tvCustomerCategoryValue = findViewById(R.id.tvCustomerCategoryValue)
        ivFullCategoryCamera = findViewById(R.id.ivCameraFullCategory)
        ivCustomerCategoryCamera = findViewById(R.id.ivCameraCustomerCategory)


        btnSave.setOnClickListener(this)
        btnClear.setOnClickListener(this)
        ivFullCategoryCamera.setOnClickListener(this)
        ivCustomerCategoryCamera.setOnClickListener(this)

        askUserForFilePermission()

    }


    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        val openGlVersionString =
            (Objects.requireNonNull(
                this
                    .getSystemService(Context.ACTIVITY_SERVICE)
            ) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MinOpenGlVersin) {
            Log.e(TAG, "Sceneform requires OpenGL ES ${MinOpenGlVersin} later")
            Toast.makeText(
                this,
                "Sceneform requires OpenGL ES ${MinOpenGlVersin} or later",
                Toast.LENGTH_LONG
            )
                .show()
            finish()
            return false
        }
        return true
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.ivCameraFullCategory -> {
                isFullCategorySelected = true
                if (permissionsGranted)
                    showDialog()
                else
                    checkExternalStoragePermission()
            }
            R.id.ivCameraCustomerCategory -> {
                isFullCategorySelected = false
                if (permissionsGranted)
                    showDialog()
                else
                    checkExternalStoragePermission()
            }
            R.id.btnClear -> clearValues()

            R.id.btnSave -> {
                saveBitmapToDisk(layoutParent.takeScreenShot())
            }
        }

    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.custom_dialog, null)
        val btn2Points = view.findViewById<Button>(R.id.btn2Points)
        val btnMultiPoints = view.findViewById<Button>(R.id.btnMultiplePoints)

        builder.setView(view)
        btn2Points.setOnClickListener {
            gotoArScreen(is2PointsSelected = true)
            builder.dismiss()
        }
        btnMultiPoints.setOnClickListener {
            gotoArScreen(is2PointsSelected = false)
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    @Throws(IOException::class)
    fun saveBitmapToDisk(bitmap: Bitmap) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Screenshots"
        )
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.US)
        val formattedDate: String = df.format(c.time)
        val mediaFile = File(
            file,
            "FieldVisualizer$formattedDate.jpeg"
        )
        tvURI.text = mediaFile.absolutePath
        if (file.mkdirs()) {
            val fileOutputStream = FileOutputStream(mediaFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        }

    }

    private fun clearValues() {
        sosValue = 0.0
        fullCategorySpace = ""
        customerCategorySpace = ""

        tvSOSValue.text = ""
        tvFullCategoryValue.text = ""
        tvCustomerCategoryValue.text = ""
        ivFullCategoryCamera.setImageResource(0)
        ivCustomerCategoryCamera.setImageResource(0)
        mBitmapOBj?.recycle()

        ivFullCategoryCamera.setImageResource(R.drawable.ic_camera)
        ivCustomerCategoryCamera.setImageResource(R.drawable.ic_camera)
    }

    private fun gotoArScreen(is2PointsSelected: Boolean) {

        startActivityForResult(Intent(this, ArActivity::class.java).apply {
            putExtra(INTENT_TYPE_SELECTION, is2PointsSelected)
        }, INTENT_RECEIVE_DATA)
    }

    private fun requestExternalStoragePermission(): Boolean {
        return if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            val builder = AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("Alert!")
                .setMessage("External storage permission allows us to access data from storage. Please allow in App Settings for additional functionality.")
                .setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
            builder.show()
            false
        } else {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                PERMISSION_REQUEST_CODE
            )
            true
        }
    }

    private fun askUserForFilePermission() {
        permissionsGranted = if (!checkExternalStoragePermission()) {
            requestExternalStoragePermission()
        } else
            true
    }

    private fun checkExternalStoragePermission(): Boolean {
        val result = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return if (result == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestExternalStoragePermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == INTENT_RECEIVE_DATA) {
            if (resultCode == RESULT_OK) {
                data?.let {
                    it.getStringExtra(INTENT_RECEIVE_DATA.toString())?.let { value ->
                        if (isFullCategorySelected) {
                            fullCategorySpace = value
                        } else {
                            customerCategorySpace = value
                        }

                        setValues()
                    }
                }
            }
        }
    }

    private fun setValues() {
        tvFullCategoryValue.text = fullCategorySpace.plus(" ${getString(R.string.unit)}")
        tvCustomerCategoryValue.text = customerCategorySpace.plus(" ${getString(R.string.unit)}")

        if (fullCategorySpace.isNotEmpty() && customerCategorySpace.isNotEmpty()) {
            sosValue = fullCategorySpace.toDouble() * customerCategorySpace.toDouble()
            tvSOSValue.text = sosValue.toString()
        }

        if (isFullCategorySelected)
            ivFullCategoryCamera.setImageBitmap(mBitmapOBj)
        else
            ivCustomerCategoryCamera.setImageBitmap(mBitmapOBj)
    }
}

