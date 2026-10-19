package cl.duoc.teqgastos.domain.validators

import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

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

    // =========================================================================
    // TASAS DE CONVERSIÓN OFFLINE A CLP (Fijas y configurables por parámetro)
    // =========================================================================
    const val TASA_UF_CLP_DEFAULT = 38_000.0
    const val TASA_USD_CLP_DEFAULT = 950.0
    const val TASA_CLP_DEFAULT = 1.0

    // =========================================================================
    // TOPES MÁXIMOS POR CATEGORÍA EN CLP (Política de Gastos TEQMED SPA)
    // =========================================================================
    const val TOPE_ALIMENTACION_CLP = 15_000.0
    const val TOPE_ALOJAMIENTO_CLP = 65_000.0
    const val TOPE_PEAJE_ESTACIONAMIENTO_CLP = 25_000.0
    const val TOPE_COMBUSTIBLE_CLP = 60_000.0
    const val TOPE_INSUMOS_CLP = 50_000.0

    private val TOPES_POR_CATEGORIA_CLP: Map<String, Double> = mapOf(
        GastoEntity.CAT_ALIMENTACION to TOPE_ALIMENTACION_CLP,
        GastoEntity.CAT_ALOJAMIENTO to TOPE_ALOJAMIENTO_CLP,
        GastoEntity.CAT_PEAJE_ESTACIONAMIENTO to TOPE_PEAJE_ESTACIONAMIENTO_CLP,
        GastoEntity.CAT_COMBUSTIBLE to TOPE_COMBUSTIBLE_CLP,
        GastoEntity.CAT_INSUMOS to TOPE_INSUMOS_CLP
    )

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
     * 4. Conversor offline de moneda a Pesos Chilenos (CLP).
     * Convierte montos en UF ($38.000 CLP por defecto), USD ($950 CLP por defecto) o CLP (1:1)
     * antes de evaluar los topes máximos por categoría.
     *
     * @param montoOriginal Monto numérico en la moneda de origen.
     * @param moneda Código de moneda ("CLP", "UF", "USD").
     * @param tasaUfClp Valor de 1 UF en CLP (por defecto $38.000).
     * @param tasaUsdClp Valor de 1 USD en CLP (por defecto $950).
     * @return Monto equivalente en CLP redondeado a entero contable.
     */
    fun convertirAClp(
        montoOriginal: Double,
        moneda: String,
        tasaUfClp: Double = TASA_UF_CLP_DEFAULT,
        tasaUsdClp: Double = TASA_USD_CLP_DEFAULT
    ): Double {
        if (montoOriginal <= 0.0) return 0.0
        val factor = when (moneda.trim().uppercase()) {
            GastoEntity.MONEDA_UF -> tasaUfClp
            GastoEntity.MONEDA_USD -> tasaUsdClp
            GastoEntity.MONEDA_CLP -> TASA_CLP_DEFAULT
            else -> TASA_CLP_DEFAULT
        }
        return (montoOriginal * factor).roundToLong().toDouble()
    }

    /**
     * Obtiene el tope máximo permitido en CLP para una categoría de gasto dada.
     */
    fun obtenerTopeCategoriaClp(categoria: String): Double? {
        return TOPES_POR_CATEGORIA_CLP[categoria.trim().uppercase()]
    }

    /**
     * 5. Valida el monto y los Topes por Categoría en CLP tras convertir la moneda offline:
     * - ALIMENTACION: máx $15.000 CLP
     * - ALOJAMIENTO: máx $65.000 CLP
     * - PEAJE_ESTACIONAMIENTO: máx $25.000 CLP
     * - COMBUSTIBLE: máx $60.000 CLP
     * - INSUMOS: máx $50.000 CLP
     *
     * @param categoria Categoría seleccionada del gasto.
     * @param montoOriginal Monto ingresado en la moneda seleccionada.
     * @param moneda Moneda del gasto ("CLP", "UF", "USD").
     * @param tasaUfClp Tasa UF configurable (por defecto $38.000 CLP).
     * @param tasaUsdClp Tasa USD configurable (por defecto $950 CLP).
     * @return `null` si el monto convertido respeta el tope de la categoría, o mensaje de error detallado.
     */
    fun validarTopePorCategoria(
        categoria: String,
        montoOriginal: Double,
        moneda: String = GastoEntity.MONEDA_CLP,
        tasaUfClp: Double = TASA_UF_CLP_DEFAULT,
        tasaUsdClp: Double = TASA_USD_CLP_DEFAULT
    ): String? {
        if (montoOriginal <= 0.0) {
            return "El monto del gasto debe ser mayor a 0"
        }

        val topeMaximoClp = obtenerTopeCategoriaClp(categoria)
            ?: return "Categoría de gasto no reconocida ($categoria)"

        val montoEnClp = convertirAClp(
            montoOriginal = montoOriginal,
            moneda = moneda,
            tasaUfClp = tasaUfClp,
            tasaUsdClp = tasaUsdClp
        )

        if (montoEnClp > topeMaximoClp) {
            val montoFormateado = formatearClp(montoEnClp)
            val topeFormateado = formatearClp(topeMaximoClp)
            return "El monto equivalente ($montoFormateado CLP) supera el tope máximo de $topeFormateado CLP para ${formatearNombreCategoria(categoria)}"
        }

        return null
    }

    /**
     * Sobrecarga para validar directamente el texto del campo de monto junto con la categoría y moneda.
     */
    fun validarMontoTextoSegunCategoria(
        montoTexto: String,
        categoria: String,
        moneda: String = GastoEntity.MONEDA_CLP,
        tasaUfClp: Double = TASA_UF_CLP_DEFAULT,
        tasaUsdClp: Double = TASA_USD_CLP_DEFAULT
    ): String? {
        val limpio = montoTexto.trim().replace(",", ".")
        if (limpio.isEmpty()) {
            return "El monto es obligatorio"
        }
        val monto = limpio.toDoubleOrNull()
            ?: return "Ingrese un monto numérico válido"

        return validarTopePorCategoria(
            categoria = categoria,
            montoOriginal = monto,
            moneda = moneda,
            tasaUfClp = tasaUfClp,
            tasaUsdClp = tasaUsdClp
        )
    }

    /**
     * 6. Valida que exista la URI de la fotografía del comprobante/boleta adjunta.
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

    // =========================================================================
    // VALIDACIONES DE AUTENTICACIÓN Y FLUJO DE RENDICIÓN
    // =========================================================================

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
     * Valida el Centro de Costo al crear una carpeta de rendición.
     */
    fun validarCentroCosto(centroCosto: String): String? {
        if (centroCosto.trim().isEmpty()) {
            return "El centro de costo es obligatorio"
        }
        if (centroCosto.trim().length < 4) {
            return "Ingrese un código de centro de costo válido (ej: CC-MANT-CLINICO-01)"
        }
        return null
    }

    /**
     * Valida el comentario de auditoría de Jefatura (obligatorio al Observar o Rechazar).
     */
    fun validarComentarioRevision(nuevoEstado: String, comentario: String): String? {
        val requiereMotivo = nuevoEstado == RendicionEntity.ESTADO_OBSERVADA ||
            nuevoEstado == RendicionEntity.ESTADO_RECHAZADA
        if (requiereMotivo && comentario.trim().length < 5) {
            return "Debe indicar un motivo detallado (mínimo 5 caracteres) para observar o rechazar"
        }
        return null
    }

    /**
     * Valida el código de comprobante de pago ERP ingresado por Finanzas.
     */
    fun validarCodigoPagoErp(codigoPago: String): String? {
        if (codigoPago.trim().isEmpty()) {
            return "El código de transferencia/comprobante ERP es obligatorio"
        }
        if (codigoPago.trim().length < 4) {
            return "Código ERP demasiado corto (ej: ERP-TRF-2026-001)"
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

    private fun formatearNombreCategoria(categoria: String): String {
        return when (categoria.trim().uppercase()) {
            GastoEntity.CAT_ALIMENTACION -> "Alimentación"
            GastoEntity.CAT_ALOJAMIENTO -> "Alojamiento"
            GastoEntity.CAT_PEAJE_ESTACIONAMIENTO -> "Peaje y Estacionamiento"
            GastoEntity.CAT_COMBUSTIBLE -> "Combustible"
            GastoEntity.CAT_INSUMOS -> "Insumos"
            else -> categoria
        }
    }
}
