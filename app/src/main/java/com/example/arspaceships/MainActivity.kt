package com.example.arspaceships

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment
    private val modelResIds = arrayOf(
        R.raw.star_destroyer,
        R.raw.tie_silencer,
        R.raw.xwing
    )
    private var curCameraPosition = Vector3.zero()
    private val nodes = mutableListOf<RotatingNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = ar_frag as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            val randomId = modelResIds.random()
            loadModelAndAddToScene(hitResult.createAnchor(), randomId)
        }

        arFragment.arSceneView.scene.addOnUpdateListener {
            updateNodes()
        }
    }

    private fun addNodeToScene(anchor: Anchor,modelRenderable: ModelRenderable,spaceShip: SpaceShip) {
        val anchorNode = AnchorNode(anchor)
        val rotatingNode =  RotatingNode(spaceShip.degreesPerSecond).apply {
            setParent(anchorNode)
        }

       Node().apply {
           renderable = modelRenderable
           setParent(rotatingNode)
           localPosition = Vector3(spaceShip.radius, spaceShip.height, 0f)
           localRotation = Quaternion.eulerAngles(Vector3(0f, spaceShip.rotationDegrees, 0f))
       }

        arFragment.arSceneView.scene.addChild(anchorNode)
        nodes.add(rotatingNode)
    }

    fun updateNodes(){
        curCameraPosition = arFragment.arSceneView.scene.camera.worldPosition
        for(node in nodes){
            node.worldPosition = Vector3(curCameraPosition.x,node.worldPosition.y,curCameraPosition.z)
        }

    }

    private fun loadModelAndAddToScene(anchor: Anchor, modelResId:Int){
        ModelRenderable.builder().setSource(this,modelResId)
            .build()
            .thenAccept {
                val spaceShip = when(modelResId){
                    R.raw.star_destroyer -> SpaceShip.StarDestroyer
                    R.raw.tie_silencer -> SpaceShip.TieSilencer
                    R.raw.xwing -> SpaceShip.XWing
                    else -> SpaceShip.XWing
                }
                addNodeToScene(anchor,it,spaceShip)
            }.exceptionally {
                Toast.makeText(this, "Error creating node: $it", Toast.LENGTH_LONG).show()
                null
            }
    }
}