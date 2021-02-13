package kr.ac.kumoh.s20171278.map_map_challenge.main

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_activity_detail_memo.*
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity

class DetailMemoActivity : AppCompatActivity() {
    companion object {
        const val KEY_ALBUM_NAME = "album_name"
        const val PICTURE_REQUEST_CODE = 111
        const val ALBUM_DATA = "album_data"
        const val LOCAL_ARRAY = "local_array"
        const val MARKER = "marker"
    }

    private lateinit var albumName: String
    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    private lateinit var markerPos: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_detail_memo)

        albumName = intent.getStringExtra(KEY_ALBUM_NAME)
        albumData = intent.getParcelableArrayListExtra(ALBUM_DATA)
        markerPos = intent.getParcelableExtra(MARKER)

//        val picasso = Picasso.Builder(this).build()

        val mGeocoder: Geocoder = Geocoder(this)
        var siteList: List<Address>? = null
        var locaArray: ArrayList<LatLng> = arrayListOf()
        var loca: LatLng

        for (i in 0 until albumData.size) {
            // 주소 -> 좌표
            siteList = mGeocoder.getFromLocationName(
                albumData?.get(i)?.site,
                100
            )

            var split = siteList[0].toString().split(",")
            var lati = split[10].substring(split[10].indexOf("=") + 1)
            var long = split[12].substring(split[12].indexOf("=") + 1)
            loca = LatLng(lati.toDouble(), long.toDouble())
            locaArray.add(loca)
        }



        for (i in 0 until albumData.size) {
            if (markerPos == locaArray[i]) {
                var change = albumData[i].imageArray.toString()

                var delChange = change.replace("[", "")
                delChange = delChange.replace("]", "")

                var splChange = delChange.split(", ")
                var album: ArrayList<String> = arrayListOf()
                for (element in splChange)
                    album.add(element)

                dMemoSite.text = albumData[i].site
                dMemoTitle.setText(albumData[i].title)
                dMemoView.adapter = ViewPagerAdapter(albumData[i].imageArray!!)
                dMemoDate.text = albumData[i].date
                dMemoContent.setText(albumData[i].content)

                dMemoTag.setText(albumData[i].tag)
            }
        }


        btnModi.setOnClickListener {
            for (i in 0 until locaArray.size){
                if (markerPos == locaArray[i]){
                    albumData[i].content = dMemoContent.text.toString()
                    albumData[i].title = dMemoTitle.text.toString()
                    albumData[i].tag = dMemoTag.text.toString()

                }
            }

            val intent = Intent(applicationContext, MainTabActivity::class.java)
            intent.putExtra(KEY_ALBUM_NAME, albumName)
            intent.putExtra(ALBUM_DATA, albumData)
            startActivity(intent)
        }

        btnDel.setOnClickListener {
            for (i in 0 until locaArray.size){
                if (markerPos == locaArray[i]){
                    albumData.removeAt(i)
                }
            }

            val intent = Intent(applicationContext, MainTabActivity::class.java)
            intent.putExtra(KEY_ALBUM_NAME, albumName)
            intent.putExtra(ALBUM_DATA, albumData)
            startActivity(intent)

        }

    }

    inner class ViewPagerAdapter(val item: ArrayList<String>) : PagerAdapter() {

        override fun getCount(): Int {
            return item.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater
                .from(container.context)
                .inflate(R.layout.main_fragment_main_memo_viewpager, container, false)
            val image: ImageView = view.findViewById(R.id.memoImage)

            val items = Uri.parse(item[position])

            Glide.with(container.context)
                .load(items)
                .placeholder(R.color.colorGray)
//                .fit()
                .centerCrop()
                .into(image)

            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            // memoIndicator.attachTo(memoView)
            //container.removeView(view as View)
        }
    }
}
