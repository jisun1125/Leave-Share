package kr.ac.kumoh.s20171278.map_map_challenge.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.album_activity_album_tab.*
import kotlinx.android.synthetic.main.album_activity_album_tab.set_date
import kotlinx.android.synthetic.main.main_activity_main.*
import kr.ac.kumoh.s20171278.map_map_challenge.*
import kr.ac.kumoh.s20171278.map_map_challenge.main.DetailMemoActivity.Companion.ALBUM_DATA
import kr.ac.kumoh.s20171278.map_map_challenge.main.DetailMemoActivity.Companion.KEY_ALBUM_NAME
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.ui.AlbumSectionsPagerAdapter
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.select.main.SelectTapActivity
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.ui.ShareSectionsPagerAdapter

class SharedTabActivity : AppCompatActivity() {
    companion object{
        const val KEY_USER_UID = "keyUserUid"
        const val KEY_SHARED_ALBUM_NAME = "keySharedAlbumName"
        const val SEGMENT_USER = "segmentUser"
        const val SEGMENT_ALBUM = "segmentAlbum"
    }

    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    private var locaArray: ArrayList<LatLng> = arrayListOf()
    private lateinit var albumName: String
    private var userUid: String? = null
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.album_activity_album_tab)
        albumName = this.intent.getStringExtra(KEY_ALBUM_NAME)!!
        albumData = this.intent.getParcelableArrayListExtra(ALBUM_DATA)!!

        val sectionsPagerAdapter =
            ShareSectionsPagerAdapter(
                this,
                supportFragmentManager
            )
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        // 툴바 불러오기
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)

        set_date.setText(intent.getStringExtra(MainActivity.KEY_ALBUM_NAME))

    }
}