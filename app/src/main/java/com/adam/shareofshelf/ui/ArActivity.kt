package com.adam.shareofshelf.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adam.shareofshelf.R
import com.adam.shareofshelf.utils.Constants
import com.adam.shareofshelf.utils.ShareOfShelfExtensions.mBitmapOBj
import com.adam.shareofshelf.utils.ShareOfShelfExtensions.mBitmapOBj2
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_ar.*
import java.io.IOException
import kotlin.math.pow
import kotlin.math.sqrt


class ArActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private var maxDistance = 0.0
    private var sumOfMultiPoints = 0f
    private var distanceTextCM = ""
    private lateinit var initCM: String
    private var is2pointTypeSelected = false
    private var is1stCameraSelected = false
    private var arFragment: ArFragment? = null
    private val placedAnchors = ArrayList<Anchor>()
    private var cubeRenderable: ModelRenderable? = null
    private val fromGroundNodes = ArrayList<List<Node>>()
    private val placedAnchorNodes = ArrayList<AnchorNode>()
    private var distanceCardViewRenderable: ViewRenderable? = null
    private var textBetweenPointsViewList: ArrayList<ViewRenderable> = arrayListOf()
    private val midAnchors: MutableMap<String, Anchor> = mutableMapOf()
    private val midAnchorNodes: MutableMap<String, AnchorNode> = mutableMapOf()
    private val multipleDistances = Array(
        Constants.maxNumMultiplePoints
    ) { Array<TextView?>(Constants.maxNumMultiplePoints) { null } }

    //Views
    private lateinit var btnSet: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        bindViews()
    }

    private fun bindViews() {

        intent?.extras?.let {
            is2pointTypeSelected = it.getBoolean(INTENT_TYPE_SELECTION)
            is1stCameraSelected = it.getBoolean(INTENT_CAMERA_SELECTION)
            maxDistance = it.getDouble(INTENT_MAX_DISTANCE)
        }
        btnSet = findViewById(R.id.btnSet)
        initCM = resources.getString(R.string.initCM)
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?

        initRenderable()
        setArFragmentListener()

        btnReset.setOnClickListener {
            clearAllAnchors()
        }
        btnSet.setOnClickListener {
            //Sending back the values to previous screen
            if (is2pointTypeSelected && placedAnchors.size > 1) {
                val value = distanceTextCM.split(" ")[0]
                if (maxDistance > 0) {
                    if (value.toDouble() <= maxDistance)
                        process2Points(value)
                    else
                        Toast.makeText(this, "Limit exceeded", Toast.LENGTH_LONG).show()
                } else
                    process2Points(value)

            } else
                if (!is2pointTypeSelected && placedAnchors.size > 1) {
                    for (anchor in 0 until placedAnchorNodes.size - 1) {
                        if (anchor % 2 == 0) {
                            val distanceMeter = calculateDistance(
                                placedAnchorNodes[anchor].worldPosition,
                                placedAnchorNodes[anchor + 1].worldPosition
                            )
                            val distanceCM = changeUnit(distanceMeter, "cm")
                            sumOfMultiPoints += distanceCM
                        }
                    }
                    if (maxDistance > 0) {
                        if (sumOfMultiPoints.toDouble() <= maxDistance)
                            processMultiPoints()
                        else
                            Toast.makeText(this, getString(R.string.limit_exceeded), Toast.LENGTH_LONG).show()
                    } else
                        processMultiPoints()

                }
        }
    }

    private fun processMultiPoints() {
        layoutButtons.visibility = View.GONE
        takeImageOfSceneView()
        intent.putExtra(
            INTENT_RECEIVE_DATA.toString(),
            "%.2f".format(sumOfMultiPoints)
        )
        setResult(RESULT_OK, intent)
        finish()
        Log.e("Total distance", "Total distance: $sumOfMultiPoints cm")
    }

    private fun process2Points(value: String) {
        layoutButtons.visibility = View.GONE
        takeImageOfSceneView()
        intent.putExtra(INTENT_RECEIVE_DATA.toString(), value)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun takeImageOfSceneView() {
        arFragment?.let {
            val view = it.arSceneView

            val bitmap = Bitmap.createBitmap(
                view.width, view.height,
                Bitmap.Config.ARGB_8888
            )
            val handlerThread = HandlerThread("PixelCopier")
            handlerThread.start()
            PixelCopy.request(view, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    try {
                        if (is1stCameraSelected)
                            mBitmapOBj = bitmap
                        else
                            mBitmapOBj2 = bitmap
                        //saveBitmapToDisk(bitmap)
                    } catch (e: IOException) {
                        val toast = Toast.makeText(
                            this, e.toString(),
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                        return@request
                    }

                }
                handlerThread.quitSafely()
            }, Handler(handlerThread.looper))
        }

    }


    private fun initRenderable() {
        MaterialFactory.makeTransparentWithColor(
            this,
            Color(Color.RED)
        )
            .thenAccept { material: Material? ->
                cubeRenderable = ShapeFactory.makeSphere(
                    0.01f,
                    Vector3.zero(),
                    material
                )
                /*cubeRenderable = ShapeFactory.makeCube (
                    Vector3(20f, 20f, 20f),
                    Vector3.zero(),
                    material
                )*/
                /* cubeRenderable = ShapeFactory.makeCylinder (
                     0.01f,
                     0.2f,
                     Vector3.zero(),
                     material
                 )*/
                cubeRenderable!!.isShadowCaster = false
                cubeRenderable!!.isShadowReceiver = false
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }

        ViewRenderable
            .builder()
            .setView(this, R.layout.distance_text_layout)
            .build()
            .thenAccept {
                distanceCardViewRenderable = it
                distanceCardViewRenderable!!.isShadowCaster = false
                distanceCardViewRenderable!!.isShadowReceiver = false
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }

    }

    private fun setArFragmentListener() {

        arFragment?.let {
            it.setOnTapArPlaneListener { hitResult: HitResult, _: Plane?, _: MotionEvent? ->
                if (cubeRenderable == null || distanceCardViewRenderable == null) return@setOnTapArPlaneListener
                // Creating Anchor.
                if (is2pointTypeSelected)
                    tapDistanceOf2Points(hitResult)
                else {
                    ViewRenderable
                        .builder()
                        .setView(this, R.layout.distance_text_layout)
                        .build()
                        .thenAccept { viewRender->
                            viewRender.isShadowCaster = false
                            viewRender.isShadowReceiver = false
                            textBetweenPointsViewList.add(viewRender)
                        }
                        .exceptionally { throwable->
                            val builder = AlertDialog.Builder(this)
                            builder.setMessage(throwable.message).setTitle("Error")
                            val dialog = builder.create()
                            dialog.show()
                            return@exceptionally null
                        }

                    tapDistanceOfMultiplePoints(hitResult)
                }
            }
        }
    }

    private fun tapDistanceOfMultiplePoints(hitResult: HitResult) {

        if (placedAnchorNodes.size >= Constants.maxNumMultiplePoints) {
            //clearAllAnchors()
            Toast.makeText(this, "Limit Exceeded", Toast.LENGTH_LONG).show()
        } else {
            val size = placedAnchors.size
            if (size % 2 == 0)
                placeAnchor(hitResult, cubeRenderable)
            else
                placeAnchorWithTextInBetween(hitResult, size - 1, size)
        }


        Log.i(TAG, "Number of anchors: ${placedAnchorNodes.size}")
    }

    private fun placeAnchorWithTextInBetween(
        hitResult: HitResult,
        start: Int,
        end: Int
    ) {
        placeAnchor(hitResult, this.cubeRenderable)
        val midPosition = floatArrayOf(
            (placedAnchorNodes[start].worldPosition.x + placedAnchorNodes[end].worldPosition.x) / 2,
            (placedAnchorNodes[start].worldPosition.y + placedAnchorNodes[end].worldPosition.y) / 2,
            (placedAnchorNodes[start].worldPosition.z + placedAnchorNodes[end].worldPosition.z) / 2
        )
        val quaternion = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
        val pose = Pose(midPosition, quaternion)

        placeMidAnchor(pose, textBetweenPointsViewList[start], arrayOf(start, end))
    }

    private fun tapDistanceOf2Points(hitResult: HitResult) {
        when (placedAnchorNodes.size) {
            0 -> {
                placeAnchor(hitResult, cubeRenderable)
            }
            1 -> {
                placeAnchor(hitResult, cubeRenderable)

                val midPosition = floatArrayOf(
                    (placedAnchorNodes[0].worldPosition.x + placedAnchorNodes[1].worldPosition.x) / 2,
                    (placedAnchorNodes[0].worldPosition.y + placedAnchorNodes[1].worldPosition.y) / 2,
                    (placedAnchorNodes[0].worldPosition.z + placedAnchorNodes[1].worldPosition.z) / 2
                )
                val quaternion = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
                val pose = Pose(midPosition, quaternion)

                placeMidAnchor(pose, distanceCardViewRenderable)
            }
            else -> {
                clearAllAnchors()
                placeAnchor(hitResult, cubeRenderable)
            }
        }
    }

    private fun placeAnchor(hitResult: HitResult, renderable: Renderable?) {
        val anchor = hitResult.createAnchor()
        placedAnchors.add(anchor)

        val anchorNode = AnchorNode(anchor).apply {
            isSmoothed = true
            arFragment?.let {
                setParent(it.arSceneView.scene)
            }
        }
        placedAnchorNodes.add(anchorNode)

        arFragment?.let {
            renderable?.let { render ->
                val node = TransformableNode(it.transformationSystem)
                    .apply {
                        this.rotationController.isEnabled = false
                        this.scaleController.isEnabled = false
                        this.translationController.isEnabled = true
                        this.renderable = render
                        setParent(anchorNode)
                    }
                it.arSceneView.scene.addOnUpdateListener(this)
                it.arSceneView.scene.addChild(anchorNode)
                node.select()
            }

        }

    }

    private fun placeMidAnchor(
        pose: Pose,
        renderable: Renderable?,
        between: Array<Int> = arrayOf(0, 1)
    ) {
        val midKey = "${between[0]}_${between[1]}"
        arFragment?.let { mFragment ->
            val anchor = mFragment.arSceneView.session?.createAnchor(pose)
            anchor?.let {
                midAnchors[midKey] = it
            }
            val anchorNode = AnchorNode(anchor).apply {
                isSmoothed = true
                setParent(mFragment.arSceneView.scene)
            }
            midAnchorNodes[midKey] = anchorNode

            renderable?.let { render ->
                TransformableNode(mFragment.transformationSystem)
                    .apply {
                        this.rotationController.isEnabled = false
                        this.scaleController.isEnabled = false
                        this.translationController.isEnabled = true
                        this.renderable = render
                        setParent(anchorNode)
                    }
            }
            mFragment.arSceneView.scene.addOnUpdateListener(this)
            mFragment.arSceneView.scene.addChild(anchorNode)
        }

    }


    private fun clearAllAnchors() {
        if (is1stCameraSelected)
            mBitmapOBj?.recycle()
        else
            mBitmapOBj2?.recycle()
        placedAnchors.clear()
        distanceTextCM = "" // make it empty with clear
        arFragment?.let {
            for (anchorNode in placedAnchorNodes) {
                it.arSceneView.scene.removeChild(anchorNode)
                anchorNode.isEnabled = false
                anchorNode.anchor?.detach()
                anchorNode.setParent(null)
            }
        }
        sumOfMultiPoints = 0f
        placedAnchorNodes.clear()
        midAnchors.clear()
        arFragment?.let {
            for ((_, anchorNode) in midAnchorNodes) {
                it.arSceneView.scene.removeChild(anchorNode)
                anchorNode.isEnabled = false
                anchorNode.anchor?.detach()
                anchorNode.setParent(null)
            }
        }
        midAnchorNodes.clear()
        for (i in 0 until Constants.maxNumMultiplePoints) {
            for (j in 0 until Constants.maxNumMultiplePoints) {
                if (multipleDistances[i][j] != null) {
                    multipleDistances[i][j]!!.text = if (i == j) "-" else initCM
                }
            }
        }
        fromGroundNodes.clear()
    }
    override fun onUpdate(p0: FrameTime?) {

        if (is2pointTypeSelected && distanceTextCM.isEmpty())
            measureDistanceOf2Points()
        else {
            if (distanceTextCM.isEmpty())
                measureMultipleDistances()
        }
    }

    private fun measureMultipleDistances() {
        if (placedAnchors.size > 1) {
            for (i in 0 until placedAnchorNodes.size - 1) {
                for (j in i + 1 until placedAnchorNodes.size) {
                    val distanceMeter = calculateDistance(
                        placedAnchorNodes[i].worldPosition,
                        placedAnchorNodes[j].worldPosition
                    )
                    val distanceCM = changeUnit(distanceMeter, "cm")
                    val distanceCMFloor = "%.2f".format(distanceCM)
                    val textView = (textBetweenPointsViewList[j - 1].view as LinearLayout)
                        .findViewById<TextView>(R.id.distanceCard)
                    textView.text = distanceCMFloor
                }
            }
        }
    }


    private fun measureDistanceOf2Points() {
        if (placedAnchorNodes.size == 2) {
            val distanceMeter = calculateDistance(
                placedAnchorNodes[0].worldPosition,
                placedAnchorNodes[1].worldPosition
            )
            measureDistanceOf2Points(distanceMeter)
        }
    }

    private fun measureDistanceOf2Points(distanceMeter: Float) {
        distanceTextCM = makeDistanceTextWithCM(distanceMeter)
        val textView = (distanceCardViewRenderable?.view as LinearLayout)
            .findViewById<TextView>(R.id.distanceCard)
        textView.text = distanceTextCM
    }

    private fun makeDistanceTextWithCM(distanceMeter: Float): String {
        val distanceCM = changeUnit(distanceMeter, "cm")
        val distanceCMFloor = "%.2f".format(distanceCM)
        return "$distanceCMFloor cm"
    }

    private fun changeUnit(distanceMeter: Float, unit: String): Float {
        return when (unit) {
            "cm" -> distanceMeter * 100
            "mm" -> distanceMeter * 1000
            else -> distanceMeter
        }
    }

    private fun calculateDistance(objectPose0: Vector3, objectPose1: Vector3): Float {
        return calculateDistance(
            objectPose0.x - objectPose1.x,
            objectPose0.y - objectPose1.y,
            objectPose0.z - objectPose1.z
        )
    }

    private fun calculateDistance(x: Float, y: Float, z: Float): Float {
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }


}