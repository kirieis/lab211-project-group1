package dao;

import util.DBUtil;
import model.Batch;
import java.sql.*;
import java.util.*;

public class BatchDAO {

    // FIFO theo hạn dùng
    public List<Batch> findFIFO(String medicineId) throws Exception {
        List<Batch> list = new ArrayList<>();

        String sql = """
            SELECT * FROM Batch
            WHERE medicine_id = ? AND quantity > 0
            ORDER BY expiry_date ASC
        """;

        PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
        ps.setString(1, medicineId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Batch b = new Batch();
            b.setBatchId(rs.getInt("batch_id"));
            b.setMedicineId(rs.getString("medicine_id"));
            b.setQuantity(rs.getInt("quantity"));
            b.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
            list.add(b);
        }
        return list;
    }

    public void updateQuantity(int batchId, int quantity) throws Exception {
        String sql = "UPDATE Batch SET quantity=? WHERE batch_id=?";
        PreparedStatement ps = DBUtil.getConnection().prepareStatement(sql);
        ps.setInt(1, quantity);
        ps.setInt(2, batchId);
        ps.executeUpdate();
    }
}
