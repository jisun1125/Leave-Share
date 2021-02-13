package kr.ac.kumoh.s20171278.map_map_challenge.album.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_detail_tour.*
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.TourListActivity.Companion.TOUR_DATA
import kr.ac.kumoh.s20171278.map_map_challenge.main.DetailMemoActivity
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class DetailTourActivity : AppCompatActivity() {
    private lateinit var mQueue: RequestQueue
    val key = "dACSB9BZ19USbe90Ni4lY7ifg0T%2FDsyEmtIj2EcoWqs%2BJqcaYwHIr150GHXAVYGU%2BII9fTQDzjgHf%2BSnE%2FXNIg%3D%3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_tour)
        val tourContentId = this.intent.getStringExtra(TOUR_DATA)
        getDetailTourData(tourContentId)
    }
    // TourAPI 설명서 중 16 공통정보 조회 참조
    fun getDetailTourData(contentId:String){

        val url = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailCommon?MobileOS=AND&MobileApp=TestApp&ServiceKey=$key&contentId=$contentId&defaultYN=Y&firstImageYN=Y&addrinfoYN=Y&overviewYN=Y&_type=json"
        mQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET,
            url, null,
            Response.Listener { response ->
                val responseObj: JSONObject = response.getJSONObject("response")
                val bodyObj: JSONObject = responseObj.getJSONObject("body")
                val items: JSONObject? = bodyObj.optJSONObject("items")
                if(items==null){
                  //  Log.d("lll items null", "items: "+ items)
                    // Toast.makeText(this, "근처 관광 정보가 없어요", Toast.LENGTH_LONG).show()
                }

                else{
                    val item: JSONObject = items.getJSONObject("item")
                    Log.d("lll cnt 1", "item: "+ item)
                    detailTourTitle.text = item.getString("title")
                    detailTourAddr.text = item.getString("addr1")
                    var homepage = item.optString("homepage", "없음")
                    val doc : Document = Jsoup.parseBodyFragment(homepage)
                    val body = doc.getElementsByTag("a")
                    if(body.size>0){
                        homepage = body.get(0).attr("href")
                        Log.d("lll", "html tag ${body.get(0).attr("href")}")
                    }


                    var overview = item.getString("overview")
                    Log.d("lll", "overview: $overview \nhomepage: $homepage")
                    overview = overview.replace("<br/>", "\n")
                    overview = overview.replace("<br>", "\n")
                    overview = overview.replace("""<br \\/>""", "\n")
                    overview = overview.replace("<br />", "\n")

                    // 줄바꿈을 제외한 HTML 태그 또는 모든 태그 형식 제거
                    overview = overview.replace("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "")

                    // homepage : 형식 제거 후 http...로 시작하는 맨 처음 문자열만 추출?
                    detailTourHomepage.text = homepage
                    detailTourOverview.text = overview

                    Glide.with(applicationContext)
                        .load(item.optString("firstimage", "no value"))
                        .centerCrop()
                        .placeholder(R.drawable.img_default)
                        .into(detailTourImage)
                }
            },
            Response.ErrorListener { error -> Log.e("RESULT", error.toString())
            }
        )
        request.tag = TourListActivity.QUEUE_TAG
        mQueue.add(request)
    }
}