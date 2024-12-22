public interface OperasiDatabase {
    void create(Object obj);
    void cekHewan(String tableName);
    void beriMakan(String tableName);
    void update(int id, String tableName, Object obj);
    void sembelih(int id, String tableName);
}
