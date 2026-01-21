package service;

import dao.BatchDAO;
import model.Batch;
import java.util.*;

public class InventoryService {

    private BatchDAO batchDAO = new BatchDAO();

    // Bán thuốc theo FIFO
    public synchronized void sell(String medicineId, int qty) throws Exception {
        List<Batch> batches = batchDAO.findFIFO(medicineId);

        for (Batch b : batches) {
            if (qty <= 0) break;

            int used = Math.min(b.getQuantity(), qty);
            b.setQuantity(b.getQuantity() - used);
            batchDAO.updateQuantity(b.getBatchId(), b.getQuantity());
            qty -= used;
        }

        if (qty > 0) {
            throw new Exception("Out of stock");
        }
    }
}
