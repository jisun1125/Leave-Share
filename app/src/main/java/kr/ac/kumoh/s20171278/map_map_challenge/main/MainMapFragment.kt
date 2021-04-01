package kr.ac.kumoh.s20171278.map_map_challenge.main


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kr.ac.kumoh.s20171278.map_map_challenge.main.DetailMemoActivity.Companion.ALBUM_DATA
import kr.ac.kumoh.s20171278.map_map_challenge.main.DetailMemoActivity.Companion.MARKER
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_ALBUM_NAME
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity

/**
 * A simple [Fragment] subclass.
 */


class MainMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
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
        albumData = activity?.intent!!.getParcelableArrayListExtra(ALBUM_DATA)!!

        var rootView = inflater.inflate(R.layout.main_fragment_main_map, container, false)
        mView = rootView.findViewById(R.id.fragment_main_mv) as MapView
        mView.onCreate(savedInstanceState)
        mView.onResume()
        mView.getMapAsync(this)
        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val mGeocoder: Geocoder = Geocoder(context)
        var siteList: List<Address>? = null
        var loca: LatLng
        val marker: MarkerOptions = MarkerOptions()
        val polyLineOptions: PolylineOptions = PolylineOptions()

        for (i in 0 until albumData!!.size){
            if (albumData[i].site == "") {
                continue
            }
            else {
                // 주소 -> 좌표
                siteList = mGeocoder.getFromLocationName(
                    albumData?.get(i)?.site,
                    100
                )

                var split = siteList[0].toString().split(",")
                var lati = split[10].substring(split[10].indexOf("=") + 1)
                var long = split[12].substring(split[12].indexOf("=") + 1)
                albumData[i].lati = lati.toDouble()
                albumData[i].long = long.toDouble()
                Log.d("llla", "siteList: $siteList, ")
                Log.d("llla", "albumData ${albumData.get(i).site}, la: ${albumData.get(i).lati}, lo: ${albumData.get(i).long}")
                loca = LatLng(lati.toDouble(), long.toDouble())

                locaArray.add(loca)
                // 마커표시
                marker.position(loca)
                googleMap.addMarker(marker)

                googleMap.setOnMarkerClickListener(this)
                val boundsBuilder: LatLngBounds.Builder = LatLngBounds.builder()
                for (i in 0 until locaArray.size)
                    boundsBuilder.include(locaArray[i])
                var bounds: LatLngBounds = boundsBuilder.build()
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
            }
        // 경로표시
        polyLineOptions.width(5f).color(Color.RED)
        polyLineOptions.addAll(locaArray)
        googleMap.addPolyline(polyLineOptions)
        }
    }


    override fun onMarkerClick(marker: Marker?): Boolean {

        val intent = Intent(context, DetailMemoActivity::class.java)
        intent.putExtra(SelectImageActivity.KEY_ALBUM_NAME, albumName)
        intent.putExtra(ALBUM_DATA, albumData)
        intent.putExtra(MARKER, marker?.position)
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
