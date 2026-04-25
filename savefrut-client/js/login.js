// --- Configuración ---
const API_URL = "http://localhost:8091/api";

// --- Selectores de Elementos ---
const loginForm = document.getElementById("login-form");
const emailInput = document.getElementById("email-input");
const passwordInput = document.getElementById("password-input");
const errorMessage = document.getElementById("error-message");
const loginButton = document.getElementById("login-button");

// --- Event Listener ---
loginForm.addEventListener("submit", async (event) => {
    
    event.preventDefault(); 
    errorMessage.textContent = "";
    loginButton.textContent = "Ingresando...";
    loginButton.disabled = true;

    const email = emailInput.value;
    const password = passwordInput.value;

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || "Usuario o contraseña incorrectos.");
        }

        const data = await response.json();

        // 7. Guardamos el token y el email
        localStorage.setItem("savefrut_token", data.token);
        localStorage.setItem("savefrut_user_email", data.email);

        // =================================================
        // ¡¡AQUÍ ESTÁ LA LÓGICA DE REDIRECCIÓN CORREGIDA!!
        // =================================================
        alert("¡Inicio de sesión exitoso!");
        
        // 8.1. Buscamos la URL que "envio.js" debió guardar
        const redirectUrl = localStorage.getItem("redirect_url_after_login");
        
        if (redirectUrl) {
            // 8.2. Si existe (login OBLIGATORIO):
            
            // Borramos la URL para que no se use de nuevo
            localStorage.removeItem("redirect_url_after_login");
            
            // ¡Redirigimos de vuelta al flujo de checkout!
            window.location.href = redirectUrl; // (Esto te manda a "envio.html")
            
        } else {
            // 8.3. Si NO existe (login OPCIONAL desde Home):
            
            // Redirigimos al inicio
            window.location.href = "index.html"; 
        }
        // =================================================

    } catch (error) {
        errorMessage.textContent = error.message;
        loginButton.textContent = "Ingresar";
        loginButton.disabled = false;
    }
});
