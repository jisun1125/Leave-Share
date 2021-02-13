package kr.ac.kumoh.s20171278.map_map_challenge

import android.app.ProgressDialog
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.main_activity_main.*
import kotlinx.android.synthetic.main.main_activity_main.view.*
import kotlinx.android.synthetic.main.navigation_layout.*
import kotlinx.android.synthetic.main.navigation_layout.view.*
import kotlinx.android.synthetic.main.shared_activity_shared_album_item.*
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumListActivity
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumTabActivity.Companion.KEY_SHARED_ALBUM_INDEX
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumTabActivity.Companion.KEY_SHARED_ALBUM_NAME
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumTabActivity.Companion.KEY_USER_UID
import kr.ac.kumoh.s20171278.map_map_challenge.home.HomeSectionsPagerAdapter
import kr.ac.kumoh.s20171278.map_map_challenge.search.SearchTourActivity
import kr.ac.kumoh.s20171278.map_map_challenge.share.main.ShareTapActivity
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.SharedAlbumActivity
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    // 테스트 체크 용 변수
    private var isTesting : Boolean = false

    companion object {
        const val KEY_ALBUM_NAME = "album_name"
        const val KEY_TEST = "isTesting"
        const val PICTURE_REQUEST_CODE = 1111
        const val KEY_SHARE_TEMP = "share_temp_array"

        const val KEY_SHARE_USER_UID = "share_user_uid"

        // 공유 앨범 보여주는 code
        const val SHARE_SAVE = 113

    }
    val db = FirebaseFirestore.getInstance()
    val mStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageRef: StorageReference = mStorage.reference

    private var userUid: String? = null
    private var userName: String? = null
    lateinit var albumName: String
    private lateinit var mMap: GoogleMap
    var shareUserUid: String? = null   // 공유한 회원의 uid
    var shareAlbumName: String? = null   // 공유한 앨범 이름
    var shareAlbumIndex: String? = null  // 공유한 앨범 중 체크박스로 선택된 메모 리스트. doxId로 구성되있고 ,로 구분함
    // ex) 0, 1, 3, 5번 메모를 선택한 경우 -> '0,1,3,5,'
    val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_main)

        val homeSectionsPagerAdapter =
            HomeSectionsPagerAdapter(
            this,
            supportFragmentManager
        )

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = homeSectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        //툴바 설정
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayHomeAsUpEnabled(true)
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        ab.setDisplayShowTitleEnabled(false)

        val requestPermissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_MEDIA_LOCATION
        )

        // read, write : 사진 선택 라이브러리 필요 권한
        // internet, accrss_fine_loaction : Geocoder 필요 권한

        //앨범 생성 버튼
        btnMakeAlbum.setOnClickListener {
            val permissionCheck = PermissionCheck(this, requestPermissions)
            permissionCheck.permissionCheck()
            showAlbumCreatePopup()

        }

        // 공유앨범 링크 수신
        let {
            userUid = auth.currentUser?.uid
            if (userUid!= null){
                Log.d("aaaa userUid", userUid)
            }
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(intent)
                    .addOnSuccessListener {
                        if(it == null){
                            Log.d("aaaa deeplink", "No have dynamic link")
                        }
                        var deepLink: Uri? = null
                        if (it != null){
                            deepLink = it.link
                            Log.d("aaaa deeplink", deepLink.toString())

                            val segment: List<String>? = deepLink?.pathSegments
                            shareUserUid = deepLink?.getQueryParameter(KEY_USER_UID)
                            shareAlbumName = deepLink?.getQueryParameter(KEY_SHARED_ALBUM_NAME)
                            shareAlbumIndex = deepLink?.getQueryParameter(KEY_SHARED_ALBUM_INDEX)
                            Log.d("aaaa deeplink", "shareuserUid: ${shareUserUid.toString()}")
                            Log.d("aaaa deeplink", "shareAlbumName: ${shareAlbumName.toString()}")
                            Log.d("aaaa deep link", "sharedAlbumIndex: ${shareAlbumIndex}")

                            shareAlbumDown(userUid, shareUserUid, shareAlbumName, shareAlbumIndex)
                        }

                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"딥링크 없음", Toast.LENGTH_SHORT).show()
                    }
        }

        //네비게이션
        navigationView.setNavigationItemSelectedListener(this)
        val nav_header_view = navigationView.getHeaderView(0)


        val hid = nav_header_view.findViewById<TextView>(R.id.header_id)
        val hemail = nav_header_view.findViewById<TextView>(R.id.header_email)

        db.collection("user").document(userUid.toString()).get()
            .addOnSuccessListener { document ->
                Log.d("ggg album", "${document.id} => ${document.data}")
                userName = document.get("userName") as String?
                Log.d("ggg album", userName)
            }
            .addOnCompleteListener {
                hid.text = userName
                hemail.text = auth.currentUser?.email
            }
    }

    // 네비게이션 드로어
    override fun onNavigationItemSelected(item: MenuItem): Boolean{
        val id = item.itemId
        when (id) {
            R.id.itemLogout->{
                auth.signOut()
                Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

                //로그인 ui로 보냄
                val intent = Intent(applicationContext, MainLoginActivity::class.java)
                startActivity(intent)
            }
            R.id.itemAlbum1 -> { // 앨범
                val intent: Intent = Intent(applicationContext, SearchActivity::class.java)
                intent.putExtra("type", "album")
                startActivity(intent)
                return true
            }
            R.id.itemShareAlbum1 -> { // 공유된 앨범 보기
                val intent: Intent = Intent(applicationContext, SearchActivity::class.java)
                intent.putExtra("type", "share")
                startActivity(intent)
                return true
            }
            R.id.itemTourSearch -> { // 관광지 정보 검색
                Log.d("lll", "itemTourSearch")
                val intent: Intent = Intent(this, SearchTourActivity::class.java)
                startActivity(intent)
                return true
            }


        }
        return false
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {  // 액션바 메뉴 불러오기
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.sub_menu, menu)
        return true
    }

    // /menu/sub_menu.xml
    override fun onOptionsItemSelected(item: MenuItem): Boolean {   // 액션바 메뉴 클릭 처리
        val id = item.itemId
        when (id) {
            android.R.id.home->{ // 네비게이션 드로어 열기
                main_navigation.openDrawer(GravityCompat.START)
            }
            R.id.itemAlbum1 -> { // 앨범검색
                val intent: Intent = Intent(applicationContext, SearchActivity::class.java)
                intent.putExtra("type", "album")
                startActivity(intent)
                return true
            }
            R.id.itemShareAlbum1 -> { // 공유앨범검색
                val intent: Intent = Intent(applicationContext, SearchActivity::class.java)
                intent.putExtra("type", "share")
                startActivity(intent)
                return true
            }
            R.id.itemTourSearch -> { // 관광지 정보 검색
                Log.d("lll", "itemTourSearch")
                val intent: Intent = Intent(this, SearchTourActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.itemLogout ->{
                auth.signOut()
                Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

                //로그인 ui로 보냄
                val intent = Intent(applicationContext, MainLoginActivity::class.java)
                startActivity(intent)

                //로그아웃 시 mainActivity 종료
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }


    // 앨범 생성 이름 입력용 alertDialog
    private fun showAlbumCreatePopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.main_album_creat_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        val editText: EditText = view.findViewById(R.id.editText)
        textView.text = "생성할 앨범의 이름을 입력해주세요"
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("앨범 생성")
            .setPositiveButton("만들기") { dialog, which ->
                albumName = editText.text.toString()

                //사용자 갤러리에서 사진 고름
                val intent = Intent(applicationContext, SelectImageActivity::class.java)
                intent.putExtra("isTesting", isTesting)
                intent.putExtra(KEY_ALBUM_NAME, editText.text.toString())
                startActivity(intent)
            }
            .setNeutralButton("취소", null)
            .create()
        alertDialog.setView(view)
        alertDialog.show()
    }


    //공유 앨범 받아오기
    private fun shareAlbumDown(userUid: String?, shareUserUid: String?, shareAlbumName: String?, shareAlbumIndex: String?){
        val progressDialog: ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("공유 앨범을 받아오는 중입니다.")
        progressDialog.setCancelable(true)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
        progressDialog.show()

        val shareAlbum = hashMapOf(
            "shareUserUid" to shareUserUid,
            "shareAlbumIndex" to shareAlbumIndex
        )

        val db = FirebaseFirestore.getInstance()
        val tempArray: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
        db.collection("user").document("$userUid")
            .update("shareAlbumList", FieldValue.arrayUnion(shareAlbumName))
        db.collection("user").document("$userUid")
            .collection("ShareAlbum").document("$shareAlbumName")
            .set(shareAlbum)

        progressDialog.dismiss()
        val intent = Intent(this, ShareTapActivity::class.java)
        intent.putExtra(AlbumListActivity.ALBUM_DATA, tempArray)
        intent.putExtra(KEY_ALBUM_NAME, shareAlbumName)
        intent.putExtra(KEY_SHARE_USER_UID, shareUserUid)
        startActivity(intent)
    }

}

