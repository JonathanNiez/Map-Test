package com.capstone.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import org.json.JSONObject


class MapKotlin : AppCompatActivity() {


    private var mapView: com.mapbox.maps.MapView? = null
    private var annotationApi: AnnotationPlugin? = null
    private lateinit var annotationConfig: AnnotationConfig
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var layerID = "map_annotation"
    private var markerList: ArrayList<PointAnnotationOptions> = ArrayList()
    private var latitudeList: ArrayList<Double> = ArrayList()
    private var longitudeList: ArrayList<Double> = ArrayList()

    private val TAG: String = "MapActivity"
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    private lateinit var whereToLayout: FrameLayout
    private lateinit var setLocationLayout: FrameLayout
    private lateinit var imgBackBtn: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_kotlin)

        setLocationLayout = findViewById(R.id.setLocationLayout)
        whereToLayout = findViewById(R.id.whereToLayout)
        mapView = findViewById(R.id.mapView)
        imgBackBtn = findViewById(R.id.imgBackBtn)


        createDummyMarkers()
        checkLocationPermission()

        imgBackBtn.setOnClickListener {
            finish()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.setLocation

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.setLocation -> {
                    setLocationLayout.visibility = View.VISIBLE
                }

                R.id.bookings -> {
                    setLocationLayout.visibility = View.GONE
                    val intent = Intent(this, Bookings::class.java)
                    startActivity(intent)
                }

                R.id.help -> {
                    setLocationLayout.visibility = View.GONE

                }
            }
            true
        }


        mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS,
            // After the style is loaded, initialize the Location component.
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    zoomCamera()

                    annotationApi = mapView?.annotations
                    annotationConfig = AnnotationConfig(
                        layerId = layerID
                    )

                    pointAnnotationManager =
                        annotationApi?.createPointAnnotationManager(annotationConfig)

                    showPassengerMarkerOnMap()

                }
            }
        )


    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, proceed to location retrieval
        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun zoomCamera() {
        mapView!!.getMapboxMap().setCamera(
            CameraOptions.Builder().center(Point.fromLngLat(125.60288851565707, 7.065679527724293))
                .zoom(10.0)
                .build()

        )
    }

    private fun createDummyMarkers() {
        latitudeList.add(7.191233292469051)
        longitudeList.add(125.45471619362087)

        latitudeList.add(7.0569787855098625)
        longitudeList.add(125.52385607415545)

        latitudeList.add(7.11563147517752)
        longitudeList.add(125.55617682891864)
    }

    private fun showPassengerMarkerOnMap() {

        pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener { annotation: PointAnnotation ->

            onMarkerItemClick(annotation)
            true
        })

        var bitmap =
            convertDrawableToBitmap(AppCompatResources.getDrawable(this, R.drawable.location_3))

        for (i in 0 until 3) {
            var jsonObject = JSONObject()
            jsonObject.put("someValue", 1)

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(longitudeList[i], latitudeList[i]))
                .withData(Gson().fromJson(jsonObject.toString(), JsonElement::class.java))
                .withIconImage(bitmap)

            markerList.add(pointAnnotationOptions)
        }

        pointAnnotationManager?.create(markerList)
    }

    private fun onMarkerItemClick(marker: PointAnnotation): Boolean {
        var jsonElement: JsonElement? = marker.getData()

        AlertDialog.Builder(this)
            .setTitle("Dummy Marker")
            .setMessage("Interacted Marker: $marker")
            .setPositiveButton("K") { dialog, whichButton ->
                dialog.dismiss()
            }.show()

        return true
    }

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap {

        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable?.constantState
            val drawable = constantState?.newDrawable()?.mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth, drawable!!.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }


    private fun flyToCameraPosition() {
        var cameraCenterCoordinates = com.mapbox.geojson.Point.fromLngLat(8.0061, 46.5778)

        var cameraOptions = CameraOptions.Builder()
            .center(cameraCenterCoordinates)
            .bearing(130.0)
            .pitch(75.0)
            .zoom(13.0)
            .build()

        var animationOptions = MapAnimationOptions.Builder().duration(15000).build()

        mapView!!.getMapboxMap().flyTo(cameraOptions, animationOptions)
    }

    private fun addCustomAnnotation() {
        val annotationApi = mapView?.annotations
        val pointAnnotationManager =
            mapView?.let { annotationApi?.createPointAnnotationManager(it) }
        // Set options for the resulting symbol layer.
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            // Define a geographic coordinate.
            .withPoint(Point.fromLngLat(18.06, 59.31))
            // Specify the bitmap you assigned to the point annotation
            // The bitmap will be added to map style automatically.
            .withIconImage(getDrawable(R.drawable.location_3)!!.toBitmap())
// Add the resulting pointAnnotation to the map.
        pointAnnotationManager?.create(pointAnnotationOptions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to location retrieval
            } else {
                // Permission denied, handle accordingly (show error message, etc.)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }


    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

}