import java.time.LocalDate;

public class HewanPotong {
    private String pemilik;
    private String jenis;
    private double berat;
    private String asal;
    private int jumlah;
    private int permintaanPotong;
    private LocalDate tanggalUntuk;
    private String kategori;
    private int tingkatLapar;

    public HewanPotong(String pemilik, String jenis, double berat, String asal, int jumlah, int permintaanPotong, LocalDate tanggalUntuk, String kategori, int tingkatLapar) {
        this.pemilik = pemilik;
        this.jenis = jenis;
        this.berat = berat;
        this.asal = asal;
        this.jumlah = jumlah;
        this.permintaanPotong = permintaanPotong;
        this.tanggalUntuk = tanggalUntuk;
        this.kategori = kategori;
        this.tingkatLapar = tingkatLapar;
    }

    public String getPemilik() {
        return pemilik;
    }

    public String getJenis() {
        return jenis;
    }

    public double getBerat() {
        return berat;
    }

    public String getAsal() {
        return asal;
    }

    public int getJumlah() {
        return jumlah;
    }

    public int getPermintaanPotong() {
        return permintaanPotong;
    }

    public LocalDate getTanggalUntuk() {
        return tanggalUntuk;
    }

    public String getKategori() {
        return kategori;
    }

    public int getTingkatLapar() {
        return tingkatLapar;
    }
}
