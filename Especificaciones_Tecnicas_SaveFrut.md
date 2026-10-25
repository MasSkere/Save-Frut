# **SAVEFRUT: Plataforma E-commerce para la Reducción de Mermas de Frutas**

**Curso:** Desarrollo de Aplicaciones Web II (4697)  
**Profesor:** Carlos Israel Accilio Cruz  
**Ciclo:** Sexto \- 2026  
**Coordinador:** \[TU NOMBRE COMPLETO\] \- \[TU CÓDIGO\]  
**Integrantes:** \[NOMBRES EN MAYÚSCULAS\]

## **5.1. Resumen**

SaveFrut es una plataforma web de e-commerce diseñada para combatir el desperdicio de alimentos conectando a vendedores minoristas con consumidores finales. El sistema aplica descuentos dinámicos (hasta 60%) a productos próximos a vencer. Técnicamente, está desarrollado bajo una arquitectura distribuida que emplea Angular en el frontend y Spring Boot (Java) en el backend. Integra patrones avanzados de seguridad con JWT y contraseñas encriptadas con BCrypt, comunicación asíncrona mediante RabbitMQ para un sistema de fidelización (Eco-Puntos), caché con Redis para optimizar el catálogo, y resiliencia con Circuit Breaker al procesar pagos mediante la pasarela externa Mercado Pago.

## **5.2. Introducción**

El desperdicio de alimentos es una problemática global con un fuerte impacto económico y ambiental en el Perú, donde el 13% de la pérdida de vegetales y frutas ocurre en la fase de comercialización. En este contexto, el presente proyecto plantea el desarrollo de "SaveFrut", una solución tecnológica orientada a modernizar la oferta de productos altamente perecibles en mercados tradicionales. A través de la plataforma, los comerciantes amplían su visibilidad y recuperan su inversión, mientras los usuarios acceden a precios competitivos. El documento detalla el análisis situacional, los objetivos trazados bajo la metodología SMART, y el diseño arquitectónico de la aplicación web, demostrando la aplicación práctica de conceptos de microservicios, seguridad, resiliencia y tolerancia a fallos exigidos para entornos de producción modernos.

## **5.3. Diagnóstico**

El sector de comercialización de alimentos frescos en el Perú enfrenta un desafío crítico respecto a la gestión de inventarios y la pérdida de productos altamente perecibles. Para comprender la magnitud de este problema y plantear una solución efectiva mediante la plataforma "SaveFrut", se ha realizado un análisis SEPTE evaluando las siguientes tres variables del entorno:

> * **Variable Tecnológica:** En la actualidad, el sector minorista tradicional de venta de frutas opera con un nivel de digitalización casi nulo. Las herramientas de gestión se limitan al uso empírico de mensajería instantánea y a la exhibición física en mostradores. Los vendedores carecen de un sistema centralizado de control de stock, seguimiento automatizado de fechas de vencimiento o pasarelas de pago digitales integradas que les permitan dinamizar sus ventas o aplicar descuentos programados. Esta carencia tecnológica impide conectar la oferta de productos próximos a vencer con la demanda de los consumidores que buscan precios accesibles.  
> * **Variable Económica:** La falta de herramientas para dar rápida salida a los productos genera un alto margen de pérdida económica diaria y una rentabilidad deficiente. Cuando un vendedor detecta que su inventario está próximo a perder su vida útil, su única estrategia es rematar los precios manualmente y ofrecerlos solo al tráfico peatonal que transita por su puesto físico, perdiendo la oportunidad de recuperar su inversión a través de un público más amplio.  
> * **Variable Ecológica:** El desperdicio alimentario tiene un impacto ambiental severo. En el Perú, se estima que se pierden 5.6 millones de toneladas de frutas y vegetales al año, lo que representa el 44.4% del desperdicio alimentario total del país \[1\]. Peor aún, aproximadamente el 13% de esta pérdida total ocurre específicamente en la fase de comercialización \[1\]. Esto genera una inmensa cantidad de residuos orgánicos (mermas) que, al descomponerse en vertederos, contribuyen a la emisión de gases de efecto invernadero.

