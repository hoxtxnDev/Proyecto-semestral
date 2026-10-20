# 📱🍏 4. Adaptabilidad Android / iOS, Paleta de Diseño y Guía de Testing en Android Studio

**Proyecto:** TeqGastos — TEQMED SPA  
**Integrantes:** Horacio Navarrete y Maximiliano Ojeda  

---

## 1. Adaptabilidad Multiplataforma: Android (Material Design 3) e iOS (Cupertino HIG)

El proyecto ha sido diseñado bajo una arquitectura **Compose Multiplatform Ready** y cuenta con un motor de **Adaptabilidad Visual en Vivo** ([`TeqAdaptiveSections.kt`](../app/src/main/java/cl/duoc/teqgastos/ui/components/TeqAdaptiveSections.kt) y [`TeqFigmaComponents.kt`](../app/src/main/java/cl/duoc/teqgastos/ui/components/TeqFigmaComponents.kt)) que permite alternar en tiempo real entre **Android** e **iOS** desde el chip superior (`Android` / `iOS`) de la cabecera o desde la pestaña **Ajustes**:

| Dimensión | Modo Android Nativo (`ANDROID_MATERIAL`) | Modo iOS Adaptable (`IOS_CUPERTINO`) |
| :--- | :--- | :--- |
| **Guía de Diseño** | Google Material Design 3 (M3) | Apple Human Interface Guidelines (HIG / Cupertino) |
| **Áreas Seguras (`WindowInsets`)** | `statusBarsPadding()` + `navigationBarsPadding()` integrados con `enableEdgeToEdge()` | Respeta el área del **Dynamic Island / Notch** superior y despliega el **Home Indicator Pill** inferior de iOS |
| **Geometría y Bordes** | Tarjetas con radio `18dp–19dp` y botones de `15dp` | Tarjetas con curvatura continua tipo *Squircle* (`24dp`) y cabecera curva de `38dp` |
| **Barra Inferior (`TeqBottomNav`)** | Barra inferior Material 3 de ancho completo con elevación `10dp` | Tab Bar translúcida estilo iOS (`RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)`) con indicador de inicio inferior |
| **Capa de Dominio y Datos** | 100% Kotlin puro en [`GastoValidator.kt`](../app/src/main/java/cl/duoc/teqgastos/domain/validators/GastoValidator.kt) y Coroutines/Flows estándar | Compartible al 100% en `commonMain` de Kotlin Multiplatform (KMP) para compilar en iOS (`iosArm64` / `iosSimulatorArm64`) |

---

## 2. Sistema de Diseño y Paleta de Colores Corporativa (Figma `src-interface`)

Todos los componentes visuales ([`TeqTheme.kt`](../app/src/main/java/cl/duoc/teqgastos/ui/theme/TeqTheme.kt)) utilizan exactamente la paleta de colores definida en el prototipo de Figma:

| Token en Código (`TeqTheme.kt`) | Hexadecimal | Uso en la Interfaz |
| :--- | :---: | :--- |
| `TeqNavy` | `#1B2A4A` | Color primario institucional (Cabeceras curvas, tarjetas Hero de Finanzas/TeqRuta, textos principales) |
| `TeqTeal` | `#00BFA5` | Acento corporativo TEQMED (Botones primarios, indicadores activos, órbitas decorativas, badges) |
| `TeqTealDark` | `#067D71` | Textos e íconos de énfasis sobre fondos claros (`TeqTealSoft`) |
| `TeqBackground` | `#F5F7FA` | Fondo general de las pantallas para alto contraste con tarjetas blancas (`#FFFFFF`) |
| `TeqApproveBg` / `TeqApproveText` | `#DCF8EA` / `#147D52` | Estado **APROBADA / PAGADA ERP** y validación de tope cumplido |
| `TeqObserveBg` / `TeqObserveText` | `#FFF2CE` / `#B57816` | Estado **OBSERVADA** y advertencias de auditoría |
| `TeqRejectBg` / `TeqRejectText` | `#FDE2E2` / `#C44A4A` | Estado **RECHAZADA**, exceso de tope presupuestario y errores en `ValidatedTextField` |
| `TeqPendingPillBg` / `TeqPendingPillText` | `#FFF4D6` / `#C48A1A` | Estado **PENDIENTE** de evaluación en Jefatura |

---

## 3. Guía Paso a Paso de Testing en Android Studio (Recorrido de 5 Minutos para la Defensa)

