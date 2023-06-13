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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// This Class is used for registration from new user
class RegisterScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrasi)

        // Variable that can be clicked
        var username = findViewById<EditText>(R.id.username)
        var password = findViewById<EditText>(R.id.password)
        var confirmpassword = findViewById<EditText>(R.id.check_password)
        var submit_signup = findViewById<LinearLayout>(R.id.submit_singup)
        var signin = findViewById<TextView>(R.id.signin)
        var btnshow_pw = findViewById<TextView>(R.id.showpassword)

        // For SignIn
        submit_signup.setOnClickListener{ view: View? ->
            Log.v("TAG", "Masuk")
            adduser(username,password,confirmpassword)
        }
        // For Go to Login Layout
        signin.setOnClickListener{ view: View? ->
            val intent = Intent(this, LoginScreenActivity::class.java)
            startActivity(intent)
        }

        // For show password and hide password
        btnshow_pw.setOnClickListener {
            // Check if password is visible or not
            if (password.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Password is currently visible, make it not visible
                confirmpassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                val showPasswordDrawable  = ContextCompat.getDrawable(this, R.drawable.ic_baseline_remove_red_eye_24)
                btnshow_pw.setCompoundDrawablesRelativeWithIntrinsicBounds(showPasswordDrawable,null,null,null)
            } else {
                // Password is currently hidden
                password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                confirmpassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                val showPasswordDrawable  = ContextCompat.getDrawable(this, R.drawable.ic_baseline_remove_red_eye_24_2)
                btnshow_pw.setCompoundDrawablesRelativeWithIntrinsicBounds(showPasswordDrawable,null,null,null)
            }

            // Move the cursor to end of the password
            password.setSelection(password.text.length)
            confirmpassword.setSelection(confirmpassword.text.length)

        }
    }

    // Function to add new user from EditText that user already fill
    fun adduser(email:EditText,password:EditText,confirmpassword:EditText): Boolean{
        // Create auth variable for
        var auth = Firebase.auth

        // Check if password is same with confirm password or not
        if (password.text.toString()!=confirmpassword.text.toString()){
            password.setError("Password tidak sama")
            confirmpassword.setError("Konfirmasi Password tidak sama")
            return true
        }
        else {
            // Check if password,email or confirm password is empty or not
            if (email.text.toString()=="" || password.text.toString()==""|| confirmpassword.text.toString()==""){
                email.setError("Tidak Boleh kosong")
                password.setError("Tidak Boleh kosong")
                confirmpassword.setError("Tidak Boleh kosong")
                return true
            }
            if (email.text.toString()==""){
                email.setError("Tidak Boleh kosong")
                return true
            }
            if (password.text.toString()==""){
                password.setError("Tidak Boleh kosong")
                return true
            }
            password.clearFocus()
            confirmpassword.clearFocus()
            val progresLog = ProgressDialog(this)
            progresLog.setMessage("Mendaftarkan...")
            progresLog.setCancelable(false)
            progresLog.show()
            auth.createUserWithEmailAndPassword(
                email.text.toString(),
                password.text.toString()
            ).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.v("TAG", "Test")

                        Toast.makeText(
                            baseContext,
                            "Sukses !",
                            Toast.LENGTH_SHORT,
                        ).show()
                        // Sign in success, update UI with the signed-in user's information
                        if (progresLog.isShowing) progresLog.dismiss()
                        val intent = Intent(this, LoginScreenActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        if (progresLog.isShowing) progresLog.dismiss()
                        Log.v("TAG", "Gagal")

                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext,
                            "Daftar gagal",
                            Toast.LENGTH_SHORT,
                        ).show()
                        email.setError("Email sudah pernah dipakai")
                    }
                }
        }
        return true
    }





}