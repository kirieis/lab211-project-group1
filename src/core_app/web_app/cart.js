const $ = (id) => document.getElementById(id);

function formatVND(n) {
  return n.toLocaleString("vi-VN") + "đ";
}

function getCart() {
  return JSON.parse(localStorage.getItem("cart") || "[]");
}

function saveCart(cart) {
  localStorage.setItem("cart", JSON.stringify(cart));
  renderCart();
}

function getUnitConversionInfo(item) {
  if (item.isLiquid) return '';
  if (item.unit !== 'Viên') return '';

  const vienPerVi = item.vienPerVi || 10;
  const viPerHop = item.viPerHop || 3;
  const vienPerHop = vienPerVi * viPerHop;

  if (item.qty >= vienPerHop) {
    const hop = Math.floor(item.qty / vienPerHop);
    const remainder = item.qty % vienPerHop;
    if (remainder === 0) {
      return `<span class="unit-convert">= ${hop} Hộp</span>`;
    } else {
      return `<span class="unit-convert">= ${hop} Hộp + ${remainder} Viên</span>`;
    }
  } else if (item.qty >= vienPerVi) {
    const vi = Math.floor(item.qty / vienPerVi);
    const remainder = item.qty % vienPerVi;
    if (remainder === 0) {
      return `<span class="unit-convert">= ${vi} Vỉ</span>`;
    } else {
      return `<span class="unit-convert">= ${vi} Vỉ + ${remainder} Viên</span>`;
    }
  }
  return '';
}

function renderCart() {
  const cart = getCart();
  const list = $("cartList");

  if (cart.length === 0) {
    list.innerHTML = `
      <div style="text-align: center; padding: 40px;">
        <p class="muted">Giỏ hàng của bạn đang trống</p>
        <a href="home.html" class="btn btn--primary" style="margin-top: 20px;">Quay lại mua sắm</a>
      </div>
    `;
    $("subtotal").textContent = "0đ";
    $("totalPrice").textContent = "0đ";
    return;
  }

  let subtotal = 0;
  list.innerHTML = cart.map((item, index) => {
    const itemTotal = item.price * item.qty;
    subtotal += itemTotal;
    const conversionInfo = getUnitConversionInfo(item);

    return `
      <div class="cart-item">
        <div class="cart-item__info">
          <h3>${item.name}</h3>
          <p>Mã: ${item.id} • Đơn vị: <b>${item.unit}</b></p>
        </div>
        <div class="cart-item__qty">
          <button class="qty-btn" onclick="updateQty(${index}, -1)">-</button>
          <input type="number" class="qty-input" value="${item.qty}" min="1" onchange="setQty(${index}, this.value)" />
          <button class="qty-btn" onclick="updateQty(${index}, 1)">+</button>
          ${conversionInfo}
        </div>
        <div style="font-weight: 700; color: var(--primary); min-width: 100px; text-align: right;">
          ${formatVND(itemTotal)}
        </div>
        <button class="btn" style="color: var(--danger); padding: 8px;" onclick="removeItem(${index})">🗑</button>
      </div>
    `;
  }).join("");

  $("subtotal").textContent = formatVND(subtotal);
  $("totalPrice").textContent = formatVND(subtotal);
}

window.updateQty = (index, delta) => {
  const cart = getCart();
  cart[index].qty = Math.max(1, cart[index].qty + delta);
  saveCart(cart);
};

window.setQty = (index, value) => {
  const cart = getCart();
  const qty = parseInt(value) || 1;
  cart[index].qty = Math.max(1, qty);
  saveCart(cart);
};

window.removeItem = (index) => {
  const cart = getCart();
  cart.splice(index, 1);
  saveCart(cart);
};

// Checkout button
document.addEventListener('DOMContentLoaded', () => {
  const btnCheckout = $("btnCheckout");
  if (btnCheckout) {
    btnCheckout.addEventListener('click', () => {
      const cart = getCart();
      if (cart.length === 0) {
        alert('Giỏ hàng trống!');
        return;
      }
      window.location.href = 'checkout.html';
    });
  }
});

// Boot
renderCart();
