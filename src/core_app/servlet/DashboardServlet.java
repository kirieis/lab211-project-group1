package core_app.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import core_app.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String sql = """
            SELECT 
                m.medicine_id,
                m.name,
                b.batch_id,
                b.expiry_date,
                b.quantity_available,
                b.import_price
            FROM Medicine m
            JOIN Batch b ON m.medicine_id = b.medicine_id
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            out.print("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) out.print(",");
                first = false;

                out.print("""
                    {
                      "id":"%s",
                      "name":"%s",
                      "batchId":"%s",
                      "expiry":"%s",
                      "quantity":%d,
                      "price":%f
                    }
                """.formatted(
                        rs.getString("medicine_id"),
                        rs.getString("name"),
                        rs.getString("batch_id"),
                        rs.getDate("expiry_date"),
                        rs.getInt("quantity_available"),
                        rs.getDouble("import_price")
                ));
            }
            out.print("]");

        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
