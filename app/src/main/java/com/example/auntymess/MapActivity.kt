package com.example.auntymess

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var MessName: String?="Mess Location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Get latitude and longitude from the intent
        latitude = intent.getStringExtra("latitude")?.toDoubleOrNull() ?: 0.0
        longitude = intent.getStringExtra("longitude")?.toDoubleOrNull() ?: 0.0
        MessName=intent.getStringExtra("messName")

        // Initialize the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Set the map's initial location and zoom
        val location = LatLng(latitude, longitude)
        map.addMarker(MarkerOptions().position(location).title(MessName))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}
