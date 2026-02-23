package core_app.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core_app.dao.InvoiceDAO;
import core_app.model.Customer;
import core_app.model.Invoice;
import core_app.model.InvoiceDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/sepay/create-order")
public class SepayOrderServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }

        System.out.println("[SEPAY] Order Request Body: " + sb.toString());

        try {
            JsonObject jsonBody = gson.fromJson(sb.toString(), JsonObject.class);

            Invoice inv = new Invoice();
            inv.setPaymentMethod("SEPAY");
            inv.setStatus("PENDING");

            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                Customer c = (Customer) session.getAttribute("currentUser");
                inv.setCustomerId(c.getCustomerId());
                System.out.println("[SEPAY] Customer ID: " + c.getCustomerId());
            } else {
                System.out.println("[SEPAY] WARNING: No logged-in customer");
            }

            // Parse items
            if (jsonBody.has("items")) {
                jsonBody.getAsJsonArray("items").forEach(itemElement -> {
                    JsonObject itemObj = itemElement.getAsJsonObject();
                    String id = itemObj.has("id") ? itemObj.get("id").getAsString() : "UNKNOWN";
                    String name = itemObj.has("name") ? itemObj.get("name").getAsString() : "Unknown";
                    int qty = itemObj.has("qty") ? itemObj.get("qty").getAsInt() : 1;
                    double price = itemObj.has("price") ? itemObj.get("price").getAsDouble() : 0;
                    System.out.println("[SEPAY] Item: " + id + " | " + name + " | qty=" + qty + " | price=" + price);
                    inv.addInvoiceDetail(new InvoiceDetail(id, name, qty, price));
                });
            }

            // Set totalAmount LAST (preserve discounted price from frontend)
            double totalAmount = jsonBody.has("totalAmount") ? jsonBody.get("totalAmount").getAsDouble() : 0;
            inv.setTotalAmount(totalAmount);
            System.out.println("[SEPAY] Total Amount: " + totalAmount);

            // Save to DB
            int invoiceId = invoiceDAO.addInvoice(inv);
            System.out.println("[SEPAY] Invoice ID: " + invoiceId);

            if (invoiceId == -1) {
                resp.setStatus(500);
                out.print("{\"status\":\"error\", \"message\":\"Loi luu hoa don vao database. Kiem tra SQL Server.\"}");
            } else {
                out.print("{\"status\":\"success\", \"invoiceId\":" + invoiceId + "}");
            }

        } catch (Exception e) {
            System.err.println("[SEPAY] ERROR: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Unknown error";
            out.print("{\"status\":\"error\", \"message\":\"" + msg + "\"}");
        }
    }
}
