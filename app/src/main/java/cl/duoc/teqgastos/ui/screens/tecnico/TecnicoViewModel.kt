package cl.duoc.teqgastos.ui.screens.tecnico

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.domain.validators.GastoValidator
import cl.duoc.teqgastos.utils.NativeResourcesHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Definición de las 5 categorías del diseño Figma (`src-interface/App.tsx`) vinculadas a `GastoEntity`.
 */
enum class CategoriaGastoUi(
    val codigo: String,
    val nombreVisible: String,
    val topeEtiqueta: String
) {
    ALIMENTACION(GastoEntity.CAT_ALIMENTACION, "Alimentación", "$15.000/día"),
    ALOJAMIENTO(GastoEntity.CAT_ALOJAMIENTO, "Alojamiento", "$65.000/noche"),
    COMBUSTIBLE(GastoEntity.CAT_COMBUSTIBLE, "Combustible", "$60.000/carga"),
    PEAJE_ESTAC(GastoEntity.CAT_PEAJE_ESTACIONAMIENTO, "Peaje / Estac.", "$25.000/evento"),
    INSUMOS(GastoEntity.CAT_INSUMOS, "Insumos", "$50.000/compra")
}

/**
 * Estado reactivo del flujo de Técnico (Fase 5 y Fase 6 - Indicador IE 2.4.1).
 * Soporta tanto `TecnicoHomeScreen` (bandeja de rendiciones, creación de rendición e importación TeqRuta)
 * como `FormularioGastoScreen` (validación GastoValidator, cámara FileProvider, GPS FusedLocation y anti-duplicados).
 */
data class TecnicoUiState(
    val rendicionSeleccionadaId: Int? = null,
    val centroCostoActivo: String = "CC-104 · Operaciones Concepción",
    val medioPagoActivo: String = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
    val mostrarDialogoNuevaRendicion: Boolean = false,
    val nuevoCentroCostoInput: String = "CC-104 · Operaciones Concepción",
    val nuevoCentroCostoError: String? = null,
    val nuevoMedioPagoInput: String = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
    val rutProveedor: String = "",
    val rutError: String? = null,
    val folioBoleta: String = "",
    val folioError: String? = null,
    val montoTexto: String = "",
    val montoError: String? = null,
    val moneda: String = GastoEntity.MONEDA_CLP,
    val categoriaSeleccionada: CategoriaGastoUi = CategoriaGastoUi.ALIMENTACION,
    val fechaGastoMillis: Long = System.currentTimeMillis(),
    val fechaError: String? = null,
    val fotoUri: String? = null,
    val fotoError: String? = null,
    val latitud: Double = NativeResourcesHelper.DEFAULT_LAT_CONCEPCION,
    val longitud: Double = NativeResourcesHelper.DEFAULT_LON_CONCEPCION,
    val ubicacionCapturada: Boolean = false,
    val capturandoGps: Boolean = false,
    val searchQuery: String = "",
    val tabActiva: String = "Inicio",
    val isSubmitting: Boolean = false,
    val mensajeExito: String? = null,
    val mensajeErrorGeneral: String? = null
) {
    val montoOriginalNumerico: Double
        get() = montoTexto.trim().replace(",", ".").toDoubleOrNull() ?: 0.0

    val montoConvertidoClp: Double
        get() = GastoValidator.convertirAClp(montoOriginalNumerico, moneda)
}

/**
 * ViewModel para el flujo de Técnico en Terreno (`TecnicoHomeScreen` y `FormularioGastoScreen`).
 */
