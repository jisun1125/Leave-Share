package kr.ac.kumoh.s20171278.map_map_challenge.share.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.share_activity_share_tap.*
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_SHARE_TEMP
import kr.ac.kumoh.s20171278.map_map_challenge.*
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_ALBUM_NAME
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_SHARE_USER_UID
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity.Companion.ALBUM_DATA
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumListActivity
import kr.ac.kumoh.s20171278.map_map_challenge.share.main.ui.S_SectionsPagerAdapter

class ShareTapActivity : AppCompatActivity() {
    companion object{
        // 공유 받았는데 로그인 안되있을때 로그인시키러 보내는 코드
        const val SHARE_LOGIN_CODE = 1112
    }
    // 공유받고 바로 보여주는 액티비티
    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    private var locaArray: ArrayList<LatLng> = arrayListOf()
    private lateinit var albumName: String
    private var userUid: String? = null
    private var shareUserUid: String? = null
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    data class ShareUser(
            val shareUserUid: String? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        let {
            userUid = auth.currentUser?.uid
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.share_activity_share_tap)
        shareUserUid = this.intent.getStringExtra(KEY_SHARE_USER_UID)

        albumName = this.intent.getStringExtra(KEY_ALBUM_NAME)
        albumData = this.intent.getParcelableArrayListExtra(AlbumListActivity.ALBUM_DATA)


        if (userUid!= null){  // 로그인 된 경우
            btnShareSave.text = "공유 앨범 저장하기"
        }
        val sectionsPagerAdapter = S_SectionsPagerAdapter(
                this,
                supportFragmentManager
        )
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter

        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        // 툴바 불러오기
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)

        set_date.setText(intent.getStringExtra(MainActivity.KEY_ALBUM_NAME))

        btnShareSave.setOnClickListener {
            if (userUid!= null){
                shareAlbumSave(userUid, albumData, albumName, shareUserUid)
            }
            else{
                intent = Intent(this, ShareLoginActivity::class.java)
                intent.putExtra(ALBUM_DATA, albumData)
                intent.putExtra(MainActivity.KEY_ALBUM_NAME, albumName)
                intent.putExtra(KEY_SHARE_USER_UID, shareUserUid)
                startActivityForResult(intent, SHARE_LOGIN_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SHARE_LOGIN_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //로그인하고 돌아왔으니
                val auth = FirebaseAuth.getInstance()
                let {
                    userUid = auth.currentUser?.uid
                }

                shareAlbumSave(userUid, albumData, albumName, shareUserUid)
                if (shareUserUid!= null && albumName != null && albumData!=null){
                    Log.d("aaaa shareAlbu", "로그인하고 값 받음")
                }
                else{
                    Log.d("aaaa shareAlbu", "로그인하고 널값 받음")
                }
            }
            else {
                Log.i("Matisse", "resultCode != Activity.RESULT_OK")
            }
        }
    }
    private fun shareAlbumSave(userUid: String?, tempArray: ArrayList<SelectImageActivity.dbSite>?, shareAlbumName: String?, shareUserUid: String?){
        val db = FirebaseFirestore.getInstance()
        db.collection("user").document("S]"+userUid.toString())
                .update("albumList", FieldValue.arrayUnion(shareAlbumName))


        val def =  db.collection("user").document("S]"+userUid.toString())
                .collection(shareAlbumName.toString())
        for (i in 0 until tempArray!!.size) {
            def.document().set(tempArray?.get(i)!!).addOnSuccessListener { result->
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                Log.w("aaaa saveAlbum", "Error getting documents: ", exception)
            }
        }

        Toast.makeText(this,"공유 앨범 정보를 저장했습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }
    private fun uploadImage(imageArray: ArrayList<String>?){
        val mStorage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = mStorage.reference
        if (imageArray != null) {
            for (i in 0 until imageArray.size){
                val tempUri: Uri = Uri.parse(imageArray.get(i))
                val riversRef: StorageReference = storageRef.child("$userUid/S]$albumName/${tempUri.lastPathSegment}")

                val uploadTask = riversRef.putFile(tempUri)

                uploadTask.addOnFailureListener { e->
                    Log.e("aaaa uploadImage", "imageUpload error")
                }
            }
        }

    }
}
