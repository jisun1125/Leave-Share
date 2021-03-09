package kr.ac.kumoh.s20171278.map_map_challenge

import android.app.Activity
import android.app.ProgressDialog
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
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.engine.impl.PicassoEngine
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.main_activity_select_image.*
import kotlinx.android.synthetic.main.main_select_image_item.*
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_TEST
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.TourListActivity
import kr.ac.kumoh.s20171278.map_map_challenge.main.MainTabActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream

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
                      var title: String? = "",
                      var tag: String? = "",
                      var docId: Int? = null

    ): Parcelable
    // shareUser: 공유한 사람 이름
    // 공유할 때 변경해서 전송됨
    data class ImageData(val site:String?=null, val date:String?=null, val image:Uri?=null, val path: String?=null)
    private var dataArray = ArrayList<ImageData>()

    private var checkList = arrayListOf<Boolean>()

    private var uriArray = ArrayList<Uri>()
    private var pathArray = ArrayList<String>()

    var mArray = ArrayList<dbSite>()  // 총 이미지에서 나오는 장소 클래스 모음

    var mPathArray = ArrayList<dbSite>()

    private val mAdapter = SiteAdapter()

    // firestore 선언언
    lateinit var firestore : FirebaseFirestore

    private lateinit var albumName:String
    private var isTesting:Boolean = false

    private var userUid: String? = null

    private lateinit var mQueue: RequestQueue
    val key = "dACSB9BZ19USbe90Ni4lY7ifg0T%2FDsyEmtIj2EcoWqs%2BJqcaYwHIr150GHXAVYGU%2BII9fTQDzjgHf%2BSnE%2FXNIg%3D%3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_select_image)
        isTesting = intent.getBooleanExtra(KEY_TEST, false)

        //MainActivity 에서 intent로 보낸 앨범 이름 받기
        albumName = intent.getStringExtra(MainActivity.KEY_ALBUM_NAME)!!
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
                .maxOriginalSize(2)
                .thumbnailScale(0.85f)  // 썸네일 이미지 스케일(85퍼센트 크기)
                .imageEngine(GlideEngine())  // 이미지 엔진(Picasso 엔진과 Glide 엔진 중 선택가능)
                // Picasso 엔진으로 선택
                    // 2020-10-26 Glide로 변경
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

        btnTag.setOnClickListener {
            val tempSize: Int = checkList.size
            val tempLen: Int = mPathArray.size
            var totalCnt: Int = 0
            val tempTagArray = arrayOfNulls<String>(tempSize)
      //      val tempTagArray = ArrayList<String>(initialCapacity =tempSize)
            Log.d("lll", "tempSize: $tempSize, tempLen: $tempLen, tempTagArray size: ${tempTagArray.size}")

            for(i in 0 until checkList.size){
                if(checkList[i]){
                    Log.d("lll", "$i 번째 체크")
                    tempTagArray[i] = ""
                  //  Log.d("lll", "tempTagArray size: ${tempTagArray.size}, mPathArray size: ${mPathArray[i].imageArray!!.size}")
                    var clrCnt: Int = 0
                    var clrBoolean: Boolean = true
                    for (j in 0 until mPathArray[i].imageArray!!.size){
                        val progressDialog: ProgressDialog = ProgressDialog(this)
                        progressDialog.setMessage("태그를 다는 중입니다.")
                        progressDialog.setCancelable(true)
                        progressDialog.setCanceledOnTouchOutside(false)
                        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
                        progressDialog.show()

                        val tempExifInterface = ExifInterface(mPathArray[i].imageArray?.get(j)!!)
                        val la : Double = getLa(tempExifInterface)
                        val lo : Double = getLo(tempExifInterface)
                        val url = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?MobileOS=AND&MobileApp=TestApp&ServiceKey="+key+"&listYN=Y&contentTypeId=12&arrange=S&mapX=$lo&mapY=$la&radius=2000&_type=json"
                     //   Log.d("lll", "la : $la, lo: $lo")

                        var resultTitle: String? = null

                        mQueue = Volley.newRequestQueue(this)

                        val request = JsonObjectRequest(
                            Request.Method.GET,
                            url, null,
                            Response.Listener { response ->
                                val bodyObj: JSONObject = response.getJSONObject("response").getJSONObject("body")
                                val items: JSONObject? = bodyObj.optJSONObject("items")
                                if(items==null){
                                    Log.d("lll items null", "$i 번째 items: "+ items)
                                    // Toast.makeText(this, "근처 관광 정보가 없어요", Toast.LENGTH_LONG).show()
                                }
                                else if(bodyObj.getInt("totalCount") == 1){
                                    val item: JSONObject = items.getJSONObject("item")
                                    resultTitle= item.getString("title")
                                }
                                else{
                                    val item: JSONArray = items.getJSONArray("item")
                                    val touristData: JSONObject = item.getJSONObject(0)
                                    resultTitle = touristData.getString("title")
                                }

                                if(resultTitle!=null){
                                    val replace = resultTitle!!.replace(" ", "")
                                    val replace2 = replace.replace("\\[[^]]*\\]".toRegex(), "")
                                    val replace3 = replace2.replace("\\([^)]*\\)".toRegex(), "")
                                    Log.d("lll", "replace 1: $replace, 2: $replace2, 3: $replace3")
                                    tempTagArray[i] += " $replace3"
                                    Log.d("lll", "$i 번째 tempTagArray: ${tempTagArray[i]}, clrCnt: $clrCnt")
                                   // tempTagArray.add(resultTitle.toString())
                                }
                                if(mPathArray[i].imageArray!!.size-1 == clrCnt) {
                                    val tempTagSplit = tempTagArray[i]?.split(" ")?.distinct()
                                    Log.d("lll", "split: $tempTagSplit")
                                  //  val tempDis = tempTagSplit!!.distinct()
                                    Log.d("lll", "distinct(): $tempTagSplit")
                                    val tempTagSplitJoin = tempTagSplit?.joinToString("#")
                                    Log.d("lll", "join: $tempTagSplitJoin")
                                    mArray[i].tag = tempTagSplitJoin
                                    Log.d("lll", "최종 관광지 태그: ${mArray[i].tag}")
                                }
                                else{
                                    clrCnt += 1
                                }
                                progressDialog.dismiss()
                            },
                            Response.ErrorListener { error -> Log.e("RESULT", error.toString())
                            }
                        )
                        request.tag = TourListActivity.QUEUE_TAG
                        mQueue.add(request)
                    }
                    if (mPathArray[i].imageArray!!.size == clrCnt){
                        // 중복 제거
                        Log.d("lll", "$i 번째 tempArray: $tempTagArray")
//                        tempTagArray.distinct()
//                        Log.d("lll", "$i 번째 tempArray 중복제거: $tempTagArray")
//                        var tempTag: String = tempTagArray.joinToString()
//                        Log.d("lll", "$i 번째 tempTag String: $tempTag")
//                        tempTag.replace(" ", "")
//                        Log.d("lll", "$i 번째 tempTagArray = $tempTagArray, tempTag= $tempTag")
//                        tempTagArray = arrayListOf()
                    }
// 이 for문에도 위에 cirCnt 센거처럼 조건 넣어서 딜레이 걸면 tempArray 안겹치고 될거같은데
                }
                else{
                    Log.d("lll", "$i 번째 체크안함")
                }
            }
            // 관광지 태깅
            // 일단 태그 바로 달아줌
        }
    }

    fun getLaLoTourTagData(la: String, lo: String, tagArray: ArrayList<String>){
        val url = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?MobileOS=AND&MobileApp=TestApp&ServiceKey="+key+"&listYN=Y&contentTypeId=12&arrange=S&mapX=$lo&mapY=$la&radius=2000&_type=json"
        Log.d("lll", "la : $la, lo: $lo")

        var resultTitle: String? = null

        mQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET,
            url, null,
            Response.Listener { response ->
                val bodyObj: JSONObject = response.getJSONObject("response").getJSONObject("body")
                //   val jsonData = response.getJSONObject("items")
   //             Log.d("lll", "responseObj: "+ responseObj)

//                val bodyObj: JSONObject = responseObj.getJSONObject("body")
   //             Log.d("lll", "bodyObj: "+ bodyObj)

                val items: JSONObject? = bodyObj.optJSONObject("items")
  //              Log.d("lll", "items: "+ items)
                if(items==null){
                    Log.d("lll items null", "items: "+ items)
                    // Toast.makeText(this, "근처 관광 정보가 없어요", Toast.LENGTH_LONG).show()
                }
                else if(bodyObj.getInt("totalCount") == 1){
                    val item: JSONObject = items.getJSONObject("item")
   //                 Log.d("lll cnt 1", "item: "+ item)
                    resultTitle= "#"+item.getString("title")
                 //   tourData.add(TourListActivity.Tour(item.getString("title"), item.getString("addr1"), item.optString("firstimage2", "no value"), item.getString("contentid")))
                }
                else{
                    val item: JSONArray = items.getJSONArray("item")
     //               Log.d("lll", "item: "+ item)
                    val touristData: JSONObject = item.getJSONObject(0)
     //               Log.d("lll", "첫번째 아이템: "+ touristData)
                    resultTitle = "#"+touristData.getString("title")
                  //  tourData.add(TourListActivity.Tour(touristData.getString("title"), touristData.getString("addr1"), touristData.optString("firstimage2", "no value"), touristData.getString("contentid")))
                }

                Log.d("lll", "tag return $resultTitle")
                if(resultTitle!=null){
                    tagArray.add(resultTitle.toString())
                }
             //   Log.d("lll", "tourData: " + tourData)

            },
            Response.ErrorListener { error -> Log.e("RESULT", error.toString())
            }
        )

        request.tag = TourListActivity.QUEUE_TAG
        mQueue.add(request)

