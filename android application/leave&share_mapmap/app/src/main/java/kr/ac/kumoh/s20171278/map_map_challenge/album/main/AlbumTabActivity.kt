package kr.ac.kumoh.s20171278.map_map_challenge.album.main

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
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.SharedAlbumActivity
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.SharedTabActivity

class AlbumTabActivity : AppCompatActivity() {
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
        albumName = this.intent.getStringExtra(KEY_ALBUM_NAME)
        albumData = this.intent.getParcelableArrayListExtra(ALBUM_DATA)

        val sectionsPagerAdapter =
            AlbumSectionsPagerAdapter(
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {  // 액션바 메뉴 불러오기
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {   // 액션바 메뉴 클릭 처리
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.itemSharedAlbum->{ // 공유된 앨범 보기
                val intent: Intent = Intent(this, SharedAlbumActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.itemShareAlbum->{ // 공유하기
                val auth = FirebaseAuth.getInstance()
                let {
                    userUid = auth.currentUser?.uid

                }
                if (userUid != null){
                    val intent = Intent(this, SelectTapActivity::class.java)
                    intent.putExtra(KEY_ALBUM_NAME, albumName)
                    intent.putExtra(ALBUM_DATA, albumData)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}