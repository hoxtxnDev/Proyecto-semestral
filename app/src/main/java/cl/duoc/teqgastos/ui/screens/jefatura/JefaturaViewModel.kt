package cl.duoc.teqgastos.ui.screens.jefatura

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AccionRevisionJefatura(val etiqueta: String, val estadoDestino: String) {
    OBSERVAR("Observar", RendicionEntity.ESTADO_OBSERVADA),
    RECHAZAR("Rechazar", RendicionEntity.ESTADO_RECHAZADA)
}

/**
 * Detalle auditado de un ítem de gasto individual para la vista de inspección de Jefatura (Fase 7).
 */
data class GastoAuditadoUi(
    val gasto: GastoEntity,
    val cumpleTope: Boolean,
    val detalleTope: String,
    val topeMaximoClp: Double
)

/**
 * Modelo enriquecido para las tarjetas de evaluación de Jefatura (`ManagerScreen` de Figma).
 * Incluye el desglose completo de gastos (`gastosAuditados`), fotos, coordenadas GPS y validación de topes.
 */
data class ItemEvaluacionJefatura(
    val rendicionId: Int,
    val nombreTecnico: String,
    val iniciales: String,
    val centroCosto: String,
    val medioPago: String,
    val rutProveedorPrincipal: String,
    val folioPrincipal: String,
    val categoriaPrincipal: String,
    val montoTotalClp: Double,
    val estado: String,
    val comentarioRevision: String?,
    val gastosAuditados: List<GastoAuditadoUi>,
    val todosCumplenTope: Boolean
)

data class JefaturaUiState(
    val searchQuery: String = "",
    val mostrarSoloPendientes: Boolean = true,
    val tabActiva: String = "Inicio",
    val rendicionExpandidaId: Int? = null,
    val gastoInspeccionado: GastoAuditadoUi? = null,
    val itemEnModal: ItemEvaluacionJefatura? = null,
    val accionModal: AccionRevisionJefatura? = null,
    val comentarioModal: String = "",
    val comentarioError: String? = null,
    val mensajeFeedback: String? = null
)

/**
 * ViewModel para el rol JEFATURA (Fase 7).
 * Lista todas las rendiciones enviadas, expone el desglose de gastos con auditoría de topes,
 * foto de boleta y GPS, y exige comentario obligatorio validado antes de Observar o Rechazar en Room.
 */
