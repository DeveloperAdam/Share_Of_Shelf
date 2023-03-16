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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.adam.shareofshelf.ShareOfShelfExtensions.mBitmapOBj
import com.adam.shareofshelf.ShareOfShelfExtensions.mBitmapOBj2
import com.adam.shareofshelf.ShareOfShelfExtensions.takeScreenShot
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


const val TAG = "Device Support"
const val INTENT_RECEIVE_DATA = 0
const val PERMISSION_REQUEST_CODE = 1100
const val INTENT_TYPE_SELECTION = "1000"
const val INTENT_CAMERA_SELECTION = "1100"
class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    //Variables
    private var is2PointsSelected = false
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
        ivPreview1.setOnClickListener(this)
        ivPreview2.setOnClickListener(this)
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
            R.id.ivPreview1 ->{
                startActivity(Intent(this@DashboardActivity,ImagePreviewActivity::class.java).apply {
                    putExtra(INTENT_TYPE_SELECTION, true)
                })
            }
            R.id.ivPreview2 ->{
                startActivity(Intent(this@DashboardActivity,ImagePreviewActivity::class.java).apply {
                    putExtra(INTENT_TYPE_SELECTION, false)
                })
            }
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
        val btnSet = view.findViewById<Button>(R.id.btnSet)
        val etPointsCount = view.findViewById<EditText>(R.id.etPointsCount)
        val layoutEditText = view.findViewById<ConstraintLayout>(R.id.layoutEditText)

        builder.setView(view)
        btn2Points.setOnClickListener {
            is2PointsSelected = true
            gotoArScreen()
            builder.dismiss()
        }
        btnMultiPoints.setOnClickListener {
            layoutEditText.visibility = View.VISIBLE
        }
        btnSet.setOnClickListener {
            val points = etPointsCount.text.toString()
            Constants.maxNumMultiplePoints = points.toInt()
            is2PointsSelected = false
            gotoArScreen()
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
        ivPreview1.setImageResource(0)
        ivPreview2.setImageResource(0)
        mBitmapOBj?.recycle()
        mBitmapOBj2?.recycle()

        ivPreview1.visibility = View.GONE
        ivPreview2.visibility = View.GONE

    }

    private fun gotoArScreen() {

        startActivityForResult(Intent(this, ArActivity::class.java).apply {
            putExtra(INTENT_TYPE_SELECTION, is2PointsSelected)
            putExtra(INTENT_CAMERA_SELECTION, isFullCategorySelected)
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
            val value = DecimalFormat("##.##").format(sosValue)
            tvSOSValue.text = value
        }
        if (isFullCategorySelected)
            ivFullCategoryCamera.setImageBitmap(mBitmapOBj)
        else
            ivCustomerCategoryCamera.setImageBitmap(mBitmapOBj)
    }
}

