package id.ac.ukdw.fruit_freshned_app

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

// This activity is used for create layout that show information for specific data
class DetailActivity : AppCompatActivity() {
    // Variabel to logout
    lateinit var mGoogleSignInClient: GoogleSignInClient

    // Variabel Firestore
    var firestore: FirebaseFirestore? = null

    // Val simpan Id Data saat ini
    var idData : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Toolbar
        // For Toolbar
        setSupportActionBar(findViewById(R.id.toolbarUmum))
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // For Firestore
        firestore = FirebaseFirestore.getInstance()

        // Variabel to Input
        val nama_kegiatan = findViewById<TextView>(R.id.nama_kegiatan)
        val hasil_prediksi = findViewById<TextView>(R.id.hasil_prediksi)
        val dari = findViewById<TextView>(R.id.dari)

        // Layout for image fruit
        val gambar_url = findViewById<ImageView>(R.id.gambarBuah)
        val progresLog = ProgressDialog(this)

        // Add information if app still load the data
        progresLog.setMessage("Memuat Data ...")
        progresLog.setCancelable(false)
        progresLog.show()

        // Get data from firestore by searching using "nama_kegiatan"
        firestore?.collection("data_buah")
            ?.whereEqualTo("nama_kegiatan",intent.getStringExtra("nama_kegiatan").toString())?.get()!!
            .addOnSuccessListener { documents ->
                // Make sure only data that created by user that log in right now that can be see.
                for (document in documents) {
                    val url_buah = document.data["url_buah"].toString()
                    val atIndex_user_image = url_buah.indexOf("↟")
                    if (atIndex_user_image == -1) {
                        continue
                    }
                    val username_image = url_buah.substring(0, atIndex_user_image)

                    var auth = Firebase.auth
                    val atIndex_user_login = auth.currentUser!!.email?.indexOf("@")
                    val user_name_login = atIndex_user_login?.let { auth.currentUser!!.email?.substring(0, it) }

                    // This is check if user that log in right now only see data that created by user
                    if (username_image==user_name_login){
                        // Dapatin data id document saat ini
                        idData = document.id
                        nama_kegiatan.setText(document.data["nama_kegiatan"].toString())
                        hasil_prediksi.setText(document.data["hasil_prediksi"].toString())
                        dari.setText(document.data["dari"].toString())
                        val storageReference = FirebaseStorage.getInstance().reference
                        val photoReference = storageReference.child("image/${document.data["url_buah"].toString()}")
                        val lclFile = File.createTempFile("TmpIMG","jpg")
                        photoReference.getFile(lclFile)
                            .addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(lclFile.absolutePath)
                                gambar_url.setImageBitmap(bitmap)
                                if (progresLog.isShowing) progresLog.dismiss()
                            }
                            .addOnFailureListener {
                            }
                        }
                    }
            }
    }
    // This function is show menu on top right
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    // This function is handle logout from item in menu top right
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.logout -> {
                Firebase.auth.signOut()
                val requestgsso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.webClient))
                    .requestEmail()
                    .build()
                mGoogleSignInClient= GoogleSignIn.getClient(this,requestgsso)
                mGoogleSignInClient.signOut().addOnCompleteListener {
                    val intent= Intent(this, LoginScreenActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}