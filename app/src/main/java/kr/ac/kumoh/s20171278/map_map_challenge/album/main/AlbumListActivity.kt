package kr.ac.kumoh.s20171278.map_map_challenge.album.main

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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.album_activity_album_list.*
import kotlinx.android.synthetic.main.album_activity_album_list.recyclerView
import kotlinx.android.synthetic.main.main_activity_main.*
import kotlinx.android.synthetic.main.shared_activity_shared_album.*
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SearchActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity

class AlbumListActivity : Fragment() {
    companion object {
        const val KEY_ALBUM_NAME = "album_name"
        const val PICTURE_REQUEST_CODE = 111
        const val ALBUM_DATA = "album_data"
        const val LOCAL_ARRAY = "local_array"
        const val MARKER = "marker"
    }

    val db = FirebaseFirestore.getInstance()
    data class Album(
        val name: String? = null,
        var image: String? = null
    )
    private var imageArray: ArrayList<String> = arrayListOf()  // image 받아옴
    private var albumList: ArrayList<Album> = arrayListOf()  // 두 개 합친 dataClass ArrayList
    private var albumNameList: ArrayList<String> = arrayListOf()  // 앨범 이름
    private var userUid: String? = null

    val mStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageRef: StorageReference = mStorage.reference

    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    private val mAdapter = AlbumAdapter()

    private var progressDialog: ProgressDialog? = null


    override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.album_activity_album_list, container, false)
        val auth = FirebaseAuth.getInstance()
        let {
            userUid = auth.currentUser?.uid
        }
        Log.d("dd", "앨범끝내고돌아옴")
        val textNotify: TextView? = view?.findViewById(R.id.textNotify)
        setUpalbumNameList()
        // fragment로 바꾸고 나니까 progressdialog가 안끝나서 일단

        return rootView
    }


    private fun setUpalbumImage(){
        // progress Dialog 불러옴
        if (progressDialog == null){
//            progressDialog = ProgressDialog(context)
//            progressDialog?.setMessage("앨범 목록을 불러오는 중입니다.")
//            progressDialog?.setCancelable(true)
//            progressDialog?.setCanceledOnTouchOutside(false)
//            progressDialog?.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
//          //  progressDialog?.show()
        }

        for (i in 0 until albumNameList.size){
            db.collection("user").document(userUid.toString())
                .collection(albumNameList[i]).get()
                .addOnSuccessListener { result->
                    val temp: String? = result.documents[0].toObject(SelectImageActivity.dbSite::class.java)?.imageArray?.get(0)
                    val tempUri = Uri.parse(temp)
                    var downloadUri: Uri? = null
                    val pathReference = storageRef.child("$userUid/${albumNameList[i]}/${tempUri.lastPathSegment}")
                    pathReference.downloadUrl.addOnSuccessListener { task ->
                        downloadUri = task
                        val SdownloadUri: String = task.toString()
                        val tempAlbum = Album(albumNameList[i], SdownloadUri)
                        albumList.add(tempAlbum)

                        if (albumNameList.size == albumList.size){
                            // progress Dialog 종료

                            Log.d("dd", "어뎁터연결")
                            if (progressDialog != null){
                                Log.d("dd", "is showing")
                             //   progressDialog?.cancel()
//                                progressDialog?.dismiss()
                            }
                            albumList.sortBy { data -> data.name }
                            adapterConnect()
                        }
                    }
                }
        }
    }

    private fun setUpalbumNameList() {

        val db = FirebaseFirestore.getInstance()
        //앨범 이름 list 받아옴
        db.collection("user").document(userUid.toString()).get()
            .addOnSuccessListener { document ->
                albumNameList = document.get("albumList") as ArrayList<String>
                if (albumNameList.size == 0){
                    textNotify.text = "아직 앨범이 없어요!\n앨범생성으로 여행기를 작성해 보세요!"
                    textNotify.setTextSize(2, 20F)  // unit: 0(dp), 1(px), 2(sp)
                }
                else{
                    textNotify.text = ""
                    textNotify.setTextSize(2, 0F)
                    setUpalbumImage()
                }

            }
    }

    private fun adapterConnect(){

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3) // 한 줄에 item 세 개 들어감
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
        mAdapter.notifyDataSetChanged()
    }


    inner class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val albumName: TextView  // 앨범 이름
            val albumCover: ImageView  // 앨범 대표 이미지(맨 앞 사진 한장)

            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                albumName = root.findViewById(R.id.albumName)
                albumCover= root.findViewById(R.id.albumCover)
            }

            // 클릭 이벤트
            // SelectionImagePopupActivity : 누른 장소에 있는 사진을 recyclerView 형태로 보여줌
            override fun onClick(v: View?) {
//                Log.d("ggg albumList click",albumList[adapterPosition].name)
                db.collection("user").document(userUid.toString())
                    .collection(albumList[adapterPosition].name.toString()).get()
                    .addOnSuccessListener { result->
                        for (document in result){
                            val temp: SelectImageActivity.dbSite = document.toObject(
                                SelectImageActivity.dbSite::class.java)
                            albumData.add(temp)
                        }
                        albumData.sortBy { data -> data.date }
                    }
                    .addOnCompleteListener {
                        val intent : Intent = Intent(context, AlbumTabActivity::class.java)
                        albumData.sortBy { data->data.date }

                        intent.putExtra(KEY_ALBUM_NAME, albumList[adapterPosition].name)
                        intent.putExtra(ALBUM_DATA, albumData)
                        startActivity(intent)
                        albumData = arrayListOf()
                    }
            }
        }

        override fun getItemCount(): Int {
            return albumList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(context).inflate(R.layout.album_activity_album_list_item, parent, false) //context가 없으니ㅏ 이거 씀 applicationContext
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            val picasso: Picasso = Picasso.Builder(context)
//                .listener { _, _, e -> e.printStackTrace() }
//                .build()
            holder.albumName.text = albumList[position].name // 앨범 이름
            val uri: Uri = Uri.parse(albumList[position].image)
            Glide.with(context!!)  // 사진
                .load(uri)  // 넣을 Uri 데이터
//                .fit()  // 이미지 늘림없이 imageView에 맞춤
                .centerCrop()  // 센터 크롭 중앙을 기준으로 잘라내기
                .placeholder(R.drawable.img_default)  // 로드되지 않은 경우 사용될 기본 이미지
                .into(holder.albumCover)  // 들어갈 imageView 위치
        }
    }
}
