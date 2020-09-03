package kr.ac.kumoh.s20171278.map_map_challenge

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_activity_select_image_popup.*

class SelectImagePopupActivity : AppCompatActivity() {
  lateinit var iArray: ArrayList<String>
    private val iAdapter = ImageAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_select_image_popup)
        siteName.text = intent.getStringExtra("siteName")
        iArray = intent.getStringArrayListExtra("data")
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(applicationContext, 2) // 한 줄에 item 두 개 들어감
            itemAnimator = DefaultItemAnimator()
            adapter = iAdapter
        }
        btnClose.setOnClickListener(){  // 확인 버튼
            finish()  // 액티비티 닫기
        }
    }


    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>(){

        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val detailImgView: ImageView  // 장소 대표 이미지(맨 앞 사진 한장)
            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                detailImgView= root.findViewById(R.id.detailImgView)
            }

            override fun onClick(v: View?) {
                Log.d("iArray", "눌림")
                //TODO("Not yet implemented")
            }

        }
        override fun getItemCount(): Int {
            return iArray.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(applicationContext).inflate(R.layout.main_select_image_image_popup_item, parent, false) //context가 없으니ㅏ 이거 씀 applicationContext
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val uri: Uri = Uri.parse(iArray[position])
            val picasso:Picasso = Picasso.Builder(applicationContext)
                .listener { _, _, e -> e.printStackTrace() }
                .build()
            picasso  // 사진
                .load(iArray[position])  // 넣을 Uri 데이터
                .fit()  // 이미지 늘림없이 imageView에 맞춤
                .centerCrop()  // 센터 크롭 중앙을 기준으로 잘라내기
                .placeholder(R.drawable.img_default)  // 로드되지 않은 경우 사용될 기본 이미지
                .into(holder.detailImgView)  // 들어갈 imageView 위치
        }

    }

}
