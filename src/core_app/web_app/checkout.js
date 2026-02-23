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

// ==== DISCOUNT CODES ====
const DISCOUNT_CODES = {
    'A7CO': 7,
    'ANHSITA': 10,
    'THANHHOA': 36
};

let appliedDiscount = 0;
let subtotal = 0;
let isLoggedIn = false;
let currentUser = null;
let pollingInterval = null;

// Tạo mã đơn hàng duy nhất cho nội dung CK
function generateTransferCode() {
    const ts = Date.now().toString(36).toUpperCase().slice(-5);
    const rnd = Math.random().toString(36).substring(2, 5).toUpperCase();
    return `GHP${ts}${rnd}`;
}
const transferCode = generateTransferCode();

// ==== CHECK LOGIN ====
async function checkLoginStatus() {
    try {
        const res = await fetch('api/auth-status');
        const data = await res.json();
        isLoggedIn = data.isLoggedIn;
        currentUser = data.isLoggedIn ? data : null;
        updateDiscountUI();
    } catch (e) {
        isLoggedIn = false;
        currentUser = null;
        updateDiscountUI();
    }
}

function updateDiscountUI() {
    const discountRow = document.querySelector('.discount-row');
    if (!discountRow) return;
    if (!isLoggedIn) {
        discountRow.innerHTML = `
            <div style="width: 100%; text-align: center; padding: 12px; background: #fef3c7; border-radius: 8px; border: 1px solid #fcd34d;">
                <span style="color: #92400e;">🔒 <a href="login.html" style="color: #1d4ed8; text-decoration: underline; font-weight: 600;">Đăng nhập</a> để sử dụng mã giảm giá</span>
            </div>
        `;
    }
}

