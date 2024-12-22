import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class RPHManager implements OperasiDatabase {
    private static Connection koneksi;
    private List<HewanPotong> daftarHewan;

    public RPHManager() {
        daftarHewan = new ArrayList<>();
    }

    public static void connectDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/rph";
            String user = "root";
            String password = "";

            koneksi = DriverManager.getConnection(url, user, password);
            System.out.println("Koneksi ke database berhasil.");
        } catch (SQLException e) {
            System.out.println("Koneksi ke database gagal: " + e.getMessage());
        }
    }

    public void tambahKeDaftar(HewanPotong hewan) {
        daftarHewan.add(hewan);
        System.out.println("Hewan ditambahkan ke daftar sementara.");
    }

    public void tampilkanDaftarHewan() {
        if (daftarHewan.isEmpty()) {
            System.out.println("Daftar hewan kosong.");
            return;
        }

        System.out.println("\n=== Daftar Hewan Sementara ===");
        for (int i = 0; i < daftarHewan.size(); i++) {
            HewanPotong hewan = daftarHewan.get(i);
            System.out.printf("%d. Pemilik: %s, Jenis: %s, Jumlah: %d, Tanggal Untuk: %s\n",
                              i + 1, hewan.getPemilik(), hewan.getJenis(), hewan.getJumlah(), hewan.getTanggalUntuk());
        }
    }

    public void prosesDaftarKeDatabase() {
        if (daftarHewan.isEmpty()) {
            System.out.println("Tidak ada hewan untuk diproses ke database.");
            return;
        }

        for (HewanPotong hewan : daftarHewan) {
            create(hewan);
        }
        daftarHewan.clear();
        System.out.println("Semua hewan dalam daftar telah diproses ke database.");
    }

    private static void validasiJenis(String jenis, String kategori) throws InvalidJenisException {
        jenis = jenis.trim(); // Menghapus spasi berlebih

        if (kategori.equalsIgnoreCase("mamalia")) {
            if (!jenis.equalsIgnoreCase("sapi") && !jenis.equalsIgnoreCase("domba") && !jenis.equalsIgnoreCase("kambing") && !jenis.equalsIgnoreCase("kerbau")) {
                throw new InvalidJenisException("Jenis mamalia tidak valid: " + jenis);
            }
        } else if (kategori.equalsIgnoreCase("unggas")) {
            if (!jenis.equalsIgnoreCase("ayam") && !jenis.equalsIgnoreCase("bebek") && !jenis.equalsIgnoreCase("itik")) {
                throw new InvalidJenisException("Jenis unggas tidak valid: " + jenis);
            }
        } else {
            throw new InvalidJenisException("Kategori tidak dikenali: " + kategori);
        }
    }

    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    public void startMonitoring() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (koneksi == null) {
                    return;
                }
                try {
                    String updateMamalia = "UPDATE mamalia SET tingkat_lapar = GREATEST(tingkat_lapar - 5, 0)";
                    Statement statementMamalia = koneksi.createStatement();
                    statementMamalia.executeUpdate(updateMamalia);

                    String updateUnggas = "UPDATE unggas SET tingkat_lapar = GREATEST(tingkat_lapar - 3, 0)";
                    Statement statementUnggas = koneksi.createStatement();
                    statementUnggas.executeUpdate(updateUnggas);

                } catch (SQLException e) {
                    System.out.println("Gagal memperbarui tingkat kelaparan : " + e.getMessage());
                }
            }
        }, 0, 60000);
    }

    @Override
    public void create(Object obj) {
        if (koneksi == null) {
            System.out.println("Koneksi ke database tidak tersedia. Tidak dapat menambahkan data.");
            return;
        }

        try {
            if (obj instanceof Mamalia mamalia) {
                String sql = "INSERT INTO mamalia (pemilik, jenis, berat, asal, jumlah, tanggal_untuk, tingkat_lapar) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = koneksi.prepareStatement(sql);
                statement.setString(1, mamalia.getPemilik());
                statement.setString(2, mamalia.getJenis());
                statement.setDouble(3, mamalia.getBerat());
                statement.setString(4, mamalia.getAsal());
                statement.setInt(5, mamalia.getJumlah());
                statement.setDate(6, Date.valueOf(mamalia.getTanggalUntuk()));
                statement.setInt(7, mamalia.getTingkatLapar());

                int rowsAffected = statement.executeUpdate();
                System.out.println(rowsAffected > 0 ? "Mamalia berhasil ditambahkan ke database." : "Gagal menambahkan mamalia.");
            } else if (obj instanceof Unggas unggas) {
                String sql = "INSERT INTO unggas (pemilik, jenis, asal, jumlah, permintaan_potong, tanggal_untuk, tingkat_lapar) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = koneksi.prepareStatement(sql);
                statement.setString(1, unggas.getPemilik());
                statement.setString(2, unggas.getJenis());
                statement.setString(3, unggas.getAsal());
                statement.setInt(4, unggas.getJumlah());
                statement.setInt(5, unggas.getPermintaanPotong());
                statement.setDate(6, Date.valueOf(unggas.getTanggalUntuk()));
                statement.setInt(7, unggas.getTingkatLapar());

                int rowsAffected = statement.executeUpdate();
                System.out.println(rowsAffected > 0 ? "Unggas berhasil ditambahkan ke database." : "Gagal menambahkan unggas.");
            }
        } catch (SQLException e) {
            System.out.println("Gagal menambahkan data: " + e.getMessage());
        }
    }

    @Override
    public void cekHewan(String tableName) {
        if (koneksi == null) {
            System.out.println("Koneksi ke database tidak tersedia. Tidak dapat membaca data.");
            return;
        }

        try {
            String sql = "SELECT * FROM " + tableName;
            Statement statement = koneksi.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            System.out.println("\n=== Cek Hewan Tabel " + tableName + " ===");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String pemilik = resultSet.getString("pemilik");
                String jenis = resultSet.getString("jenis");
                int tingkatLapar = resultSet.getInt("tingkat_lapar");
                Date tanggalUntuk = resultSet.getDate("tanggal_untuk");

                // Hitung waktu hitung mundur
                LocalDate tanggalSekarang = LocalDate.now();
                LocalDate tanggalTujuan = tanggalUntuk.toLocalDate();
                long hariTersisa = ChronoUnit.DAYS.between(tanggalSekarang, tanggalTujuan);

                // Tampilkan data
                System.out.println("ID: " + id);
                System.out.println("Pemilik: " + pemilik);
                System.out.println("Jenis: " + jenis);
                System.out.println("Tingkat Kelaparan: " + tingkatLapar + "%");
                System.out.println("Waktu Tersisa: " + hariTersisa + " hari");

                // Peringatan untuk memberi makan
                if (tingkatLapar < 50) {
                    System.out.println("[Peringatan] Hewan ini perlu diberi makan segera.");
                }

                System.out.println("-----------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Gagal membaca data: " + e.getMessage());
        }
    }

    @Override
    public void beriMakan(String tableName) {
        if (koneksi == null) {
            System.out.println("Koneksi ke database tidak tersedia. Tidak dapat memberi makan hewan.");
            return;
        }

        try {
            String sql = "UPDATE " + tableName + " SET tingkat_lapar = 100 WHERE tingkat_lapar < 50";
            Statement statement = koneksi.createStatement();
            int rowsUpdated = statement.executeUpdate(sql);

            if (rowsUpdated > 0) {
                System.out.println("Berhasil memberi makan hewan dengan tingkat kelaparan di bawah 50%.");
            } else {
                System.out.println("Tidak ada hewan dengan tingkat kelaparan di bawah 50%.");
            }
        } catch (SQLException e) {
            System.out.println("Gagal memberi makan hewan: " + e.getMessage());
        }
    }

    @Override
    public void update(int id, String tableName, Object obj) {
        if (koneksi == null) {
            System.out.println("Koneksi ke database tidak tersedia. Tidak dapat mengupdate data.");
            return;
        }

        try {
            if (obj instanceof Mamalia mamalia && tableName.equalsIgnoreCase("mamalia")) {
                String sql = "UPDATE mamalia SET pemilik = ?, jenis = ?, berat = ?, asal = ?, jumlah = ?, tanggal_untuk = ?, tingkat_lapar = ? WHERE id = ?";
                PreparedStatement statement = koneksi.prepareStatement(sql);
                statement.setString(1, mamalia.getPemilik());
                statement.setString(2, mamalia.getJenis());
                statement.setDouble(3, mamalia.getBerat());
                statement.setString(4, mamalia.getAsal());
                statement.setInt(5, mamalia.getJumlah());
                statement.setDate(6, Date.valueOf(mamalia.getTanggalUntuk()));
                statement.setInt(7, mamalia.getTingkatLapar());
                statement.setInt(8, id);

                int rowsUpdated = statement.executeUpdate();
                System.out.println(rowsUpdated > 0 ? "Mamalia berhasil diupdate." : "Mamalia dengan ID " + id + " tidak ditemukan.");
            } else if (obj instanceof Unggas unggas && tableName.equalsIgnoreCase("unggas")) {
                String sql = "UPDATE unggas SET pemilik = ?, jenis = ?, asal = ?, jumlah = ?, permintaan_potong = ?, tanggal_untuk = ?, tingkat_lapar = ? WHERE id = ?";
                PreparedStatement statement = koneksi.prepareStatement(sql);
                statement.setString(1, unggas.getPemilik());
                statement.setString(2, unggas.getJenis());
                statement.setString(3, unggas.getAsal());
                statement.setInt(4, unggas.getJumlah());
                statement.setInt(5, unggas.getPermintaanPotong());
                statement.setDate(6, Date.valueOf(unggas.getTanggalUntuk()));
                statement.setInt(7, unggas.getTingkatLapar());
                statement.setInt(8, id);

                int rowsUpdated = statement.executeUpdate();
                System.out.println(rowsUpdated > 0 ? "Unggas berhasil diupdate." : "Unggas dengan ID " + id + " tidak ditemukan.");
            } else {
                System.out.println("Objek tidak cocok dengan tabel " + tableName + ".");
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengupdate data hewan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sembelih(int id, String tableName) {
        if (koneksi == null) {
            System.out.println("Koneksi ke database tidak tersedia. Tidak dapat menyembelih hewan.");
            return;
        }

        try {
            String selectSql = "SELECT * FROM " + tableName + " WHERE id = ?";
            PreparedStatement selectStatement = koneksi.prepareStatement(selectSql);
            selectStatement.setInt(1, id);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String pemilik = resultSet.getString("pemilik");
                String jenis = resultSet.getString("jenis");
                int jumlah = resultSet.getInt("jumlah");
                String asal = resultSet.getString("asal");
                String tanggaldisembelih = getCurrentDateTime();

                if (tableName.equals("mamalia")) {
                    double berat = resultSet.getDouble("berat");
                    double beratDaging = (berat * 0.9) * jumlah; // 90% dari berat
                    String insertSql = "INSERT INTO daging_mamalia (pemilik, jenis, berat_daging, asal, jumlah, tanggal_disembelih) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement insertStatement = koneksi.prepareStatement(insertSql);
                    insertStatement.setString(1, pemilik);
                    insertStatement.setString(2, jenis);
                    insertStatement.setDouble(3, beratDaging);
                    insertStatement.setString(4, asal);
                    insertStatement.setInt(5, jumlah);
                    insertStatement.setString(6, tanggaldisembelih);

                    insertStatement.executeUpdate();
                } else if (tableName.equals("unggas")) {
                    int permintaanPotong = resultSet.getInt("permintaan_potong");
                    int totalPotongan = jumlah * permintaanPotong;
                    String insertSql = "INSERT INTO daging_unggas (pemilik, jenis, asal, total_potongan, jumlah, tanggal_disembelih) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement insertStatement = koneksi.prepareStatement(insertSql);
                    insertStatement.setString(1, pemilik);
                    insertStatement.setString(2, jenis);
                    insertStatement.setString(3, asal);
                    insertStatement.setInt(4, totalPotongan);
                    insertStatement.setInt(5, jumlah);
                    insertStatement.setString(6, tanggaldisembelih);

                    insertStatement.executeUpdate();
                }

                String deleteSql = "DELETE FROM " + tableName + " WHERE id = ?";
                PreparedStatement deleteStatement = koneksi.prepareStatement(deleteSql);
                deleteStatement.setInt(1, id);
                deleteStatement.executeUpdate();

                System.out.println("Hewan dengan ID " + id + " telah disembelih.");
            } else {
                System.out.println("Hewan dengan ID " + id + " tidak ditemukan di tabel " + tableName + ".");
            }
        } catch (SQLException e) {
            System.out.println("Gagal menyembelih hewan: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        RPHManager manager = new RPHManager();
        RPHManager.connectDatabase();
        manager.startMonitoring();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Sistem Manajemen RPH ===");
            System.out.println("1. Tambah Hewan ke Daftar Sementara");
            System.out.println("2. Tampilkan Daftar Hewan Sementara");
            System.out.println("3. Proses Daftar Hewan ke Database");
            System.out.println("4. Cek Hewan");
            System.out.println("5. Beri Makan Hewan");
            System.out.println("6. Update Hewan");
            System.out.println("7. Sembelih Hewan");
            System.out.println("8. Keluar");
            System.out.println("-----------------------------");
            System.out.print("Pilih menu: ");

            int pilihan = scanner.nextInt();
            scanner.nextLine();

            switch (pilihan) {
                case 1 -> {
                    try {
                        System.out.print("Masukkan pemilik: ");
                        String pemilik = scanner.nextLine();
                        System.out.print("Masukkan kategori (mamalia/unggas): ");
                        String kategori = scanner.nextLine();
                        System.out.print("Masukkan jenis: ");
                        String jenis = scanner.nextLine();
                        RPHManager.validasiJenis(jenis, kategori);

                        System.out.print("Masukkan asal: ");
                        String asal = scanner.nextLine();
                        System.out.print("Masukkan jumlah: ");
                        int jumlah = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Masukkan tanggal untuk (YYYY-MM-DD): ");
                        LocalDate tanggalUntuk = LocalDate.parse(scanner.nextLine());

                        HewanPotong hewan;
                        if (kategori.equalsIgnoreCase("mamalia")) {
                            System.out.print("Masukkan berat: ");
                            double berat = scanner.nextDouble();
                            scanner.nextLine();
                            hewan = new Mamalia(pemilik, jenis, berat, asal, jumlah, tanggalUntuk, 100);
                        } else {
                            System.out.print("Masukkan permintaan potong: ");
                            int permintaanPotong = scanner.nextInt();
                            scanner.nextLine();
                            hewan = new Unggas(pemilik, jenis, asal, jumlah, permintaanPotong, tanggalUntuk, 100);
                        }
                        manager.tambahKeDaftar(hewan);
                    } catch (InvalidJenisException e) {
                        System.out.println(e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Terjadi kesalahan: " + e.getMessage());
                    }
                }
                case 2 -> manager.tampilkanDaftarHewan();
                case 3 -> manager.prosesDaftarKeDatabase();
                case 4 -> {
                        System.out.print("Masukkan kategori (mamalia/unggas): ");
                        String kategori = scanner.nextLine();
                        
                        manager.cekHewan(kategori);
                }
                case 5 -> {
                    
                        System.out.print("Masukkan kategori (mamalia/unggas): ");
                        String kategori = scanner.nextLine();
                        manager.beriMakan(kategori);
                    
                }
                case 6 -> {
                    try {
                        System.out.print("Masukkan kategori (mamalia/unggas): ");
                        String kategori = scanner.nextLine();

                        System.out.print("Masukkan ID hewan yang ingin diupdate: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Masukkan pemilik baru: ");
                        String pemilik = scanner.nextLine();
                        System.out.print("Masukkan jenis baru: ");
                        String jenisBaru = scanner.nextLine();
                        System.out.print("Masukkan asal baru: ");
                        String asal = scanner.nextLine();
                        System.out.print("Masukkan jumlah baru: ");
                        int jumlah = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Masukkan tanggal untuk baru (YYYY-MM-DD): ");
                        LocalDate tanggalUntuk = LocalDate.parse(scanner.nextLine());

                        if (kategori.equalsIgnoreCase("mamalia")) {
                            System.out.print("Masukkan berat baru: ");
                            double berat = scanner.nextDouble();
                            scanner.nextLine();
                            Mamalia mamalia = new Mamalia(pemilik, jenisBaru, berat, asal, jumlah, tanggalUntuk, 100);
                            manager.update(id, "mamalia", mamalia);
                        } else {
                            System.out.print("Masukkan permintaan potong baru: ");
                            int permintaanPotong = scanner.nextInt();
                            scanner.nextLine();
                            Unggas unggas = new Unggas(pemilik, jenisBaru, asal, jumlah, permintaanPotong, tanggalUntuk, 100);
                            manager.update(id, "unggas", unggas);
                        }
                        
                    } catch (Exception e) {
                        System.out.println("Terjadi kesalahan: " + e.getMessage());
                    }
                }
                case 7 -> {
                    try {
                        System.out.print("Masukkan kategori (mamalia/unggas): ");
                        String kategori = scanner.nextLine();
                        
                        System.out.print("Masukkan ID hewan yang ingin disembelih: ");
                        int id = scanner.nextInt();
                        manager.sembelih(id, kategori);
                    } catch (Exception e) {
                        System.out.println("Terjadi kesalahan: " + e.getMessage());
                    }
                }
                case 8 -> {
                    System.out.println("Keluar dari sistem. Terima kasih.");
                    running = false;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }

        scanner.close();
    }
}
