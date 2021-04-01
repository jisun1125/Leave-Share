package kr.ac.kumoh.s20171278.map_map_challenge.album.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity.Companion.KEY_ALBUM_NAME
import kr.ac.kumoh.s20171278.map_map_challenge.main.DetailMemoActivity


class AlbumMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    lateinit var mView: MapView
    //googleMap을 MainMapFragment 안에 MapView로 받아와서 띄움
    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    private var locaArray: ArrayList<LatLng> = arrayListOf()
    val db = FirebaseFirestore.getInstance()
    private lateinit var albumName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        albumName = activity?.intent!!.getStringExtra(KEY_ALBUM_NAME)!!
        albumData = activity?.intent!!.getParcelableArrayListExtra(AlbumListActivity.ALBUM_DATA)!!

        for (i in 0 until albumData.size){
            if (LatLng(albumData[i].lati, albumData[i].long) == LatLng(0.0, 0.0)) {
                continue
            }
            else {
                locaArray.add(LatLng(albumData[i].lati, albumData[i].long))
            }
        }

        var rootView = inflater.inflate(R.layout.album_fragment_album_map, container, false)
        mView = rootView.findViewById(R.id.fragment_main_mv) as MapView
        mView.onCreate(savedInstanceState)
        mView.onResume()
        mView.getMapAsync(this)

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val marker: MarkerOptions = MarkerOptions()
        var polyLineOptions: PolylineOptions = PolylineOptions()

        Log.d("999", "locaArray: $locaArray" + locaArray.size)
        for (i in 0 until locaArray.size){
            // 마커표시

            marker.position(locaArray[i])
            googleMap.addMarker(marker)

            val boundsBuilder: LatLngBounds.Builder = LatLngBounds.builder()
            for (i in 0 until locaArray.size)
                boundsBuilder.include(locaArray[i])
            var bounds: LatLngBounds = boundsBuilder.build()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        }
        polyLineOptions.width(5f).color(Color.RED)
        polyLineOptions.addAll(locaArray)
        googleMap.addPolyline(polyLineOptions)

        googleMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        // 팝업
        val intent = Intent(context, AlbumDetailMemoActivity::class.java)
        intent.putExtra(SelectImageActivity.KEY_ALBUM_NAME, albumName)
        intent.putExtra(AlbumListActivity.ALBUM_DATA, albumData)
        intent.putExtra(DetailMemoActivity.MARKER, marker?.position)
        startActivity(intent)

        return true
    }

    override fun onStart() {
        mView.onStart()
        super.onStart()
    }

    override fun onResume() {
        mView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mView.onLowMemory()
        super.onLowMemory()
    }
}
