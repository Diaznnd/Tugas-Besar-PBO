import java.time.LocalDate;

public class Unggas extends HewanPotong {
    private int permintaanPotong;

    public Unggas(String pemilik, String jenis, String asal, int jumlah, int permintaanPotong, LocalDate tanggalUntuk, int tingkatLapar) {
        super(pemilik, jenis, 0, asal, jumlah, permintaanPotong, tanggalUntuk, "unggas", tingkatLapar);
        this.permintaanPotong = permintaanPotong;
    }

    public int getPermintaanPotong() {
        return permintaanPotong;
    }

    public void setPermintaanPotong(int permintaanPotong) {
        this.permintaanPotong = permintaanPotong;
    }
}
