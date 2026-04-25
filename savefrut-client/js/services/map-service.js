// Esta variable será nuestro mapa de RECOJO
let recojoMap;

// Esta variable será nuestro mapa de DOMICILIO
let domicilioMap;
let domicilioMarker;

/**
 * ¡FUNCIÓN GLOBAL DE GOOGLE MAPS!
 * Llamada por el <script> de Google en el HTML.
 */
function initMap() {
    const limaCoords = { lat: -12.046374, lng: -77.042793 };
    
    // 1. Crea el mapa de RECOJO (estático)
    const recojoMapDiv = document.getElementById("mapa-recojo");
    if (recojoMapDiv) {
        recojoMap = new google.maps.Map(recojoMapDiv, {
            zoom: 15, center: limaCoords, disableDefaultUI: true, zoomControl: true
        });
    }

    // 2. Crea el mapa de DOMICILIO (interactivo)
    const domicilioMapDiv = document.getElementById("mapa-domicilio");
    if (domicilioMapDiv) {
        domicilioMap = new google.maps.Map(domicilioMapDiv, {
            zoom: 12, center: limaCoords
        });

        // 3. Crea el PIN 📍 que el usuario puede mover
        domicilioMarker = new google.maps.Marker({
            position: limaCoords, map: domicilioMap, draggable: true,
            title: "Arrastra este pin a tu dirección"
        });

        // 4. Llama al "callback" en envio.js cuando el pin se mueva
        domicilioMap.addListener('center_changed', () => {
            if (domicilioMarker) domicilioMarker.setPosition(domicilioMap.getCenter());
        });
        
        domicilioMarker.addListener('dragend', () => {
            if (typeof onPinMoveCallback === 'function') {
                const newCoords = domicilioMarker.getPosition();
                onPinMoveCallback(newCoords.lat(), newCoords.lng());
            }
        });
    }

    // --- ¡ESTE ES EL ARREGLO! ---
    // 5. Ahora que los mapas SÍ existen, 
    //    llama a la función principal en envio.js para cargar todo lo demás.
    if (typeof iniciarPaginaEnvio === 'function') {
        iniciarPaginaEnvio();
    } else {
        console.error("No se encontró la función iniciarPaginaEnvio() en envio.js");
    }
    // --- FIN DEL ARREGLO ---
}

/**
 * Actualiza el mapa de RECOJO (el estático)
 */
async function fillPickupInfo(vendedorId) {
    if (!vendedorId) return;
    try {
        // (API_URL debe estar definido en el script principal, envio.js)
        const response = await fetch(`${API_URL}/vendedores/${vendedorId}`);
        if (!response.ok) throw new Error("Tienda no encontrada");
        const tienda = await response.json();
        
        document.getElementById("recojo-tienda-nombre").innerText = tienda.razonSocial;
        document.getElementById("recojo-tienda-direccion").innerText = tienda.direccion || 'Dirección no disponible';
        document.getElementById("recojo-tienda-horario").innerText = `Horario: ${tienda.horario || 'No especificado'}`;
        
        if (tienda.latitud && tienda.longitud && typeof google !== 'undefined') {
            const lat = parseFloat(tienda.latitud);
            const lng = parseFloat(tienda.longitud);
            const coords = { lat, lng };

            // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
            // 1. Espera a que el mapa 'recojoMap' esté listo
            const map = await waitForMap('recojo');
            
            // 2. Ahora que SÍ existe, úsalo
            map.setCenter(coords);
            new google.maps.Marker({ 
                position: coords, 
                map: map, 
                title: tienda.razonSocial 
            });
            // --- FIN DE LA CORRECCIÓN ---

        } else {
            document.getElementById('mapa-recojo').classList.add('hidden');
        }
    } catch (error) {
        console.error(error);
        document.getElementById("recojo-tienda-unica").innerHTML = `<p class="text-red-500">Error al cargar datos de la tienda.</p>`;
    }
}

/**
 * Llama a la API de Geocodificación Inversa de Google
 */
async function reverseGeocode(coords) {
    if (typeof google === 'undefined') {
        console.error("Google Maps SDK no está listo.");
        return;
    }
    const geocoder = new google.maps.Geocoder();
    const response = await geocoder.geocode({ location: coords });
    if (response.results[0]) {
        return response.results[0].formatted_address;
    } else {
        throw new Error("No se encontraron resultados para esta ubicación.");
    }
}

/**
 * ==========================================================
 * ¡NUEVA FUNCIÓN DE ESPERA!
 * Esta función soluciona el error de "mapa no inicializado".
 * ==========================================================
 */
function waitForMap(mapType) {
    return new Promise((resolve) => {
        const interval = setInterval(() => {
            const mapObject = (mapType === 'recojo') ? recojoMap : domicilioMap;
            // Si el objeto 'mapObject' (ej. recojoMap) ya fue creado por initMap...
            if (mapObject) {
                clearInterval(interval); // Deja de esperar
                resolve(mapObject); // Devuelve el mapa
            }
            // ...si no, espera 100ms y vuelve a chequear.
        }, 100);
    });
}
// ==========================================================