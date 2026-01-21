package controller;

import service.InventoryService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/api/sell")
public class POSServlet extends HttpServlet {

    private InventoryService service = new InventoryService();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String med = req.getParameter("medicineId");
            int qty = Integer.parseInt(req.getParameter("quantity"));
            service.sell(med, qty);
            resp.setStatus(200);
        } catch (Exception e) {
            resp.setStatus(400);
        }
    }
}
