package kr.ac.kumoh.s20171278.map_map_challenge

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.location.Address
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.PicassoEngine
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.main_activity_select_image.*
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_TEST
import kr.ac.kumoh.s20171278.map_map_challenge.main.MainTabActivity
import java.io.File
import java.io.IOException

class SelectImageActivity : AppCompatActivity() {
    companion object {
        const val KEY_ALBUM_NAME = "album_name"
        const val PICTURE_REQUEST_CODE = 111
        const val ALBUM_DATA = "album_data"
    }
    @Parcelize
    data class dbSite(var site:String? = null,
                      var imageArray: ArrayList<String>? = null,
                      var date:String? = null,
                      var albumName: String? = null,
                      var shareUserUid: String? = null,
                      var lati: Double = 0.0,
                      var long: Double = 0.0,
                      var select: Int = 0,
                      var shareUser: String? = "",
                      var content: String? = "",
                      var title: String? = ""

    ): Parcelable
    // shareUser: 공유한 사람 이름
    // 공유할 때 변경해서 전송됨
    data class ImageData(val site:String?=null, val date:String?=null, val image:Uri?=null)
    private var dataArray = ArrayList<ImageData>()

    private var uriArray = ArrayList<Uri>()
    private var pathArray = ArrayList<String>()

    var mArray = ArrayList<dbSite>()  // 총 이미지에서 나오는 장소 클래스 모음

    private val mAdapter = SiteAdapter()

    // firestore 선언언
    lateinit var firestore : FirebaseFirestore

    private lateinit var albumName:String
    private var isTesting:Boolean = false

