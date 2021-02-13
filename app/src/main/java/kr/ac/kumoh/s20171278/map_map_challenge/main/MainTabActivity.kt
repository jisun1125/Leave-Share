package kr.ac.kumoh.s20171278.map_map_challenge.main

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.FirebaseStorage.getInstance
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.main_activity_main_tab.*
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity.Companion.ALBUM_DATA
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity.Companion.KEY_ALBUM_NAME
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumListActivity
import kr.ac.kumoh.s20171278.map_map_challenge.main.ui.SectionsPagerAdapter

class MainTabActivity : AppCompatActivity() {
    private var albumData: ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    private var locaArray: ArrayList<LatLng> = arrayListOf()
    private lateinit var albumName: String
    private var userUid: String? = null
    val db = FirebaseFirestore.getInstance()
    val mStorage: FirebaseStorage = getInstance()
    val storageRef: StorageReference = mStorage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_main_tab)
        albumName = this.intent.getStringExtra(KEY_ALBUM_NAME)
        albumData = this.intent.getParcelableArrayListExtra(ALBUM_DATA)

        val sectionsPagerAdapter =
            SectionsPagerAdapter(
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

        btnPushData.setOnClickListener {

            val progressDialog: ProgressDialog = ProgressDialog(this)
            progressDialog.setMessage("앨범을 업로드하는 중입니다.")
            progressDialog.setCancelable(true)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
            progressDialog.show()

            //db save
            val auth = FirebaseAuth.getInstance()
            let {
                userUid = auth.currentUser?.uid
            }
            if (userUid != null) {
                var saveCnt = 0

                for (i in 0 until albumData.size) {
                    val s: String? = albumData.get(i).site
                    val d: String? = albumData.get(i).date
                    var image: ArrayList<String>? = arrayListOf()
                    var docidx: Int? = albumData.get(i).docId
                    Log.d("aaa MainTab AlbumUpload", "docidx: " + docidx.toString() + ", i: " + i.toString())
                    image = albumData.get(i).imageArray
                    var downimage: Int = 0
                    for (j in 0 until image!!.size) {
                        val tempUri: Uri = Uri.parse(image.get(j))
                        val riversRef: StorageReference =
                            storageRef.child("$userUid/$albumName/${tempUri.lastPathSegment}")
                        val uploadTask = riversRef.putFile(tempUri)

                        uploadTask.addOnFailureListener { e ->
                            Log.e("aaaa uploadImage", "imageUpload error")
                        }

                        uploadTask.addOnSuccessListener {
                            downimage += 1
                            if (image.size == downimage) {
                                db.collection("user").document("$userUid")
                                    .collection(albumName).document(docidx.toString())
                                    .set(albumData.get(i)).addOnSuccessListener { result ->
                                        saveCnt += 1

                                        if(saveCnt == albumData.size){
                                            db.collection("user").document(userUid.toString())
                                                .update("albumList", FieldValue.arrayUnion(albumName))
                                            progressDialog.dismiss()
                                            Toast.makeText(this, "앨범 정보를 저장했습니다.", Toast.LENGTH_SHORT).show()
                                            val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }


                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                                        Log.w(
                                            "aaaa saveAlbum",
                                            "Error getting documents: ",
                                            exception
                                        )
                                    }
                            }

                        }

                    }

                }
            } else {
                Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

        }

    }
}
