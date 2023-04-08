package com.adam.shareofshelf.ui

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
import com.adam.shareofshelf.R
import com.adam.shareofshelf.retrofit.DaeemServiceInterface
import com.adam.shareofshelf.retrofit.RetrofitClient
import com.adam.shareofshelf.ui.adapter.OnBranchClickListener
import com.adam.shareofshelf.ui.data.BranchDataModel
import com.adam.shareofshelf.ui.data.CustomerDataModel
import com.adam.shareofshelf.utils.Constants
import com.adam.shareofshelf.utils.Constants.INTENT_CUSTOMER
import com.adam.shareofshelf.utils.ShareOfShelfExtensions.mBitmapOBj
import com.adam.shareofshelf.utils.ShareOfShelfExtensions.mBitmapOBj2
import com.adam.shareofshelf.utils.ShareOfShelfExtensions.takeScreenShot
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import android.util.Base64
import androidx.core.view.isVisible
import com.adam.shareofshelf.ui.adapter.OnItemClickListener
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


const val TAG = "Device Support"
const val INTENT_RECEIVE_DATA = 0
const val PERMISSION_REQUEST_CODE = 1100
const val INTENT_TYPE_SELECTION = "1000"
const val INTENT_CAMERA_SELECTION = "1100"
const val INTENT_MAX_DISTANCE = "1101"

