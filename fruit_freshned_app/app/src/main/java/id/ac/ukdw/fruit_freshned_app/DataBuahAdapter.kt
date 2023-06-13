package id.ac.ukdw.fruit_freshned_app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlin.collections.ArrayList

// This function is used to send data that already got from firestore (BuahItemList Activity) To RecycleView. So this function
// Will process array of data class that already sent and fill it to layout that user will see.
class DataBuahAdapter(val listDataBuah : ArrayList<DataBuah>) :RecyclerView.Adapter<DataBuahAdapter.DataBuahHolder>(){
    class  DataBuahHolder(val v:View):RecyclerView.ViewHolder(v){

        // Variabel to create object from Class DataBuah that contain data in Firestore
        var dataBuahVar : DataBuah? = null

        // bindView to get data and put it on layout, this function get parameter Object from Data Class DataBuah
        fun bindView(DataBuah : DataBuah){

            // We get it's data and put it on TextView
            this.dataBuahVar = DataBuah
            v.findViewById<TextView>(R.id.tv_judulBuku).text = "${DataBuah.nama_kegiatan}"
            v.findViewById<TextView>(R.id.tv_penerbit).text = "${DataBuah.hasil_prediksi}"

            // We try to get IMAGE data fro Firestorage, so we need to make Instance from it and geet the data
            val storageReference = FirebaseStorage.getInstance().reference
            val photoReference = storageReference.child("image/${DataBuah.url_buah}")
            val lclFile = File.createTempFile("TmpIMG","jpg")
            photoReference.getFile(lclFile)
                .addOnSuccessListener {
                    // Create bitmap and put in on ImageView, so it will work
                    val bitmap = BitmapFactory.decodeFile(lclFile.absolutePath)
                    v.findViewById<ImageView>(R.id.imageItem).setImageBitmap(bitmap)
                    v.findViewById<ProgressBar>(R.id.loading).setVisibility(View.GONE);
                }
                .addOnFailureListener {
                }

            // Area Event Click

            // Delete Data
            // Variabel Firestor
            var firestore: FirebaseFirestore? = null
            firestore = FirebaseFirestore.getInstance()
            v.findViewById<TextView>(R.id.hapusItem).setOnClickListener {
                // Success
                // Dapatin ID dari Firestore
                var buahList : BuahItemList = BuahItemList()
                val builder = AlertDialog.Builder(v.context)
                val penulis1 = DataBuah.dari

                // Send message to make sure if want to delete the prediction data
                builder.setMessage("Apakah anda ingin menghapus data buku '${DataBuah.nama_kegiatan}' ?")
                    .setCancelable(false)
                    // if select yes, then search the data and make sure it only delete data from user data who is log in.
                    .setPositiveButton("Ya") { dialog, id ->
                        val ids_buah_user_login: ArrayList<String> = arrayListOf()
                        firestore?.collection("data_buah")?.get()!!
                            .addOnSuccessListener { documents2 ->
                                for (document in documents2) {
                                    val url_buah = document.data["url_buah"].toString()
                                    val atIndex_user_image = url_buah.indexOf("↟")
                                    if (atIndex_user_image == -1) {
                                        continue
                                    }
                                    val username_image = url_buah.substring(0, atIndex_user_image)

                                    var auth = Firebase.auth
                                    val atIndex_user_login = auth.currentUser!!.email?.indexOf("@")
                                    val user_name_login = atIndex_user_login?.let { auth.currentUser!!.email?.substring(0, it) }
                                    // Add data id that can be delete, all this process will make sure data with same "nama_kegiatan"
                                    // will deleted just from user who are log in
                                    if (username_image==user_name_login){
                                        ids_buah_user_login.add(document.id)
                                    }
                                }
                            }
                        // Delete data from firestore
                        val idDocument = firestore.collection("data_buah")
                            .whereEqualTo("nama_kegiatan",DataBuah.nama_kegiatan.toString()).get()
                            .addOnSuccessListener { documents ->
                                for (dataDoc in documents) {
                                    // Delete data id that can be deleted
                                    if (ids_buah_user_login.contains(dataDoc.id)){
                                        Log.v("CEK DELETE",documents.toString())
                                        firestore.collection("data_buah").document(dataDoc.id).delete()
                                    }
                                }
                                Toast.makeText(v.context, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show()
                                val i: Intent = Intent( v.context,BuahItemList::class.java)
                                i.putExtra("refresh",true)
                                i.putExtra("name",penulis1)
                                v.context.startActivity(i)
                            }
                    }
                    // If select "tidak" then cancel this operation
                    .setNegativeButton("Tidak") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
            // DETAIL EVENT
            // To see detail information from each data in new layout, so user can see full image from that
            v.findViewById<CardView>(R.id.itemBuku).setOnClickListener {
                val penulis1 = DataBuah.dari
                val idDocument = firestore.collection("data_buah")
                    .whereEqualTo("nama_kegiatan",DataBuah.nama_kegiatan.toString()).get()
                    .addOnSuccessListener { documents ->
                        // This process is make sure only data from user that login that can see by user right now
                        for (dataDoc in documents) {
                            val url_buah = dataDoc.data["url_buah"].toString()
                            val atIndex_user_image = url_buah.indexOf("↟")
                            if (atIndex_user_image == -1) {
                                continue
                            }
                            val username_image = url_buah.substring(0, atIndex_user_image)

                            var auth = Firebase.auth
                            val atIndex_user_login = auth.currentUser!!.email?.indexOf("@")
                            val user_name_login = atIndex_user_login?.let { auth.currentUser!!.email?.substring(0, it) }

                            if (username_image==user_name_login){
                                val i: Intent = Intent( v.context,DetailActivity::class.java)
                                i.putExtra("nama_kegiatan",dataDoc.data["nama_kegiatan"].toString())
                                i.putExtra("name",penulis1)
                                v.context.startActivity(i)
                            }

                        }
                    }
            }
        }
    }

    // This function is default from RecycleView ( For add item_buah layout )
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBuahHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_buah, parent, false)
        return DataBuahHolder(v)
    }
    // This function is default from RecycleView ( To get item data each index from ArrayOfDataClass )
    override fun onBindViewHolder(holder: DataBuahAdapter.DataBuahHolder, position: Int) {
        holder.bindView(listDataBuah[position])
    }
    // This function is default from RecycleView ( To Get Count Item )
    override fun getItemCount(): Int {
        return listDataBuah.size
    }
}