El análisis de estas variables evidencia una clara oportunidad de mejora: la necesidad de implementar una plataforma e-commerce sostenible que digitalice el inventario perecible, automatice descuentos dinámicos (hasta un 60%) según la proximidad de vencimiento y amplíe la visibilidad de las ofertas. Esta solución tecnológica abordará simultáneamente la recuperación económica de los comerciantes y la mitigación del impacto ambiental de las mermas alimentarias.

## **5.4. Objetivos**

> * **OBJ 1.-** Incrementar la visibilidad de los vendedores de frutas en un 80% dentro de la plataforma al cabo de 3 meses de implementado el sistema.  
> * **OBJ 2.-** Lograr una tasa de retención de clientes del 40% durante los primeros 3 meses mediante la implementación del sistema interactivo de acumulación de puntos por "frutas salvadas".

## **5.5. Justificación del Proyecto**

De acuerdo con el diagnóstico situacional, la necesidad de ejecutar el presente proyecto se justifica por los siguientes motivos:  
**Relevancia Técnica:** Se requiere la implementación de una plataforma de comercio electrónico centralizada que automatice la aplicación de descuentos dinámicos (hasta un 60%) según la proximidad de vencimiento de los productos. Esto permitirá digitalizar el inventario perecible, asegurar transacciones mediante plataformas como Mercado Pago y organizar la información en tiempo real para evitar la pérdida de datos y mejorar la toma de decisiones del negocio.  
**Impacto Operativo:** Permitirá reducir drásticamente el volumen de merma física en los puestos de venta, optimizando la exhibición de ofertas hacia un público masivo sin depender del tránsito peatonal. Esto impacta directamente en la recuperación de costos para el vendedor, ofrece acceso a alimentos económicos para la población y promueve una política de sostenibilidad ambiental frente a la crisis de desperdicio alimentario.  
**Beneficiarios Directos:**

> * Los vendedores minoristas de frutas, quienes recuperarán la inversión de productos próximos a vencer y digitalizarán sus ventas.  
> * Los consumidores finales, quienes accederán a alimentos frescos a un menor costo, acumulando beneficios interactivos.

**Beneficiarios Indirectos:**

> * El medio ambiente, al reducir significativamente la generación de residuos orgánicos (mermas).  
> * La comunidad general, al fomentarse una economía local más dinámica y sostenible.

## **5.6. Definición y alcance (Especificaciones Técnicas)**

### **1\. Arquitectura General y Orquestación**

El sistema "SaveFrut" abandona el enfoque monolítico para adoptar una **Arquitectura basada en Microservicios**, lo que permite escalar de forma independiente los procesos de mayor demanda (como la visualización del catálogo). Toda la infraestructura (microservicios, bases de datos, caché y brokers de mensajería) se despliega y orquesta de forma local utilizando **Docker y Docker Compose**. Esto garantiza que el entorno de desarrollo sea idéntico al de producción, asegurando aislamiento, portabilidad y un despliegue unificado con el comando docker-compose up. Como único punto de entrada para los clientes (Frontend en Angular), se implementará un **API Gateway** (Spring Cloud Gateway), encargado de enrutar las peticiones al microservicio correspondiente y centralizar la configuración de CORS.

### **2\. Desglose de Microservicios**

La lógica de negocio se ha dividido en los siguientes servicios independientes:

> * **API Gateway (Puerto 8080):** Actúa como proxy inverso. Recibe las peticiones del cliente y las redirige internamente. No contiene lógica de negocio.  
> * **Microservicio de Identidad y Usuarios (ms-identidad):** Responsable del registro, inicio de sesión, encriptación de contraseñas con BCrypt y gestión de roles (Cliente, Vendedor, Admin). Emite y gestiona los tokens de acceso y Refresh Tokens.  
> * **Microservicio de Catálogo e Inventario (ms-catalogo):** Gestiona los productos, categorías, medición y control de stock. Es un servicio de alta concurrencia, por lo que integra **Redis** como caché para acelerar la carga del catálogo y el cálculo de descuentos dinámicos por vencimiento.  
> * **Microservicio de Órdenes y Pagos (ms-ordenes):** Procesa el carrito de compras, genera la orden, calcula el costo total (incluyendo tarifas de envío) y se comunica de forma externa con la API de Mercado Pago. Implementa mecanismos de idempotencia para evitar el cobro duplicado de órdenes.  
> * **Microservicio de Fidelización (ms-puntos):** Encargado de gestionar el programa de recompensas (Eco-Puntos). Este servicio no recibe peticiones HTTP directas del usuario durante la compra, sino que escucha pasivamente los eventos de "Orden Pagada" a través del broker de mensajería (RabbitMQ/Kafka) para sumar los puntos en segundo plano, garantizando un alto rendimiento.

