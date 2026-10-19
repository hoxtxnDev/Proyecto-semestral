package cl.duoc.teqgastos.domain.validators

import java.text.NumberFormat
import java.util.Locale

/**
 * Validador de reglas de negocio desacoplado de la UI (Fase 3 - Indicador IE 2.1.2).
 *
 * Ningún @Composable debe ejecutar validaciones directamente; todas las reglas son
 * evaluadas por este componente puro de dominio e invocadas desde los ViewModels.
 * Cada función retorna `null` cuando el valor es válido, o un `String` con el mensaje
 * descriptivo del error para alimentar directamente `ValidatedTextField`.
 */
object GastoValidator {

    // Expresiones regulares para RUT chileno (con puntos XX.XXX.XXX-X o sin puntos XXXXXXXX-X)
    private val REGEX_RUT_CON_PUNTOS = Regex("^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$")
    private val REGEX_RUT_SIN_PUNTOS = Regex("^\\d{7,8}-[\\dkK]$")

    // Expresión regular básica para correo electrónico corporativo
    private val REGEX_EMAIL = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Ventana máxima de antigüedad permitida para rendir una boleta: 5 días en milisegundos
    const val DIAS_MAX_ANTIGUEDAD = 5L
    const val MILLIS_POR_DIA = 24L * 60L * 60L * 1000L
    const val VENTANA_MAX_ANTIGUEDAD_MILLIS = DIAS_MAX_ANTIGUEDAD * MILLIS_POR_DIA

    // Tolerancia de 5 minutos para evitar falsos positivos por desfase de reloj al seleccionar "Hoy"
    private const val TOLERANCIA_FUTURO_MILLIS = 5L * 60L * 1000L

    /**
     * 1. Valida el RUT del proveedor en formato básico XX.XXX.XXX-X o sin puntos con guion (XXXXXXXX-X).
     *
     * @param rut Texto ingresado por el usuario.
     * @return `null` si es válido, o mensaje de error en caso contrario.
     */
    fun validarRutProveedor(rut: String): String? {
        val limpio = rut.trim()
        if (limpio.isEmpty()) {
            return "El RUT del proveedor es obligatorio"
        }
        if (!limpio.contains("-")) {
            return "El RUT debe incluir guion y dígito verificador (ej: 76.123.456-7)"
        }
        val formatoValido = REGEX_RUT_CON_PUNTOS.matches(limpio) || REGEX_RUT_SIN_PUNTOS.matches(limpio)
        if (!formatoValido) {
            return "Formato de RUT inválido. Use XX.XXX.XXX-X o XXXXXXXX-X"
        }
        return null
    }

    /**
     * 2. Valida el Folio de la boleta o factura (obligatorio, exclusivamente numérico y mayor a 0).
     *
     * @param folio Texto ingresado para el número de folio.
     * @return `null` si es válido, o mensaje de error en caso contrario.
     */
    fun validarFolioBoleta(folio: String): String? {
        val limpio = folio.trim()
        if (limpio.isEmpty()) {
            return "El folio de la boleta es obligatorio"
        }
        if (!limpio.all { it.isDigit() }) {
            return "El folio debe contener solo números"
        }
        val numeroFolio = limpio.toLongOrNull()
        if (numeroFolio == null || numeroFolio <= 0L) {
            return "El número de folio debe ser mayor a cero"
        }
        return null
    }

    /**
     * 3. Valida la fecha del gasto:
     * - No puede ser una fecha futura respecto a `ahoraMillis`.
     * - No puede tener más de 5 días de antigüedad respecto a `ahoraMillis` (control de gastos fuera de plazo).
     *
     * @param fechaGastoMillis Timestamp del gasto en milisegundos.
     * @param ahoraMillis Timestamp actual de referencia (por defecto `System.currentTimeMillis()`).
     * @return `null` si está dentro del plazo permitido, o mensaje de error en caso contrario.
     */
    fun validarFechaGasto(
        fechaGastoMillis: Long?,
        ahoraMillis: Long = System.currentTimeMillis()
    ): String? {
        if (fechaGastoMillis == null || fechaGastoMillis <= 0L) {
            return "Debe seleccionar una fecha válida para el gasto"
        }
        if (fechaGastoMillis > (ahoraMillis + TOLERANCIA_FUTURO_MILLIS)) {
            return "La fecha del gasto no puede ser futura"
        }
        val antiguedadMillis = ahoraMillis - fechaGastoMillis
        if (antiguedadMillis > VENTANA_MAX_ANTIGUEDAD_MILLIS) {
            return "Gasto fuera de plazo: la boleta supera los $DIAS_MAX_ANTIGUEDAD días de antigüedad permitidos"
        }
        return null
    }

    /**
     * 4. Valida que exista la URI de la fotografía del comprobante/boleta adjunta.
     *
     * @param fotoUri URI en formato String de la evidencia fotográfica capturada.
     * @return `null` si existe evidencia adjunta, o mensaje de error si está vacía/nula.
     */
    fun validarFotoComprobante(fotoUri: String?): String? {
        if (fotoUri.isNullOrBlank()) {
            return "Debe adjuntar una fotografía del comprobante o boleta"
        }
        return null
    }

    /**
     * Valida el correo electrónico en el formulario de autenticación.
     */
    fun validarEmail(email: String): String? {
        val limpio = email.trim()
        if (limpio.isEmpty()) {
            return "El correo corporativo es obligatorio"
        }
        if (!REGEX_EMAIL.matches(limpio)) {
            return "Ingrese un correo válido (ej: usuario@teqmed.cl)"
        }
        return null
    }

    /**
     * Valida la contraseña en el formulario de autenticación.
     */
    fun validarPassword(password: String): String? {
        if (password.isBlank()) {
            return "La contraseña es obligatoria"
        }
        if (password.length < 4) {
            return "La contraseña debe tener al menos 4 caracteres"
        }
        return null
    }

    /**
     * Formatea un monto en pesos chilenos (CLP) para mensajes claros al usuario.
     */
    fun formatearClp(montoClp: Double): String {
        val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
        formato.maximumFractionDigits = 0
        return formato.format(montoClp)
    }
}
