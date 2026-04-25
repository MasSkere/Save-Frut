// --- Configuración ---
const API_URL = "http://localhost:8091/api";

// --- Selectores del DOM ---
const cartTitle = document.getElementById("cart-title");
const itemsContainer = document.getElementById("cart-items-container");
const emptyCartMessage = document.getElementById("empty-cart-message");
const summarySubtotal = document.getElementById("summary-subtotal");
const varietyFeeRow = document.getElementById("variety-fee-row");
const summaryVarietyFee = document.getElementById("summary-variety-fee");
const summaryShipping = document.getElementById("summary-shipping"); // (Este lo seteamos en envio.html)
const summaryTotal = document.getElementById("summary-total");
const checkoutButton = document.getElementById("checkout-button");

// --- Constantes de Reglas de Negocio ---
// (Estas constantes se usarán en envio.js)
const SHIPPING_FEE = 10.00;
const VARIETY_FEE = 5.00;

// --- Funciones del Carrito (Copiadas de app.js) ---
function getCart() {
    const cart = localStorage.getItem('savefrut_cart');
    return cart ? JSON.parse(cart) : [];
}

function saveCart(cart) {
    localStorage.setItem('savefrut_cart', JSON.stringify(cart));
}

function formatCurrency(number) {
    return Number(number).toFixed(2);
}

// --- Lógica Principal de la Página ---
document.addEventListener("DOMContentLoaded", () => {
    renderCart(); // Dibuja el carrito
    
    // --- ¡CAMBIO IMPORTANTE! ---
    // El botón "Proceder al Pago" en esta página (Mi Carrito)
    // ahora solo navega al siguiente paso: "Envío".
    checkoutButton.addEventListener("click", () => {
        
        // (Opcional: validamos que el carrito no esté vacío)
        const cart = getCart();
        if (cart.length === 0) {
            alert("Tu carrito está vacío.");
            return;
        }
        
        // Simplemente navega a la página de envío
        window.location.href = "envio.html"; 
    });
    // --- FIN DEL CAMBIO ---
});

/**
 * Dibuja toda la página del carrito y calcula los totales.
 */
function renderCart() {
    const cart = getCart();

    if (cart.length === 0) {
        emptyCartMessage.style.display = 'block';
        itemsContainer.innerHTML = ''; // Limpiamos
        cartTitle.innerText = "Tu Carrito (0 items)";
    } else {
        emptyCartMessage.style.display = 'none';
        itemsContainer.innerHTML = ''; // Limpiamos
        
        cart.forEach(item => {
            const itemHTML = createCartItemHTML(item);
            itemsContainer.innerHTML += itemHTML;
        });

        // Hacemos que los botones de +/- y eliminar funcionen
        addEventListenersToCartItems();
    }
    
    // (Paso final) Calcular y mostrar el resumen
    calculateSummary(cart);
}

/**
 * Crea el HTML para un solo item en el carrito
 */
function createCartItemHTML(item) {
    const subtotal = item.precio * item.cantidad;
    return `
        <div class="flex items-center py-4" data-id="${item.id}">
            <img src="${item.imagen}" alt="${item.nombre}" class="w-16 h-16 rounded-lg object-cover mr-4">
            <div class="flex-grow">
                <h3 class="font-semibold text-gray-800">${item.nombre}</h3>
                <!-- ¡TU REGLA DE NEGOCIO! (Mostrar Tienda) -->
                <p class="text-sm text-gray-500">${item.nombreVendedor}</p>
                <span class="text-sm text-gray-500">S/ ${formatCurrency(item.precio)}</span>
            </div>
            <div class="flex items-center space-x-3">
                <button class="qty-decrease bg-gray-200 w-6 h-6 rounded-full">-</button>
                <span class="font-medium">${item.cantidad}</span>
                <button class="qty-increase bg-gray-200 w-6 h-6 rounded-full">+</button>
            </div>
            <span class="font-semibold text-gray-800 w-20 text-right">S/ ${formatCurrency(subtotal)}</span>
            <button class="remove-item text-gray-400 hover:text-red-500 ml-4">
                <i data-feather="trash-2" class="w-5 h-5"></i>
            </button>
        </div>
    `;
}

/**
 * Añade funcionalidad a los botones de + / - / eliminar
 */
function addEventListenersToCartItems() {
    document.querySelectorAll(".remove-item").forEach(button => {
        button.addEventListener("click", (e) => {
            const itemId = e.currentTarget.closest("[data-id]").dataset.id;
            removeFromCart(Number(itemId));
        });
    });
    
    document.querySelectorAll(".qty-increase").forEach(button => {
        button.addEventListener("click", (e) => {
            const itemId = e.currentTarget.closest("[data-id]").dataset.id;
            updateQuantity(Number(itemId), 1);
        });
    });
    
    document.querySelectorAll(".qty-decrease").forEach(button => {
        button.addEventListener("click", (e) => {
            const itemId = e.currentTarget.closest("[data-id]").dataset.id;
            updateQuantity(Number(itemId), -1);
        });
    });
    
    // Activa los iconos de pluma (basura)
    feather.replace();
}

/**
 * Actualiza la cantidad de un item en el carrito
 */
function updateQuantity(id, change) {
    let cart = getCart();
    const item = cart.find(i => i.id === id);
    
    if (item) {
        item.cantidad += change;
        if (item.cantidad <= 0) {
            // Si la cantidad es 0 o menos, elimina el item
            cart = cart.filter(i => i.id !== id);
        }
    }
    
    saveCart(cart);
    renderCart(); // Vuelve a dibujar todo
}

/**
 * Elimina un item del carrito
 */
function removeFromCart(id) {
    let cart = getCart();
    cart = cart.filter(i => i.id !== id);
    saveCart(cart);
    renderCart(); // Vuelve a dibujar todo
}

/**
 * Calcula el Subtotal, Tarifa de Variedad, y Total
 * (¡MODIFICADO! Ya no calcula el envío aquí)
 */
function calculateSummary(cart) {
    const subtotal = cart.reduce((total, item) => total + (item.precio * item.cantidad), 0);
    
    // Regla de Tarifa por Variedad
    const sellerIds = new Set(cart.map(item => item.vendedorId));
    let fee = 0;
    if (sellerIds.size > 1) {
        fee = VARIETY_FEE;
        varietyFeeRow.style.display = 'flex';
        summaryVarietyFee.innerText = `S/ ${formatCurrency(fee)}`;
    } else {
        varietyFeeRow.style.display = 'none';
    }
    
    // --- El total en esta página es solo Subtotal + Tarifa ---
    const total = subtotal + fee;
    
    // Actualizamos el HTML
    summarySubtotal.innerText = `S/ ${formatCurrency(subtotal)}`;
    summaryTotal.innerText = `S/ ${formatCurrency(total)}`;
    
	    
    const totalItems = cart.reduce((sum, item) => sum + item.cantidad, 0);
    cartTitle.innerText = `Tu Carrito (${totalItems} items)`;
}