package kr.ac.kumoh.s20171278.map_map_challenge.album.main

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tour_list.*
import kotlinx.android.synthetic.main.activity_tour_list_item.*
import kotlinx.android.synthetic.main.share_activity_share_tap.*
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.hashtag.Hashtag
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.zip.Inflater


class TourListActivity : AppCompatActivity() {

    companion object {
        const val KEY_ALBUM_NAME = "album_name"
        const val PICTURE_REQUEST_CODE = 111
        const val ALBUM_DATA = "album_data"
        const val LOCAL_ARRAY = "local_array"
        const val MARKER = "marker"
        const val QUEUE_TAG = "volley"
        const val TOUR_DATA = "tour_data"
    }

    data class Tour(
        val title:String,
        val addresses:String,
        val thumbnail: String,
        val contentid: String
        )

    val db = FirebaseFirestore.getInstance()
    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    private var userUid: String? = null
    val mStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val mAdapter = TourAdapter()
    // 여러개 넣으려고 만든거
    val tourData: ArrayList<Tour> = arrayListOf()
    private lateinit var mQueue: RequestQueue
    val key = "dACSB9BZ19USbe90Ni4lY7ifg0T%2FDsyEmtIj2EcoWqs%2BJqcaYwHIr150GHXAVYGU%2BII9fTQDzjgHf%2BSnE%2FXNIg%3D%3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_list)

        albumData = intent!!.getParcelableArrayListExtra(ALBUM_DATA)
        var saveCnt = 0
        for(i in 0 until albumData.size){
            getLaLoTourData(albumData[i].lati.toString(), albumData[i].long.toString())
            saveCnt = saveCnt + 1

        }
        tourRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
        mAdapter.notifyDataSetChanged()


        Log.d("lll", albumData.size.toString())
    }

    fun getLaLoTourData(la: String, lo: String){
        val url = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?MobileOS=AND&MobileApp=TestApp&ServiceKey="+key+"&listYN=Y&contentTypeId=12&arrange=S&mapX=$lo&mapY=$la&radius=2000&_type=json"
        Log.d("lll", "la : $la, lo: $lo")

        mQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET,
            url, null,
            Response.Listener { response ->
                val responseObj: JSONObject = response.getJSONObject("response")
                //   val jsonData = response.getJSONObject("items")
                Log.d("lll", "responseObj: "+ responseObj)

                val bodyObj: JSONObject = responseObj.getJSONObject("body")
                Log.d("lll", "bodyObj: "+ bodyObj)

                val items: JSONObject? = bodyObj.optJSONObject("items")
                Log.d("lll", "items: "+ items)
                if(items==null){
                    Log.d("lll items null", "items: "+ items)
                   // Toast.makeText(this, "근처 관광 정보가 없어요", Toast.LENGTH_LONG).show()
                }
                else if(bodyObj.getInt("totalCount") == 1){
                    val item: JSONObject = items.getJSONObject("item")
                    Log.d("lll cnt 1", "item: "+ item)
                    tourData.add(Tour(item.getString("title"), item.getString("addr1"), item.optString("firstimage2", "no value"), item.getString("contentid")))
                }
                else{
                    val item: JSONArray = items.getJSONArray("item")
                    Log.d("lll", "item: "+ item)
                    val touristData: JSONObject = item.getJSONObject(0)
                    Log.d("lll", "첫번째 아이템: "+ touristData)
                    tourData.add(Tour(touristData.getString("title"), touristData.getString("addr1"), touristData.optString("firstimage2", "no value"), touristData.getString("contentid")))
                }
                showJsonList(tourData)
                Log.d("lll", "tourData: " + tourData)

            },
            Response.ErrorListener { error -> Log.e("RESULT", error.toString())
            }
        )

        request.tag = QUEUE_TAG
        mQueue.add(request)
    }

    private fun showJsonList(tourData: ArrayList<Tour>){
        Log.d("lll", "showJsonList, tourData: " + tourData)
        tourRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        mQueue.cancelAll(QUEUE_TAG)
    }

    inner class TourAdapter : RecyclerView.Adapter<TourAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            // 나머지(주차시설, 쉬는날 등등)는 제대로 돌아가면 추가할예정
            val tourTitle: TextView  // 관광지 이름
            val tourAddress: TextView  // 관광지 주소
            val tourImage: ImageView  // 관광지 썸네일
            // val tourResult:TextView   // 테스트용 나중에 지울거임

            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                tourTitle = root.findViewById(R.id.tourTitle)
                tourAddress = root.findViewById(R.id.tourAddress)
                tourImage= root.findViewById(R.id.tourImage)
                //  tourResult =root.findViewById(R.id.result)
            }

            // 클릭 이벤트
            // SelectionImagePopupActivity : 누른 장소에 있는 사진을 recyclerView 형태로 보여줌
            override fun onClick(v: View?) {
                val intent : Intent = Intent(applicationContext, DetailTourActivity::class.java)
                intent.putExtra(TOUR_DATA, tourData[adapterPosition].contentid)
                startActivity(intent)
                Log.d("ggg tourList click", adapterPosition.toString() + "번째 관광지 정보")
            }
        }

        override fun getItemCount(): Int {
            return tourData.size
            //return 3
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(applicationContext).inflate(R.layout.activity_tour_list_item, parent, false)
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            //  holder.tourResult.text = tourData?.get(position).title
            // title : 관광지명
//            val picasso: Picasso = Picasso.Builder(applicationContext)
//                .listener { _, _, e -> e.printStackTrace() }
//                .build()
            holder.tourTitle.text = tourData?.get(position).title
            // addr1 : 서울시 마포구 어쩌구 자세히 나오는 주소
            holder.tourAddress.text = tourData?.get(position).addresses
            // firstimage2 : 썸네일 이미지
            Glide.with(applicationContext)
                .load(tourData?.get(position).thumbnail)
//                .fit()
                .centerCrop()
                .placeholder(R.drawable.img_default)
                .into(holder.tourImage)
            // 여러개는 이렇게 해서 넣을라고 했는데 안들어가지네,,,,,,?

        }
    }
}
