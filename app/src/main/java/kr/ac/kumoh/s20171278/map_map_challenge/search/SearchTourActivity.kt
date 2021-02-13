package kr.ac.kumoh.s20171278.map_map_challenge.search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.search_activity_search_tour.*
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.DetailTourActivity
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.TourListActivity
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder

class SearchTourActivity : AppCompatActivity() {
    private lateinit var searchText: String

    private lateinit var mQueue: RequestQueue
    val key = "dACSB9BZ19USbe90Ni4lY7ifg0T%2FDsyEmtIj2EcoWqs%2BJqcaYwHIr150GHXAVYGU%2BII9fTQDzjgHf%2BSnE%2FXNIg%3D%3D"
//    val tourData:ArrayList<TourListActivity.Tour> = arrayListOf()
    val mArray:ArrayList<TourListActivity.Tour> = arrayListOf()
    private val mAdapter = TourAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity_search_tour)

        tSearchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    getSearchTourData(query)

                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })

    }

    fun getSearchTourData(query:String){
        mQueue = Volley.newRequestQueue(this)

        val keyword : String = URLEncoder.encode(query, "UTF-8")

        // 13 키워드 검색 조회 참고
        val url = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/searchKeyword?MobileOS=AND&MobileApp=TestApp&ServiceKey=$key&listYN=Y&contentTypeId=12&arrange=A&keyword=$keyword&_type=json"
        val tempArray:ArrayList<TourListActivity.Tour> = arrayListOf()
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
                    tempArray.add(
                        TourListActivity.Tour(
                            item.getString("title"),
                            item.getString("addr1"),
                            item.optString("firstimage", "no value"),
                            item.getString("contentid")
                        )
                    )
                }
                else{
                    val item: JSONArray = items.getJSONArray("item")
                    Log.d("lll", "item: "+ item)
                    val touristData: JSONObject = item.getJSONObject(0)
                    Log.d("lll", "첫번째 아이템: "+ touristData)
                    tempArray.add(
                        TourListActivity.Tour(
                            touristData.getString("title"),
                            touristData.getString("addr1"),
                            touristData.optString("firstimage1", "no value"),
                            touristData.getString("contentid")
                        )
                    )
                }
                tSearchList(query, tempArray)
                Log.d("lll", "tempArray: " + tempArray)

            },
            Response.ErrorListener { error -> Log.e("RESULT", error.toString())
            }
        )

        request.tag = TourListActivity.QUEUE_TAG
        mQueue.add(request)
    }

    override fun onStop() {
        super.onStop()
    //    mQueue.cancelAll(TourListActivity.QUEUE_TAG)
    }

    private fun tSearchList(filterString: String, tempArray: ArrayList<TourListActivity.Tour>){
        mArray.clear()
        mArray.addAll(tempArray)
        tSearchTextView.text = "[${filterString}] 검색 결과 : ${tempArray.size}개"

        tSearchRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            itemAnimator = DefaultItemAnimator()
        }
        mAdapter.notifyDataSetChanged()
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
                intent.putExtra(TourListActivity.TOUR_DATA, mArray[adapterPosition].contentid)
                startActivity(intent)
                Log.d("ggg tourList click", adapterPosition.toString() + "번째 관광지 정보")
            }
        }

        override fun getItemCount(): Int {
            return mArray.size
            //return 3
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(applicationContext).inflate(R.layout.activity_tour_list_item, parent, false)
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tourTitle.text = mArray?.get(position).title
            // addr1 : 서울시 마포구 어쩌구 자세히 나오는 주소
            holder.tourAddress.text = mArray?.get(position).addresses
            // firstimage1: 대표 이미지
            Glide.with(applicationContext)
                .load(mArray?.get(position).thumbnail)
                .centerCrop()
                .placeholder(R.drawable.img_default)
                .into(holder.tourImage)

        }
    }
}