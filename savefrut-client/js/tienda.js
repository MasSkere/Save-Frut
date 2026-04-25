// --- Configuración ---
const API_URL = "http://localhost:8091/api";

// --- Selectores de Elementos ---
const tituloContainer = document.getElementById("tienda-titulo");
const productosContainer = document.getElementById("productos-container");
const cartCountIcon = document.getElementById("carrito-contador");

// --- Función Principal ---
document.addEventListener("DOMContentLoaded", () => {
    // 1. Leemos el ID de la tienda desde la URL (ej: tienda.html?id=1)
    const params = new URLSearchParams(window.location.search);
    const vendedorId = params.get('id');

    if (!vendedorId) {
        tituloContainer.innerText = "Tienda no encontrada";
        productosContainer.innerHTML = "<p class='text-red-500'>No se especificó una tienda.</p>";
        return;
    }

    // 2. Cargamos todo
    cargarTituloTienda(vendedorId);
    cargarProductosPorVendedor(vendedorId);
    updateCartIcon(); // Actualiza el ícono al cargar
});

/**
 * Carga el nombre de la tienda y lo pone en el H1
 */
async function cargarTituloTienda(id) {
    try {
        // Llamamos al endpoint que nos da los datos de 1 vendedor
        const response = await fetch(`${API_URL}/vendedores/${id}`); 
        if (!response.ok) throw new Error("Tienda no encontrada");
        
        const tienda = await response.json();
        tituloContainer.innerText = tienda.razonSocial; // Solo muestra el nombre
    } catch (error) {
        tituloContainer.innerText = "Error al cargar tienda";
        console.error("Error al cargar título:", error);
    }
}

/**
 * Carga los productos de esa tienda
 */
async function cargarProductosPorVendedor(id) {
    if (!productosContainer) return;
    
    productosContainer.innerHTML = `<p class="text-gray-500 col-span-4 text-center">Cargando productos...</p>`;

    try {
        // Llamamos al endpoint de productos por vendedor
        const response = await fetch(`${API_URL}/vendedores/${id}/productos`);
        if (!response.ok) throw new Error("Error en el servidor");
        
        const productos = await response.json();

        if (productos.length === 0) {
            productosContainer.innerHTML = "<p class='col-span-4 text-center text-gray-500'>Esta tienda no tiene productos disponibles.</p>";
            return;
        }

        productosContainer.innerHTML = ""; // Limpiamos
        
        productos.forEach(producto => {
            const { etiqueta, color } = getEtiquetaVencimiento(producto.diasParaVencer);
            
            const card = document.createElement("div");
            card.className = "bg-white border rounded-lg shadow-sm overflow-hidden flex flex-col";
            card.innerHTML = `
                <div class="relative">
                    <img src="${producto.imagenUrl || `https://placehold.co/400x300/EBF5EE/2F855A?text=${producto.nombre.replace(' ', '+')}`}" 
                         alt="${producto.nombre}" 
                         class="w-full h-48 object-cover">
                    <span class="absolute top-2 left-2 px-2 py-1 rounded text-xs font-semibold text-white ${color}">
                        ${etiqueta}
                    </span>
                </div>
                <div class="p-4 flex-grow">
                    <!-- Ya no mostramos el nombre del vendedor, porque estamos en su tienda -->
                    <h3 class="text-lg font-semibold text-gray-800">${producto.nombre}</h3>
                    <div class="mt-2">
                        <span class="text-2xl font-bold text-green-600">S/ ${formatCurrency(producto.precioDescuento)}</span>
                        <span class="text-lg text-gray-400 line-through ml-2">S/ ${formatCurrency(producto.precioNormal)}</span>
                        <span class="text-sm text-gray-500 ml-1">/ ${producto.nombreMedicion}</span>
                    </div>
                </div>
                <div class="p-4 pt-0">
                    <button class="add-to-cart-btn w-full bg-green-600 text-white py-2 rounded-lg font-medium hover:bg-green-700 transition-colors">
                        Añadir al carrito
                    </button>
                </div>
            `;
            
            // --- Lógica de Carrito ---
            const addButton = card.querySelector(".add-to-cart-btn");
            addButton.addEventListener("click", () => {
                const item = {
                    id: producto.id,
                    nombre: producto.nombre,
                    precio: producto.precioDescuento,
                    imagen: producto.imagenUrl || `https://placehold.co/400x300/EBF5EE/2F855A?text=${producto.nombre.replace(' ', '+')}`,
                    cantidad: 1,
                    vendedorId: producto.vendedorId,
                    nombreVendedor: producto.nombreVendedor
                };
                addToCart(item);
            });
            // --- Fin Lógica de Carrito ---

            productosContainer.appendChild(card);
        });

    } catch (error) {
        productosContainer.innerHTML = `<p class="text-red-500 col-span-4">Error al cargar los productos. Por favor, intenta más tarde.</p>`;
        console.error("Error al cargar productos:", error);
    }
}

// ==========================================================
// FUNCIONES "Cerebro" del Carrito (Copiadas de app.js)
// ==========================================================

function getCart() {
    const cart = localStorage.getItem('savefrut_cart');
    return cart ? JSON.parse(cart) : [];
}

function saveCart(cart) {
    localStorage.setItem('savefrut_cart', JSON.stringify(cart));
}

function addToCart(itemToAdd) {
    let cart = getCart();
    const itemExistente = cart.find(item => item.id === itemToAdd.id);
    if (itemExistente) {
        itemExistente.cantidad += 1;
    } else {
        cart.push(itemToAdd);
    }
    saveCart(cart);
    updateCartIcon();
    alert(`${itemToAdd.nombre} ha sido añadido al carrito.`);
}

function updateCartIcon() {
    const cart = getCart();
    const totalItems = cart.reduce((total, item) => total + item.cantidad, 0); 
    if (cartCountIcon) {
        if (totalItems > 0) {
            cartCountIcon.textContent = totalItems;
            cartCountIcon.style.display = "flex";
        } else {
            cartCountIcon.style.display = "none";
        }
    }
}

// ==========================================================
// FUNCIONES DE UTILIDAD (Copiadas)
// ==========================================================

function getEtiquetaVencimiento(diasParaVencer) {
    if (diasParaVencer === 0) {
        return { etiqueta: "Vence hoy", color: "bg-red-500" };
    } else if (diasParaVencer === 1) {
        return { etiqueta: "Vence mañana", color: "bg-yellow-500" };
    } else {
        return { etiqueta: `Vence en ${diasParaVencer} días`, color: "bg-blue-500" };
    }
}

function formatCurrency(number) {
    return Number(number).toFixed(2);
}
