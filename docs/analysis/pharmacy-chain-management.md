# Pharmacy Chain Management System
(Mô hình Long Châu / Pharmacity)

## 📌 Project Overview
Hệ thống quản lý chuỗi nhà thuốc với các chức năng:
- Quản lý kho thuốc theo **Batch (FIFO)**
- Cảnh báo thuốc **sắp hết hạn / hết tồn**
- Bán hàng tại quầy (**POS Interface**)
- Simulator các máy POS gửi dữ liệu về Server trung tâm

---

## 👥 Actors
- **Admin / Chain Manager**
- **Store Manager / Pharmacist**
- **Customer**
- **POS Device (Simulator)**

---

## 🧩 Core Entities
- Medicine (Thuốc)
- Batch (Lô thuốc – Hạn sử dụng)
- Store (Cửa hàng)
- Inventory (Tồn kho theo Store + Batch)
- Customer
- Pharmacist (User)
- Invoice
- InvoiceItem
- POS_Device
- POS_Order

---

## 🔑 Business Rules
- Xuất kho theo **FIFO (Expiry Date tăng dần)**
- Không bán thuốc **đã hết hạn**
- Cảnh báo batch sắp hết hạn (configurable)
- Xử lý đồng thời nhiều POS → cần transaction

---

## 🗂 ER Diagram

```mermaid
erDiagram
    MEDICINE {
        int medicine_id PK
        string name
        string generic_name
        string dosage_form
        string strength
        string unit
        boolean prescription_required
        string manufacturer
        string barcode
    }

    BATCH {
        int batch_id PK
        int medicine_id FK
        string batch_number
        date manufacture_date
        date expiry_date
        int quantity_received
        float unit_cost
    }

    STORE {
        int store_id PK
        string name
        string address
        string phone
    }

    INVENTORY {
        int inventory_id PK
        int store_id FK
        int batch_id FK
        int quantity_available
        int reserved_quantity
    }

    USER {
        int user_id PK
        string username
        string full_name
        string role
    }

    CUSTOMER {
        int customer_id PK
        string name
        string phone
    }

    INVOICE {
        int invoice_id PK
        string invoice_number
        datetime created_at
        float total_amount
        string payment_method
        string status
    }

    INVOICE_ITEM {
        int invoice_item_id PK
        int invoice_id FK
        int medicine_id FK
        int batch_id FK
        int quantity
        float unit_price
        float discount
    }

    INVENTORY_TRANSACTION {
        int tx_id PK
        int store_id FK
        int batch_id FK
        string tx_type
        int quantity
        datetime created_at
    }

    SUPPLIER {
        int supplier_id PK
        string name
        string contact_info
    }

    POS_DEVICE {
        int pos_device_id PK
        int store_id FK
        string device_identifier
        datetime last_seen_at
    }

    %% Relationships
    MEDICINE ||--o{ BATCH : has
    SUPPLIER ||--o{ BATCH : supplies

    STORE ||--o{ INVENTORY : holds
    BATCH ||--o{ INVENTORY : stored_as

    STORE ||--o{ INVOICE : generates
    USER ||--o{ INVOICE : creates
    CUSTOMER ||--o{ INVOICE : owns

    INVOICE ||--o{ INVOICE_ITEM : contains
    MEDICINE ||--o{ INVOICE_ITEM : sold_as
    BATCH ||--o{ INVOICE_ITEM : taken_from

    STORE ||--o{ INVENTORY_TRANSACTION : records
    BATCH ||--o{ INVENTORY_TRANSACTION : affects

    STORE ||--o{ POS_DEVICE : has