class TecnicoViewModel(
    private val repository: TeqGastosRepository,
    private val tecnicoId: Int,
    private val nombreTecnico: String
) : ViewModel() {

    val centrosCostoDisponibles = listOf(
        "CC-104 · Operaciones Concepción",
        "CC-MANT-CLINICO-01 · Hospital Regional",
        "CC-208 · Soporte Biomédico Sur"
    )

    val mediosPagoDisponibles = listOf(
        RendicionEntity.MEDIO_FONDO_POR_RENDIR to "Fondo por Rendir",
        RendicionEntity.MEDIO_TARJETA_CORPORATIVA to "Tarjeta Corporativa",
        RendicionEntity.MEDIO_DINERO_PROPIO to "Dinero Propio (Reembolso)"
    )

    private val _uiState = MutableStateFlow(TecnicoUiState())
    val uiState: StateFlow<TecnicoUiState> = _uiState.asStateFlow()

    /** Lista reactiva de rendiciones en Room. */
    val misRendiciones: StateFlow<List<RendicionEntity>> = repository
        .obtenerTodasLasRendiciones()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Mapa reactivo `rendicionId -> List<GastoEntity>` para expandir tarjetas con `animateContentSize`. */
    val gastosPorRendicion: StateFlow<Map<Int, List<GastoEntity>>> = repository
        .obtenerTodosLosGastos()
        .map { lista -> lista.groupBy { it.rendicionId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // =========================================================================
    // 1. GESTIÓN DE RENDICIONES Y TRASLADO TEQRUTA (TecnicoHomeScreen)
    // =========================================================================

    fun abrirDialogoNuevaRendicion() {
        _uiState.update {
            it.copy(
                mostrarDialogoNuevaRendicion = true,
                nuevoCentroCostoInput = it.centroCostoActivo,
                nuevoCentroCostoError = null,
                nuevoMedioPagoInput = it.medioPagoActivo
            )
        }
    }

    fun cerrarDialogoNuevaRendicion() {
        _uiState.update {
            it.copy(
                mostrarDialogoNuevaRendicion = false,
                nuevoCentroCostoError = null
            )
        }
    }

    fun onNuevoCentroCostoChange(valor: String) {
        _uiState.update {
            it.copy(
                nuevoCentroCostoInput = valor,
                nuevoCentroCostoError = GastoValidator.validarCentroCosto(valor)
            )
        }
    }

    fun onNuevoMedioPagoChange(medioPago: String) {
        _uiState.update { it.copy(nuevoMedioPagoInput = medioPago) }
    }

    /**
     * Crea una nueva carpeta de rendición en estado `BORRADOR` seleccionando Centro de Costo y Medio de Pago.
     */
    fun crearNuevaRendicion(onCreated: (Int) -> Unit = {}) {
        val actual = _uiState.value
        val errorCc = GastoValidator.validarCentroCosto(actual.nuevoCentroCostoInput)
        if (errorCc != null) {
            _uiState.update { it.copy(nuevoCentroCostoError = errorCc) }
            return
        }

        viewModelScope.launch {
            val nuevoId = repository.crearRendicion(
                RendicionEntity(
                    tecnicoId = tecnicoId,
                    nombreTecnico = nombreTecnico,
                    centroCosto = actual.nuevoCentroCostoInput.trim(),
                    medioPago = actual.nuevoMedioPagoInput,
                    fechaCreacion = System.currentTimeMillis(),
                    montoTotalClp = 0.0,
                    estado = RendicionEntity.ESTADO_BORRADOR
                )
            ).toInt()

            _uiState.update {
                it.copy(
                    mostrarDialogoNuevaRendicion = false,
                    rendicionSeleccionadaId = nuevoId,
                    centroCostoActivo = actual.nuevoCentroCostoInput.trim(),
                    medioPagoActivo = actual.nuevoMedioPagoInput,
                    mensajeExito = "Nueva rendición #$nuevoId creada (${actual.nuevoCentroCostoInput.trim()}).",
                    mensajeErrorGeneral = null
                )
            }
            onCreated(nuevoId)
        }
    }

    /**
     * FASE 6: Botón "Importar Traslado TeqRuta".
     * Inyecta automáticamente en Room un gasto de Peaje ($4.800 CLP) y otro de Combustible ($22.500 CLP)
     * pre-calculados desde la app hermana TeqRuta con `origenTeqRuta = true` y GPS de Concepción.
     */
    fun importarTrasladoTeqRuta(rendicionIdDestino: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, mensajeErrorGeneral = null, mensajeExito = null) }

            val ahora = System.currentTimeMillis()
            val sufijoFolio = (ahora % 10000L).toInt().coerceAtLeast(100)

            // Busca una rendición en BORRADOR existente o crea una dedicada al traslado TeqRuta
            val rendicionId = rendicionIdDestino
                ?: misRendiciones.value.firstOrNull { it.estado == RendicionEntity.ESTADO_BORRADOR }?.id
                ?: repository.crearRendicion(
                    RendicionEntity(
                        tecnicoId = tecnicoId,
                        nombreTecnico = nombreTecnico,
                        centroCosto = _uiState.value.centroCostoActivo,
                        medioPago = _uiState.value.medioPagoActivo,
                        fechaCreacion = ahora,
                        montoTotalClp = 0.0,
                        estado = RendicionEntity.ESTADO_BORRADOR
                    )
                ).toInt()

            // 1. Gasto pre-calculado de Peaje (Ruta Itata / Agua Amarilla - Concepción)
            val gastoPeaje = GastoEntity(
                rendicionId = rendicionId,
                rutProveedor = "76.543.210-K",
                folioBoleta = "80$sufijoFolio",
                fechaGastoMillis = ahora,
                categoria = GastoEntity.CAT_PEAJE_ESTACIONAMIENTO,
                moneda = GastoEntity.MONEDA_CLP,
                montoOriginal = 4800.0,
                montoConvertidoClp = 4800.0,
                fotoUri = "content://cl.duoc.teqgastos.fileprovider/boletas_cache/teqruta_peaje_$sufijoFolio.jpg",
                latitud = NativeResourcesHelper.DEFAULT_LAT_CONCEPCION,
                longitud = NativeResourcesHelper.DEFAULT_LON_CONCEPCION,
                origenTeqRuta = true
            )

            // 2. Gasto pre-calculado de Combustible (Traslado Técnico en Terreno - Concepción)
            val gastoCombustible = GastoEntity(
                rendicionId = rendicionId,
                rutProveedor = "99.520.000-7",
                folioBoleta = "90$sufijoFolio",
                fechaGastoMillis = ahora,
                categoria = GastoEntity.CAT_COMBUSTIBLE,
                moneda = GastoEntity.MONEDA_CLP,
                montoOriginal = 22500.0,
                montoConvertidoClp = 22500.0,
                fotoUri = "content://cl.duoc.teqgastos.fileprovider/boletas_cache/teqruta_combustible_$sufijoFolio.jpg",
                latitud = NativeResourcesHelper.DEFAULT_LAT_CONCEPCION,
                longitud = NativeResourcesHelper.DEFAULT_LON_CONCEPCION,
                origenTeqRuta = true
            )

            repository.insertarGasto(gastoPeaje)
            repository.insertarGasto(gastoCombustible)

            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    rendicionSeleccionadaId = rendicionId,
                    mensajeExito = "Traslado TeqRuta importado en Rendición #$rendicionId: Peaje ($4.800) + Combustible ($22.500) = $27.300 CLP."
                )
            }
        }
    }

    /**
     * Envía una rendición en estado BORRADOR u OBSERVADA a evaluación de Jefatura (`PENDIENTE_JEFATURA`).
     */
    fun enviarRendicionAJefatura(rendicionId: Int) {
        viewModelScope.launch {
            repository.enviarRendicionAJefatura(rendicionId)
            _uiState.update {
                it.copy(
                    mensajeExito = "Rendición #$rendicionId enviada exitosamente a evaluación de Jefatura.",
                    mensajeErrorGeneral = null
                )
            }
        }
    }

    fun eliminarGastoDeRendicion(gastoId: Int, rendicionId: Int) {
        viewModelScope.launch {
            repository.eliminarGasto(gastoId, rendicionId)
            _uiState.update {
                it.copy(mensajeExito = "Gasto eliminado y monto total actualizado.")
            }
        }
    }

    // =========================================================================
    // 2. FORMULARIO DE GASTO CON CÁMARA Y GPS (FormularioGastoScreen)
    // =========================================================================

    fun prepararFormularioParaRendicion(rendicionId: Int?) {
        val rendicion = misRendiciones.value.find { it.id == rendicionId }
        _uiState.update { actual ->
            actual.copy(
                rendicionSeleccionadaId = rendicionId,
                centroCostoActivo = rendicion?.centroCosto ?: actual.centroCostoActivo,
                medioPagoActivo = rendicion?.medioPago ?: actual.medioPagoActivo,
                rutProveedor = "",
                rutError = null,
                folioBoleta = "",
                folioError = null,
                montoTexto = "",
                montoError = null,
                moneda = GastoEntity.MONEDA_CLP,
                categoriaSeleccionada = CategoriaGastoUi.ALIMENTACION,
                fechaGastoMillis = System.currentTimeMillis(),
                fechaError = null,
                fotoUri = null,
                fotoError = null,
                latitud = NativeResourcesHelper.DEFAULT_LAT_CONCEPCION,
                longitud = NativeResourcesHelper.DEFAULT_LON_CONCEPCION,
                ubicacionCapturada = false,
                tabActiva = "Rendiciones",
                mensajeExito = null,
                mensajeErrorGeneral = null
            )
        }
    }

    fun alternarCentroCosto() {
        _uiState.update { actual ->
            val idx = centrosCostoDisponibles.indexOf(actual.centroCostoActivo)
            val siguiente = centrosCostoDisponibles[(idx + 1) % centrosCostoDisponibles.size]
            actual.copy(centroCostoActivo = siguiente)
        }
    }

    fun onRutChange(nuevoRut: String) {
        val error = GastoValidator.validarRutProveedor(nuevoRut)
        _uiState.update {
            it.copy(
                rutProveedor = nuevoRut,
                rutError = error,
                mensajeErrorGeneral = null,
                mensajeExito = null
            )
        }
    }

    fun onFolioChange(nuevoFolio: String) {
        val error = GastoValidator.validarFolioBoleta(nuevoFolio)
        _uiState.update {
            it.copy(
                folioBoleta = nuevoFolio,
                folioError = error,
                mensajeErrorGeneral = null,
                mensajeExito = null
            )
        }
    }

    fun onMontoChange(nuevoMonto: String) {
        val actual = _uiState.value
        val error = GastoValidator.validarMontoTextoSegunCategoria(
            montoTexto = nuevoMonto,
            categoria = actual.categoriaSeleccionada.codigo,
            moneda = actual.moneda
        )
        _uiState.update {
            it.copy(
                montoTexto = nuevoMonto,
                montoError = error,
                mensajeErrorGeneral = null,
                mensajeExito = null
            )
        }
    }

    fun onMonedaChange(nuevaMoneda: String) {
        val actual = _uiState.value
        val errorMonto = if (actual.montoTexto.isNotBlank()) {
            GastoValidator.validarMontoTextoSegunCategoria(
                montoTexto = actual.montoTexto,
                categoria = actual.categoriaSeleccionada.codigo,
                moneda = nuevaMoneda
            )
        } else {
            null
        }
        _uiState.update {
            it.copy(
                moneda = nuevaMoneda,
                montoError = errorMonto,
                mensajeErrorGeneral = null
            )
        }
    }

    fun onCategoriaChange(nuevaCategoria: CategoriaGastoUi) {
        val actual = _uiState.value
        val errorMonto = if (actual.montoTexto.isNotBlank()) {
            GastoValidator.validarMontoTextoSegunCategoria(
                montoTexto = actual.montoTexto,
                categoria = nuevaCategoria.codigo,
                moneda = actual.moneda
            )
        } else {
            null
        }
        _uiState.update {
            it.copy(
                categoriaSeleccionada = nuevaCategoria,
                montoError = errorMonto,
                mensajeErrorGeneral = null
            )
        }
    }

    fun onFechaChange(nuevaFechaMillis: Long) {
        val errorFecha = GastoValidator.validarFechaGasto(nuevaFechaMillis)
        _uiState.update {
            it.copy(
                fechaGastoMillis = nuevaFechaMillis,
                fechaError = errorFecha,
                mensajeErrorGeneral = null
            )
        }
    }

    /**
     * Registra la URI devuelta por `ActivityResultContracts.TakePicture()` tras capturar la fotografía
     * de la boleta mediante `FileProvider`.
     */
    fun onFotoCapturada(uriString: String?) {
        _uiState.update {
            it.copy(
                fotoUri = uriString,
                fotoError = GastoValidator.validarFotoComprobante(uriString),
                mensajeErrorGeneral = null
            )
        }
    }

    fun onToggleFotoComprobante(uriPersonalizada: String? = null) {
        _uiState.update { actual ->
            val nuevaUri = when {
                uriPersonalizada != null -> uriPersonalizada
                actual.fotoUri == null -> "content://cl.duoc.teqgastos.fileprovider/boletas_cache/IMG_3841.jpg"
                else -> null
            }
            actual.copy(
                fotoUri = nuevaUri,
                fotoError = GastoValidator.validarFotoComprobante(nuevaUri),
                mensajeErrorGeneral = null
            )
        }
    }

    /**
     * Captura las coordenadas GPS reales del dispositivo usando `NativeResourcesHelper.obtenerUbicacionActual(context)`
     * o asigna las coordenadas por defecto de Concepción (`-36.8270, -73.0503`) si el emulador no tiene GPS activo.
     */
    fun capturarUbicacionGps(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(capturandoGps = true) }
            val (lat, lon) = NativeResourcesHelper.obtenerUbicacionActual(context)
            _uiState.update {
                it.copy(
                    latitud = lat,
                    longitud = lon,
                    ubicacionCapturada = true,
                    capturandoGps = false
                )
            }
        }
    }

    fun onActualizarUbicacion(lat: Double, lon: Double) {
        _uiState.update {
            it.copy(latitud = lat, longitud = lon, ubicacionCapturada = true)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(tabActiva = tab, mensajeExito = null, mensajeErrorGeneral = null) }
    }

    /**
     * Valida todos los campos del formulario con `GastoValidator`, verifica en Room que la boleta
     * no esté duplicada (`existeBoletaDuplicada`) y guarda el gasto en la rendición activa o la envía.
     */
    fun enviarRendicion(onSuccess: () -> Unit = {}) {
        val actual = _uiState.value
        val errorRut = GastoValidator.validarRutProveedor(actual.rutProveedor)
        val errorFolio = GastoValidator.validarFolioBoleta(actual.folioBoleta)
        val errorMonto = GastoValidator.validarMontoTextoSegunCategoria(
            montoTexto = actual.montoTexto,
            categoria = actual.categoriaSeleccionada.codigo,
            moneda = actual.moneda
        )
        val errorFecha = GastoValidator.validarFechaGasto(actual.fechaGastoMillis)
        val errorFoto = GastoValidator.validarFotoComprobante(actual.fotoUri)

        if (errorRut != null || errorFolio != null || errorMonto != null || errorFecha != null || errorFoto != null) {
            _uiState.update {
                it.copy(
                    rutError = errorRut,
                    folioError = errorFolio,
                    montoError = errorMonto,
                    fechaError = errorFecha,
                    fotoError = errorFoto,
                    mensajeErrorGeneral = "Revisa los campos marcados en rojo antes de guardar la boleta.",
                    mensajeExito = null
                )
            }
            return
        }

        val montoOrig = actual.montoOriginalNumerico
        val montoClp = actual.montoConvertidoClp

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, mensajeErrorGeneral = null, mensajeExito = null) }

            // Control Anti-Fraude: Verificar en Room si la boleta (RUT + Folio + Monto) está duplicada
            val duplicada = repository.existeBoletaDuplicada(
                rutProveedor = actual.rutProveedor,
                folioBoleta = actual.folioBoleta,
                montoOriginal = montoOrig
            )
            if (duplicada) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        folioError = "Boleta duplicada: este RUT, Folio y Monto ya fue rendido",
                        mensajeErrorGeneral = "Alerta Anti-Fraude: La boleta Folio ${actual.folioBoleta} del RUT ${actual.rutProveedor} ya existe en Room."
                    )
                }
                return@launch
            }

            val rendicionExistenteId = actual.rendicionSeleccionadaId
            if (rendicionExistenteId != null) {
                repository.insertarGasto(
                    GastoEntity(
                        rendicionId = rendicionExistenteId,
                        rutProveedor = actual.rutProveedor.trim(),
                        folioBoleta = actual.folioBoleta.trim(),
                        fechaGastoMillis = actual.fechaGastoMillis,
                        categoria = actual.categoriaSeleccionada.codigo,
                        moneda = actual.moneda,
                        montoOriginal = montoOrig,
                        montoConvertidoClp = montoClp,
                        fotoUri = actual.fotoUri,
                        latitud = actual.latitud,
                        longitud = actual.longitud,
                        origenTeqRuta = false
                    )
                )
            } else {
                repository.registrarRendicionConGasto(
                    tecnicoId = tecnicoId,
                    nombreTecnico = nombreTecnico,
                    centroCosto = actual.centroCostoActivo,
                    medioPago = actual.medioPagoActivo,
                    rutProveedor = actual.rutProveedor.trim(),
                    folioBoleta = actual.folioBoleta.trim(),
                    fechaGastoMillis = actual.fechaGastoMillis,
                    categoria = actual.categoriaSeleccionada.codigo,
                    moneda = actual.moneda,
                    montoOriginal = montoOrig,
                    montoConvertidoClp = montoClp,
                    fotoUri = actual.fotoUri,
                    latitud = actual.latitud,
                    longitud = actual.longitud,
                    origenTeqRuta = false
                )
            }

            _uiState.update {
                TecnicoUiState(
                    centroCostoActivo = actual.centroCostoActivo,
                    medioPagoActivo = actual.medioPagoActivo,
                    tabActiva = "Inicio",
                    mensajeExito = "¡Boleta Folio ${actual.folioBoleta} registrada por ${GastoValidator.formatearClp(montoClp)} CLP!"
                )
            }
            onSuccess()
        }
    }
}

class TecnicoViewModelFactory(
    private val repository: TeqGastosRepository,
    private val tecnicoId: Int,
    private val nombreTecnico: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TecnicoViewModel::class.java)) {
            return TecnicoViewModel(repository, tecnicoId, nombreTecnico) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
