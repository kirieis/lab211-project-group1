const $ = (id) => document.getElementById(id);

function formatVND(n) {
    return n.toLocaleString("vi-VN") + "đ";
}

function getCart() {
    return JSON.parse(localStorage.getItem("cart") || "[]");
}

function clearCart() {
    localStorage.removeItem("cart");
}

// Discount codes
const DISCOUNT_CODES = {
    'A7CO': 7,
    'ANHSITA': 10
};

let appliedDiscount = 0;
let subtotal = 0;

function generateOrderId() {
    const timestamp = Date.now().toString(36).toUpperCase();
    const random = Math.random().toString(36).substring(2, 5).toUpperCase();
    return `ĐH-${timestamp}${random}`;
}

const orderId = generateOrderId();

function renderOrderItems() {
    const cart = getCart();
    const container = $("orderItems");

    if (cart.length === 0) {
        container.innerHTML = '<p class="muted">Không có sản phẩm nào trong đơn hàng</p>';
        return;
    }

    subtotal = 0;
    container.innerHTML = cart.map(item => {
        const itemTotal = item.price * item.qty;
        subtotal += itemTotal;
        return `
            <div class="order-item">
                <div>
                    <div style="font-weight: 600;">${item.name}</div>
                    <div style="font-size: 13px; color: var(--text-muted);">${item.qty} ${item.unit} × ${formatVND(item.price)}</div>
                </div>
                <div style="font-weight: 700; color: var(--primary);">
                    ${formatVND(itemTotal)}
                </div>
            </div>
        `;
    }).join("");

    updatePrices();
}

function updatePrices() {
    $("subtotal").textContent = formatVND(subtotal);

    const discountAmount = Math.round(subtotal * appliedDiscount / 100);
    const total = subtotal - discountAmount;

    if (appliedDiscount > 0) {
        $("discountLine").style.display = "flex";
        $("discountAmount").textContent = `-${formatVND(discountAmount)} (${appliedDiscount}%)`;
    } else {
        $("discountLine").style.display = "none";
    }

    $("totalPrice").textContent = formatVND(total);
    $("transferAmount").textContent = formatVND(total);
    $("transferContent").textContent = orderId;
}

function applyDiscount() {
    const code = $("discountCode").value.trim().toUpperCase();
    const messageEl = $("discountMessage");

    if (!code) {
        messageEl.innerHTML = '<span style="color: var(--danger);">Vui lòng nhập mã giảm giá!</span>';
        return;
    }

    if (DISCOUNT_CODES[code]) {
        appliedDiscount = DISCOUNT_CODES[code];
        messageEl.innerHTML = `<span style="color: var(--success);">✓ Áp dụng mã "${code}" thành công! Giảm ${appliedDiscount}%</span>`;
        updatePrices();
    } else {
        messageEl.innerHTML = '<span style="color: var(--danger);">✗ Mã giảm giá không hợp lệ!</span>';
        appliedDiscount = 0;
        updatePrices();
    }
}

function confirmPayment() {
    const cart = getCart();
    if (cart.length === 0) {
        alert('Đơn hàng trống!');
        return;
    }

    // Show success modal
    $("orderId").textContent = orderId;
    $("successModal").classList.add("active");

    // Clear cart
    clearCart();
}

window.goHome = () => {
    window.location.href = 'home.html';
};

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
    renderOrderItems();

    $("btnApplyDiscount").addEventListener('click', applyDiscount);

    $("discountCode").addEventListener('keydown', (e) => {
        if (e.key === 'Enter') applyDiscount();
    });

    $("btnConfirmPayment").addEventListener('click', confirmPayment);
});
