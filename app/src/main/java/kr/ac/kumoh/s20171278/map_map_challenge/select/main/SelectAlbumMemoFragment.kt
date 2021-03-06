package kr.ac.kumoh.s20171278.map_map_challenge.select.main


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.album_fragment_album_memo.*
import kotlinx.android.synthetic.main.album_fragment_album_memo.recyclerView
import kotlinx.android.synthetic.main.select_fragment_select_album_memo.*
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity.Companion.ALBUM_DATA
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity.Companion.KEY_ALBUM_NAME
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumTabActivity
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumTabActivity.Companion.KEY_SHARED_ALBUM_INDEX
import kotlin.collections.arrayListOf as arrayListOf1


/**
 * A simple [Fragment] subclass.
 */
class SelectAlbumMemoFragment : Fragment(){
    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf1()
    private lateinit var albumName : String
    private var userUid: String? = null
    val db = FirebaseFirestore.getInstance()
    var shareUserName: String? = null

    private var selectArray: ArrayList<SelectImageActivity.dbSite> = arrayListOf1()
    private var selectIdxArray: ArrayList<String> = arrayListOf1()
    private var selectIdx: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val auth = FirebaseAuth.getInstance()
        userUid = auth.currentUser?.uid

        albumName = activity?.intent!!.getStringExtra(KEY_ALBUM_NAME)!!
        albumData = activity!!.intent.getParcelableArrayListExtra(ALBUM_DATA)!!
        db.collection("user").document("$userUid")
                .get().addOnSuccessListener { result->
                    shareUserName = result.get("userName").toString()
                }
        val view: View = inflater.inflate(R.layout.select_fragment_select_album_memo, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mAdapter = MemoAdapter()
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
            itemAnimator = DefaultItemAnimator()
        }
        mAdapter.notifyDataSetChanged()

        btnShareOk.setOnClickListener {
            // 체크박스에서 선택한 메모 docId를 selectIdx에 쉼표로 구분해 String으로 추가함
            for (i in 0 until albumData.size)
                if (albumData[i].select==1){
                    selectIdx += albumData[i].docId.toString() + ','
                }
            generateContentLink()
        }
    }

    private fun getShareDeepLink(): Uri {
        return Uri.parse("https://example.com/${AlbumTabActivity.SEGMENT_USER}?" +
                "${AlbumTabActivity.KEY_USER_UID}=${userUid}&" +
                "${AlbumTabActivity.KEY_SHARED_ALBUM_NAME}=${albumName}&" +
                "${KEY_SHARED_ALBUM_INDEX}=${selectIdx}"
        )
    }

    private fun generateContentLink() {
        val baseUrl = getShareDeepLink()
        val domain = "https://leaveshare.page.link"
        FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(baseUrl)
            .setDomainUriPrefix(domain)
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder("kr.ac.kumoh.s20171278.map_map_challenge").build())
            .buildShortDynamicLink()
            .addOnSuccessListener { result->
                val shortLink = result.shortLink
                val flowchartLink = result.previewLink
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString())

                startActivity(Intent.createChooser(intent, "링크 공유하기"))
            }.addOnFailureListener { e->
                Log.e("aaaa shortlink", "fail make shortLInk")
            }

    }

    private fun onShareClicked(){
        val link = generateContentLink()

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link.toString())

        startActivity(Intent.createChooser(intent, "Share Link"))
    }

    inner class MemoAdapter: RecyclerView.Adapter<MemoAdapter.ViewHolder>() {
//        val picasso = Picasso.Builder(activity).build()

        inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root){
            val memoSite = itemView.findViewById<TextView>(R.id.memoSite)
            val memoTitle = itemView.findViewById<TextView>(R.id.memoTitle)
            val memoContent = itemView.findViewById<TextView>(R.id.memoContent)
            val memoViewPager = itemView.findViewById<ViewPager>(R.id.memoView)
            val memoDate = itemView.findViewById<TextView>(R.id.memoDate)
            val memoTag = itemView.findViewById<TextView>(R.id.memoTag)
            val memoCheckBox = itemView.findViewById<CheckBox>(R.id.selectCheckbox)
        }

        override fun getItemCount(): Int{
            return albumData.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(context).inflate(R.layout.select_fragment_select_album_memo_item, parent, false)
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageList: ArrayList<String> = arrayListOf1()
            val items: ArrayList<String> = arrayListOf1()

            albumData[position].imageArray!!.let { items.add(it.toString()) }

            val dataDel: List<String> = items
            var data = dataDel[0].replace("[", "")
            data = data.replace("]", "")

            var dataSplit: List<String> = listOf()
            dataSplit = data.split(", ")
            for (j in dataSplit.indices)
                imageList.add(dataSplit[j])

            holder.memoSite.text = albumData[position].site
            holder.memoTitle.text = albumData[position].title

            holder.memoViewPager.adapter = ViewPagerAdapter(
                albumData[position].imageArray!!
            )
            holder.memoDate.text = albumData[position].date
            holder.memoContent.text = albumData[position].content
            holder.memoTag.text = albumData[position].tag
            holder.memoCheckBox.isChecked = false

            holder.memoCheckBox.setOnClickListener {v->
                val check: CheckBox = v as CheckBox

                if (check.isChecked){
                    check.isChecked = true
                    albumData[position].select = 1
                }
               else
                {
                    check.isChecked = false
                    albumData[position].select = 0
                }

            }
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
                .inflate(R.layout.select_fragment_select_album_memo_view, container, false)

            val image: ImageView = view.findViewById(R.id.selectShareMemoImage)

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