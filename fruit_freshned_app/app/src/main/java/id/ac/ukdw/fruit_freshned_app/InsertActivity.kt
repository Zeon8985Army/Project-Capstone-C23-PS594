package id.ac.ukdw.fruit_freshned_app

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

// API1
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.*
import org.json.JSONObject

// API 3
import okhttp3.MultipartBody
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException

class InsertActivity : AppCompatActivity() {
    // Variable to logout
    lateinit var GoogleSout: GoogleSignInClient

    // Variable that is not used
    var editTextList = ArrayList<EditText>()

    // Variable Firestore
    var firestore: FirebaseFirestore? = null
    var successInsertData : Boolean? = null

    // Variable for Load image
    lateinit var ImageURI : Uri
    lateinit var statusGambar : String
    var ImageURIList = ArrayList<Uri>(2)
    var urlGambarBuah : String = ""

    // Variable for make unique tittle
    var statusUniqeNamaKegiatan : Boolean = false
    var daftarBuah = ArrayList<String>()

    // Variable for save prediction
    var results = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        // Indentity  from user that login right now
        val user_name = intent.getStringExtra("name").toString()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert)

        // For Toolbar
        setSupportActionBar(findViewById(R.id.toolbarUmum))
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Variable Firestore
        firestore = FirebaseFirestore.getInstance()

        // Variable Button
        val btnAddData = findViewById<LinearLayout>(R.id.prediksi)
        val btnAddGambarDepan = findViewById<CardView>(R.id.addGambarBuah)
        val btnDeleteImgCvrDpn = findViewById<CardView>(R.id.delete_gambar)

        // Input
        val nama_kegiatan = findViewById<EditText>(R.id.nama_kegiatan)
        val dari = findViewById<EditText>(R.id.dari)

        // Variable Layout Image
        val gambarBuah = findViewById<ImageView>(R.id.gambarBuah)

        // Set Value "dari" or creator of data
        dari.setText(user_name)

        // Add Image to ImageView
        btnAddGambarDepan.setOnClickListener {
            statusGambar="Depan"
            pilihGambar()
        }
        // Add Data to Firestore
        btnAddData.setOnClickListener {
            tambahData(nama_kegiatan,dari,daftarBuah,editTextList)
        }
        // To Delete Image that already sent to ImageView
        btnDeleteImgCvrDpn.setOnClickListener {
            deleteGambar(gambarBuah)
        }
        // Get all "nama_kegiatan" that already created by user that log in right now, for makesure
        // the nama_kegiatan is unique
        firestore?.collection("data_buah")?.get()!!
            .addOnSuccessListener { documents ->
                // This process for make sure only save "nama_kegiatan" data that created by user that log in right now
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
                    if (username_image==user_name_login){
                        daftarBuah.add(document.data["nama_kegiatan"].toString())
                    }
                }
            }
    }
    // This function is to hanlde, menu item in top right
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    // This function is use for logout by click one of item in menu top right
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.logout -> {
                Firebase.auth.signOut()
                val requestgsso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.webClient))
                    .requestEmail()
                    .build()
                GoogleSout= GoogleSignIn.getClient(this,requestgsso)
                GoogleSout.signOut().addOnCompleteListener {
                    val intent= Intent(this, LoginScreenActivity::class.java)
                    Toast.makeText(this,"Keluar",Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // This function is used for adding new data and check the input data. It will store the data to firestore
    fun tambahData(nama_kegiatan:EditText,dari:EditText,
                   daftarJudul: ArrayList<String>, editTextListP:ArrayList<EditText>): Boolean {
        // Cek if all input is filled
        if (nama_kegiatan.text.toString()==""){
            nama_kegiatan.setError("Judul harus diisi")
            nama_kegiatan.requestFocus()
            return true
        }
        // Cek if all input is filled
        if (dari.text.toString()==""){
            dari.setError("Pembuat harus diisi")
            dari.requestFocus()
            return true
        }
        // Get the image input area
        val gambarBuah = findViewById<ImageView>(R.id.gambarBuah)
        val rl_coverDpn = findViewById<RelativeLayout>(R.id.rl_coverDpn)
        val addCoverDpn = findViewById<CardView>(R.id.addGambarBuah)

        // Check if Image is filled
        if (gambarBuah.getDrawable()==null ){
            rl_coverDpn.setBackgroundResource(R.drawable.background_border_relativelayout_red)
            addCoverDpn.focusable
            Toast.makeText(this, "Gambar harus diisi", Toast.LENGTH_SHORT).show()
            return true
        }
        // Check in nama_kegiatan is unique. This will help in searching
        for (judulItem in daftarJudul){
            if (nama_kegiatan.text.toString()==judulItem){
                statusUniqeNamaKegiatan=true
                break
            }
        }
        // If not unique, give information to UI
        if (statusUniqeNamaKegiatan==true){
            nama_kegiatan.setError("Nama pengecekan sudah ada")
            nama_kegiatan.requestFocus()
            statusUniqeNamaKegiatan=false
            return true
        }
        // If Image is not empty then do pridiction
        if (gambarBuah.getDrawable()!=null){
            var test = ""
            runBlocking {
                test = async { predictGambarDua(nama_kegiatan,dari,daftarJudul,editTextListP) }.await()
                async { uploadGambar() }.await()
            }
        }
        return true
    }

    // To Select Image from Android image directory and put it on ImageView
    fun pilihGambar(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,100)
    }

    // This function is used for complete select Image from gallery and put it on ImageView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val gambarCoverDpn = findViewById<ImageView>(R.id.gambarBuah)
        if (requestCode == 100 && resultCode == RESULT_OK){
            if (statusGambar=="Depan") {
                ImageURI = data?.data!!
                ImageURIList.add(0, ImageURI)
                gambarCoverDpn.getLayoutParams().height = 1200;
                gambarCoverDpn.setImageURI(ImageURIList[0])
            }
        }
    }
    suspend fun predictGambarDua(judul:EditText,penulis1:EditText,
                                 daftarJudul: ArrayList<String>, editTextListP:ArrayList<EditText>):String{
        val gambarCoverDpn = findViewById<ImageView>(R.id.gambarBuah)
        if (successInsertData==false){
            Toast.makeText(this, "Data gagal ditambahkan", Toast.LENGTH_SHORT).show()
            return ""
        }else{
            val progresLog = ProgressDialog(this)
            progresLog.setMessage("Prediksi Data...")
            progresLog.setCancelable(false)
            progresLog.show()
            val fileName = UUID.randomUUID().toString()+"|Depan"
            urlGambarBuah = fileName
            val bitmap = (gambarCoverDpn.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
            val data = baos.toByteArray()
            baos.close()
            val file = File.createTempFile("image", null, cacheDir)
            file.writeBytes(data)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )
                .build()
            val request = Request.Builder()
                .url("https://model-api-v2-2vhkzpmj5a-de.a.run.app/upload-predict?API_KEY=QzIzLVBTNTk0LUJpc21pbGxhaC1MdWx1cw==")
                .post(requestBody)
                .build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    // Handle failure
                    Log.v("TAG", e.toString())
                }
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        try {
                            val jsonResponse = JSONObject(responseData)
                            val imageUrl = jsonResponse.getString("imageUrl")
                            var result = jsonResponse.getString("result")
                            var result_fs = ""

                            Log.v("TAG", "Image URL: $imageUrl")
                            Log.v("TAG", "Result: $result")
                            if (result.toFloat() < 10){
                                result_fs="Segar"
                            }
                            else{
                                result_fs="Kurang Segar"
                            }

                            // get date for input time
                            val currentDate = Date()

                            val calendar = Calendar.getInstance()
                            calendar.time = currentDate

                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH) + 1 // Note: Calendar.MONTH is zero-based
                            val day = calendar.get(Calendar.DAY_OF_MONTH)
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                            val minute = calendar.get(Calendar.MINUTE)
                            val second = calendar.get(Calendar.SECOND)

                            var dataBuah:DataBuah? = null
                            dataBuah = DataBuah(
                                nama_kegiatan = judul.text.toString(),
                                hasil_prediksi =  result_fs.toString(),
                                dari =  penulis1.text.toString(),
                                url_buah = urlGambarBuah,
                                date= "${year}:${month}:${day} ${hour}:${minute}:${second}"
                            )
                            firestore?.collection("data_buah")?.add(dataBuah)?.addOnSuccessListener {
                                successInsertData = true
                            }?.addOnFailureListener {
                                successInsertData = false
                            }
                            if (progresLog.isShowing) progresLog.dismiss()
                            val user_name = intent.getStringExtra("name").toString()
                            val intent = Intent(this@InsertActivity, BuahItemList::class.java)
                            intent.putExtra("name",user_name)
                            startActivity(intent)
                            finish()
                        } catch (e: JSONException) {
                            Log.v("TAG", "Error parsing JSON response: ${e.message}")
                        }
                    } else {
                        Log.v("TAG", "Unsuccessful API response: ${response.code}")
                    }
                    // Handle response
                }
            })
            Log.v("TAG", "Cek return: $results")
            return results
        }
    }
    // Function for upload data image to Firestorage, Image will store file name by add user identity
    suspend fun uploadGambar():Boolean{
        val user_name = intent.getStringExtra("name").toString()
        val gambarBuah = findViewById<ImageView>(R.id.gambarBuah)
        val progresLog = ProgressDialog(this)
        // Show message "unggah Data" to users
        progresLog.setMessage("Unggah Data ...")
        progresLog.setCancelable(false)
        progresLog.show()
        // Check if there are fail submit data in firestore or not, if exist cancel this process
        if (successInsertData==false){
            if (progresLog.isShowing) progresLog.dismiss()
            Toast.makeText(this, "Data gagal ditambahkan", Toast.LENGTH_SHORT).show()
            return true
        // If there are no error, add data by it's creator identity so each file has identifier from it's creator
        }else{
            val fileName = user_name+"↟"+UUID.randomUUID().toString()+"|Depan"
            urlGambarBuah = fileName
            // Start to add directory to save
            val firestorage = FirebaseStorage.getInstance().getReference("image/$fileName")
            // Create Bitmap from ImageView that already has Image
            val bitmap = (gambarBuah.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            // Convert to Bitmap with quality 1/4
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
            val data = baos.toByteArray()
            baos.close()
            // Try to upload data
            firestorage.putBytes(data)
                .addOnSuccessListener { storageResult ->
                    if (progresLog.isShowing) progresLog.dismiss()
                    // After complete upload data, set null ImageView
                    gambarBuah.setImageURI(null)
                }
                .addOnFailureListener {
                    if (progresLog.isShowing) progresLog.dismiss()
                    Toast.makeText(this, "Gambar gagal ditambahkan", Toast.LENGTH_SHORT).show()
                }
            return true
        }
        return true
    }

    // To Delete ImageView contain, set to null
    fun deleteGambar(gambar : ImageView){
        gambar.setImageURI(null)
    }
}