// --- Configuración ---
const API_URL = "http://localhost:8091/api";

// --- Selectores de Elementos ---
const categoriasContainer = document.getElementById("categorias-container");
const productosContainer = document.getElementById("productos-container");
const tiendasContainer = document.getElementById("tiendas-container");
const navProfileButton = document.getElementById("nav-profile-button");
const searchInput = document.getElementById("search-input");
const searchResults = document.getElementById("search-results");
const cartCountIcon = document.getElementById("carrito-contador");

// --- Función Principal ---
document.addEventListener("DOMContentLoaded", () => {
    actualizarBotonPerfil();
    cargarCategorias();
    cargarProductos();
    cargarTiendas();
    initSearch();
	updateCartIcon();
});

// ==========================================================
// FUNCIÓN 5: LÓGICA DE LOGIN/LOGOUT
// ==========================================================
function actualizarBotonPerfil() {
    const userEmail = localStorage.getItem("savefrut_user_email");
    const navProfileButton = document.getElementById("nav-profile-button");
    const logoutButton = document.getElementById("logout-button"); // Asumiendo que añadiste el botón

    if (userEmail && navProfileButton && logoutButton) {
        navProfileButton.textContent = `Hola, ${userEmail.split('@')[0]}`;
        navProfileButton.href = "#"; 
        logoutButton.classList.remove("hidden");
        logoutButton.addEventListener("click", () => {
            localStorage.removeItem("savefrut_token");
            localStorage.removeItem("savefrut_user_email");
            alert("¡Sesión cerrada! Volviendo al inicio.");
            window.location.href = "index.html";
        });
    }
}

