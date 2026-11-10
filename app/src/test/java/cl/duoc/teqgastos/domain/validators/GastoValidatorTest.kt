package cl.duoc.teqgastos.domain.validators

import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para `GastoValidator`.
 * Verifica conversor offline, políticas de topes por categoría, RUTs, plazos y validaciones.
 */
class GastoValidatorTest {

    // =========================================================================
    // 1. VALIDACIÓN DE RUT PROVEEDOR
    // =========================================================================

    @Test
    fun validarRutProveedor_retornaNull_cuandoRutConPuntosEsValido() {
        val resultado = GastoValidator.validarRutProveedor("76.123.456-7")
        assertNull("RUT con puntos válido debe retornar null", resultado)
    }

    @Test
    fun validarRutProveedor_retornaNull_cuandoRutSinPuntosConGuionEsValido() {
        val resultado = GastoValidator.validarRutProveedor("76123456-K")
        assertNull("RUT sin puntos con guion válido debe retornar null", resultado)
    }

    @Test
    fun validarRutProveedor_retornaError_cuandoRutEstaVacio() {
        val resultado = GastoValidator.validarRutProveedor("  ")
        assertEquals("El RUT del proveedor es obligatorio", resultado)
    }

    @Test
    fun validarRutProveedor_retornaError_cuandoNoTieneGuion() {
        val resultado = GastoValidator.validarRutProveedor("761234567")
        assertEquals("El RUT debe incluir guion y dígito verificador (ej: 76.123.456-7)", resultado)
    }

    @Test
    fun validarRutProveedor_retornaError_cuandoFormatoEsInvalido() {
        val resultado = GastoValidator.validarRutProveedor("123-ABC")
        assertEquals("Formato de RUT inválido. Use XX.XXX.XXX-X o XXXXXXXX-X", resultado)
    }

    // =========================================================================
    // 2. VALIDACIÓN DE FOLIO BOLETA
    // =========================================================================

    @Test
    fun validarFolioBoleta_retornaNull_cuandoFolioEsNumericoPositivo() {
        assertNull(GastoValidator.validarFolioBoleta("12345"))
        assertNull(GastoValidator.validarFolioBoleta("  80921  "))
    }

    @Test
    fun validarFolioBoleta_retornaError_cuandoEstaVacio() {
        assertEquals("El folio de la boleta es obligatorio", GastoValidator.validarFolioBoleta(""))
    }

    @Test
    fun validarFolioBoleta_retornaError_cuandoContieneLetras() {
        assertEquals("El folio debe contener solo números", GastoValidator.validarFolioBoleta("F-1234"))
    }

    @Test
    fun validarFolioBoleta_retornaError_cuandoEsCeroONegativo() {
        assertEquals("El número de folio debe ser mayor a cero", GastoValidator.validarFolioBoleta("0"))
    }

    // =========================================================================
    // 3. VALIDACIÓN DE FECHA Y PLAZO MAXIMO (5 DÍAS)
    // =========================================================================

    @Test
    fun validarFechaGasto_retornaNull_cuandoEstaDentroDelPlazo() {
        val ahora = 1_700_000_000_000L
        val hace2Dias = ahora - (2 * GastoValidator.MILLIS_POR_DIA)
        assertNull(GastoValidator.validarFechaGasto(hace2Dias, ahoraMillis = ahora))
    }

    @Test
    fun validarFechaGasto_retornaError_cuandoEsNulaOCero() {
        assertEquals("Debe seleccionar una fecha válida para el gasto", GastoValidator.validarFechaGasto(null))
        assertEquals("Debe seleccionar una fecha válida para el gasto", GastoValidator.validarFechaGasto(0L))
    }

    @Test
    fun validarFechaGasto_retornaError_cuandoEsFutura() {
        val ahora = 1_700_000_000_000L
        val futuro = ahora + (10 * 60 * 1000L) // 10 minutos en el futuro (supera tolerancia de 5 min)
        assertEquals("La fecha del gasto no puede ser futura", GastoValidator.validarFechaGasto(futuro, ahoraMillis = ahora))
    }

    @Test
    fun validarFechaGasto_retornaError_cuandoSuperaLos5Dias() {
        val ahora = 1_700_000_000_000L
        val hace7Dias = ahora - (7 * GastoValidator.MILLIS_POR_DIA)
        val resultado = GastoValidator.validarFechaGasto(hace7Dias, ahoraMillis = ahora)
        assertEquals("Gasto fuera de plazo: la boleta supera los 5 días de antigüedad permitidos", resultado)
    }

    // =========================================================================
    // 4. CONVERSOR OFFLINE A PESOS CHILENOS (CLP)
    // =========================================================================

    @Test
    fun convertirAClp_mantieneMonto_cuandoMonedaEsClp() {
        val convertido = GastoValidator.convertirAClp(15000.0, GastoEntity.MONEDA_CLP)
        assertEquals(15000.0, convertido, 0.001)
    }

    @Test
    fun convertirAClp_calculaCorrectamente_cuandoMonedaEsUf() {
        // 2 UF @ $38.000 CLP = $76.000 CLP
        val convertido = GastoValidator.convertirAClp(2.0, GastoEntity.MONEDA_UF, tasaUfClp = 38000.0)
        assertEquals(76000.0, convertido, 0.001)
    }

    @Test
    fun convertirAClp_calculaCorrectamente_cuandoMonedaEsUsd() {
        // 12 USD @ $950 CLP = $11.400 CLP
        val convertido = GastoValidator.convertirAClp(12.0, GastoEntity.MONEDA_USD, tasaUsdClp = 950.0)
        assertEquals(11400.0, convertido, 0.001)
    }

