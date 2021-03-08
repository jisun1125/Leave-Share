package kr.ac.kumoh.s20171278.map_map_challenge


import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.search_activity_search.*
import kotlinx.android.synthetic.main.search_activity_search.recyclerView
import kotlinx.android.synthetic.main.shared_activity_shared_album.*
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumListActivity
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumTabActivity
import kr.ac.kumoh.s20171278.map_map_challenge.main.DetailMemoActivity
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.SharedAlbumActivity
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.SharedAlbumActivity.*
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.SharedTabActivity
import org.w3c.dom.Text

class SearchActivity : AppCompatActivity() {

    // 검색어
    private lateinit var searchText: String

    // mArray: recyclerView 적용하기 위한 array
    var mArray:ArrayList<SelectImageActivity.dbSite> = arrayListOf()

    val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var albumNameList: ArrayList<String> = arrayListOf()
    var shareAlbumNameList: ArrayList<String> = arrayListOf()
    var keyword: Int? = null
    var type: String? = null
    var userUid: String? = null
    var searchData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity_search)

    //    intent로 type 받기
       // type = "d"
        type = intent.getStringExtra("type")
        keyword = intent.getIntExtra("keyword", 0)
        userUid = auth.currentUser?.uid
        if (userUid== null){
            Toast.makeText(this, "로그인 후 검색할 수 있습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        db.collection("user").document("$userUid")
            .get().addOnSuccessListener { d ->
                shareAlbumNameList = d.get("shareAlbumList") as ArrayList<String>
                Log.d("sss shareAlbum", shareAlbumNameList.toString()) // ok
            }
        if (keyword != 0){
            val hashSearch = type
            set_date.text = "#$hashSearch 태그 검색"
            SearchKeyword("all", hashSearch!!)
        }
        else {  // MainActivity -> 네비게이션 드로어 통한 접근
            if (type == "album"){
                set_date.text = "앨범 검색"
            }
            else{
                set_date.text = "공유 앨범 검색"
            }

            searchView.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        SearchKeyword(type, query)

                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        return false
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchData.clear()
    }

    fun searchList(filterString: String, tempArray: ArrayList<SelectImageActivity.dbSite>){
        // temp.site 안에 filterString 글자가 있을 떄 아이템을 FilterList에 담음
        val filterList = tempArray.filter{temp->
            temp.site!!.contains(filterString) || temp.title!!.contains(filterString) || temp.content!!.contains(filterString) || temp.tag!!.contains(filterString)
        }

        mArray.clear()
        mArray.addAll(filterList)

        searchTextView.text = "[${filterString}] 검색 결과 : ${mArray.size}개"

        val mAdapter = MemoAdapter()

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            itemAnimator = DefaultItemAnimator()
        }
        mAdapter.notifyDataSetChanged()
    }

    // 앨범 리스트 반환, 공유앨범 리스트 반환 함수로 각각 나눠야 함
    fun SearchKeyword(type: String?, searchText: String){
        val progressDialog: ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("$searchText 키워드로 검색 중입니다.")
        progressDialog.setCancelable(true)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
        progressDialog.show()

        var aMemoCnt = 0
        var aFlag = false

        var sMemoCnt = 0
        var sFlag = false

        // 앨범 개수 카운트
        var memoCnt = 0
        var albumCnt = 0
        // 공유 앨범 개수 카운트
        var shareAlbumCnt = 0

        if(type == "all"){
            // 내 앨범 리스트 받아오기
            db.collection("user").document("$userUid").get()
                .addOnSuccessListener { d ->
                    albumNameList = d.get("albumList") as ArrayList<String>
                    albumCnt = 0
                    val tempArray: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
                    for (i in 0 until albumNameList.size) {

                        db.collection("user").document("$userUid")
                            .collection(albumNameList[i]).get()
                            .addOnSuccessListener { result ->
                                aMemoCnt = 0
                                for (document in result) {
                                    tempArray.add(document.toObject(SelectImageActivity.dbSite::class.java))
                                    aMemoCnt += 1
                                }
                                if (aMemoCnt == result.size()) {
                                    albumCnt += 1
                                    if (albumCnt == albumNameList.size && aMemoCnt == result.size()) {
                                        aFlag = true
                                        // 공유받은 앨범 리스트 받아오기
                                        db.collection("user").document("S]$userUid")
                                            .get().addOnSuccessListener { d ->
                                                shareAlbumNameList = d.get("albumList") as ArrayList<String>

                                                when (shareAlbumNameList.size) {
                                                    0 -> {
                                                        searchList(searchText, tempArray)
                                                        progressDialog.dismiss()
                                                    }
                                                    else -> {
                                                        shareAlbumCnt = 0
                                                        for (j in 0 until shareAlbumNameList.size) {

                                                            db.collection("user").document("S]$userUid")
                                                                .collection(shareAlbumNameList[j]).get()
                                                                .addOnSuccessListener { result ->
                                                                    memoCnt = 0
                                                                    for (document in result) {
                                                                        tempArray.add(document.toObject(SelectImageActivity.dbSite::class.java))
                                                                        memoCnt += 1
                                                                    }
                                                                    if (memoCnt == result.size()) {
                                                                        shareAlbumCnt += 1
                                                                        if (shareAlbumCnt == shareAlbumNameList.size && memoCnt == result.size()) {
                                                                            searchList(searchText, tempArray)
                                                                            progressDialog.dismiss()
                                                                        }
                                                                    }
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Log.d("aaaa search", "error $e")
                                                                }

                                                        }
                                                    }
                                                }


                                            }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.d("aaaa search", "error $e")
                            }

                    }
                }


        }
        else if(type == "album"){
            // 내 앨범 리스트 받아오기
            db.collection("user").document("$userUid").get()
                .addOnSuccessListener { d ->
                    albumNameList = d.get("albumList") as ArrayList<String>
                    albumCnt = 0
                    val tempArray: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
                    for (i in 0 until albumNameList.size) {
                        db.collection("user").document("$userUid")
                            .collection(albumNameList[i]).get()
                            .addOnSuccessListener { result ->
                                memoCnt = 0
                                for (document in result) {
                                    tempArray.add(document.toObject(SelectImageActivity.dbSite::class.java))
                                    memoCnt += 1
                                }
                                if (memoCnt == result.size()) {
                                    albumCnt += 1
                                    if (albumCnt == albumNameList.size && memoCnt == result.size()) {
                                        searchList(searchText, tempArray)
                                        progressDialog.dismiss()
                                        searchData = tempArray
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.d("aaaa search", "error $e")
                            }

                    }
                }
        }

        //share
        else{
            val tempArray: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
            for (i in 0 until shareAlbumNameList.size) {
                var shareAlbumIndex: ArrayList<String> = arrayListOf()
                var shareUserUid: String = ""
                // 공유앨범 정보(제목, 앨범 공유자 uid, 앨범 공유자 이름, 인덱스 List) 보관
                var tempShareAlbum: ShareAlbum = ShareAlbum(
                    name = shareAlbumNameList[i]
                )
                db.collection("user").document(userUid.toString())
                    .collection("ShareAlbum").document(shareAlbumNameList[i]).get()
                    .addOnSuccessListener { result ->
                        tempShareAlbum.index =
                            result.get("shareAlbumIndex") as ArrayList<String>
                        shareUserUid = result.get("shareUserUid") as String
                        tempShareAlbum.shareUserUid = shareUserUid
                        db.collection("user").document(shareUserUid).get()
                            .addOnSuccessListener { result ->
                                tempShareAlbum.shareUser =
                                    result.get("userName") as String
                            }
                        db.collection("user").document(shareUserUid)
                            .collection(shareAlbumNameList[i]).get()
                            .addOnSuccessListener { result ->
                                memoCnt = 0
                                for (document in result) {
                                    memoCnt += 1
                                    if (document.id.toString() in tempShareAlbum.index) {
                                        val temp: SelectImageActivity.dbSite =
                                            document.toObject(
                                                SelectImageActivity.dbSite::class.java
                                            )
                                        temp.shareUser = tempShareAlbum.shareUser
                                        Log.d("ssss if", temp.toString())
                                        tempArray.add(temp)
                                    }
                                }
                                if (memoCnt == result.size()) {
                                    shareAlbumCnt += 1
                                    if (shareAlbumCnt == shareAlbumNameList.size && memoCnt == result.size()) {
                                        searchList(searchText, tempArray)
                                        progressDialog.dismiss()
                                        searchData = tempArray
                                        Log.d("ssss searchData", searchData.toString())
                                    }
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.d("aaaa search", "error $e")
                    }
            }
            shareAlbumCnt = 0
        }

    }

    inner class MemoAdapter(): RecyclerView.Adapter<MemoAdapter.ViewHolder>() {
//        val picasso = Picasso.Builder(applicationContext).build()

        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val searchTitle: TextView
            val searchContent: TextView
            val searchViewPager: ViewPager
            val searchAlbum: TextView
            val searchUser: TextView

            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                searchTitle = root.findViewById(R.id.searchTitle)
                searchContent= root.findViewById(R.id.searchContent)
                searchViewPager = root.findViewById(R.id.searchView)
                searchAlbum = root.findViewById(R.id.searchAlbum)
                searchUser = root.findViewById(R.id.searchUser)
            }

            override fun onClick(v: View?) {
                // 공유앨범, 일반앨범 경우 구분해서 해야 함
                // 차라리 액티비티 2개 만드는게 나을거같음
                val tempClicked: SelectImageActivity.dbSite = mArray[adapterPosition]
                val tempIntentAlbum: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
                if (tempClicked.albumName in shareAlbumNameList){
                    // 공유 앨범 검색
                    var tempShareAlbum: ShareAlbum = ShareAlbum(
                        name = tempClicked.albumName
                    )
                    val intent = Intent(applicationContext, SharedTabActivity::class.java)
                    db.collection("user").document(userUid.toString())
                        .collection("ShareAlbum").document(tempClicked.albumName.toString()).get()
                        .addOnSuccessListener { result->
                            tempShareAlbum.index = result.get("shareAlbumIndex") as ArrayList<String>
                            tempShareAlbum.shareUserUid = result.get("shareUserUid") as String

                        }
                        .addOnCompleteListener{
                            db.collection("user").document(tempShareAlbum.shareUserUid.toString())
                                .collection(tempShareAlbum.name.toString()).get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        Log.d("ssss result", result.toString())
                                        Log.d("ssss result docu", document.toString())
                                        if (document.id.toString() in tempShareAlbum.index) {
                                            val temp: SelectImageActivity.dbSite = document.toObject(
                                                SelectImageActivity.dbSite::class.java
                                            )
                                            Log.d("ssss if", temp.toString())
                                            tempIntentAlbum.add(temp)
                                        }
                                    }
                                }
                                .addOnCompleteListener{
                                    intent.putExtra(DetailMemoActivity.ALBUM_DATA, tempIntentAlbum)
                                    intent.putExtra(DetailMemoActivity.KEY_ALBUM_NAME, tempClicked.albumName)
                                    startActivity(intent)
                                }
                        }
                }
                else{
                    // 앨범 검색 결과
                   // Toast.makeText(applicationContext, "앨범 검색 결과 click event", Toast.LENGTH_LONG).show()
                    db.collection("user").document("$userUid")
                        .collection("${tempClicked.albumName}").get().addOnSuccessListener { result->

                            for (document in result){
                                tempIntentAlbum.add(document.toObject(SelectImageActivity.dbSite::class.java))
                            }
                            if(tempIntentAlbum.size == result.size()){
                                tempIntentAlbum.sortBy { data->data.date }
                                val intent = Intent(applicationContext, SharedTabActivity::class.java)
                                intent.putExtra(DetailMemoActivity.KEY_ALBUM_NAME, tempClicked.albumName)
                                intent.putExtra(DetailMemoActivity.ALBUM_DATA, tempIntentAlbum)
                                startActivity(intent)
                            }
                        }
                }

//                intent.putExtra(DetailMemoActivity.KEY_ALBUM_NAME, tempClicked.albumName)
//                startActivity(intent)

            }
        }

        override fun getItemCount(): Int{
            return mArray.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(applicationContext).inflate(R.layout.search_activity_search_item, parent, false)
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var imageList: ArrayList<String> = arrayListOf()
            val items: ArrayList<String> = arrayListOf()
            mArray[position].imageArray!!.let { items.add(it.toString()) }

            holder.searchTitle.text = mArray.get(position).site
            holder.searchViewPager.adapter = ViewPagerAdapter(
                mArray.get(position).imageArray!!
            )
            holder.searchContent.text = mArray.get(position).date
            holder.searchAlbum.text = mArray.get(position).albumName
            holder.searchUser.text = mArray.get(position).shareUser
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
                .inflate(R.layout.search_activity_search_viewpager, container, false)
            val image: ImageView = view.findViewById(R.id.searchImage)

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