class JefaturaViewModel(
    private val repository: TeqGastosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JefaturaUiState())
    val uiState: StateFlow<JefaturaUiState> = _uiState.asStateFlow()

    val itemsEvaluacion: StateFlow<List<ItemEvaluacionJefatura>> = combine(
        repository.obtenerTodasLasRendiciones(),
        repository.obtenerTodosLosGastos()
    ) { rendiciones, gastos ->
        val gastosPorRendicion = gastos.groupBy { it.rendicionId }
        rendiciones.map { rendicion ->
            val listaGastos = gastosPorRendicion[rendicion.id].orEmpty()
            val gastosAuditados = listaGastos.map { g ->
                val errorTope = GastoValidator.validarTopePorCategoria(
                    categoria = g.categoria,
                    montoOriginal = g.montoOriginal,
                    moneda = g.moneda
                )
                val topeMax = GastoValidator.obtenerTopeCategoriaClp(g.categoria) ?: 0.0
                GastoAuditadoUi(
                    gasto = g,
                    cumpleTope = (errorTope == null),
                    detalleTope = errorTope ?: "Cumple tope (${GastoValidator.formatearClp(topeMax)} CLP)",
                    topeMaximoClp = topeMax
                )
            }

            val primerGasto = listaGastos.firstOrNull()
            val partesNombre = rendicion.nombreTecnico.trim().split(" ").filter { it.isNotBlank() }
            val iniciales = partesNombre.take(2).joinToString("") { it.take(1).uppercase() }.ifEmpty { "TQ" }

            ItemEvaluacionJefatura(
                rendicionId = rendicion.id,
                nombreTecnico = rendicion.nombreTecnico,
                iniciales = iniciales,
                centroCosto = rendicion.centroCosto,
                medioPago = rendicion.medioPago,
                rutProveedorPrincipal = primerGasto?.rutProveedor ?: "76.123.456-7",
                folioPrincipal = primerGasto?.folioBoleta?.let {
                    if (it.startsWith("F-") || it.startsWith("B-")) it else "F-$it"
                } ?: "F-00${rendicion.id}",
                categoriaPrincipal = primerGasto?.categoria ?: GastoEntity.CAT_ALIMENTACION,
                montoTotalClp = rendicion.montoTotalClp,
                estado = rendicion.estado,
                comentarioRevision = rendicion.comentarioRevision,
                gastosAuditados = gastosAuditados,
                todosCumplenTope = gastosAuditados.all { it.cumpleTope }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun alternarFiltroPendientes() {
        _uiState.update { it.copy(mostrarSoloPendientes = !it.mostrarSoloPendientes) }
    }

    fun onTabSelected(tab: String) {
        _uiState.update {
            it.copy(
                tabActiva = tab,
                mostrarSoloPendientes = (tab == "Inicio" || tab == "Rendiciones"),
                mensajeFeedback = null
            )
        }
    }

    fun alternarExpansionRendicion(rendicionId: Int) {
        _uiState.update { actual ->
            actual.copy(
                rendicionExpandidaId = if (actual.rendicionExpandidaId == rendicionId) null else rendicionId
            )
        }
    }

    fun inspeccionarGasto(gastoAuditado: GastoAuditadoUi?) {
        _uiState.update { it.copy(gastoInspeccionado = gastoAuditado) }
    }

    fun aprobarRendicion(item: ItemEvaluacionJefatura) {
        viewModelScope.launch {
            repository.resolverRevisionJefatura(
                rendicionId = item.rendicionId,
                nuevoEstado = RendicionEntity.ESTADO_APROBADA,
                comentarioRevision = "Aprobado por Jefatura conforme a política TEQMED SPA"
            )
            _uiState.update {
                it.copy(
                    mensajeFeedback = "Rendición #${item.rendicionId} de ${item.nombreTecnico} APROBADA y enviada a Finanzas."
                )
            }
        }
    }

    fun abrirModalRevision(item: ItemEvaluacionJefatura, accion: AccionRevisionJefatura) {
        _uiState.update {
            it.copy(
                itemEnModal = item,
                accionModal = accion,
                comentarioModal = "",
                comentarioError = null,
                mensajeFeedback = null
            )
        }
    }

    fun cerrarModalRevision() {
        _uiState.update {
            it.copy(
                itemEnModal = null,
                accionModal = null,
                comentarioModal = "",
                comentarioError = null
            )
        }
    }

    fun onComentarioModalChange(nuevoComentario: String) {
        val limitado = nuevoComentario.take(240)
        val estadoDestino = _uiState.value.accionModal?.estadoDestino ?: RendicionEntity.ESTADO_OBSERVADA
        val error = GastoValidator.validarComentarioRevision(estadoDestino, limitado)
        _uiState.update {
            it.copy(
                comentarioModal = limitado,
                comentarioError = error
            )
        }
    }

    /**
     * Valida obligatoriamente el comentario de observación/rechazo mediante `GastoValidator`
     * antes de actualizar el estado en Room.
     */
    fun confirmarAccionModal() {
        val actual = _uiState.value
        val item = actual.itemEnModal ?: return
        val accion = actual.accionModal ?: return

        val error = GastoValidator.validarComentarioRevision(accion.estadoDestino, actual.comentarioModal)
        if (error != null) {
            _uiState.update { it.copy(comentarioError = error) }
            return
        }

        viewModelScope.launch {
            repository.resolverRevisionJefatura(
                rendicionId = item.rendicionId,
                nuevoEstado = accion.estadoDestino,
                comentarioRevision = actual.comentarioModal.trim()
            )
            _uiState.update {
                it.copy(
                    itemEnModal = null,
                    accionModal = null,
                    comentarioModal = "",
                    comentarioError = null,
                    mensajeFeedback = "Rendición #${item.rendicionId} marcada como ${accion.estadoDestino}."
                )
            }
        }
    }
}

class JefaturaViewModelFactory(
    private val repository: TeqGastosRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JefaturaViewModel::class.java)) {
            return JefaturaViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
