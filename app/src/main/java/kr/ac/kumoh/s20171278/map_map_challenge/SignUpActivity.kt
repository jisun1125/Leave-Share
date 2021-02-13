package kr.ac.kumoh.s20171278.map_map_challenge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.main_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_sign_up)
        val auth = FirebaseAuth.getInstance()
    //    auth.signOut()
        var name = findViewById<EditText>(R.id.signName)
        var email = findViewById<EditText>(R.id.signEmail)
        var password = findViewById<EditText>(R.id.signPass)

        btnOK.setOnClickListener {
            val mEmail = email.text.toString().trim()
            val mPass = password.text.toString().trim()
            val mName = name.text.toString().trim()
            val mAlbumList: ArrayList<String> = arrayListOf()   // 사용자의 앨범 목록
            val mShareAlbumList: ArrayList<String> = arrayListOf()  // 공유받은 앨범 목록
            Log.d("sign", "$mEmail $mPass")
         //   val albumList: ArrayList<String> = arrayListOf()

            auth.createUserWithEmailAndPassword(mEmail, mPass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("sign", "create:success")
                        var userUid = auth.currentUser!!.uid
                        val docData = hashMapOf(
                            "userEmail" to mEmail,
                            "userPass" to mPass,
                            "userUid" to userUid,
                            "userName" to mName,
                            "albumList" to mAlbumList,
                            "shareAlbumList" to mShareAlbumList
                            )
                        val SdocData = hashMapOf(
                                "albumList" to mShareAlbumList

                        )
                        var db = FirebaseFirestore.getInstance()
                        db.collection("user").document(userUid).set(docData)
                            .addOnSuccessListener { Log.d("aaaa authuser", "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w("aaaa authuser", "Error writing document", e) }
                        db.collection("user").document("S]${userUid}")
                                .set(SdocData)
                        val intent = Intent(applicationContext, MainLoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("sign", "create:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()

                        name.setText("")
                        email.setText("")
                        password.setText("")
                        name.requestFocus()
                    }
                }
        }

    }
}
