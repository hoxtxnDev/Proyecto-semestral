package cl.duoc.teqgastos.ui.screens.tecnico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.domain.validators.GastoValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 * Estado inmutable del formulario y bandeja de Técnico.
 */
data class TecnicoUiState(
    val centroCostoActivo: String = "CC-104 · Operaciones Norte",
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
    val latitud: Double = -33.4489,
    val longitud: Double = -70.6693,
    val searchQuery: String = "",
    val tabActiva: String = "Rendiciones",
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
 * ViewModel para la vista de Técnico (`TechnicianScreen` de Figma).
 * Orquesta todas las validaciones en tiempo real mediante `GastoValidator` y persiste en Room.
 */
class TecnicoViewModel(
    private val repository: TeqGastosRepository,
    private val tecnicoId: Int,
    private val nombreTecnico: String
) : ViewModel() {

    private val centrosCostoDisponibles = listOf(
        "CC-104 · Operaciones Norte",
        "CC-MANT-CLINICO-01 · Hospital Central",
        "CC-208 · Soporte Biomédico Sur"
    )

    private val _uiState = MutableStateFlow(TecnicoUiState())
    val uiState: StateFlow<TecnicoUiState> = _uiState.asStateFlow()

    val misRendiciones: StateFlow<List<RendicionEntity>> = repository
        .obtenerTodasLasRendiciones()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    fun onToggleFotoComprobante(uriPersonalizada: String? = null) {
        _uiState.update { actual ->
            val nuevaUri = when {
                uriPersonalizada != null -> uriPersonalizada
                actual.fotoUri == null -> "content://teqgastos/comprobantes/IMG_3841.jpg"
                else -> null
            }
            actual.copy(
                fotoUri = nuevaUri,
                fotoError = GastoValidator.validarFotoComprobante(nuevaUri),
                mensajeErrorGeneral = null
            )
        }
    }

    fun onActualizarUbicacion(lat: Double, lon: Double) {
        _uiState.update {
            it.copy(latitud = lat, longitud = lon)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(tabActiva = tab, mensajeExito = null, mensajeErrorGeneral = null) }
    }

    /**
     * Ejecuta todas las reglas de `GastoValidator`, verifica anti-duplicidad en Room
     * y registra la rendición para evaluación de Jefatura.
     */
    fun enviarRendicion() {
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
                    mensajeErrorGeneral = "Revisa los campos con error antes de enviar la rendición.",
                    mensajeExito = null
                )
            }
            return
        }

        val montoOrig = actual.montoOriginalNumerico
        val montoClp = actual.montoConvertidoClp

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, mensajeErrorGeneral = null, mensajeExito = null) }

            // Control Anti-Fraude: Verificar si la boleta (RUT + Folio + Monto) ya fue rendida en Room
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
                        mensajeErrorGeneral = "Alerta Anti-Fraude: La boleta Folio ${actual.folioBoleta} del RUT ${actual.rutProveedor} ya existe en el sistema."
                    )
                }
                return@launch
            }

            repository.registrarRendicionConGasto(
                tecnicoId = tecnicoId,
                nombreTecnico = nombreTecnico,
                centroCosto = actual.centroCostoActivo,
                rutProveedor = actual.rutProveedor.trim(),
                folioBoleta = actual.folioBoleta.trim(),
                fechaGastoMillis = actual.fechaGastoMillis,
                categoria = actual.categoriaSeleccionada.codigo,
                moneda = actual.moneda,
                montoOriginal = montoOrig,
                montoConvertidoClp = montoClp,
                fotoUri = actual.fotoUri,
                latitud = actual.latitud,
                longitud = actual.longitud
            )

            _uiState.update {
                TecnicoUiState(
                    centroCostoActivo = actual.centroCostoActivo,
                    mensajeExito = "¡Rendición enviada exitosamente por ${GastoValidator.formatearClp(montoClp)} CLP a Jefatura!"
                )
            }
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