// ==== RENDER ITEMS ====
function renderOrderItems() {
    const cart = getCart();
    const container = $("orderItems");
    if (!container) return;

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

// ==== UPDATE PRICES (Safe - no crash) ====
function updatePrices() {
    const subtotalEl = $("subtotal");
    const totalPriceEl = $("totalPrice");
    const discountLineEl = $("discountLine");
    const discountAmountEl = $("discountAmount");

    if (subtotalEl) subtotalEl.textContent = formatVND(subtotal);

    const discountAmount = Math.round(subtotal * appliedDiscount / 100);
    const total = subtotal - discountAmount;

    if (appliedDiscount > 0 && discountLineEl) {
        discountLineEl.style.display = "flex";
        if (discountAmountEl) discountAmountEl.textContent = `-${formatVND(discountAmount)} (${appliedDiscount}%)`;
    } else if (discountLineEl) {
        discountLineEl.style.display = "none";
    }

    if (totalPriceEl) totalPriceEl.textContent = formatVND(total);

    // Cập nhật thông tin chuyển khoản
    const transferAmountEl = $("transferAmount");
    const transferContentEl = $("transferContent");
    if (transferAmountEl) transferAmountEl.textContent = formatVND(total);
    if (transferContentEl) transferContentEl.textContent = transferCode;
}

function getFinalAmount() {
    return subtotal - Math.round(subtotal * appliedDiscount / 100);
}

// ==== APPLY DISCOUNT ====
function applyDiscount() {
    const messageEl = $("discountMessage");
    if (!messageEl) return;

    if (!isLoggedIn) {
        messageEl.innerHTML = '<span style="color: var(--danger);">⛔ Vui lòng đăng nhập để sử dụng mã giảm giá!</span>';
        return;
    }

    const codeEl = $("discountCode");
    if (!codeEl) return;
    const code = codeEl.value.trim().toUpperCase();

    if (!code) {
        messageEl.innerHTML = '<span style="color: var(--danger);">Vui lòng nhập mã giảm giá!</span>';
        return;
    }

    // Admin code
    if (code === 'ADMIN_FREE') {
        const userRole = (currentUser && currentUser.role) ? currentUser.role.toUpperCase() : "";
        if (isLoggedIn && userRole === 'ADMIN') {
            appliedDiscount = 100;
            messageEl.innerHTML = `<span style="color: #6366f1; font-weight: 700;">🛡️ ADMIN: Chế độ bán hàng 0đ đã kích hoạt!</span>`;
            updatePrices();
            return;
        } else {
            messageEl.innerHTML = `<span style="color: var(--danger);">⛔ Mã này chỉ dành riêng cho Admin!</span>`;
            return;
        }
    }

    if (DISCOUNT_CODES[code]) {
        appliedDiscount = DISCOUNT_CODES[code];
        messageEl.innerHTML = `<span style="color: green; font-weight: 600;">✓ Áp dụng mã "${code}" thành công! Giảm ${appliedDiscount}%</span>`;
        updatePrices();
    } else {
        messageEl.innerHTML = '<span style="color: var(--danger);">✗ Mã giảm giá không hợp lệ!</span>';
        appliedDiscount = 0;
        updatePrices();
    }
}

// ==== CONFIRM PAYMENT (Bấm nút → tạo đơn → spinner chờ xác nhận) ====
async function confirmPayment() {
    const cart = getCart();
    if (cart.length === 0) {
        alert('Giỏ hàng trống!');
        return;
    }

    const btnConfirm = $("btnConfirmPayment");
    btnConfirm.disabled = true;
    btnConfirm.classList.add("btn-disabled");
    btnConfirm.innerHTML = "⏳ ĐANG XỬ LÝ...";

    const finalAmount = getFinalAmount();

    try {
        // Gửi lên server tạo invoice
        const res = await fetch('api/sepay/create-order', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ totalAmount: finalAmount, items: cart })
        });

        const result = await res.json();

        if (res.ok && result.invoiceId) {
            const invoiceId = result.invoiceId;
            // Hiện spinner chờ SePay xác nhận
            showWaitingSpinner(invoiceId, finalAmount);
            // Bắt đầu polling - cứ 5 giây check 1 lần
            startPollingOrderStatus(invoiceId);
            // Xóa giỏ hàng
            clearCart();
        } else {
            alert('Lỗi: ' + (result.message || 'Không thể tạo đơn hàng'));
            resetButton(btnConfirm);
        }
    } catch (e) {
        console.error("Payment error:", e);
        alert('Lỗi kết nối! Vui lòng kiểm tra mạng và thử lại.');
        resetButton(btnConfirm);
    }
}

function resetButton(btn) {
    btn.disabled = false;
    btn.classList.remove("btn-disabled");
    btn.innerHTML = "✅ ĐÃ CHUYỂN TIỀN - XÁC NHẬN";
}

// ==== SPINNER - Chờ SePay ====
function showWaitingSpinner(invoiceId, amount) {
    const modal = $("successModal");
    const content = modal.querySelector(".success-content");

    content.innerHTML = `
        <div style="text-align: center; padding: 30px 20px;">
            <div style="margin: 0 auto 25px; width: 80px; height: 80px; border: 6px solid #e2e8f0; border-top-color: #6366f1; border-radius: 50%; animation: spin 1s linear infinite;"></div>
            <h2 style="color: #1e40af; margin-bottom: 12px; font-size: 22px;">⏳ Đang chờ xác nhận thanh toán...</h2>
            <p style="color: #64748b; margin-bottom: 20px; font-size: 15px;">Hệ thống đang tự động kiểm tra giao dịch của bạn qua SePay.</p>
            
            <div style="background: linear-gradient(135deg, #f0fdf4, #ecfdf5); border: 1px solid #86efac; border-radius: 16px; padding: 20px; margin-bottom: 20px;">
                <p style="color: #166534; font-weight: 800; font-size: 20px; margin: 0 0 8px;">💸 ${formatVND(amount)}</p>
                <p style="color: #166534; font-size: 14px; margin: 0;">Mã đơn hàng: <b>#${invoiceId}</b></p>
            </div>

            <div style="display: flex; align-items: center; gap: 8px; justify-content: center; color: #94a3b8; font-size: 13px;">
                <div style="width: 8px; height: 8px; background: #22c55e; border-radius: 50%; animation: pulse 1.5s ease-in-out infinite;"></div>
                Đang lắng nghe phản hồi từ SePay... Vui lòng không đóng trang này.
            </div>
        </div>
    `;
    modal.classList.add("active");
}

