package kr.ac.kumoh.s20171278.map_map_challenge

import android.content.Intent
import android.graphics.Color
import android.graphics.Color.RED
import android.graphics.ColorSpace.match
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PatternMatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.main_sign_up.*
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_sign_up)

        var name = findViewById<EditText>(R.id.signName)
        var email = findViewById<EditText>(R.id.signEmail)
        var password = findViewById<EditText>(R.id.signPass)
        var password2 = findViewById<EditText>(R.id.signPass2)  // 비밀번호 입력 확인용

        var checkPassBoolean: Boolean = false

        // 비밀번호 일치 여부 체크
        checkPass.addTextChangedListener(object: TextWatcher{
            // 문자 입력 전
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            // EditText에 변화가 있을 경우
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            // EditText에 입력이 끝난 후후
            override fun afterTextChanged(p0: Editable?) {
                if (password.text.toString() == password2.text.toString()){
                    checkPass.text = "비밀번호가 일치합니다."
                    checkPass.setTextColor(Color.parseColor("#FF0000"))
                    checkPassBoolean = true
                }
                else{
                    checkPass.text = "비밀번호가 일치하지 않습니다."
                    checkPass.setTextColor(Color.parseColor("#000000"))
                    checkPassBoolean = false
                }
            }
        })

        btnOK.setOnClickListener {
            val mEmail = email.text.toString().trim()
            val mPass = password.text.toString().trim()
            val mPass2 = password2.text.toString().trim()
            val mName = name.text.toString().trim()
            if (checkPassBoolean
                &&(Pattern.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9!@#\$%^&*]).{6,15}\$", mPass))){
                signUp(mEmail, mPass, mName)
            }
            else if(!checkPassBoolean){
                Toast.makeText(this, "입력된 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
            else if(!Pattern.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9!@#\$%^&*]).{6,15}\$", mPass)){
                Toast.makeText(this, "비밀번호가 형식에 맞지 않습니다.", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "비밀번호를 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 회원가입
    private fun signUp(mEmail:String, mName:String, mPass:String){
        val mAlbumList: ArrayList<String> = arrayListOf()   // 사용자의 앨범 목록
        val mShareAlbumList: ArrayList<String> = arrayListOf()  // 공유받은 앨범 목록
        auth.createUserWithEmailAndPassword(mEmail, mPass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userUid = auth.currentUser!!.uid
                    val docData = hashMapOf(
                        "userEmail" to mEmail,
                        "userPass" to mPass,
                        "userUid" to userUid,
                        "userName" to mName,
                        "albumList" to mAlbumList,
                        "shareAlbumList" to mShareAlbumList
                    )

                    val db = FirebaseFirestore.getInstance()
                    db.collection("user").document(userUid).set(docData)
                        .addOnSuccessListener { Log.d("aaaa authuser", "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w("aaaa authuser", "Error writing document", e) }

                    val intent = Intent(applicationContext, MainLoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()

                    signName.setText("")
                    signEmail.setText("")
                    signPass.setText("")
                    signPass2.setText("")
                    signName.requestFocus()
                }
            }
    }
}