**Persistencia de Datos:** Para mantener la independencia, cada microservicio gestionará su propio esquema de base de datos en **PostgreSQL**, evitando las claves foráneas cruzadas entre servicios y garantizando un bajo acoplamiento.

### **3\. Seguridad y Gestión de Sesiones (JWT \+ Refresh Token)**

La seguridad del ecosistema distribuido se gestiona sin estado (stateless), garantizando que las instancias puedan escalar horizontalmente sin depender de sesiones en memoria (HttpSession).

> * **Autenticación Centralizada:** El ms-identidad es el único responsable de validar credenciales (hasheadas con BCrypt) y emitir los tokens.  
> * **Access Token (JWT):** Se emite un JWT de corta duración (ej. 15 minutos) firmado digitalmente, el cual es enviado en las cabeceras HTTP de cada petición. El API Gateway o los microservicios validan su firma localmente sin necesidad de consultar a la base de datos, agilizando el tráfico.  
> * **Refresh Token y Rotación:** Para mantener una experiencia de usuario fluida sin obligarlo a logearse constantemente, se implementa un mecanismo de Refresh Tokens. Este token de larga duración se almacena en **Redis** y se envía al cliente de forma segura (como cookie HttpOnly). Cuando el Access Token caduca, el frontend utiliza el Refresh Token para solicitar uno nuevo. Por seguridad, se aplica la **rotación de tokens**: cada vez que se usa un Refresh Token, este se invalida en Redis y se emite uno nuevo, mitigando ataques de robo de sesión. Aunque el guardado en Redis parezca introducir un "estado", esto resuelve el problema de la revocación de accesos manteniendo el principio de microservicios stateless.

### **4\. Comunicación (Síncrona y Asíncrona)**

Para evitar el alto acoplamiento típico de un monolito, los microservicios de SaveFrut se comunican bajo dos modalidades estrictas:

> * **Comunicación Síncrona (OpenFeign):** Utilizada exclusivamente cuando un servicio requiere una respuesta inmediata para continuar su flujo. Se implementa **Feign Client** (de Spring Cloud) para realizar llamadas REST declarativas. Por ejemplo: ms-ordenes consultando a ms-catalogo para verificar si hay stock suficiente en el instante del Checkout.  
> * **Comunicación Asíncrona (RabbitMQ / Kafka):** Utilizada para procesos que no deben bloquear la respuesta al usuario. Al confirmarse un pago en Mercado Pago, ms-ordenes publica un evento llamado OrdenPagadaEvent en el Message Broker. El ms-puntos consume este mensaje y actualiza los Eco-Puntos del cliente de manera desacoplada.

### **5\. Tolerancia a Fallos y Observabilidad**

Para garantizar la alta disponibilidad exigida en entornos productivos, el proyecto integra:

> * **Resiliencia (Circuit Breaker):** Las integraciones con agentes externos (como la API de Mercado Pago o Google Maps) están protegidas con **Resilience4j**. Si la pasarela de pagos experimenta caídas o latencia alta, el Circuit Breaker se abre, deteniendo las peticiones y retornando un mensaje de error controlado (Fallback) para evitar el colapso de los recursos del backend.  
> * **Monitoreo y Trazabilidad:** Se exponen métricas de salud y rendimiento mediante **Spring Boot Actuator** (/actuator/health, /actuator/prometheus). Estas métricas son recolectadas por **Prometheus** y visualizadas en tiempo real a través de tableros interactivos en **Grafana**. Adicionalmente, los logs de todos los contenedores Docker se centralizarán utilizando el **ELK Stack (Elasticsearch, Logstash, Kibana)** para facilitar el diagnóstico de errores distribuidos.