// ==========================================================
// FUNCIÓN NUEVA: Búsqueda Dinámica
// ==========================================================
function initSearch() {
    if (!searchInput || !searchResults) return;

    // Escucha cada vez que el usuario teclea algo
    searchInput.addEventListener('input', async (e) => {
        const termino = e.target.value.trim();

        // Si el término es muy corto, oculta los resultados
        if (termino.length < 2) {
            searchResults.innerHTML = "";
            searchResults.classList.add("hidden");
            return;
        }

        try {
            // Llama al nuevo endpoint de la API
            const response = await fetch(`${API_URL}/productos/buscar?term=${termino}`);
            if (!response.ok) throw new Error("Error en la búsqueda");

            const productos = await response.json();

            // Si no hay resultados, muestra un mensaje
            if (productos.length === 0) {
                searchResults.innerHTML = `<div class="p-3 text-sm text-gray-500">No se encontraron resultados.</div>`;
                searchResults.classList.remove("hidden");
                return;
            }

            // Dibuja la lista de resultados
            searchResults.innerHTML = productos.map(producto => `
                <a href="#" class="flex items-center p-3 hover:bg-gray-100 border-b">
                    <img src="${producto.imagenUrl || `https://placehold.co/40x40/EBF5EE/2F855A?text=${producto.nombre[0]}`}" 
                         class="w-10 h-10 rounded-lg object-cover mr-3">
                    <div>
                        <p class="font-medium text-sm text-gray-800">${producto.nombre}</p>
                        <p class="text-sm text-green-600">S/ ${formatCurrency(producto.precioDescuento)}</p>
                    </div>
                </a>
            `).join("");
            
            searchResults.classList.remove("hidden");

        } catch (error) {
            console.error("Error en búsqueda:", error);
            searchResults.innerHTML = `<div class="p-3 text-sm text-red-500">Error al buscar.</div>`;
            searchResults.classList.remove("hidden");
        }
    });

    // Cierra el dropdown si se hace clic fuera
    document.addEventListener('click', (e) => {
        if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
            searchResults.classList.add("hidden");
        }
    });
}

// ==========================================================
// FUNCIÓN 1: CARGAR CATEGORÍAS (Sin cambios)
// ==========================================================
async function cargarCategorias() {
    if (!categoriasContainer) return;
    
    try {
        const response = await fetch(`${API_URL}/categorias`);
        if (!response.ok) throw new Error("Error en el servidor");
        
        const categorias = await response.json();
        
        if (categorias.length === 0) {
            categoriasContainer.innerHTML = "<p>No hay categorías disponibles.</p>";
            return;
        }
        
        categoriasContainer.innerHTML = categorias.map(categoria => `
            <a href="productos.html?id=${categoria.id}" 
               class="bg-gray-100 rounded-lg p-4 flex flex-col items-center justify-center hover:bg-gray-200 transition-colors">
                <span class="text-3xl">${categoria.iconoEmoji || '🍓'}</span>
                <span class="mt-2 font-medium text-gray-700">${categoria.nombre}</span>
            </a>
        `).join("");

    } catch (error) {
        categoriasContainer.innerHTML = `<p class="text-red-500">No se pudieron cargar las categorías.</p>`;
        console.error("Error al cargar categorías:", error);
    }
}

// ==========================================================
// FUNCIÓN 2: CARGAR PRODUCTOS (¡MODIFICADA!)
// ==========================================================
async function cargarProductos() {
    if (!productosContainer) return;
    
    productosContainer.innerHTML = `<p class="text-gray-500 col-span-4 text-center">Cargando productos...</p>`;

    try {
        const response = await fetch(`${API_URL}/productos`);
        if (!response.ok) throw new Error("Error en el servidor");
        
        const productos = await response.json();

        if (productos.length === 0) {
            productosContainer.innerHTML = "<p class='col-span-4 text-center text-gray-500'>No hay productos disponibles por ahora.</p>";
            return;
        }

        productosContainer.innerHTML = ""; // Limpiamos
        
        productos.forEach(producto => {
            const { etiqueta, color } = getEtiquetaVencimiento(producto.diasParaVencer);
            
            // Creamos un elemento de tarjeta (en lugar de innerHTML)
            // para poder añadirle un EventListener
            const card = document.createElement("div");
            card.className = "border rounded-lg shadow-sm overflow-hidden flex flex-col";
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

            // Añadimos el EventListener al botón de la tarjeta
            const addButton = card.querySelector(".add-to-cart-btn");
            addButton.addEventListener("click", () => {
                // Creamos el item que queremos guardar
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

            productosContainer.appendChild(card);
        });

    } catch (error) {
        productosContainer.innerHTML = `<p class="text-red-500 col-span-4 text-center">Error al cargar los productos. Por favor, intenta más tarde.</p>`;
        console.error("Error al cargar productos:", error);
    }
}

// ==========================================================
// FUNCIÓN 3: CARGAR TIENDAS (Sin cambios)
// ==========================================================
async function cargarTiendas() {
    if (!tiendasContainer) return;
    
    try {
        const response = await fetch(`${API_URL}/vendedores`);
        if (!response.ok) throw new Error("Error en el servidor");
        
        const tiendas = await response.json();
        
        if (tiendas.length === 0) {
            tiendasContainer.innerHTML = "<p>No hay tiendas disponibles.</p>";
            return;
        }

        tiendasContainer.innerHTML = tiendas.map(tienda => `
            <a href="tienda.html?id=${tienda.vendedorId}" 
               class="bg-white rounded-lg p-5 border flex items-center hover:bg-gray-50 transition-colors">
                <span class="bg-green-100 p-3 rounded-full">
                    <i data-feather="home" class="text-green-600"></i>
                </span>
                <div class="ml-4">
                    <h4 class="font-semibold text-gray-800">${tienda.razonSocial}</h4>
                    <p class="text-sm text-gray-600">${tienda.direccion}</p>
                    <span class="text-sm font-medium text-green-600 hover:underline mt-1 inline-block">Ver productos</span>
                </div>
            </a>
        `).join("");
        
        feather.replace();

    } catch (error) {
        tiendasContainer.innerHTML = `<p class="text-red-500 col-span-2 text-center">No se pudieron cargar las tiendas.</p>`;
        console.error("Error al cargar tiendas:", error);
    }
}

// ==========================================================
// FUNCIÓN 4: FUNCIONES DE UTILIDAD (Sin cambios)
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

// ==========================================================
// FUNCIONES NUEVAS: "Cerebro" del Carrito
// ==========================================================

/**
 * Obtiene el carrito desde localStorage.
 * @returns {Array} El carrito (una lista de items).
 */
function getCart() {
    const cart = localStorage.getItem('savefrut_cart');
    return cart ? JSON.parse(cart) : [];
}

/**
 * Guarda el carrito en localStorage.
 * @param {Array} cart - El array del carrito a guardar.
 */
function saveCart(cart) {
    localStorage.setItem('savefrut_cart', JSON.stringify(cart));
}

/**
 * Lógica para añadir un item al carrito.
 * @param {object} itemToAdd - El producto a añadir.
 */
function addToCart(itemToAdd) {
    let cart = getCart();
    
    // 1. Buscamos si el item ya existe en el carrito
    const itemExistente = cart.find(item => item.id === itemToAdd.id);

    if (itemExistente) {
        // 2. Si existe, solo aumentamos la cantidad
        itemExistente.cantidad += 1;
    } else {
        // 3. Si es nuevo, lo añadimos al array
        cart.push(itemToAdd);
    }

    // 4. Guardamos el carrito actualizado
    saveCart(cart);

    // 5. Actualizamos el ícono del carrito
    updateCartIcon();
    
    // (Opcional) Mostramos una alerta
    alert(`${itemToAdd.nombre} ha sido añadido al carrito.`);
}

/**
 * Actualiza el número en el ícono del carrito.
 */
function updateCartIcon() {
    const cart = getCart();
    // Sumamos la 'cantidad' de todos los items
    const totalItems = cart.reduce((total, item) => total + item.cantidad, 0); 

    if (totalItems > 0) {
        cartCountIcon.textContent = totalItems;
        cartCountIcon.style.display = "flex";
    } else {
        cartCountIcon.style.display = "none";
    }
}



function formatCurrency(number) {
    return Number(number).toFixed(2);
}