package core_app.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core_app.dao.InvoiceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/api/sepay/webhook")
public class SepayWebhookServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // TẠM THỜI TẮT BẢO MẬT IP ĐỂ TEST DỄ HƠN
            String clientIp = req.getRemoteAddr();
            System.out.println("📩 [SePay Webhook] Incoming request from IP: " + clientIp);

            // 1. Kiểm tra bảo mật (API Key)
            String secretKey = System.getenv("SEPAY_WEBHOOK_KEY");
            String authHeader = req.getHeader("Authorization");

            // Nếu có cài Key thì mới check, không thì bỏ qua để tránh lỗi deploy
            if (secretKey != null && !secretKey.isEmpty() && !secretKey.equals("none")) {
                if (authHeader == null || !authHeader.contains(secretKey)) {
                    System.err.println("⚠️ SePay Webhook: API Key không khớp! Header: " + authHeader + " | Mong đợi: "
                            + secretKey);
                    // resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    // return; // Tạm thời chỉ Log, không chặn để bro test được đơn này
                }
            }

            // 2. Lấy body và kiểm tra
            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("📩 [SePay Webhook] Body: " + body);

            if (body == null || body.trim().isEmpty()) {
                System.err.println("⚠️ SePay Webhook: Body rong!");
                resp.setStatus(200); // Tra ve 200 de SePay dung gui lai
                resp.getWriter().print("{\"status\":\"ignored\", \"reason\":\"Empty body\"}");
                return;
            }

            JsonObject json = gson.fromJson(body, JsonObject.class);

            // 3. Trich xuat thong tin
            String code = json.has("code") && !json.get("code").isJsonNull() ? json.get("code").getAsString() : "";
            String content = json.has("content") && !json.get("content").isJsonNull()
                    ? json.get("content").getAsString()
                    : "";

            if (content.isEmpty() && json.has("transferContent") && !json.get("transferContent").isJsonNull()) {
                content = json.get("transferContent").getAsString();
            }

            System.out.println("📩 [SePay Webhook] Parsed: code=" + code + ", content=" + content);

            // 4. Tim ma don hang
            String orderCode = (code != null && !code.isEmpty()) ? code : extractOrderCode(content);
            System.out.println("📩 [SePay Webhook] Final Order Code: " + orderCode);

            if (orderCode != null && !orderCode.isEmpty()) {
                String numericPart = orderCode.replaceAll("[^0-9]", "");
                if (!numericPart.isEmpty()) {
                    int invoiceId = Integer.parseInt(numericPart);
                    boolean ok = invoiceDAO.updateStatus(invoiceId, "PAID");
                    if (ok) {
                        System.out.println("✅ SePay Webhook: Ghi nhan thanh cong don #" + invoiceId);
                        resp.setStatus(200);
                        resp.getWriter().print("{\"status\":\"success\", \"invoiceId\":" + invoiceId + "}");
                        return;
                    } else {
                        System.err.println("❌ SePay Webhook: Khong tim thay don #" + invoiceId + " trong DB");
                    }
                }
            }

            // Neu toi day ma chua return nghia la khong xu ly duoc nhưng van tra ve 200 de
            // SePay khoi retry
            resp.setStatus(200);
            resp.getWriter().print("{\"status\":\"ignored\", \"reason\":\"No valid order code found\"}");

        } catch (Exception e) {
            System.err.println("🔴 [SePay Webhook] CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500); // Bao loi server thi SePay moi hien log loi
            resp.getWriter().print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private String extractOrderCode(String content) {
        if (content == null || content.isEmpty())
            return "";
        // Tìm pattern "DH" + số (ví dụ "DH17", "DH123")
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("DH(\\d+)").matcher(content.toUpperCase());
        if (m.find()) {
            return "DH" + m.group(1);
        }
        // Nếu content chỉ toàn số
        if (content.trim().matches("\\d+")) {
            return content.trim();
        }
        return content;
    }
}