    @Test
    fun convertirAClp_retornaCero_cuandoMontoOriginalEsCeroONegativo() {
        assertEquals(0.0, GastoValidator.convertirAClp(0.0, GastoEntity.MONEDA_CLP), 0.001)
        assertEquals(0.0, GastoValidator.convertirAClp(-50.0, GastoEntity.MONEDA_USD), 0.001)
    }

    // =========================================================================
    // 5. EVALUACIÓN DE TOPES POR CATEGORÍA
    // =========================================================================

    @Test
    fun obtenerTopeCategoriaClp_retornaMontoCorrectoSegunPolitica() {
        assertEquals(15000.0, GastoValidator.obtenerTopeCategoriaClp(GastoEntity.CAT_ALIMENTACION)!!, 0.001)
        assertEquals(65000.0, GastoValidator.obtenerTopeCategoriaClp(GastoEntity.CAT_ALOJAMIENTO)!!, 0.001)
        assertEquals(25000.0, GastoValidator.obtenerTopeCategoriaClp(GastoEntity.CAT_PEAJE_ESTACIONAMIENTO)!!, 0.001)
        assertEquals(60000.0, GastoValidator.obtenerTopeCategoriaClp(GastoEntity.CAT_COMBUSTIBLE)!!, 0.001)
        assertEquals(50000.0, GastoValidator.obtenerTopeCategoriaClp(GastoEntity.CAT_INSUMOS)!!, 0.001)
        assertNull(GastoValidator.obtenerTopeCategoriaClp("CATEGORIA_INEXISTENTE"))
    }

    @Test
    fun validarTopePorCategoria_retornaNull_cuandoRespetaElTope() {
        // Alimentación tope $15.000 CLP. Probar $12.000 CLP
        val resultado = GastoValidator.validarTopePorCategoria(
            categoria = GastoEntity.CAT_ALIMENTACION,
            montoOriginal = 12000.0,
            moneda = GastoEntity.MONEDA_CLP
        )
        assertNull("Monto $12.000 CLP debe cumplir el tope de $15.000 CLP", resultado)
    }

    @Test
    fun validarTopePorCategoria_retornaError_cuandoExcedeElTope() {
        // Alimentación tope $15.000 CLP. Probar $20 USD ($19.000 CLP)
        val resultado = GastoValidator.validarTopePorCategoria(
            categoria = GastoEntity.CAT_ALIMENTACION,
            montoOriginal = 20.0,
            moneda = GastoEntity.MONEDA_USD,
            tasaUsdClp = 950.0
        )
        assertNotNull(resultado)
        assertTrue(resultado!!.contains("supera el tope máximo"))
    }

    @Test
    fun validarMontoTextoSegunCategoria_validaMontoTextoCorrectamente() {
        // Texto "12,5" en USD = 12.5 USD * 950 = $11.875 CLP (cumple tope $15.000)
        assertNull(
            GastoValidator.validarMontoTextoSegunCategoria(
                montoTexto = "12,5",
                categoria = GastoEntity.CAT_ALIMENTACION,
                moneda = GastoEntity.MONEDA_USD
            )
        )

        // Texto no numérico
        assertEquals(
            "Ingrese un monto numérico válido",
            GastoValidator.validarMontoTextoSegunCategoria("abc", GastoEntity.CAT_ALIMENTACION)
        )
    }

    // =========================================================================
    // 6. VALIDACIONES DE COMPROBANTE, CENTRO DE COSTO Y REVISIÓN
    // =========================================================================

    @Test
    fun validarFotoComprobante_retornaNull_cuandoTieneUri() {
        assertNull(GastoValidator.validarFotoComprobante("content://boletas/foto1.jpg"))
    }

    @Test
    fun validarFotoComprobante_retornaError_cuandoEsNulaOVacia() {
        assertEquals("Debe adjuntar una fotografía del comprobante o boleta", GastoValidator.validarFotoComprobante(null))
        assertEquals("Debe adjuntar una fotografía del comprobante o boleta", GastoValidator.validarFotoComprobante("  "))
    }

    @Test
    fun validarCentroCosto_retornaNull_cuandoEsValido() {
        assertNull(GastoValidator.validarCentroCosto("CC-104"))
    }

    @Test
    fun validarCentroCosto_retornaError_cuandoEsInvalido() {
        assertEquals("El centro de costo es obligatorio", GastoValidator.validarCentroCosto(""))
        assertEquals("Ingrese un código de centro de costo válido (ej: CC-MANT-CLINICO-01)", GastoValidator.validarCentroCosto("CC"))
    }

    @Test
    fun validarComentarioRevision_exigeMotivoAlObservarORechazar() {
        // En aprobación no exige comentario
        assertNull(GastoValidator.validarComentarioRevision(RendicionEntity.ESTADO_APROBADA, ""))

        // Al observar exige mínimo 5 caracteres
        assertEquals(
            "Debe indicar un motivo detallado (mínimo 5 caracteres) para observar o rechazar",
            GastoValidator.validarComentarioRevision(RendicionEntity.ESTADO_OBSERVADA, "hola")
        )

        // Comentario adecuado
        assertNull(
            GastoValidator.validarComentarioRevision(RendicionEntity.ESTADO_RECHAZADA, "Boleta ilogible por favor adjuntar PDF")
        )
    }

    @Test
    fun validarCodigoPagoErp_validaCodigoCorrectamente() {
        assertNull(GastoValidator.validarCodigoPagoErp("ERP-PAG-2026-001"))
        assertEquals("El código de transferencia/comprobante ERP es obligatorio", GastoValidator.validarCodigoPagoErp(""))
        assertEquals("Código ERP demasiado corto (ej: ERP-TRF-2026-001)", GastoValidator.validarCodigoPagoErp("PAG"))
    }
}
