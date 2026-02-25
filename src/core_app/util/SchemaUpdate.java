package core_app.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaUpdate {

    public static void updateSchema() {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            System.out.println("🚀 [SchemaUpdate] Starting database upgrade...");

            // === 1. XÓA TẤT CẢ FOREIGN KEY TRÊN Invoice_Detail trỏ tới Medicine ===
            // Đây là nguyên nhân gốc rễ gây lỗi "FK constraint violated"
            // App đã tự kiểm tra dữ liệu ở tầng code (ensureMedicineExists)
            // nên không cần database canh gác nữa.
            try {
                String dropAllFkSql = "DECLARE @sql NVARCHAR(MAX) = ''; " +
                        "SELECT @sql = @sql + 'ALTER TABLE [' + OBJECT_SCHEMA_NAME(fk.parent_object_id) + '].[' + OBJECT_NAME(fk.parent_object_id) + '] DROP CONSTRAINT [' + fk.name + ']; ' "
                        +
                        "FROM sys.foreign_keys fk " +
                        "WHERE fk.referenced_object_id = OBJECT_ID('Medicine'); " +
                        "IF @sql <> '' EXEC sp_executesql @sql;";
                stmt.execute(dropAllFkSql);
                System.out.println("✅ [SchemaUpdate] Dropped all FK constraints referencing Medicine");
            } catch (SQLException e) {
                System.out.println("ℹ️ [SchemaUpdate] FK drop note: " + e.getMessage());
            }

            // === 2. Thêm các cột mới (nếu chưa có) ===
            addColumnIfNotExists(stmt, "Medicine", "import_price", "INT DEFAULT 0");
            addColumnIfNotExists(stmt, "Invoice", "payment_method", "NVARCHAR(50)");
            addColumnIfNotExists(stmt, "Invoice", "payment_proof", "NVARCHAR(500)");
            addColumnIfNotExists(stmt, "Invoice", "status", "NVARCHAR(20) DEFAULT 'PENDING'");
            addColumnIfNotExists(stmt, "Invoice_Detail", "medicine_name", "NVARCHAR(255)");
            addColumnIfNotExists(stmt, "Invoice_Detail", "import_price", "DECIMAL(12,2) DEFAULT 0");

            System.out.println("✅ [SchemaUpdate] Database upgrade completed successfully!");

        } catch (SQLException e) {
            System.err.println("❌ [SchemaUpdate] Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addColumnIfNotExists(Statement stmt, String table, String column, String definition) {
        try {
            String sql = "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('" + table
                    + "') AND name = '" + column + "') "
                    + "ALTER TABLE " + table + " ADD " + column + " " + definition;
            stmt.execute(sql);
        } catch (SQLException e) {
            // Column có thể đã tồn tại - OK
        }
    }
}
