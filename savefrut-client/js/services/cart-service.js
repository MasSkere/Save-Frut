// --- Constantes de Reglas de Negocio ---
const SHIPPING_FEE = 10.00;
const VARIETY_FEE = 5.00;

// ===================================
// "Cerebro" del Carrito (localStorage)
// ===================================

function getCart() {
    const cart = localStorage.getItem('savefrut_cart');
    return cart ? JSON.parse(cart) : [];
}

function saveCart(cart) {
    localStorage.setItem('savefrut_cart', JSON.stringify(cart));
}

function addToCart(itemToAdd, alertar = true) {
    let cart = getCart();
    const itemExistente = cart.find(item => item.id === itemToAdd.id);
    if (itemExistente) {
        itemExistente.cantidad += 1;
    } else {
        cart.push(itemToAdd);
    }
    saveCart(cart);
    updateCartIcon();
    if (alertar) {
        alert(`${itemToAdd.nombre} ha sido añadido al carrito.`);
    }
}

function updateCartIcon() {
    const cart = getCart();
    const totalItems = cart.reduce((total, item) => total + item.cantidad, 0); 
    const cartCountIcon = document.getElementById("carrito-contador");
    if (cartCountIcon) {
        if (totalItems > 0) {
            cartCountIcon.textContent = totalItems;
            cartCountIcon.style.display = "flex";
        } else {
            cartCountIcon.style.display = "none";
        }
    }
}

// ===================================
// Lógica de Reglas de Negocio
// ===================================

function checkStoreVariety(cart) {
    const sellerIds = new Set(cart.map(item => item.vendedorId));
    const isMultiple = sellerIds.size > 1;
    return {
        isMultiple: isMultiple,
        singleStoreId: isMultiple ? null : cart[0].vendedorId 
    };
}

function updateSummary(cart, isMultiple, shippingCost) {
    const summarySubtotal = document.getElementById("summary-subtotal");
    const varietyFeeRow = document.getElementById("variety-fee-row");
    const summaryVarietyFee = document.getElementById("summary-variety-fee");
    const summaryShipping = document.getElementById("summary-shipping");
    const summaryTotal = document.getElementById("summary-total");
    const paymentButton = document.getElementById("payment-button");

    const subtotal = cart.reduce((total, item) => total + (item.precio * item.cantidad), 0);
    let varietyFee = 0;

    if (isMultiple) {
        varietyFee = VARIETY_FEE;
        if (varietyFeeRow) varietyFeeRow.classList.remove('hidden');
        if (summaryVarietyFee) summaryVarietyFee.innerText = `S/ ${formatCurrency(varietyFee)}`;
    } else {
        if (varietyFeeRow) varietyFeeRow.classList.add('hidden');
    }
    
    if (typeof shippingCost === 'undefined') {
        shippingCost = (cart.length > 0) ? SHIPPING_FEE : 0;
    }
    
    const total = subtotal + varietyFee + shippingCost;
    
    if (summarySubtotal) summarySubtotal.innerText = `S/ ${formatCurrency(subtotal)}`;
    if (summaryShipping) summaryShipping.innerText = `S/ ${formatCurrency(shippingCost)}`;
    if (summaryTotal) summaryTotal.innerText = `S/ ${formatCurrency(total)}`;
    
    if (paymentButton) paymentButton.dataset.envio = shippingCost;
}

// ===================================
// Funciones de Utilidad
// ===================================

function formatCurrency(number) {
    if (typeof number !== 'number') {
        number = parseFloat(number) || 0;
    }
    return number.toFixed(2);
}

function getToken() {
    return localStorage.getItem("savefrut_token");
}

function getEtiquetaVencimiento(diasParaVencer) {
    if (diasParaVencer === 0) {
        return { etiqueta: "Vence hoy", color: "bg-red-500" };
    } else if (diasParaVencer === 1) {
        return { etiqueta: "Vence mañana", color: "bg-yellow-500" };
    } else {
        return { etiqueta: `Vence en ${diasParaVencer} días`, color: "bg-blue-500" };
    }
}