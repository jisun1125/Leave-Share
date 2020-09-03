package kr.ac.kumoh.s20171278.map_map_challenge.ui.main

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.album_activity_album_tab.*
import kotlinx.android.synthetic.main.shared_activity_shared_album.*
import kotlinx.android.synthetic.main.shared_activity_shared_album.set_date
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SearchActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumListActivity
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumTabActivity

/// 공유앨범 조회
class SharedAlbumActivity : AppCompatActivity() {
    data class ShareAlbum(
            val shareUser: String? = null,
            val name: String? = null,
            val image: String? = null
    )
    private val mAdapter = AlbumAdapter()
    private var albumList: ArrayList<ShareAlbum> = arrayListOf()  // 두 개 합친 dataClass ArrayList
    private var albumNameList: ArrayList<String> = arrayListOf()  // 앨범 이름
    private var userUid: String? = null

    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shared_activity_shared_album)
        var auth = FirebaseAuth.getInstance()
        let {
            userUid = auth.currentUser?.uid
        }

        setUpalbumNameList()

        set_date.text = "공유된 앨범 목록"

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    val intent = Intent(applicationContext, SearchActivity::class.java)
                    intent.putExtra("type", "share")
                    intent.putExtra("newText", query)
                    startActivity(intent)

                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })

    }

    private fun setUpalbum(){
        // progress Dialog 불러옴
        val progressDialog: ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("앨범 목록을 불러오는 중입니다.")
        progressDialog.setCancelable(true)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
        progressDialog.show()
        for (i in 0 until albumNameList.size){
            db.collection("user").document("S]$userUid")
                    .collection(albumNameList[i]).get()
                    .addOnSuccessListener { result->
                        val temp: SelectImageActivity.dbSite? = result.documents[0].toObject(
                            SelectImageActivity.dbSite::class.java)
                        albumList.add(
                            ShareAlbum(
                                temp?.shareUser,
                                albumNameList[i],
                                temp?.imageArray?.get(0)
                            )
                        )

                        if (albumNameList.size == albumList.size){
                            progressDialog.dismiss()
                            adapterConnect()
                        }

                    }
        }
    }

    private fun setUpalbumNameList() {
        val db = FirebaseFirestore.getInstance()
        //앨범 이름 list 받아옴
        db.collection("user").document("S]$userUid").get()
                .addOnSuccessListener { document ->
                    albumNameList = document.get("albumList") as ArrayList<String>
                    setUpalbum()
                }

    }

    private fun adapterConnect(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(applicationContext, 3) // 한 줄에 item 세 개
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
        mAdapter.notifyDataSetChanged()
    }


    inner class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val sharedUserName: TextView  // 유저 이름
            val sharedAlbumName: TextView  // 앨범 이름
            val sharedAlbumCover: ImageView  // 앨범 대표 이미지(맨 앞 사진 한장)

            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                sharedUserName = root.findViewById(R.id.sharedUserName)
                sharedAlbumName = root.findViewById(R.id.sharedAlbumName)
                sharedAlbumCover= root.findViewById(R.id.sharedAlbumCover)
            }

            override fun onClick(v: View?){
                db.collection("user").document("S]$userUid")
                        .collection(albumList[adapterPosition].name.toString()).get()
                        .addOnSuccessListener { result->
                            for (document in result){
                                val temp: SelectImageActivity.dbSite = document.toObject(
                                    SelectImageActivity.dbSite::class.java)
                                albumData.add(temp)
                            }
                        }
                        .addOnCompleteListener {
                            albumData.sortBy { data -> data.date }
                            val intent : Intent = Intent(applicationContext, SharedTabActivity::class.java)
                            intent.putExtra(AlbumListActivity.KEY_ALBUM_NAME, albumList[adapterPosition].name)
                            intent.putExtra(AlbumListActivity.ALBUM_DATA, albumData)
                            startActivity(intent)
                        }
            }

        }

        override fun getItemCount(): Int {
            return albumList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(applicationContext).inflate(R.layout.shared_activity_shared_album_item, parent, false) //context가 없으니ㅏ 이거 씀 applicationContext
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val picasso: Picasso = Picasso.Builder(applicationContext)
                    .listener { _, _, e -> e.printStackTrace() }
                    .build()
            holder.sharedUserName.text = albumList[position].shareUser
            holder.sharedAlbumName.text = albumList[position].name
            val uri: Uri = Uri.parse(albumList[position].image)
            picasso  // 사진
                    .load(uri)  // 넣을 Uri 데이터
                    .fit()  // 이미지 늘림없이 imageView에 맞춤
                    .centerCrop()  // 센터 크롭 중앙을 기준으로 잘라내기
                    .placeholder(R.drawable.img_default)  // 로드되지 않은 경우 사용될 기본 이미지
                    .into(holder.sharedAlbumCover)  // 들어갈 imageView 위치

        }
    }
}
