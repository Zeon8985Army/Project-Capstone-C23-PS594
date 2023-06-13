package id.ac.ukdw.fruit_freshned_app

data class DataBuku_Backup(val judul: String, val penerbit: String,
                           val penulis1: String, val penulis2: String = "",
                           val penulis3: String = "", val tahunTerbit: String,
                           val jumlahHalaman: String, val urlCoverDpn: String,
                           val urlCoverblkg: String ="")
