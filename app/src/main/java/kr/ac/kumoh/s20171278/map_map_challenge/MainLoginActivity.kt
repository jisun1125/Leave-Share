package kr.ac.kumoh.s20171278.map_map_challenge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.main_activity_main_login.*

class MainLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_main_login)

        val auth = FirebaseAuth.getInstance()
        val email = findViewById<EditText>(R.id.userEmail)
        val password = findViewById<EditText>(R.id.userPass)

        btnSignUp.setOnClickListener {
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
        }

        if (auth.currentUser!= null){  // 로그인 된 경우 바로 메인으로 넘겨줌
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnLogin.setOnClickListener {
            val mEmail = email.text.toString().trim()
            val mPass = password.text.toString().trim()

            auth.signInWithEmailAndPassword(mEmail, mPass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        Log.d("login", "LogIn:success")
                        val user = auth.currentUser
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        // If sign in fails, display a message to the user.
                        Log.w("login", "LogIn:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}
