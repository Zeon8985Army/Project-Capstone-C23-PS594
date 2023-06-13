package id.ac.ukdw.fruit_freshned_app

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginScreenActivity : AppCompatActivity() {
    lateinit var GoogleSout: GoogleSignInClient
    private lateinit var firebase_auth: FirebaseAuth
    override fun onStart() {
        super.onStart()
        var auth = Firebase.auth
        val user = firebase_auth.currentUser
        if(GoogleSignIn.getLastSignedInAccount(this)!=null ){
            val intent = Intent(this, BuahItemList::class.java)
//            intent.putExtra("name",user!!.email)
            startActivity(intent)
            finish()
        }
        if (auth.currentUser != null){
            val intent = Intent(this, BuahItemList::class.java)
            var keep_email = user!!.email
            val atIndex = keep_email?.indexOf("@")
            val username = atIndex?.let { keep_email?.substring(0, it) }
            intent.putExtra("name",username)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        // Variabel klik
        var SigninGoogle = findViewById<LinearLayout>(R.id.Signin_google)
        var Signin = findViewById<LinearLayout>(R.id.submit_login)
        var register = findViewById<TextView>(R.id.registerUser)
        var btnshow_pw = findViewById<TextView>(R.id.showpassword)

        // Inisialisasi FireBase
        FirebaseApp.initializeApp(this)
        val optionGso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.webClient))
            .requestEmail()
            .build()
        GoogleSout= GoogleSignIn.getClient(this,optionGso)
        firebase_auth= FirebaseAuth.getInstance()
        SigninGoogle.setOnClickListener{ view: View? ->
            actionSignIn()
        }
        Signin.setOnClickListener { view: View? ->
            var email_obj = findViewById<EditText>(R.id.username)
            var password_obj = findViewById<EditText>(R.id.password)

            if (email_obj.text.toString()=="" || password_obj.text.toString()==""){
                email_obj.setText("........")
                password_obj.setText("........")
            }
            if (email_obj.text.toString()==""){
                email_obj.setText("........")
            }
            if (password_obj.text.toString()==""){
                password_obj.setText("........")
            }

            val progresLog = ProgressDialog(this)
            progresLog.setMessage("Masuk...")
            progresLog.setCancelable(false)
            progresLog.show()
            var email = findViewById<EditText>(R.id.username).text.toString()
            var password = findViewById<EditText>(R.id.password).text.toString()
            firebase_auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        if (progresLog.isShowing) progresLog.dismiss()
                        // Sign in success, update UI with the signed-in user's information
                        val user = firebase_auth.currentUser
                        val intent = Intent(this, BuahItemList::class.java)
                        var keep_email = user!!.email
                        val atIndex = email.indexOf("@")
                        val username = email.substring(0, atIndex)
                        intent.putExtra("name",username)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        if (progresLog.isShowing) progresLog.dismiss()
                        var email = findViewById<EditText>(R.id.username)
                        var password = findViewById<EditText>(R.id.password)
                        email.setError("Cek kembali email")
                        password.setError("Cek kembali password")
                        email.setText("")
                        password.setText("")
                    }
                }
        }

        // For Go to register activity
        register.setOnClickListener {
            val intent = Intent(this, RegisterScreenActivity::class.java)
            startActivity(intent)
        }
        // For show password and hide password
        btnshow_pw.setOnClickListener {
            var password = findViewById<EditText>(R.id.password)
            // Check if password is visible or not
            if (password.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Password is currently visible, make it not visible
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                val showPasswordDrawable  = ContextCompat.getDrawable(this, R.drawable.ic_baseline_remove_red_eye_24)
                btnshow_pw.setCompoundDrawablesRelativeWithIntrinsicBounds(showPasswordDrawable,null,null,null)
            } else {
                // Password is currently hidden
                password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                val showPasswordDrawable  = ContextCompat.getDrawable(this, R.drawable.ic_baseline_remove_red_eye_24_2)
                btnshow_pw.setCompoundDrawablesRelativeWithIntrinsicBounds(showPasswordDrawable,null,null,null)
            }

            // Move the cursor to end of the password
            password.setSelection(password.text.length)
        }
    }

    // This is from documentation in SSO
    private  fun actionSignIn(){
        val signInIntent: Intent =GoogleSout.signInIntent
        startActivityForResult(signInIntent,123)
    }
    // This is from documentation in SSO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==123){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            cekingResult(task)
        }
    }
    // This is from documentation in SSO
    private fun cekingResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? =completedTask.getResult(ApiException::class.java)
            if (account != null) {
                goToAfterAuthOkay(account)
            }
        } catch (e: ApiException){
            Toast.makeText(this,"Batal", Toast.LENGTH_SHORT).show()
        }
    }
    // This is from documentation in SSO
    // After all clear, move to BuahItemList
    private fun goToAfterAuthOkay(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        firebase_auth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful) {
                val user = firebase_auth.currentUser
                val intent = Intent(this, BuahItemList::class.java)
                intent.putExtra("name",user!!.displayName)
                startActivity(intent)
                finish()
            }
        }
    }



}