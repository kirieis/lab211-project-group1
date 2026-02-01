package core_app.model;

public class InvoiceDetail {

    private String medicineId;
    private String medicineName;
    private int quantity;
    private double unitPrice;

    public InvoiceDetail(String medicineId, String medicineName,
                         int quantity, double unitPrice) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

}
