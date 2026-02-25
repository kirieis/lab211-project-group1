package core_app.dao;

import core_app.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MedicineDAO {

    /**
     * Đảm bảo thuốc tồn tại trong DB.
     * Nếu chưa có -> tự tạo bản ghi placeholder.
     * Dùng connection riêng (auto-commit) để tránh conflict với transaction chính.
     */
    public void ensureMedicineExists(String id, String name, double sellingPrice) {
        if (id == null || id.trim().isEmpty()) {
            System.err.println("⚠️ [MedicineDAO] Skipping null/empty medicine ID");
            return;
        }
        id = id.trim();

        String checkSql = "SELECT COUNT(*) FROM Medicine WHERE medicine_id = ?";
        String insertSql = "INSERT INTO Medicine (medicine_id, name, price, quantity, unit, dosage_form, manufacturer) "
                + "VALUES (?, ?, ?, 100, N'Viên', N'Tablet', N'Unknown')";

        // Dùng connection RIÊNG (auto-commit = true) để INSERT được commit ngay lập tức
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true); // QUAN TRỌNG: commit ngay, không chờ transaction

            // 1. Kiểm tra đã tồn tại chưa
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, id);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("✅ [MedicineDAO] Medicine exists: " + id);
                    return; // Đã có rồi, không cần insert
                }
            }

            // 2. Chưa có -> Insert
            System.out.println("🔧 [MedicineDAO] Creating medicine: " + id + " | " + name);
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, id);
                // Cắt tên nếu quá dài
                String safeName = (name != null && name.length() > 100) ? name.substring(0, 100) : name;
                insertPs.setString(2, safeName != null ? safeName : "Unknown");
                insertPs.setInt(3, (int) sellingPrice);
                int rows = insertPs.executeUpdate();
                System.out.println("✅ [MedicineDAO] Created medicine: " + id + " (rows=" + rows + ")");
            }

        } catch (SQLException e) {
            // IN LỖI RA ĐỂ BIẾT TẠI SAO FAIL
            System.err.println("🔴 [MedicineDAO] FAILED to ensure medicine: " + id);
            System.err.println("🔴 [MedicineDAO] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Overload: chấp nhận connection từ bên ngoài (dùng trong InvoiceDAO)
     */
    public void ensureMedicineExists(Connection existingConn, String id, String name, double sellingPrice) {
        // Gọi lại method chính - luôn dùng connection riêng để đảm bảo commit
        ensureMedicineExists(id, name, sellingPrice);
    }

    public double getImportPrice(String medicineId) {
        String sql = "SELECT price FROM Medicine WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicineId != null ? medicineId.trim() : "");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Fallback: 70% of selling price as estimated import price
                return rs.getInt("price") * 0.7;
            }
        } catch (SQLException e) {
            System.err.println("⚠️ [MedicineDAO] getImportPrice error: " + e.getMessage());
        }
        return 0;
    }
}
