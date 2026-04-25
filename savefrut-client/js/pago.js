// --- Configuración global ---
const API_URL = "http://localhost:8091/api";
const STRIPE_PUBLIC_KEY = "pk_test_51SPUk3HM48OPPGMHUyAbQ1R1NfLgtQjN9a2RiwppNJWCUHwPifk86LgufVUM3tMpQ3f5sKNgisW2weqxdBdBHxEj00zrHlDCWk";
const stripe = Stripe(STRIPE_PUBLIC_KEY);

let elements;
let clientSecret;

// --- Referencias del DOM ---
const paymentForm = document.getElementById("payment-form");
const paymentButton = document.getElementById("payment-button");
const paymentMessage = document.getElementById("payment-message");
const submitButton = document.getElementById("submit-button");
const spinner = document.getElementById("spinner");
const buttonText = document.getElementById("button-text");

// Radios del método de pago
const radioStripe = document.getElementById("radio-stripe");
const radioEfectivo = document.getElementById("radio-efectivo");

// --- MAIN ---
document.addEventListener("DOMContentLoaded", async () => {
    const token = getToken();
    const cart = getCart();
    const shippingInfo = JSON.parse(localStorage.getItem('savefrut_shipping_info'));

    if (!token || !cart || cart.length === 0 || !shippingInfo) {
        alert("Error: faltan datos del carrito o del envío. Redirigiendo al inicio.");
        window.location.href = "index.html";
        return;
    }

    const storeInfo = checkStoreVariety(cart);
    updateSummary(cart, storeInfo.isMultiple, shippingInfo.costoEnvio);

    // Cargar Stripe automáticamente
    await iniciarStripe(token, cart, shippingInfo);

    // Escuchar radios para mostrar/ocultar formularios
    radioStripe.addEventListener("change", actualizarVista);
    radioEfectivo.addEventListener("change", actualizarVista);
    actualizarVista();

    // Listener para botón principal (efectivo)
    paymentButton.addEventListener("click", manejarPago);

    // Listener para formulario Stripe
    paymentForm.addEventListener("submit", manejarPago);
});

/**
 * Cambia la vista entre pago con tarjeta y contra entrega
 */
function actualizarVista() {
    const stripeSection = document.getElementById("stripe-section");
    const efectivoSection = document.getElementById("efectivo-section");

    if (radioStripe.checked) {
        stripeSection.style.display = "block";
        efectivoSection.style.display = "none";
    } else {
        stripeSection.style.display = "none";
        efectivoSection.style.display = "block";
    }
}

/**
 * Inicia el formulario de Stripe creando un PaymentIntent
 */
async function iniciarStripe(token, cart, shippingInfo) {
    try {
        const response = await fetch(`${API_URL}/pagos/crear-intento-pago`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({
                costoEnvio: shippingInfo.costoEnvio,
                items: cart.map(item => ({
                    id: item.id,
                    cantidad: item.cantidad
                })),
            }),
        });

        const data = await response.json();
        if (!response.ok) throw new Error(data.message || "Error al crear intento de pago.");

        clientSecret = data.clientSecret;
        elements = stripe.elements({ clientSecret });
        const paymentElement = elements.create("payment");
        paymentElement.mount("#payment-element");

    } catch (err) {
        console.error("❌ Error al inicializar Stripe:", err);
        showMessage("No se pudo conectar con la pasarela de pago.");
    }
}

/**
 * Controla el flujo de pago (Stripe o Contra Entrega)
 */
async function manejarPago(e) {
    e.preventDefault();

    const isStripe = radioStripe.checked;
    setLoading(true, isStripe ? "Procesando pago..." : "Confirmando pedido...");

    try {
        if (isStripe) {
            // --- Pago con tarjeta (Stripe) ---
            const { error, paymentIntent } = await stripe.confirmPayment({
                elements,
                redirect: "if_required"
            });

            if (error) {
                showMessage(error.message);
                setLoading(false);
                return;
            }

            if (paymentIntent.status === "succeeded") {
                const orden = await guardarOrden("Pagado (Stripe)");
                limpiarYRedirigir(orden.id);
            }

        } else {
            // --- Pago contra entrega ---
            const orden = await guardarOrden("Contra Entrega");
            limpiarYRedirigir(orden.id);
        }
    } catch (err) {
        console.error("❌ Error en el pago:", err);
        showMessage("Error al procesar el pago o guardar la orden.");
    } finally {
        setLoading(false);
    }
}

/**
 * Guarda la orden en la base de datos
 */
async function guardarOrden(estadoDePago) {
    const token = getToken();
    const cart = getCart();
    const shippingInfo = JSON.parse(localStorage.getItem('savefrut_shipping_info'));

    const ordenRequest = {
        direccionEnvio: shippingInfo.direccion,
        distritoId: shippingInfo.distritoId,
        distritoNombre: shippingInfo.distritoNombre,
        costoEnvio: shippingInfo.costoEnvio,
        items: cart.map(item => ({
            productoId: item.id,
            cantidad: item.cantidad,
            precioUnitario: item.precio
        })),
        estadoPago: estadoDePago
    };

    const res = await fetch(`${API_URL}/ordenes`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify(ordenRequest),
    });

    if (!res.ok) throw new Error("Error al guardar la orden en la BD.");

    return await res.json();
}

/**
 * Limpia el carrito y redirige al comprobante
 */
function limpiarYRedirigir(ordenId) {
    localStorage.removeItem("savefrut_cart");
    localStorage.removeItem("savefrut_shipping_info");
    localStorage.setItem("savefrut_last_order_id", ordenId);
    window.location.href = "comprobante.html";
}

// --- Funciones auxiliares ---
function showMessage(text) {
    paymentMessage.classList.remove("hidden");
    paymentMessage.textContent = text;
    setTimeout(() => {
        paymentMessage.classList.add("hidden");
        paymentMessage.textContent = "";
    }, 4000);
}

function setLoading(isLoading, message = "Procesando...") {
    const btn = radioStripe.checked ? submitButton : paymentButton;

    if (isLoading) {
        btn.disabled = true;
        btn.innerText = message;
        spinner?.classList.remove("hidden");
        buttonText?.classList.add("hidden");
    } else {
        btn.disabled = false;
        btn.innerText = radioStripe.checked ? "Pagar Ahora" : "Confirmar Pedido";
        spinner?.classList.add("hidden");
        buttonText?.classList.remove("hidden");
    }
}
