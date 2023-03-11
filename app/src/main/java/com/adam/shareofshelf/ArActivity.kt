package com.adam.shareofshelf

import android.app.AlertDialog
import android.content.Intent
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
import com.adam.shareofshelf.ShareOfShelfExtensions.mBitmapOBj
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

    private var distanceTextCM = ""
    private lateinit var initCM: String
    private var is2pointTypeSelected = false
    private var arFragment: ArFragment? = null
    private val placedAnchors = ArrayList<Anchor>()
    private var modelRender: ModelRenderable? = null
    private val fromGroundNodes = ArrayList<List<Node>>()
    private val placedAnchorNodes = ArrayList<AnchorNode>()
    private var distanceCardViewRender: ViewRenderable? = null
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
        supportActionBar?.hide()
        bindViews()
    }

    private fun bindViews() {

        intent?.extras?.let {

            is2pointTypeSelected = it.getBoolean(INTENT_TYPE_SELECTION)
        }
        btnSet = findViewById(R.id.btnSet)
        initCM = resources.getString(R.string.initCM)
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?

        initRender()
        setArFragmentListener()

        btnReset.setOnClickListener {
            clearAllAnchors()
        }
        btnSet.setOnClickListener {
            //Sending back the values to previous screen
            val value = distanceTextCM.split(" ")[0]
            layoutButtons.visibility = View.GONE


            takeImageOfSceneView()
            val intent = Intent()
            intent.putExtra(INTENT_RECEIVE_DATA.toString(), value)
            setResult(RESULT_OK, intent)
            finish()
        }
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

                        mBitmapOBj = bitmap

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


    private fun initRender() {
        MaterialFactory.makeTransparentWithColor(
            this,
            Color(Color.RED)
        )
            .thenAccept { material: Material? ->
                /*modelRender = ShapeFactory.makeSphere (
                    0.01f,
                    Vector3.zero(),
                    material
                )*/
                modelRender = ShapeFactory.makeCube (
                    Vector3(20f, 20f, 20f),
                    Vector3.zero(),
                    material
                )
               /* modelRender = ShapeFactory.makeCylinder (
                    0.01f,
                    0.2f,
                    Vector3.zero(),
                    material
                )*/
                modelRender?.let { shape->
                    shape.isShadowCaster = false
                    shape.isShadowReceiver = false
                }

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
                distanceCardViewRender = it

                distanceCardViewRender?.let { card->
                    card.isShadowCaster = false
                    card.isShadowReceiver = false
                }

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
                if (modelRender == null || distanceCardViewRender == null) return@setOnTapArPlaneListener
                // Creating Anchor.
                if (is2pointTypeSelected)
                    tapDistanceOf2Points(hitResult)
                else
                    tapDistanceOfMultiplePoints(hitResult)
            }
        }
    }

    private fun tapDistanceOfMultiplePoints(hitResult: HitResult) {
        if (placedAnchorNodes.size >= Constants.maxNumMultiplePoints) {
            clearAllAnchors()
        }
        when (placedAnchors.size) {
            0 -> {
                placeAnchor(hitResult, modelRender)
            }
            1 -> {
                placeAnchor(hitResult, modelRender)
                val midPosition = floatArrayOf(
                    (placedAnchorNodes[0].worldPosition.x + placedAnchorNodes[1].worldPosition.x) / 2,
                    (placedAnchorNodes[0].worldPosition.y + placedAnchorNodes[1].worldPosition.y) / 2,
                    (placedAnchorNodes[0].worldPosition.z + placedAnchorNodes[1].worldPosition.z) / 2
                )
                val quaternion = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
                val pose = Pose(midPosition, quaternion)

                placeMidAnchor(pose, distanceCardViewRender, arrayOf(0,1))
            }
            2 -> {
                placeAnchor(hitResult, modelRender)
                val midPosition = floatArrayOf(
                    (placedAnchorNodes[1].worldPosition.x + placedAnchorNodes[2].worldPosition.x) / 2,
                    (placedAnchorNodes[1].worldPosition.y + placedAnchorNodes[2].worldPosition.y) / 2,
                    (placedAnchorNodes[1].worldPosition.z + placedAnchorNodes[2].worldPosition.z) / 2
                )
                val quaternion = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
                val pose = Pose(midPosition, quaternion)

                placeMidAnchor(pose, distanceCardViewRender, arrayOf(1,2))
            }
            3 -> {
                placeAnchor(hitResult, modelRender)
                val midPosition = floatArrayOf(
                    (placedAnchorNodes[2].worldPosition.x + placedAnchorNodes[3].worldPosition.x) / 2,
                    (placedAnchorNodes[2].worldPosition.y + placedAnchorNodes[3].worldPosition.y) / 2,
                    (placedAnchorNodes[2].worldPosition.z + placedAnchorNodes[3].worldPosition.z) / 2
                )
                val quaternion = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
                val pose = Pose(midPosition, quaternion)

                placeMidAnchor(pose, distanceCardViewRender, arrayOf(2,3))
            }
            else -> {
                clearAllAnchors()
                placeAnchor(hitResult, modelRender)
            }
        }


        /*ViewRenderable
            .builder()
            .setView(this, R.layout.point_text_layout)
            .build()
            .thenAccept{
                it.isShadowReceiver = false
                it.isShadowCaster = false
                pointTextView = it.view as TextView
                pointTextView.text = placedAnchors.size.toString()
                placeAnchor(hitResult, it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }*/
        Log.i(TAG, "Number of anchors: ${placedAnchorNodes.size}")
    }

    private fun tapDistanceOf2Points(hitResult: HitResult) {
        when (placedAnchorNodes.size) {
            0 -> {
                placeAnchor(hitResult, modelRender)
            }
            1 -> {
                placeAnchor(hitResult, modelRender)

                val midPosition = floatArrayOf(
                    (placedAnchorNodes[0].worldPosition.x + placedAnchorNodes[1].worldPosition.x) / 2,
                    (placedAnchorNodes[0].worldPosition.y + placedAnchorNodes[1].worldPosition.y) / 2,
                    (placedAnchorNodes[0].worldPosition.z + placedAnchorNodes[1].worldPosition.z) / 2
                )
                val quaternion = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
                val pose = Pose(midPosition, quaternion)

                placeMidAnchor(pose, distanceCardViewRender)
            }
            else -> {
                clearAllAnchors()
                placeAnchor(hitResult, modelRender)
            }
        }
    }

    private fun placeAnchor(hitResult: HitResult, render: Renderable?) {
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
            render?.let { render ->
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
        render: Renderable?,
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

            render?.let { render ->
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
                multipleDistances[i][j]?.let {
                    it.text = if (i == j) "-" else initCM
                }
            }
        }
        fromGroundNodes.clear()
    }

    override fun onUpdate(p0: FrameTime?) {

        if (is2pointTypeSelected && distanceTextCM.isEmpty())
            measureDistanceOf2Points()
        else
            measureMultipleDistances()
    }

    private fun measureMultipleDistances() {
        if (placedAnchorNodes.size > 1) {
            for (i in 0 until placedAnchorNodes.size) {
                for (j in i + 1 until placedAnchorNodes.size) {
                    val distanceMeter = calculateDistance(
                        placedAnchorNodes[i].worldPosition,
                        placedAnchorNodes[j].worldPosition
                    )
                    val distanceCM = changeUnit(distanceMeter, "cm")
                    val distanceCMFloor = "%.2f".format(distanceCM)
                    /*  multipleDistances[i][j]!!.text = distanceCMFloor
                      multipleDistances[j][i]!!.text = distanceCMFloor*/
                    val textView = (distanceCardViewRender?.view as LinearLayout)
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
        val textView = (distanceCardViewRender?.view as LinearLayout)
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