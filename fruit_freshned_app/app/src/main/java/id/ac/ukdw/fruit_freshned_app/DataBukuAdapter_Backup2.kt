//package id.ac.ukdw.fruit_freshned_app
//
//
//import android.app.AlertDialog
//import android.content.Intent
//import android.graphics.BitmapFactory
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.cardview.widget.CardView
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.FirebaseStorage
//import java.io.File
//
//class DataBukuAdapter_Backup2(val listDataBuah : ArrayList<DataBuah>) :RecyclerView.Adapter<DataBukuAdapter_Backup2.DataBukuHolder>(){
//    class  DataBukuHolder(val v:View):RecyclerView.ViewHolder(v){
//        var dataBuahVar : DataBuah? = null
//
//        fun bindView(DataBuah : DataBuah){
//            this.dataBuahVar = DataBuah
//
//            v.findViewById<TextView>(R.id.tv_judulBuku).text = "${DataBuah.nama_kegiatan}"
//            v.findViewById<TextView>(R.id.tv_penerbit).text = "${DataBuah.hasil_prediksi}"
//            v.findViewById<TextView>(R.id.tv_tahunTerbit).text = ""
//
//            v.findViewById<TextView>(R.id.tv_penulis).text = "By : ${DataBuah.dari}"
//
//            val storageReference = FirebaseStorage.getInstance().reference
//            val photoReference = storageReference.child("image/${DataBuah.url_buah}")
//
//
//            val lclFile = File.createTempFile("TmpIMG","jpg")
//            photoReference.getFile(lclFile)
//                .addOnSuccessListener {
//                    val bitmap = BitmapFactory.decodeFile(lclFile.absolutePath)
//                    v.findViewById<ImageView>(R.id.imageItem).setImageBitmap(bitmap)
//                    v.findViewById<ProgressBar>(R.id.loading).setVisibility(View.GONE);
//                }
//                .addOnFailureListener {
//                }
//
//            // Event Click
//
//            // Delete Data
//            // Variabel Firestore
//            var firestore: FirebaseFirestore? = null
//            firestore = FirebaseFirestore.getInstance()
//
//            v.findViewById<TextView>(R.id.hapusItem).setOnClickListener {
//                // Success
//                // Dapatin ID dari Firestore
//                var buahList : BuahItemList = BuahItemList()
//                val builder = AlertDialog.Builder(v.context)
//                val penulis1 = DataBuah.dari
//                builder.setMessage("Apakah anda ingin menghapus data buku '${DataBuah.nama_kegiatan}' ?")
//                    .setCancelable(false)
//                    .setPositiveButton("Ya") { dialog, id ->
//                        // Delete selected note from database
//                        val idDocument = firestore.collection("data_buah")
//                            .whereEqualTo("nama_kegiatan",DataBuah.nama_kegiatan.toString()).get()
//                            .addOnSuccessListener { documents ->
//                                for (dataDoc in documents) {
//                                    firestore.collection("data_buah").document(dataDoc.id).delete()
//                                }
//                                Toast.makeText(v.context, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show()
//                                val i: Intent = Intent( v.context,BuahItemList::class.java)
//                                i.putExtra("refresh",true)
//                                i.putExtra("name",penulis1)
//                                v.context.startActivity(i)
//                            }
//                    }
//                    .setNegativeButton("Tidak") { dialog, id ->
//                        // Dismiss the dialog
//                        dialog.dismiss()
//                    }
//                val alert = builder.create()
//                alert.show()
//            }
//
////            v.findViewById<TextView>(R.id.editItem).setOnClickListener {
////                // Success
////                // Dapatin ID dari Firestore
////                val penulis1 = DataBuku.penulis1
////                val idDocument = firestore.collection("dataBuku")
////                    .whereEqualTo("judul",DataBuku.judul.toString()).get()
////                    .addOnSuccessListener { documents ->
////                        for (dataDoc in documents) {
////                            val i: Intent = Intent( v.context,EditActivity::class.java)
////                            i.putExtra("judulBuku",dataDoc.data["judul"].toString())
////                            i.putExtra("name",penulis1)
////                            v.context.startActivity(i)
////                        }
////                    }
////            }
//
//            v.findViewById<CardView>(R.id.itemBuku).setOnClickListener {
//                val penulis1 = DataBuah.dari
//                val idDocument = firestore.collection("data_buah")
//                    .whereEqualTo("nama_kegiatan",DataBuah.nama_kegiatan.toString()).get()
//                    .addOnSuccessListener { documents ->
//                        for (dataDoc in documents) {
//                            val i: Intent = Intent( v.context,DetailActivity::class.java)
//                            i.putExtra("nama_kegiatan",dataDoc.data["nama_kegiatan"].toString())
//                            i.putExtra("name",penulis1)
//                            v.context.startActivity(i)
//                        }
//                    }
//            }
////            v.findViewById<ImageView>(R.id.imageKontak).setImageResource(KontakTelp.imgKontak)
////            v.setOnClickListener {
////                if (KontakTelp.wa and  KontakTelp.tg){
////                    val i: Intent = Intent( v.context,WaTgKontakDetail::class.java)
////                    i.putExtra("namaKontak",KontakTelp.nama)
////                    i.putExtra("nomerTelp",KontakTelp.nomerTelp)
////                    i.putExtra("email",KontakTelp.email)
////                    i.putExtra("imgKontak",KontakTelp.imgKontak)
////                    v.context.startActivity(i)
////                }else if (KontakTelp.wa){
////                    val i: Intent = Intent( v.context,WaKontakDetail::class.java)
////                    i.putExtra("namaKontak",KontakTelp.nama)
////                    i.putExtra("nomerTelp",KontakTelp.nomerTelp)
////                    i.putExtra("email",KontakTelp.email)
////                    i.putExtra("imgKontak",KontakTelp.imgKontak)
////                    v.context.startActivity(i)
////                }else if(KontakTelp.tg){
////                    val i: Intent = Intent( v.context,TgKontakDetail::class.java)
////                    i.putExtra("namaKontak",KontakTelp.nama)
////                    i.putExtra("nomerTelp",KontakTelp.nomerTelp)
////                    i.putExtra("email",KontakTelp.email)
////                    i.putExtra("imgKontak",KontakTelp.imgKontak)
////                    v.context.startActivity(i)
////                }else{
////                    val i: Intent = Intent( v.context,KontakDetail::class.java)
////                    i.putExtra("namaKontak",KontakTelp.nama)
////                    i.putExtra("nomerTelp",KontakTelp.nomerTelp)
////                    i.putExtra("email",KontakTelp.email)
////                    i.putExtra("imgKontak",KontakTelp.imgKontak)
////                    v.context.startActivity(i)
////                }
//
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBukuHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_buku, parent, false)
//        return DataBukuHolder(v)
//    }
//
//    override fun onBindViewHolder(holder: DataBukuAdapter_Backup2.DataBukuHolder, position: Int) {
//        holder.bindView(listDataBuah[position])
//    }
//
//    override fun getItemCount(): Int {
//        return listDataBuah.size
//    }
//}