// ==== POLLING ====
function startPollingOrderStatus(invoiceId) {
    let tried = 0;
    pollingInterval = setInterval(async () => {
        tried++;
        try {
            const res = await fetch(`api/orders/status?invoiceId=${invoiceId}`);
            if (res.ok) {
                const data = await res.json();
                if (data.status === 'PAID') {
                    clearInterval(pollingInterval);
                    showPaymentSuccess(invoiceId);
                    return;
                }
            }
        } catch (e) {
            // Bỏ qua lỗi mạng tạm thời
        }
        // Tự dừng sau 5 phút (60 x 5s)
        if (tried >= 60) {
            clearInterval(pollingInterval);
            showPaymentTimeout(invoiceId);
        }
    }, 5000);
}

// ==== THÀNH CÔNG ====
function showPaymentSuccess(invoiceId) {
    const modal = $("successModal");
    const content = modal.querySelector(".success-content");

    content.innerHTML = `
        <div style="text-align: center; padding: 30px 20px;">
            <div style="font-size: 72px; margin-bottom: 15px;">🎉</div>
            <h2 style="color: #166534; margin-bottom: 12px;">Thanh toán thành công!</h2>
            <p style="color: #64748b; margin-bottom: 8px;">Đơn hàng <b>#${invoiceId}</b> đã được xác nhận.</p>
            <p style="color: #64748b; margin-bottom: 25px;">Cảm ơn bạn đã mua hàng tại <b>Github Pharmacy</b>! 💚</p>
            <div style="display: flex; gap: 12px;">
                <button class="btn btn--ghost" onclick="window.location.href='home.html'" style="flex: 1; justify-content: center; padding: 14px;">🏠 Trang Chủ</button>
                <button class="btn btn--primary" onclick="window.location.href='profile.html'" style="flex: 1; justify-content: center; padding: 14px;">📋 Lịch sử đơn</button>
            </div>
        </div>
    `;
}

// ==== QUÁ THỜI GIAN ====
function showPaymentTimeout(invoiceId) {
    const modal = $("successModal");
    const content = modal.querySelector(".success-content");

    content.innerHTML = `
        <div style="text-align: center; padding: 30px 20px;">
            <div style="font-size: 72px; margin-bottom: 15px;">⏰</div>
            <h2 style="color: #b45309; margin-bottom: 12px;">Chưa nhận được giao dịch</h2>
            <p style="color: #64748b; margin-bottom: 25px;">Hệ thống chưa nhận được xác nhận trong 5 phút cho đơn <b>#${invoiceId}</b>.<br>Nếu bạn đã chuyển tiền, hãy kiểm tra lại sau ít phút hoặc liên hệ hỗ trợ.</p>
            <div style="display: flex; gap: 12px;">
                <button class="btn btn--ghost" onclick="window.location.href='home.html'" style="flex: 1; justify-content: center; padding: 14px;">🏠 Trang Chủ</button>
                <button class="btn btn--primary" onclick="window.location.href='profile.html'" style="flex: 1; justify-content: center; padding: 14px;">📋 Kiểm tra đơn</button>
            </div>
        </div>
    `;
}

// ==== KHỞI ĐỘNG ====
document.addEventListener('DOMContentLoaded', async () => {
    await checkLoginStatus();
    renderOrderItems();

    const btnApply = $("btnApplyDiscount");
    if (btnApply) btnApply.addEventListener('click', applyDiscount);

    const discountInput = $("discountCode");
    if (discountInput) {
        discountInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') applyDiscount();
        });
    }

    const btnConfirm = $("btnConfirmPayment");
    if (btnConfirm) btnConfirm.addEventListener('click', confirmPayment);
});