    private var userUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_select_image)
        isTesting = intent.getBooleanExtra(KEY_TEST, false)

        //MainActivity 에서 intent로 보낸 앨범 이름 받기
        albumName = intent.getStringExtra(MainActivity.KEY_ALBUM_NAME)
        set_date.text = albumName

        if (isTesting == false){
            // 깃헙 링크 : https://github.com/zhihu/Matisse
            // 사진 선택할 때 사용한 라이브러리 : Matisse
            Matisse.from(this)
                .choose(MimeType.ofImage())  // 갤러리에 보여줄 파일 타입(이미지, 동영상 중 선택 또는 모두 보여주기 가능)
                .countable(true)  // 몇 개 골랐는지 화면에 나타내줌(오른쪽 하단에 숫자로 표시)
                .maxSelectable(30)  // 최대 선택 이미지(default:5) 필요한만큼 늘리면됨 일단 그냥해둠
                .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))  //
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)  // 160dp
                .thumbnailScale(0.85f)  // 썸네일 이미지 스케일(85퍼센트 크기)
                .imageEngine(PicassoEngine())  // 이미지 엔진(Picasso 엔진과 Glide 엔진 중 선택가능)
                // Picasso 엔진으로 선택
                .forResult(PICTURE_REQUEST_CODE)  // startActivityForResult 안에 들어가는 값과 동일
            // Request 잘 돌아왔는지 확인할 때 쓰는 값 넣어주는 곳

        }
        else{  // test 시
            Log.i("Test", "selectImageActivity")
        }

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(applicationContext, 3) // 한 줄에 item 세 개 들어감
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
            }

        btnNext.setOnClickListener {
            val intent = Intent(applicationContext, MainTabActivity::class.java)
            intent.putExtra(KEY_ALBUM_NAME, albumName)
            intent.putExtra(ALBUM_DATA, mArray)
            startActivity(intent)
            finish()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // 갤러리 값 받아오는데 사용한 라이브러리 : Matisse
                uriArray = Matisse.obtainResult(data) as ArrayList<Uri>  // uriArray에 선택한 이미지들의 Uri 넣음
                pathArray = Matisse.obtainPathResult(data) as ArrayList<String>
                // 이미지는 사용자가 선택한 순서대로 들어감

                // 장소 뽑아 넣기
                for (i in 0 until uriArray.size) {
                    dataArray.add(
                        ImageData(
                            getAddress(pathArray[i]),
                            getTime(pathArray[i]),
                            uriArray[i]
                        )
                    )
                }

                // 장소로 정렬
                // data.date 으로 바꾸면 시간으로 정렬
                dataArray.sortBy { data -> data.date }

                // mArray에 주소랑 ImageArray 저장
                saveImage(dataArray)

                mAdapter.notifyDataSetChanged()
            }
            else{
                Log.i("Matisse", "resultCode != Activity.RESULT_OK")
            }
        }
    }

    private fun saveImage(dataArray: ArrayList<ImageData>){
        var dbTempSite =
            dbSite(
                dataArray[0].site,
                null,
                dataArray[0].date
            )

        var StempUriArray : ArrayList<String> = ArrayList<String>()

        StempUriArray.add(dataArray[0].image.toString())

        if(dataArray.size == 1){  // 사진이 한 장이 경우
            if (dbTempSite.site.toString() == "주소없음"){
                // continue
                Log.d("dataArray", "사진 한장인데 주소도없음")
            }else{
                mArray.add(
                    dbSite(
                        dbTempSite.site,
                        StempUriArray,
                        dbTempSite.date,
                        albumName
                    )
                )
            }
        }

        for (i in 1 until dataArray.size){
            if (dbTempSite.site != dataArray[i].site) {  // 장소가 다른 경우 || 장소가 같더라도 사진의 시간이 다른 경우
                if (dbTempSite.site.toString() == "주소없음"){
                    continue
                }else {
                    mArray.add(
                        dbSite(
                            dbTempSite.site,
                            StempUriArray,
                            dbTempSite.date,
                            albumName
                        )
                    )
                }
                dbTempSite =
                    dbSite(
                        dataArray[i].site,
                        null,
                        dataArray[i].date
                    )  // 다른장소로 변경

                StempUriArray = arrayListOf() // 초기화

                StempUriArray.add(dataArray[i].image.toString())
            }
            else{
                StempUriArray.add(dataArray[i].image.toString())
            }
            if (i == dataArray.size-1){  // 마지막 사진 처리
                if (dbTempSite.site.toString() == "주소없음"){
                    continue
                }else{
                    mArray.add(
                        dbSite(
                            dbTempSite.site,
                            StempUriArray,
                            dbTempSite.date,
                            albumName
                        )
                    )
                }
            }
        }
    }

    // 사진 날짜 추출
    private fun getTime(filename: String): String{
        var finalTime = ""
        try {
            val exif: ExifInterface = ExifInterface(filename)
            var dateTime = getTagString(ExifInterface.TAG_DATETIME, exif)
            var time = dateTime.split(" ")

            for (i in 2..2)
                finalTime += time[i]
            finalTime = finalTime.replace(":", ".")

            for (i in 3..3)
                finalTime += " "+ time[i]
        } catch (e: IOException) {
            // Handle any errors
        }

        return finalTime
    }

    // 사진 장소 추출
    private fun getAddress(filename: String): String{
        try {
            val exif: ExifInterface = ExifInterface(filename)
            val latitude: List<String> = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)!!.split("/", ",")  // 위도 결과에서 기호를 제외한 문자열 배열 생성
            val longitude: List<String> = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)!!.split("/", ",")  // 경도 결과에서 기호를 제외한 문자열 배열 생성

            val latiD = convertGps(latitude)
            val longD = convertGps(longitude)
            val mGeocoder: Geocoder = Geocoder(this)
            val mResultList: List<Address> = mGeocoder.getFromLocation(latiD, longD, 2)
            if (mResultList.isNotEmpty()){
                var address = mResultList[0].getAddressLine(0)
                var add = address.split(" ")
                var finalAdd = ""
                for (i in 1..3)
                    finalAdd += add[i] + " "
                return finalAdd
            }
        } catch (e: IOException) {
            // Handle any errors
            Log.e("aaaa", filename)
        }

        return "주소없음"
    }


    private fun getTagString(tag: String, exif: ExifInterface): String {
        return tag + " : " + exif.getAttribute(tag) + "\n"
    }

    private fun convertGps(dms: List<String>):Double{  // 위도, 경도 변환 함수
        val dmsDo: Int = dms[0].toInt()
        var dmsMinute:Int = dms[2].toInt()
        val dmsSecond :Double = dms[4].toInt()/dms[5].toDouble()
        return String.format("%.4f",dmsDo + dmsMinute/60.0 + dmsSecond/3600.0).toDouble()
    }

    inner class SiteAdapter : RecyclerView.Adapter<SiteAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val siteName: TextView  // 장소 이름
            val siteImage: ImageView  // 장소 대표 이미지(맨 앞 사진 한장)

            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                siteName = root.findViewById(R.id.siteName)
                siteImage= root.findViewById(R.id.siteImage)
            }

            // 클릭 이벤트
            // SelectionImagePopupActivity : 누른 장소에 있는 사진을 recyclerView 형태로 보여줌
            override fun onClick(v: View?) {
                val intent : Intent = Intent(applicationContext, SelectImagePopupActivity::class.java)
                intent.putExtra("data", mArray[adapterPosition].imageArray)
                intent.putExtra("siteName", mArray[adapterPosition].site)
                startActivity(intent)
                }
        }
        override fun getItemCount(): Int {
            return mArray.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(applicationContext).inflate(R.layout.main_select_image_item, parent, false) //context가 없으니ㅏ 이거 씀 applicationContext
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val picasso:Picasso = Picasso.Builder(applicationContext)
                .listener { _, _, e -> e.printStackTrace() }
                .build()
            holder.siteName.text = mArray[position].site  // 장소
            val uri: Uri = Uri.parse(mArray[position].imageArray?.get(0))
            picasso  // 사진
                .load(uri)  // 넣을 Uri 데이터
                .fit()  // 이미지 늘림없이 imageView에 맞춤
                .centerCrop()  // 센터 크롭 중앙을 기준으로 잘라내기
                .placeholder(R.drawable.img_default)  // 로드되지 않은 경우 사용될 기본 이미지
                .into(holder.siteImage)  // 들어갈 imageView 위치
            // viewHolder가 아닌 일반 setImageUri로 사용할 경우 .into(siteImage)로 씀
        }
    }
}