## **5.7. Productos y entregables**

> * Diagrama de Arquitectura del Sistema (interacción entre cliente, API, base de datos y servicios externos).  
> * Modelo Entidad-Relación (MER) de la base de datos PostgreSQL, detallando las tablas de usuarios, productos, órdenes y categorías.  
> * Diagramas de flujo del proceso de compra y del cálculo dinámico de descuentos.  
> * Repositorios de código fuente alojados en GitHub, estructurados de forma modular (Frontend en Angular y Backend en Java).  
> * Archivos de configuración de infraestructura (Dockerfile y docker-compose.yml) que garantizan la portabilidad del proyecto.  
> * Capturas de pantalla del sistema operativo, demostrando el funcionamiento de las vistas públicas, el panel de cliente autenticado, la gestión del vendedor y el dashboard administrativo.  
> * Resultados de las pruebas funcionales de los endpoints principales (creación de órdenes, registro de productos y login).  
> * Documento de cálculo y validación del Objetivo 1, demostrando matemáticamente el incremento de la visibilidad de los productos próximos a vencer tras la simulación de uso de la plataforma.  
> * Vídeo \- Demo Reel Estructurado (3-5 minutos) que evidencia el funcionamiento técnico y los beneficios de SaveFrut.

## **5.8. Conclusiones**

> 1. La implementación de descuentos dinámicos automatizados centraliza la oferta y reduce directamente el riesgo de pérdida económica para los vendedores minoristas frente a la merma por vencimiento.  
> 2. El uso de RabbitMQ/Kafka para la gestión asíncrona de los "Eco-Puntos" permite desacoplar procesos secundarios, mejorando significativamente el tiempo de respuesta y la experiencia del usuario durante el flujo de pago.  
> 3. La integración de Spring Security con JWT y BCrypt garantiza un entorno transaccional seguro que protege la identidad de los clientes y cumple con los estándares modernos de desarrollo backend.

## **5.9. Recomendaciones**

> 1. **Orquestación (Siguiente Nivel):** Para la fase de producción intensiva, se sugiere orquestar los contenedores Docker hacia un clúster de Kubernetes, aplicando estrategias de despliegue Blue/Green para realizar actualizaciones sin afectar la disponibilidad.  
> 2. **Observabilidad:** Se recomienda configurar Spring Boot Actuator junto con herramientas de monitoreo como Prometheus y Grafana para establecer un control visual sobre las métricas y la salud del sistema.  
> 3. **Escalabilidad y Enrutamiento:** Evaluar la integración de un API Gateway y un servicio de descubrimiento (Eureka) de Spring Cloud para facilitar el balanceo de carga cuando el sistema se fragmente en múltiples microservicios (por ej. separando usuarios, pagos e inventario).

## **5.10. Glosario**

> * **JWT (JSON Web Token):** Estándar para la transmisión segura de información entre partes como un objeto JSON, usado para validar la autorización en rutas protegidas.  
> * **BCrypt:** Función de hash criptográfico diseñada para el cifrado irreversible y seguro de contraseñas en la base de datos.  
> * **RabbitMQ / Kafka:** Intermediarios de mensajes (Message Brokers) que permiten la comunicación asíncrona entre diferentes componentes del sistema.  
> * **Circuit Breaker (Resilience4j):** Patrón de diseño que evita que un fallo en un servicio externo (como Mercado Pago o Google Maps) colapse todo el backend.  
> * **Idempotencia:** Propiedad de diseño que garantiza que realizar la misma petición (por ejemplo, cobrar un pago) múltiples veces produzca el mismo resultado, evitando datos duplicados.

## **5.11. Bibliografía**

> * Davis, A. (2021). *Bootstrapping Microservices with Docker, Kubernetes, and Terraform.* Manning Publications.  
> * Thomas, V. (2022). *Cloud Native Spring in Action: With Spring Boot and Kubernetes.* Simon and Schuster.  
> * Sustainability. (2021). *Cuantificación de las pérdidas y el desperdicio de alimentos en el Perú* (Datos FAO).

## **5.12. Anexos**

\[Aquí puedes adjuntar el enlace de tus repositorios, imágenes y gráficos\]