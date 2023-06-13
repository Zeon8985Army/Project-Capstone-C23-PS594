package id.ac.ukdw.fruit_freshned_app

import android.app.ProgressDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class BuahItemList : AppCompatActivity(){

    // Variabel for Logout
    lateinit var GoogleSout: GoogleSignInClient

    // Variabel for firestore
    var firestore: FirebaseFirestore? = null

    // Variabel for Search
    lateinit var searchView: SearchView
    // Save all data
    var ListDataBuku = ArrayList<DataBuah>()
    // Save filtered data
    var ListDataBukuFiltered = ArrayList<DataBuah>()
    // Status this is nor first time load
    var statusResume :Boolean = false


    // If we refresh on this activity, because data in update if we add new data
    override fun onResume() {
        super.onResume()
        // This is check area
        if (!checkDataUpdate()){
            refreshPage()
        }else{
            if (statusResume){
                refreshPage()
            }
        }
        statusResume = true

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Get Data from user, because we need user identify to make sure each data only can be open by it's creator
        var auth = Firebase.auth
        val atIndex = auth.currentUser!!.email?.indexOf("@")
        val user_name = atIndex?.let { auth.currentUser!!.email?.substring(0, it) }

        // To add layout for list_item_buah
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_item_buah)

        // For Toolbar
        setSupportActionBar(findViewById(R.id.toolbarUmum))
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Call refreshPage function for call all data from Firestore
        refreshPage()

        // For go to insert data area
        val btnAddData= findViewById<CardView>(R.id.addData).setOnClickListener{
            val i: Intent = Intent( this,InsertActivity::class.java)
            // Save the user identity in here using Intent.PutExtra
            i.putExtra("name",user_name)
            startActivity(i)
        }
        // For Searching query that user want
        searchView = findViewById(R.id.searchView)
        // Using onQueryTextListener so it will run each update happen in this area
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
            // Main function for seaching
            override fun onQueryTextChange(newText: String): Boolean {
                // Clear previous result from searching, so new searching will contain just data that user want
                ListDataBukuFiltered.clear()
                // Loop all data that already optain from firestore
                for (data in ListDataBuku){
                    // Cek if query is equal with each data.
                    if (data.nama_kegiatan.lowercase()==newText.lowercase() || data.hasil_prediksi.lowercase()==newText.lowercase()
                        || data.dari.lowercase()==newText.lowercase() || data.url_buah.lowercase()==newText.lowercase()){
                        ListDataBukuFiltered.add(data)
                        refreshPage(ListDataBukuFiltered)
                    }else if(newText.lowercase()==""){
                        // update using this function for not found
                        refreshPage2()
                    }
                }
                return false
            }
        })
    }

    // For menu option in right-top
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu, menu)
        return true
    }
    // For manage each item menu onclicklistener (for logout area)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.logout -> {
                // Logout using documentation from Firebase
                Firebase.auth.signOut()
                val requestgsso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.webClient))
                    .requestEmail()
                    .build()
                GoogleSout= GoogleSignIn.getClient(this,requestgsso)
                GoogleSout.signOut().addOnCompleteListener {
                    val intent= Intent(this, LoginScreenActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // For refresh to get new data
    fun refreshPage(listBuahFiltered : ArrayList<DataBuah> = ArrayList()){
        // If it is not empty, then add all data to RecyclerView
        if (listBuahFiltered.size!=0) {
            val rcyBuku = findViewById<RecyclerView>(R.id.rcyItemBuku)
            rcyBuku.layoutManager = LinearLayoutManager(this)
            val KontakTelpAdapter = DataBuahAdapter(listBuahFiltered)
            rcyBuku.adapter = KontakTelpAdapter
        }else{
            // If Empty, then the data is not loaded. So, it need to be loaded
            ListDataBuku.clear()
            firestore = FirebaseFirestore.getInstance()

            // For Message, so user can wait with some animation
            val progresLog = ProgressDialog(this)
            progresLog.setMessage("Memuat data ...")
            progresLog.setCancelable(false)
            progresLog.show()

            // Get all data from data_buah
            firestore?.collection("data_buah")?.get()!!
                .addOnSuccessListener { documents ->
                    // Sort add data by it's last date so the data will show desc
                    val sortedDocuments = documents.sortedByDescending { document ->
                        val dateString = document.data["date"].toString()
                        val dateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
                        dateFormat.parse(dateString)
                    }
                    // Make sure only data from the user that is showed
                    for (document in sortedDocuments) {
                        val url_buah = document.data["url_buah"].toString()
                        val atIndex_user_image = url_buah.indexOf("↟")
                        if (atIndex_user_image == -1) {
                            continue
                        }
                        val username_image = url_buah.substring(0, atIndex_user_image)

                        var auth = Firebase.auth
                        val atIndex_user_login = auth.currentUser!!.email?.indexOf("@")
                        val user_name_login = atIndex_user_login?.let { auth.currentUser!!.email?.substring(0, it) }

                        // Show only image from user that login
                        if (username_image==user_name_login){
                            Log.v("TAG",document.data["nama_kegiatan"].toString())
                            // Add to Array of data Class
                            ListDataBuku.add(
                                DataBuah(
                                    nama_kegiatan = document.data["nama_kegiatan"].toString(),
                                    hasil_prediksi =  document.data["hasil_prediksi"].toString(),
                                    dari =  document.data["dari"].toString(),
                                    url_buah =  document.data["url_buah"].toString(),
                                    date = document.data["date"].toString()
                                )
                            )
                        }
                        // After all data is add to Array of Data class, check is it empty or not
                    }
                    // If there no data, show message "Tidak ada data ..."
                    if (ListDataBuku.size==0){
                        val rcyBuku = findViewById<RecyclerView>(R.id.rcyItemBuku)
                        rcyBuku?.let {
                            (rcyBuku.parent as? ViewGroup)?.removeView(rcyBuku)
                        }
                        val info_no_data = findViewById<TextView>(R.id.info_no_data)
                        val spToPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 527f, resources.displayMetrics)
                        val layoutParams = info_no_data.layoutParams
                        layoutParams.height = spToPixels.toInt()
                        info_no_data.layoutParams = layoutParams
                        info_no_data.setText("Tidak ada data ...")
                        if (progresLog.isShowing) progresLog.dismiss()
                    // If there data, show it to RecyclerView
                    }else{
                        val rcyBuku = findViewById<RecyclerView>(R.id.rcyItemBuku)
                        rcyBuku.layoutManager = LinearLayoutManager(this)
                        val KontakTelpAdapter = DataBuahAdapter(ListDataBuku)
                        rcyBuku.adapter = KontakTelpAdapter
                        if (progresLog.isShowing) progresLog.dismiss()
                    }

                }
        }
    }

    // This function is used if there no data that get from searching
    fun refreshPage2(){
        firestore = FirebaseFirestore.getInstance()
        val progresLog = ProgressDialog(this)
        progresLog.setMessage("Memuat data ...")
        progresLog.setCancelable(false)
        progresLog.show()
        firestore?.collection("data_buah")?.get()!!
            .addOnSuccessListener { documents ->
                val sortedDocuments = documents.sortedByDescending { document ->
                    val dateString = document.data["date"].toString()
                    val dateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
                    dateFormat.parse(dateString)
                }

                ListDataBuku.clear()
                for (document in sortedDocuments) {
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
                        Log.v("TAG",document.data["nama_kegiatan"].toString())
                        ListDataBuku.add(
                            DataBuah(
                                nama_kegiatan = document.data["nama_kegiatan"].toString(),
                                hasil_prediksi =  document.data["hasil_prediksi"].toString(),
                                dari =  document.data["dari"].toString(),
                                url_buah =  document.data["url_buah"].toString(),
                                date = document.data["date"].toString()
                            )
                        )
                    }
                }
                if (ListDataBuku.size==0){
                    val rcyBuku = findViewById<RecyclerView>(R.id.rcyItemBuku)
                    rcyBuku?.let {
                        (rcyBuku.parent as? ViewGroup)?.removeView(rcyBuku)
                    }
                    val info_no_data = findViewById<TextView>(R.id.info_no_data)
                    val spToPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 527f, resources.displayMetrics)
                    val layoutParams = info_no_data.layoutParams
                    layoutParams.height = spToPixels.toInt()
                    info_no_data.layoutParams = layoutParams
                    info_no_data.setText("Tidak ada data ...")
                    if (progresLog.isShowing) progresLog.dismiss()
                }else{
                    val rcyBuku = findViewById<RecyclerView>(R.id.rcyItemBuku)
                    rcyBuku.layoutManager = LinearLayoutManager(this)
                    val KontakTelpAdapter = DataBuahAdapter(ListDataBuku)
                    rcyBuku.adapter = KontakTelpAdapter
                    if (progresLog.isShowing) progresLog.dismiss()
                }

            }
    }

    // This functon will check update data, by checking size of array that we got from Firestore.
    // If different, then show it
    fun checkDataUpdate():Boolean{
        var ListDataLama = ArrayList<DataBuah>()

        firestore?.collection("data_buah")?.get()!!
            .addOnSuccessListener { documents ->
                val sortedDocuments = documents.sortedByDescending { document ->
                    val dateString = document.data["date"].toString()
                    val dateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
                    dateFormat.parse(dateString)
                }
                for (document in sortedDocuments) {
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
                        Log.v("TAG",document.data["nama_kegiatan"].toString())
                        ListDataLama.add(
                            DataBuah(
                                nama_kegiatan = document.data["nama_kegiatan"].toString(),
                                hasil_prediksi =  document.data["hasil_prediksi"].toString(),
                                dari =  document.data["dari"].toString(),
                                url_buah =  document.data["url_buah"].toString(),
                                date = document.data["date"].toString()
                            )
                        )
                    }
                }
            }

        if(ListDataBuku.size==ListDataLama.size){
            return true
        }else{
            return false
        }
    }

}