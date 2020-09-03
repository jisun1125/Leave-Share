package kr.ac.kumoh.s20171278.map_map_challenge.ui.main

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.album_fragment_album_memo.*
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_ALBUM_NAME

import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumListActivity

class SharedMemoFragment : Fragment() {
    private val imageList = ArrayList<String>()
    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    private lateinit var albumName : String
    private var userUid: String? = null
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        albumName = activity?.intent!!.getStringExtra(KEY_ALBUM_NAME)

        val view: View = inflater.inflate(R.layout.shared_fragment_shared_memo, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        albumData = activity?.intent!!.getParcelableArrayListExtra(AlbumListActivity.ALBUM_DATA)

        val mAdapter = MemoAdapter()

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
            itemAnimator = DefaultItemAnimator()
        }
        mAdapter.notifyDataSetChanged()
    }

    inner class MemoAdapter(): RecyclerView.Adapter<MemoAdapter.ViewHolder>() {
        val picasso = Picasso.Builder(activity).build()

        inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root){
            val memoSite = itemView.findViewById<TextView>(R.id.memoSite)
            val memoTitle = itemView.findViewById<TextView>(R.id.memoTitle)
            val memoContent = itemView.findViewById<TextView>(R.id.memoContent)
            val memoViewPager = itemView.findViewById<ViewPager>(R.id.memoView)
            val memoDate = itemView.findViewById<TextView>(R.id.memoDate)
        }

        override fun getItemCount(): Int{
            return albumData.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(context).inflate(R.layout.shared_fragment_shared_memo_item, parent, false)
            return ViewHolder(root)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var imageList: ArrayList<String> = arrayListOf()
            val items: ArrayList<String> = arrayListOf()
            albumData[position].imageArray!!.let { items.add(it.toString()) }

            var dataDel: List<String> = items
            var data = dataDel[0].replace("[", "")
            data = data.replace("]", "")

            var dataSplit: List<String> = listOf()
            dataSplit = data.split(", ")
            for (j in dataSplit.indices)
                imageList.add(dataSplit[j])

            holder.memoSite.text = albumData?.get(position)?.site
            holder.memoTitle.text = albumData?.get(position)?.title
            holder.memoViewPager.adapter = ViewPagerAdapter(picasso, imageList)
            holder.memoDate.text = albumData?.get(position)?.date
            holder.memoContent.text = albumData?.get(position)?.content
        }
    }


    inner class ViewPagerAdapter(private val picasso: Picasso, val item: ArrayList<String>) : PagerAdapter() {

        override fun getCount(): Int {
            return item.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater
                    .from(container.context)
                    .inflate(R.layout.shared_fragment_shared_memo_viewpager, container, false)

            val image: ImageView = view.findViewById(R.id.memoImage)

            val items = Uri.parse(item[position])

            picasso
                    .load(items)
                    .placeholder(R.color.colorGray)
                    .fit()
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