//        return resultTitle
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
                            uriArray[i],
                            pathArray[i]
                        )
                    )
                 //   checkList.add(i,false)
                }

            //    Log.d("lll", "dataArray: ${dataArray.size}, $dataArray")

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

        var dbImagePathSite =
            dbSite(
                dataArray[0].site,
                null,
                dataArray[0].date
            )

        var StempPathArray: ArrayList<String> = ArrayList<String>()

        var StempUriArray : ArrayList<String> = ArrayList<String>()

        StempUriArray.add(dataArray[0].image.toString())

        StempPathArray.add(dataArray[0].path.toString())
        var docIdCnd = 0

        if(dataArray.size == 1){  // 사진이 한 장이 경우
            if (dbTempSite.site.toString() == "주소없음"){
                // continue
                Log.d("dataArray", "사진 한장인데 주소도없음")
            }else{
                mArray.add(
                    dbSite(
                        docId = docIdCnd,
                        site = dbTempSite.site,
                        imageArray = StempUriArray,
                        date = dbTempSite.date,
                        albumName = albumName
                    )
                )
                mPathArray.add(
                    dbSite(
                        docId = docIdCnd,
                        site = dbTempSite.site,
                        imageArray = StempPathArray,
                        date = dbTempSite.date,
                        albumName = albumName
                    )
                )
                checkList.add(false)
            }
        }

        for (i in 1 until dataArray.size){
            Log.d("aaa docIdCnd", docIdCnd.toString())
            if (dbTempSite.site != dataArray[i].site) {  // 장소가 다른 경우 || 장소가 같더라도 사진의 시간이 다른 경우
                if (dbTempSite.site.toString() == "주소없음"){
                    continue
                }else {
                    mArray.add(
                        dbSite(
                            docId = docIdCnd,
                            site = dbTempSite.site,
                            imageArray = StempUriArray,
                            date = dbTempSite.date,
                            albumName = albumName
                        )
                    )
                    mPathArray.add(
                        dbSite(
                            docId = docIdCnd,
                            site = dbTempSite.site,
                            imageArray = StempPathArray,
                            date = dbTempSite.date,
                            albumName = albumName
                        )
                    )
                    checkList.add(false)
                    docIdCnd += 1
                }
                dbTempSite =
                    dbSite(
                        dataArray[i].site,
                        null,
                        dataArray[i].date
                    )  // 다른장소로 변경

                StempUriArray = arrayListOf() // 초기화

                StempPathArray = arrayListOf()

                StempUriArray.add(dataArray[i].image.toString())

                StempPathArray.add(dataArray[i].path.toString())
            }
            else{
                StempUriArray.add(dataArray[i].image.toString())

                StempPathArray.add(dataArray[i].path.toString())
            }
            if (i == dataArray.size-1){  // 마지막 사진 처리
                if (dbTempSite.site.toString() == "주소없음"){
                    continue
                }else{
                    mArray.add(
                        dbSite(
                            docId = docIdCnd,
                            site = dbTempSite.site,
                            imageArray = StempUriArray,
                            date = dbTempSite.date,
                            albumName = albumName
                        )
                    )
                    mPathArray.add(
                        dbSite(
                            docId = docIdCnd,
                            site = dbTempSite.site,
                            imageArray = StempPathArray,
                            date = dbTempSite.date,
                            albumName = albumName
                        )
                    )
                    checkList.add(false)
                    docIdCnd += 1
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

    // 사진 장소 추출(주소)
    private fun getAddress(filename: String): String{
        val exif: ExifInterface = ExifInterface(filename)
        try {   // 좌표일 경우
            val latiD = getLa(exif)
            val longD = getLo(exif)

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
        } catch (e: IOException) {  // 주소일 경우
            Log.e("llla", e.toString())
        }

        return "주소없음"
    }

    private fun getLa(exif: ExifInterface): Double{
        val latitude: List<String> = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)!!.split("/", ",")  // 위도 결과에서 기호를 제외한 문자열 배열 생성
        val latiD = convertGps(latitude)
        return latiD

    }

    private fun getLo(exif: ExifInterface): Double{
        val longitude: List<String> = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)!!.split("/", ",")  // 경도 결과에서 기호를 제외한 문자열 배열 생성

        val longD = convertGps(longitude)

        return longD
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
            val siteCheckbox: CheckBox

            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                siteName = root.findViewById(R.id.siteName)
                siteImage= root.findViewById(R.id.siteImage)
                siteCheckbox = root.findViewById(R.id.siteCheckbox)
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
//            val picasso:Picasso = Picasso.Builder(applicationContext)
//                .listener { _, _, e -> e.printStackTrace() }
//                .build()
            holder.siteName.text = mArray[position].site  // 장소
            val uri: Uri = Uri.parse(mArray[position].imageArray?.get(0))
            Glide.with(applicationContext)  // 사진
                .load(uri)  // 넣을 Uri 데이터
//                .fit()  // 이미지 늘림없이 imageView에 맞춤
                .centerCrop()  // 센터 크롭 중앙을 기준으로 잘라내기
                .placeholder(R.drawable.img_default)  // 로드되지 않은 경우 사용될 기본 이미지
                .into(holder.siteImage)  // 들어갈 imageView 위치
            // viewHolder가 아닌 일반 setImageUri로 사용할 경우 .into(siteImage)로 씀
            holder.siteCheckbox.setOnClickListener {v->
                val check: CheckBox = v as CheckBox

                if (check.isChecked){
                    check.isChecked = true
                    checkList[position] = true
                  //  albumData[position].select = 1
                }
                else
                {
                    check.isChecked = false
                    checkList[position] = false
                  //  albumData[position].select = 0
                }

            }
        }
    }
}
