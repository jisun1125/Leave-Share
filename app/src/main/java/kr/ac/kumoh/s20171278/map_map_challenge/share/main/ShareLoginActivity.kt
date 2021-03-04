package kr.ac.kumoh.s20171278.map_map_challenge.share.main

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.main_activity_main_login.*
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_SHARE_TEMP
import kr.ac.kumoh.s20171278.map_map_challenge.MainActivity.Companion.KEY_SHARE_USER_UID
import kr.ac.kumoh.s20171278.map_map_challenge.R
import kr.ac.kumoh.s20171278.map_map_challenge.SelectImageActivity
import kr.ac.kumoh.s20171278.map_map_challenge.SignUpActivity
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumTabActivity.Companion.KEY_SHARED_ALBUM_NAME

class ShareLoginActivity : AppCompatActivity() {
    var albumData :ArrayList<SelectImageActivity.dbSite> = arrayListOf()
    var shareUserUid: String? = null
    var shareAlbumName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_main_login)

        val auth = FirebaseAuth.getInstance()
        val email = findViewById<EditText>(R.id.userEmail)
        val password = findViewById<EditText>(R.id.userPass)

        albumData = intent.getParcelableArrayListExtra(KEY_SHARE_TEMP)!!
        shareUserUid = intent.getStringExtra(KEY_SHARE_USER_UID)
        shareAlbumName = intent.getStringExtra(KEY_SHARED_ALBUM_NAME)

        btnSignUp.setOnClickListener {
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val mEmail = email.text.toString().trim()
            val mPass = password.text.toString().trim()

            auth.signInWithEmailAndPassword(mEmail, mPass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful){
                            val user = auth.currentUser

                            val result = Intent(applicationContext, ShareTapActivity::class.java)
                            result.putExtra(KEY_SHARE_TEMP, albumData)
                            result.putExtra(KEY_SHARE_USER_UID, shareUserUid)
                            result.putExtra(KEY_SHARED_ALBUM_NAME, shareAlbumName)
                            setResult(Activity.RESULT_OK, result)

                            finish()
                        }
                        else if(task.isSuccessful && intent!= null){
                            // deep link를 받은 사용자가 로그인하지 않아서 이리고 넘어온경우

                        }
                        else {
                            Log.w("login", "LogIn:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
        }

    }
}
