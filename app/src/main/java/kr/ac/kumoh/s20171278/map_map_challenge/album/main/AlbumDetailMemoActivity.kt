package kr.ac.kumoh.s20171278.map_map_challenge.album.main

import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.album_detail_memo.*
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.hashtag.Hashtag
import java.util.regex.Pattern

class AlbumDetailMemoActivity : AppCompatActivity() {
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
        setContentView(R.layout.album_detail_memo)

        albumName = intent.getStringExtra(KEY_ALBUM_NAME)!!
        albumData = intent.getParcelableArrayListExtra(ALBUM_DATA)!!
        markerPos = intent.getParcelableExtra(MARKER)!!

        val picasso = Picasso.Builder(this).build()

        val mGeocoder: Geocoder = Geocoder(this)
        var siteList: List<Address>? = null
        var locaArray: ArrayList<LatLng> = arrayListOf()
        var loca: LatLng

        for (i in 0 until albumData.size) {
            if (albumData?.get(i)?.site == "") {
                locaArray.add(LatLng(0.0, 0.0))
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
            loca = LatLng(lati.toDouble(), long.toDouble())
            locaArray.add(loca)
            }
        }

        for (i in 0 until albumData.size) {
            if (locaArray[i] == LatLng(0.0, 0.0)) {
                continue
            }
            else {
                if (markerPos == locaArray[i]) {
                    var change = albumData[i].imageArray.toString()

                    var delChange = change.replace("[", "")
                    delChange = delChange.replace("]", "")

                    var splChange = delChange.split(", ")
                    var album: ArrayList<String> = arrayListOf()
                    for (element in splChange)
                        album.add(element)

                    aMemoSite.text = albumData[i].site
                    aMemoTitle.text = albumData[i].title
                    aMemoView.adapter = ViewPagerAdapter(albumData[i].imageArray!!)
                    aMemoDate.text = albumData[i].date
                    aMemoContent.text = albumData[i].content
                    setContent(albumData?.get(i)?.tag, aMemoTag)
                }
            }
        }

    }

    fun setContent(mTagLists: String?, tags_view: TextView) {
//        var tag = ""
        var i:Int = 0
//        while (i < mTagLists.size)
//        {
//            tag += "#" + mTagLists.get(i) + ""
//            i++
//        }
        val tag = mTagLists!!
        val hashtagSpans = getSpans(tag, '#')
        val tagsContent : SpannableString = SpannableString(tag)
        i = 0
        while (i < hashtagSpans.size)
        {
            val span = hashtagSpans.get(i)
            val hashTagStart = span[0]
            val hashTagEnd = span[1]
            val hashTag = Hashtag(applicationContext)
            hashTag.setOnClickEventListener(object: Hashtag.ClickEventListener {
                override fun onClickEvent(data:String) {
                }
            })
            tagsContent.setSpan(hashTag, hashTagStart, hashTagEnd, 0)
            i++
        }
        //   val tags_view = findViewById(R.id.textview_tag) as TextView
        if (tags_view != null)
        {
            tags_view.setMovementMethod(LinkMovementMethod.getInstance())
            tags_view.setText(tagsContent)
        }
    }
    fun getSpans(body:String, prefix:Char):ArrayList<IntArray> {
        val spans = ArrayList<IntArray>()
        val pattern = Pattern.compile(prefix + "\\w+")
        //complie(prefix + "\\w+")
        val matcher = pattern.matcher(body)
        while (matcher.find())
        {
            val currentSpan = IntArray(2)
            currentSpan[0] = matcher.start()
            currentSpan[1] = matcher.end()
            spans.add(currentSpan)
        }
        return spans
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
                .inflate(R.layout.album_detail_memo_viewpager, container, false)

            val image: ImageView = view.findViewById(R.id.aMemoImage)

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