### Paso 1: Inicio de Sesión y Validación Reactiva (30 seg)
1. Ejecutar la app en el emulador desde Android Studio (**Run `'app'`**).
2. En [`LoginScreen`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/auth/LoginScreen.kt), escribir un correo incompleto (ej. `tecnico@`) para mostrar al docente cómo [`ValidatedTextField`](../app/src/main/java/cl/duoc/teqgastos/ui/components/ValidatedTextField.kt) despliega el ícono rojo de error con animación `AnimatedVisibility`.
3. Tocar la tarjeta rápida **"Técnico"** (completa automáticamente `tecnico@teqmed.cl` / `123456` con check verde) y presionar **"Ingresar"**.

### Paso 2: Flujo Técnico en Terreno, TeqRuta, Cámara y GPS (2 min)
1. En [`TecnicoHomeScreen`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/tecnico/TecnicoHomeScreen.kt), presionar la tarjeta azul **"Importar Traslado TeqRuta"**:
   - Se inyectan automáticamente 2 gastos (**Peaje `$4.800`** + **Combustible `$22.500`**) y la tarjeta de la rendición se expande suavemente con `animateContentSize()`.
2. Presionar **"+ Registrar Boleta"** para abrir [`FormularioGastoScreen`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/tecnico/FormularioGastoScreen.kt):
   - **Probar Regla de Plazo (5 días):** Tocar el chip **"Hace 7 días (Fuera de plazo)"** → aparece la alerta roja indicando que supera los 5 días permitidos. Volver a tocar **"Hoy (En plazo)"**.
   - **Probar Conversor Multimoneda y Topes:** Seleccionar categoría **Alimentación** (Tope `$15.000 CLP`), elegir moneda **`USD`** e ingresar `20` (`20 USD = $19.000 CLP`) → el sistema muestra error porque supera el tope de `$15.000 CLP`. Cambiar el monto a `12` (`12 USD = $11.400 CLP`) → queda validado en verde.
   - **Probar Recurso Nativo 1 (Cámara):** Presionar **"Tomar Foto"** → conceder permiso de Cámara → tomar la foto en el emulador → se visualiza la miniatura real en pantalla.
   - **Probar Recurso Nativo 2 (GPS):** Presionar **"Capturar GPS"** → conceder permiso de Ubicación → se registran las coordenadas GPS (`-36.8270, -73.0503`).
   - Presionar **"Guardar Gasto en Rendición"**.
3. De vuelta en la tarjeta de la rendición, presionar **"Enviar a Jefatura"** → el estado pasa a `PENDIENTE`.
4. Tocar las pestañas inferiores **"Reportes"** y **"Ajustes"** (probar el botón **Android M3 / iOS HIG** para mostrar la adaptabilidad visual).
5. Tocar el botón **`CM` (Cerrar sesión)** en la esquina superior derecha.

### Paso 3: Flujo de Auditoría en Jefatura (1 min 30 seg)
1. En el Login, tocar la tarjeta **"Jefatura"** (`jefatura@teqmed.cl`) y presionar **"Ingresar"**.
2. En [`JefaturaScreen`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/jefatura/JefaturaScreen.kt):
   - Tocar **"Desglose"** o **"VER FOTO"** en una tarjeta para abrir el **Visor de Auditoría de Comprobante**, mostrando la foto de la boleta, las coordenadas GPS y el badge **"Cumple tope"**.
   - Presionar **"Observar"** o **"Rechazar"** en una rendición → intentar confirmar con el campo vacío para demostrar que el ViewModel exige el comentario obligatorio con ícono de error. Escribir un motivo (`"Favor adjuntar detalle legible del proveedor"`) y confirmar.
   - En otra rendición pendiente, presionar **"Aprobar"** → queda lista para Finanzas.
3. Cerrar sesión con el botón **`AS`** de la barra superior.

### Paso 4: Flujo de Cierre en Finanzas y Exportación `.CSV` a ERP (1 min)
1. En el Login, tocar la tarjeta **"Finanzas"** (`finanzas@teqmed.cl`) y presionar **"Ingresar"**.
2. En [`FinanzasScreen`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/finanzas/FinanzasScreen.kt):
   - Verificar que el consolidado superior suma únicamente las rendiciones en estado **`APROBADA`**.
   - En la tarjeta **Integración ERP**, ingresar el código de pago `PAG-2025-0089`.
   - Presionar **"Exportar a ERP (.CSV)"** → se abre el diálogo con la ruta física del archivo `.csv` generado en el dispositivo, la vista previa de las columnas contables y el botón **"Compartir / Enviar a ERP (.CSV)"** que abre el selector nativo de Android (`Intent.ACTION_SEND`).
