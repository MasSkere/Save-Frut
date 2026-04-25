// --- Configuración Global ---
const API_URL = "http://localhost:8091/api";

// --- Selectores del DOM ---
const provinciaSelect = document.getElementById("provincia-select");
const distritoSelect = document.getElementById("distrito-select");
const direccionInput = document.getElementById("direccion-input");
const radioDomicilio = document.getElementById("radio-domicilio");
const radioRecojo = document.getElementById("radio-recojo");
const formDomicilio = document.getElementById("form-domicilio");
const infoRecojo = document.getElementById("info-recojo");
const recojoTiendaUnica = document.getElementById("recojo-tienda-unica");
const recojoMultipleTiendas = document.getElementById("recojo-multiple-tiendas");
const paymentButton = document.getElementById("payment-button");
const multiStoreWarning = document.getElementById("multi-store-warning");

/**
 * ==========================================================
 * ¡FUNCIÓN DE ARRANQUE!
 * Esta es la nueva función principal.
 * Es llamada por initMap() DESPUÉS de que Google Maps ha cargado.
 * ==========================================================
 */
function iniciarPaginaEnvio() {
    
    // 1. ¡Guardia de Seguridad!
    const token = getToken(); // (Viene de cart-service.js)
    if (!token) {
        localStorage.setItem("redirect_url_after_login", "envio.html");
        alert("Debes iniciar sesión para continuar con tu envío.");
        window.location.href = "login.html";
        return; 
    }
    
    const cart = getCart(); // (Viene de cart-service.js)
    if (cart.length === 0) {
        alert("Tu carrito está vacío. Redirigiendo al inicio.");
        window.location.href = "index.html";
        return;
    }
    
    // 2. Lógica de Negocio
    const storeInfo = checkStoreVariety(cart); // (Viene de cart-service.js)
    
    if (storeInfo.isMultiple) {
        radioRecojo.disabled = true;
        radioDomicilio.checked = true;
        recojoTiendaUnica.classList.add('hidden');
        recojoMultipleTiendas.classList.remove('hidden');
        multiStoreWarning.classList.remove('hidden'); 
    } else {
        // Llama a la función del map-service
        fillPickupInfo(storeInfo.singleStoreId); // (Viene de map-service.js)
    }
    
    // 3. Carga de UI
    toggleForms();
    radioDomicilio.addEventListener("change", toggleForms);
    radioRecojo.addEventListener("change", toggleForms);

    loadProvincias();
    loadUserProfile(); 
    
    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // El botón "Ir a Pagar" ahora llama a la función de redirección.
    paymentButton.addEventListener("click", irAPaginaDePago);
}

// ==========================================================
// ¡FUNCIÓN CALLBACK PARA EL PIN-DROP!
// (Llamada desde map-service.js)
// ==========================================================
async function onPinMoveCallback(lat, lng) {
    try {
        const direccion = await reverseGeocode({ lat, lng }); // (Viene de map-service.js)
        direccionInput.value = direccion;
        direccionInput.classList.add("border-blue-500", "ring-2", "ring-blue-200"); 
    } catch (error) {
        console.error("Error en geocodificación inversa:", error);
        direccionInput.value = "No se pudo encontrar la dirección.";
    }
}
// ==========================================================


/**
 * Muestra/oculta los formularios según la selección
 */
function toggleForms() {
    const cart = getCart();
    const storeInfo = checkStoreVariety(cart); // (Viene de cart-service.js)

    if (radioDomicilio.checked) {
        formDomicilio.style.display = 'block'; 
        infoRecojo.style.display = 'none';
        updateSummary(cart, storeInfo.isMultiple, SHIPPING_FEE); // (Viene de cart-service.js)
    } else {
        formDomicilio.style.display = 'none';
        infoRecojo.style.display = 'block';
        updateSummary(cart, storeInfo.isMultiple, 0); // (Viene de cart-service.js)
    }
}

/**
 * Carga las provincias desde NUESTRA API
 */