class DashboardActivity : AppCompatActivity(), View.OnClickListener, OnBranchClickListener,
    AdapterView.OnItemSelectedListener {

    //Variables
    private var base64Image1 = ""
    private var base64Image2 = ""
    private var finalImageBase64 = ""
    private var is2PointsSelected = false
    private var permissionsGranted = false
    private var sosValue = 0.0
    private var fullCategorySpace = ""
    private var customerCategorySpace = ""
    private val minOpenGlVersion = 3.0
    private var isFullCategorySelected = false

    //Views
    private lateinit var btnClear: Button
    private lateinit var btnSave: Button
    private lateinit var tvFullCategoryValue: TextView
    private lateinit var tvCustomerCategoryValue: TextView
    private lateinit var ivFullCategoryCamera: ImageView
    private lateinit var ivCustomerCategoryCamera: ImageView

    //Data
    private var branchDataModel : BranchDataModel? = null
    private var customerDataModel: CustomerDataModel? = null
    private var branchList: ArrayList<BranchDataModel> = arrayListOf()

    private val permissionStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        if (!checkIsSupportedDeviceOrFinish()) {
            Toast.makeText(
                applicationContext, getString(R.string.device_not_supported), Toast.LENGTH_LONG
            ).show()
        } else {
            customerDataModel = intent?.getParcelableExtra(INTENT_CUSTOMER)
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
        ivPreview1.setOnClickListener(this)
        ivPreview2.setOnClickListener(this)

        askUserForFilePermission()
        fetchBranches()
    }

    private fun fetchBranches() {
        customerDataModel?.let {
            val retrofit = RetrofitClient.getInstance()
            val apiInterface = retrofit.create(DaeemServiceInterface::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    apiInterface.fetchListOfBranches(
                        customerID = it.customerId ?: "",
                        branchID = it.branchId ?: ""
                    ).enqueue(
                        object : Callback<ArrayList<BranchDataModel>> {
                            override fun onResponse(
                                call: Call<ArrayList<BranchDataModel>>,
                                response: Response<ArrayList<BranchDataModel>>
                            ) {
                                progress.visibility = View.GONE
                                layoutViews.visibility = View.VISIBLE
                                response.body()?.let {
                                    branchList = it
                                    setAdapter()
                                }
                            }

                            override fun onFailure(
                                call: Call<ArrayList<BranchDataModel>>,
                                t: Throwable
                            ) {
                                progress.visibility = View.GONE
                                layoutViews.visibility = View.VISIBLE
                                Toast.makeText(
                                    this@DashboardActivity,
                                    t.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                } catch (Ex: Exception) {
                    withContext(Dispatchers.Main) {
                        progress.visibility = View.GONE
                        layoutViews.visibility = View.VISIBLE
                    }
                    Ex.localizedMessage?.let { Log.e("Error", it) }
                }
            }
        }
    }

    private fun saveData(){
        progress.visibility = View.VISIBLE
        customerDataModel?.let {
            val retrofit = RetrofitClient.getInstance()
            val apiInterface = retrofit.create(DaeemServiceInterface::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    apiInterface.saveSOS(
                        customerID = it.customerId ?: "",
                        branchID = it.branchId ?: "",
                        imageBase64 = tvURI.text.toString(),
                        brandId = branchDataModel?.brandId ?: ""
                    ).enqueue(
                        object : Callback<String> {
                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>
                            ) {
                                progress.visibility = View.GONE
                                showSuccessDialog()

                            }

                            override fun onFailure(
                                call: Call<String>,
                                t: Throwable
                            ) {
                                progress.visibility = View.GONE
                                layoutViews.visibility = View.VISIBLE
                                Toast.makeText(
                                    this@DashboardActivity,
                                    t.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                } catch (Ex: Exception) {
                    withContext(Dispatchers.Main) {
                        progress.visibility = View.GONE
                        layoutViews.visibility = View.VISIBLE
                    }
                    Ex.localizedMessage?.let { Log.e("Error", it) }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this@DashboardActivity)
        builder.setTitle(getString(R.string.app_name))
        builder.setMessage(getString(R.string.success_msg))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
           dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setAdapter() {

        val mList : ArrayList<Any?> = arrayListOf()
        branchList.forEach{
            mList.add(it.productName ?: "")
        }

        branchDataModel = branchList[0]
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            mList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set the item click listener on the spinner
        spinner.onItemSelectedListener = this
    }


    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        val openGlVersionString = (Objects.requireNonNull(
            this.getSystemService(Context.ACTIVITY_SERVICE)
        ) as ActivityManager).deviceConfigurationInfo.glEsVersion
        if (openGlVersionString.toDouble() < minOpenGlVersion) {
            Log.e(TAG, "Scene form requires OpenGL ES $minOpenGlVersion later")
            Toast.makeText(
                this, "Scene form requires OpenGL ES $minOpenGlVersion or later", Toast.LENGTH_LONG
            ).show()
            finish()
            return false
        }
        return true
    }


    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.ivPreview1 -> {
                startActivity(Intent(
                    this@DashboardActivity, ImagePreviewActivity::class.java
                ).apply {
                    putExtra(INTENT_TYPE_SELECTION, true)
                })
            }
            R.id.ivPreview2 -> {
                startActivity(Intent(
                    this@DashboardActivity, ImagePreviewActivity::class.java
                ).apply {
                    putExtra(INTENT_TYPE_SELECTION, false)
                })
            }
            R.id.ivCameraFullCategory -> {
                isFullCategorySelected = true
                if (permissionsGranted) showDialog()
                else checkExternalStoragePermission()
            }
            R.id.ivCameraCustomerCategory -> {
                isFullCategorySelected = false
                if (permissionsGranted) showDialog()
                else checkExternalStoragePermission()
            }
            R.id.btnClear -> clearValues()

            R.id.btnSave -> {
                saveBitmapToDisk(layoutParent.takeScreenShot())
            }
        }

    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
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
        val df = SimpleDateFormat("yyyy-MM-dd HH.mm.ss")
        val formattedDate: String = df.format(c.time)
        val mediaFile = File(
            file, "FieldVisualizer$formattedDate.jpeg"
        )
        uriHeader.text = getString(R.string.image_uri)
        tvURI.text = mediaFile.absolutePath
        if (file.mkdirs()) {
            val fileOutputStream = FileOutputStream(mediaFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        }

       saveData()
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray,Base64.DEFAULT)
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
            if (fullCategorySpace.isNotEmpty()) putExtra(
                INTENT_MAX_DISTANCE,
                fullCategorySpace.toDouble()
            )
        }, INTENT_RECEIVE_DATA)
    }

    private fun requestExternalStoragePermission(): Boolean {

        return if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            val builder = AlertDialog.Builder(this).setCancelable(true).setTitle("Alert!")
                .setMessage("External storage permission allows us to access data from storage. Please allow in App Settings for additional functionality.")
                .setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
            builder.show()
            false
        } else {
            ActivityCompat.requestPermissions(
                this, permissionStorage, PERMISSION_REQUEST_CODE
            )
            true
        }
    }

    private fun askUserForFilePermission() {
        permissionsGranted = if (!checkExternalStoragePermission()) {
            requestExternalStoragePermission()
        } else true
    }

    private fun checkExternalStoragePermission(): Boolean {
        val result = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
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
            sosValue = customerCategorySpace.toDouble() / fullCategorySpace.toDouble() * 100
            val value = DecimalFormat("##.##").format(sosValue)
            tvSOSValue.text = value
        }

        if (isFullCategorySelected) {
            ivPreview1.visibility = View.VISIBLE
            ivPreview1.setImageBitmap(mBitmapOBj)
        } else {
            ivPreview2.visibility = View.VISIBLE
            ivPreview2.setImageBitmap(mBitmapOBj2)
        }
    }

    override fun onBranchClick(model: BranchDataModel) {

    }

    override fun onItemSelected(view: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        val item = view?.getItemAtPosition(position)
        item?.let {
            branchDataModel = branchList[position]
        }
    }


    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}

