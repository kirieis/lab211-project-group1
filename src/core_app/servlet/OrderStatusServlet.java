package core_app.servlet;

import core_app.dao.InvoiceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/orders/status")
public class OrderStatusServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String invoiceIdStr = req.getParameter("invoiceId");
        if (invoiceIdStr == null || invoiceIdStr.isEmpty()) {
            resp.setStatus(400);
            out.print("{\"status\":\"error\", \"message\":\"Missing invoiceId\"}");
            return;
        }

        try {
            int invoiceId = Integer.parseInt(invoiceIdStr.trim());
            String status = invoiceDAO.getInvoiceStatus(invoiceId);

            if (status != null) {
                out.print("{\"status\":\"" + status + "\", \"invoiceId\":" + invoiceId + "}");
            } else {
                resp.setStatus(404);
                out.print("{\"status\":\"NOT_FOUND\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            out.print("{\"status\":\"error\", \"message\":\"Invalid invoiceId\"}");
        }
    }
}