async function loadProvincias() {
    try {
        const response = await fetch(`${API_URL}/ubicacion/provincias`);
        if (!response.ok) throw new Error("Error al cargar provincias");
        const provincias = await response.json();
        
        provinciaSelect.innerHTML = "<option value=''>Selecciona una provincia</option>";
        provincias.forEach(p => {
            provinciaSelect.innerHTML += `<option value="${p.provinciaId}">${p.nombre}</option>`;
        });
        
        provinciaSelect.addEventListener("change", () => {
            const provId = provinciaSelect.value;
            if (provId) { loadDistritos(provId); } 
            else { 
                distritoSelect.innerHTML = "<option>Selecciona una provincia</option>"; 
                distritoSelect.disabled = true; 
            }
        });

    } catch (error) {
        console.error(error);
        provinciaSelect.innerHTML = "<option>Error al cargar</option>";
    }
}

/**
 * Carga los distritos de una provincia desde NUESTRA API
 */
async function loadDistritos(provinciaId) {
    try {
        const response = await fetch(`${API_URL}/ubicacion/distritos/${provinciaId}`);
        if (!response.ok) throw new Error("Error al cargar distritos");
        const distritos = await response.json();
        
        distritoSelect.innerHTML = "<option value=''>Selecciona un distrito</option>";
        distritos.forEach(d => {
            distritoSelect.innerHTML += `<option value="${d.distritoId}">${d.nombre}</option>`;
        });
        distritoSelect.disabled = false;

    } catch (error) {
        console.error(error);
        distritoSelect.innerHTML = "<option>Error al cargar</option>";
    }
}

/**
 * Auto-llenado de dirección Y actualización del pin del mapa.
 */
async function loadUserProfile() {
    const token = getToken(); // (Viene de cart-service.js)
    
    try {
        const response = await fetch(`${API_URL}/clientes/me`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!response.ok) throw new Error("No se pudieron cargar los datos del cliente");
        
        const cliente = await response.json();
        
        // Auto-llenar formulario
        if (cliente.direccion) {
            direccionInput.value = cliente.direccion;
        }
        
        if (cliente.distrito) {
            const provId = cliente.distrito.provinciaId;
            provinciaSelect.value = provId;
            await loadDistritos(provId); 
            distritoSelect.value = cliente.distrito.distritoId;
        }

        // Si el cliente tiene lat/lng, actualiza el mapa de domicilio
        if (cliente.latitud && cliente.longitud) {
            const lat = parseFloat(cliente.latitud);
            const lng = parseFloat(cliente.longitud);
            const coords = { lat, lng };

            const map = await waitForMap('domicilio'); // (Viene de map-service.js)
            map.setCenter(coords);
            domicilioMarker.setPosition(coords);
        }

    } catch (error) {
        console.error("Error al cargar perfil de usuario:", error);
    }
}


/**
 * ==========================================================
 * ¡FUNCIÓN DE REDIRECCIÓN A PAGO!
 * Esta es la nueva función para el botón "Ir a Pagar".
 * ==========================================================
 */
function irAPaginaDePago() {
    
    let direccionFinal, distritoIdFinal, distritoNombreFinal;
    let costoEnvio = parseFloat(paymentButton.dataset.envio);
    
    // 1. Validamos qué opción de envío está seleccionada
    if (radioDomicilio.checked) {
        direccionFinal = direccionInput.value.trim();
        distritoIdFinal = distritoSelect.value;
        
        if (!direccionFinal || !distritoIdFinal) {
            alert("Por favor, completa tu dirección y distrito.");
            return;
        }
        distritoNombreFinal = distritoSelect.options[distritoSelect.selectedIndex].text;
    } else {
        direccionFinal = document.getElementById("recojo-tienda-direccion").innerText;
        distritoNombreFinal = "Recojo en Tienda";
        distritoIdFinal = 0; // 0 para Recojo
    }

    // 2. Creamos un objeto con la información del envío
    const shippingInfo = {
        direccion: direccionFinal,
        distritoId: distritoIdFinal,
        distritoNombre: distritoNombreFinal,
        costoEnvio: costoEnvio
    };

    // 3. Lo guardamos en localStorage para que la página 'pago.html' lo lea
    localStorage.setItem('savefrut_shipping_info', JSON.stringify(shippingInfo));

    // 4. Redirigimos a la página final de pago
    window.location.href = "pago.html";
}