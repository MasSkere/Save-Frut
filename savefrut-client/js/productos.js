// --- Configuración ---
const API_URL = "http://localhost:8091/api";

// --- Selectores de Elementos ---
const tituloContainer = document.getElementById("categoria-titulo");
const productosContainer = document.getElementById("productos-container");
const cartCountIcon = document.getElementById("carrito-contador"); // ¡NUEVO!

// --- Función Principal (Se ejecuta al cargar la página) ---
document.addEventListener("DOMContentLoaded", () => {
    // 1. Leemos el ID de la categoría desde la URL
    const params = new URLSearchParams(window.location.search);
    const categoriaId = params.get('id');

    if (!categoriaId) {
        tituloContainer.innerText = "Categoría no encontrada";
        productosContainer.innerHTML = "<p class='text-red-500'>No se especificó una categoría.</p>";
        return;
    }

    cargarTituloCategoria(categoriaId);
    cargarProductosPorCategoria(categoriaId);
    updateCartIcon(); // ¡NUEVO! Actualiza el ícono al cargar
});

// --- Cargar Título (¡CORREGIDO!) ---
async function cargarTituloCategoria(id) {
    try {
        const response = await fetch(`${API_URL}/categorias/${id}`);
        if (!response.ok) throw new Error("Categoría no encontrada");
        
        const categoria = await response.json();
        // --- ¡CAMBIO AQUÍ! ---
        tituloContainer.innerText = categoria.nombre; // Solo muestra el nombre
    } catch (error) {
        tituloContainer.innerText = "Error al cargar título";
        console.error("Error al cargar título:", error);
    }
}

// --- Cargar Productos por Categoría (¡MODIFICADO!) ---
async function cargarProductosPorCategoria(id) {
    if (!productosContainer) return;
    
    productosContainer.innerHTML = `<p class="text-gray-500 col-span-4 text-center">Cargando productos...</p>`;

    try {
        const response = await fetch(`${API_URL}/productos/categoria/${id}`);
        if (!response.ok) throw new Error("Error en el servidor");
        
        const productos = await response.json();

        if (productos.length === 0) {
            productosContainer.innerHTML = "<p class='col-span-4 text-center text-gray-500'>No hay productos disponibles en esta categoría.</p>";
            return;
        }

        productosContainer.innerHTML = ""; // Limpiamos
        
        // Reutilizamos la misma lógica de `app.js` para crear las tarjetas
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
                    <p class="text-sm text-gray-500">${producto.nombreVendedor}</p>
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

            // --- ¡LÓGICA DE CARRITO AÑADIDA! ---
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
            // --- FIN LÓGICA DE CARRITO ---

            productosContainer.appendChild(card);
        });

    } catch (error) {
        productosContainer.innerHTML = `<p class="text-red-500 col-span-4">Error al cargar los productos. Por favor, intenta más tarde.</p>`;
        console.error("Error al cargar productos:", error);
    }
}

// --- Funciones de Utilidad (Copiadas de app.js) ---

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

// ==========================================================
// ¡¡NUEVO!! FUNCIONES "Cerebro" del Carrito (Copiadas de app.js)
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
