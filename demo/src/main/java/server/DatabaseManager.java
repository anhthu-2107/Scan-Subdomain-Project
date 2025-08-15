package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseManager {
    private final String url = "jdbc:mysql://localhost:3306/subdomain_scanner?useUnicode=true&characterEncoding=UTF-8";
    private final String user = "root";
    private final String password = "123456";

    public DatabaseManager() {
        try {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                String sql = "CREATE TABLE IF NOT EXISTS keys (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "aes_key VARCHAR(255) NOT NULL," +
                        "aes_iv VARCHAR(255) NOT NULL)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.executeUpdate();

                sql = "INSERT IGNORE INTO keys (id, aes_key, aes_iv) VALUES (1, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, "1234567890123456");
                stmt.setString(2, "1234567890123456");
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getKeyInfo() {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT aes_key, aes_iv FROM keys WHERE id = 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[] { rs.getString("aes_key"), rs.getString("aes_iv") };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[] { "1234567890123456", "1234567890123456" };
    }
}
