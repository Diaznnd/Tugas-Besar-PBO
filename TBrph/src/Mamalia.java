import java.time.LocalDate;

public class Mamalia extends HewanPotong {
    private double berat;
    private String asal;

    public Mamalia(String pemilik, String jenis, double berat, String asal, int jumlah, LocalDate tanggalUntuk, int tingkatLapar) {
        super(pemilik, jenis, berat, asal, jumlah, 0, tanggalUntuk, "mamalia", tingkatLapar);
        this.berat = berat;
        this.asal = asal;
    }

    public double getBerat() {
        return berat;
    }

    public void setBerat(double berat) {
        this.berat = berat;
    }

    public String getAsal() {
        return asal;
    }

    public void setAsal(String asal) {
        this.asal = asal;
    }
}
