// --- Configuración ---
const API_URL = "http://localhost:8091/api";

// --- Selectores ---
// (Muchos selectores, pero están en cart-service.js, así que los re-obtenemos aquí)
const summarySubtotal = document.getElementById("summary-subtotal");
const varietyFeeRow = document.getElementById("variety-fee-row");
const summaryVarietyFee = document.getElementById("summary-variety-fee");
const summaryShipping = document.getElementById("summary-shipping");
const summaryTotal = document.getElementById("summary-total");
const itemsContainer = document.getElementById("items-container");
const downloadPdfBtn = document.getElementById("download-pdf-btn");

// --- Lógica Principal ---
document.addEventListener("DOMContentLoaded", () => {
    
    // 1. Obtenemos el ID de la orden que guardamos en pago.js
    const ordenId = localStorage.getItem('savefrut_last_order_id');
    const userEmail = localStorage.getItem('savefrut_user_email');
    const token = getToken(); // (De cart-service.js)

    if (!ordenId || !token) {
        alert("No se encontró una orden para mostrar.");
        window.location.href = "index.html";
        return;
    }

    // 2. Llenamos los datos que ya tenemos
    document.getElementById("orden-id").innerText = `#${ordenId.padStart(6, '0')}`;
    document.getElementById("cliente-email").innerText = userEmail;
    document.getElementById("orden-fecha").innerText = new Date().toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' });
    
    // 3. (Opcional pero recomendado) Llamamos a la API para traer los datos REALES de la orden
    //    (Por ahora, usaremos los datos de la orden que ya tenemos en la API)
    //    (Idealmente, /api/ordenes/{id} nos daría los items y totales)
    
    // --- SIMULACIÓN (porque /api/ordenes/{id} no devuelve los items) ---
    // En un proyecto real, haríamos un fetch a `/api/ordenes/${ordenId}`
    // Por ahora, solo leemos los datos que la orden nos dio.
    // Vamos a simular que el fetch nos devuelve los datos:
    
    // (Simulamos los datos, en la vida real esto vendría de un fetch)
    const ordenSimulada = {
        subtotal: 3.60,
        costoEnvio: 0.00,
        tarifaVariedad: 0.00,
        total: 3.60,
        items: [
            { nombre: "Plátano de Seda", cantidad: 1, precioUnitario: 2.00 },
            { nombre: "Maracuyá", cantidad: 1, precioUnitario: 1.60 }
        ]
        // (Estos datos los recalcularíamos o los leeríamos de la orden guardada)
    };
    
    // 4. Llenamos el comprobante con los datos
    fillComprobante(ordenSimulada);
    
    // 5. Asignamos el botón de PDF
    downloadPdfBtn.addEventListener("click", () => {
        // Oculta el botón para que no salga en la captura
        downloadPdfBtn.classList.add("hidden"); 
        
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        
        // Tomamos el HTML del comprobante y lo convertimos
        const contenido = document.getElementById("comprobante-contenido");
        
        doc.html(contenido, {
            callback: function (doc) {
                doc.save(`Savefrut-Orden-${ordenId}.pdf`);
                // Vuelve a mostrar el botón
                downloadPdfBtn.classList.remove("hidden"); 
            },
            x: 10,
            y: 10,
            width: 190,
            windowWidth: 700 
        });
    });
});


function fillComprobante(orden) {
    summarySubtotal.innerText = `S/ ${formatCurrency(orden.subtotal)}`;
    summaryShipping.innerText = `S/ ${formatCurrency(orden.costoEnvio)}`;
    summaryTotal.innerText = `S/ ${formatCurrency(orden.total)}`;
    
    if (orden.tarifaVariedad > 0) {
        varietyFeeRow.classList.remove('hidden');
        summaryVarietyFee.innerText = `S/ ${formatCurrency(orden.tarifaVariedad)}`;
    } else {
        varietyFeeRow.classList.add('hidden');
    }
    
    // Llenamos los items
    itemsContainer.innerHTML = orden.items.map(item => `
        <div class="flex justify-between items-center py-4">
            <div>
                <p class="font-semibold text-gray-800">${item.nombre}</p>
                <p class="text-sm text-gray-500">Cantidad: ${item.cantidad}</p>
            </div>
            <span class="font-medium">S/ ${formatCurrency(item.precioUnitario * item.cantidad)}</span>
        </div>
    `).join("");
}