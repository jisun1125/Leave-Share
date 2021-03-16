package kr.ac.kumoh.s20171278.map_map_challenge.main

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.exifinterface.media.ExifInterface
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
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity.Companion.ALBUM_DATA
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity.Companion.KEY_ALBUM_NAME
import kr.ac.kumoh.s20171278.map_map_challenge.main.ui.SectionsPagerAdapter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*


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
        albumName = this.intent.getStringExtra(KEY_ALBUM_NAME)!!
        albumData = this.intent.getParcelableArrayListExtra(ALBUM_DATA)!!

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
            Log.d("ssss btnPushData", "start")
            val progressDialog: ProgressDialog = ProgressDialog(this)
            progressDialog.setMessage("앨범을 업로드하는 중입니다.")
            progressDialog.setCancelable(true)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
            progressDialog.show()

//            val bitmapArray: ArrayList<Bitmap?> = arrayListOf()
//            for (i in 0 until albumData.size) {
//                val image = albumData[i].imageArray
//                for (j in 0 until image!!.size) {
//                    val imageUri: Uri = Uri.parse(image[j])
//                    var bitmap: Bitmap? = null
//                    bitmap = resize(this, imageUri, 500)
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                        Log.d("ssss bit size", "${bitmap?.allocationByteCount}")
//                    }
//                    bitmapArray.add(bitmap)
//                }
//            }

         //   Log.d("ssss bitmap array", "$bitmapArray")
            // image resize process

            val auth = FirebaseAuth.getInstance()
            let {
                userUid = auth.currentUser?.uid
            }
            if (userUid != null) {
//                progressDialog.show()
                var albumImageSize: Int = 0
                var albumCount: Int = 0
                for (i in 0 until albumData.size) {
//                    val tempBitArray: ArrayList<Bitmap> = arrayListOf()
                    val tempUriArray: ArrayList<String> = arrayListOf()
                    for (j in 0 until albumData[i].imageArray!!.size) {
//                        tempBitArray.add(bitmapArray[albumImageSize]!!)
                        albumImageSize += 1
                    }
                    Log.d("ssss albumImageSize", "$albumImageSize")
                    GlobalScope.launch {
                //        uploadPhotos(tempBitArray, i)
                        uploadPhotos(albumData[i].imageArray!!, i)
                        db.collection("user").document("$userUid")
                            .collection(albumName).document("$i")
                            .set(albumData[i]).addOnSuccessListener { result ->
                                albumCount += 1
                                Log.d(
                                    "ssss upload ok",
                                    "${albumData[i]}, ${albumData.size}, i = $i"
                                )
                                if (albumCount == albumData.size -1){
                                    db.collection("user").document(userUid.toString())
                                        .update(
                                            "albumList",
                                            FieldValue.arrayUnion(albumName)
                                        )
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        applicationContext,
                                        "앨범 정보를 저장했습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent: Intent = Intent(
                                        applicationContext,
                                        MainActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
                }
            }
        }
    }

//    suspend fun uploadPhotos(photosUri: ArrayList<Bitmap>, albumIndex: Int){
    suspend fun uploadPhotos(photosUri: ArrayList<String>, albumIndex: Int){
        val tempPathString: String = "$userUid/$albumName/$albumIndex"
        val riversRef: StorageReference = storageRef.child(tempPathString)
//        val photosUrls = ArrayList<PhotoUrl>()
        val uploadedPhotosUriLink =
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                (photosUri.indices).map { index ->
                    async(Dispatchers.IO) {
                        uploadPhoto(storageRef, tempPathString, photosUri[index], index)
                    }
                }
            }.awaitAll()
        Log.d("ssss uploadPhotos", "$uploadedPhotosUriLink")
        var i = 0
        uploadedPhotosUriLink.forEach { photoUriLink ->
            albumData[albumIndex].imageArray?.set(i, photoUriLink.toString())
            i += 1
        }
        Log.d("ssss uploadPhotos", "${albumData[albumIndex].imageArray}")
    }

//    private suspend fun uploadPhoto(storageRef: StorageReference, path: String, photoFile: Bitmap, imageIndex: Int): Uri {
    private suspend fun uploadPhoto(
     storageRef: StorageReference,
     path: String,
     photoFile: String,
     imageIndex: Int
 ): Uri {
      //  val fileName = UUID.randomUUID().toString()
        val fileName = "$path/$imageIndex"
//        val baos = ByteArrayOutputStream()
//        photoFile.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//        val data = baos.toByteArray()
//        Log.d("ssss uploadPhoto", "fileName: $fileName, imageIndex: $imageIndex")
        val data = Uri.parse(photoFile)
        return storageRef.child(fileName)
         //   .putBytes(data)
            .putFile(data)
            .await()
            .storage
            .downloadUrl
            .await()
    }

    private fun resize(context: Context, uri: Uri, resize: Int): Bitmap? {
        var resizeBitmap: Bitmap? = null

        val options: BitmapFactory.Options = BitmapFactory.Options();
        try {
            val inputStream : InputStream = context.contentResolver.openInputStream(uri)!!
            val originalExif = ExifInterface(inputStream)
            val orientation: Int = originalExif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            val matrix = Matrix()
            var rotate = 0
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                rotate = 90
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                rotate = 180
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                rotate = 270
            }

            matrix.postRotate(rotate.toFloat())

            BitmapFactory.decodeStream(
                inputStream,
                null,
                options
            ); // 1번
            var width: Int = options.outWidth
            var height: Int = options.outHeight
            var samplesize: Int = 2

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break
                width /= 2
                height /= 2
                samplesize *= 2
            }

            options.inSampleSize = samplesize;
            val bitmap: Bitmap = BitmapFactory.decodeStream(
                inputStream,
                null,
                options
            )!!; //3번
            resizeBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
//            resizeBitmap = bitmap
        }
        catch (e: IOException){
            e.printStackTrace()
        }
        return resizeBitmap;
    }


}
