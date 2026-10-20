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
 * Modelo enriquecido para las tarjetas de evaluación de Jefatura (`ManagerScreen` de Figma).
 */
data class ItemEvaluacionJefatura(
    val rendicionId: Int,
    val nombreTecnico: String,
    val iniciales: String,
    val rutProveedor: String,
    val folioBoleta: String,
    val categoria: String,
    val montoTotalClp: Double,
    val estado: String,
    val comentarioRevision: String?
)

data class JefaturaUiState(
    val searchQuery: String = "",
    val mostrarSoloPendientes: Boolean = true,
    val tabActiva: String = "Inicio",
    val itemEnModal: ItemEvaluacionJefatura? = null,
    val accionModal: AccionRevisionJefatura? = null,
    val comentarioModal: String = "",
    val comentarioError: String? = null
)

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
            val primerGasto = gastosPorRendicion[rendicion.id]?.firstOrNull()
            val partesNombre = rendicion.nombreTecnico.trim().split(" ").filter { it.isNotBlank() }
            val iniciales = partesNombre.take(2).joinToString("") { it.take(1).uppercase() }.ifEmpty { "TQ" }

            ItemEvaluacionJefatura(
                rendicionId = rendicion.id,
                nombreTecnico = rendicion.nombreTecnico,
                iniciales = iniciales,
                rutProveedor = primerGasto?.rutProveedor ?: "76.123.456-7",
                folioBoleta = primerGasto?.folioBoleta?.let {
                    if (it.startsWith("F-") || it.startsWith("B-")) it else "F-$it"
                } ?: "F-00${rendicion.id}",
                categoria = primerGasto?.categoria ?: GastoEntity.CAT_ALIMENTACION,
                montoTotalClp = rendicion.montoTotalClp,
                estado = rendicion.estado,
                comentarioRevision = rendicion.comentarioRevision
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
                mostrarSoloPendientes = (tab == "Inicio" || tab == "Rendiciones")
            )
        }
    }

    fun aprobarRendicion(rendicionId: Int) {
        viewModelScope.launch {
            repository.resolverRevisionJefatura(
                rendicionId = rendicionId,
                nuevoEstado = RendicionEntity.ESTADO_APROBADA,
                comentarioRevision = "Aprobado por Jefatura"
            )
        }
    }

    fun abrirModalRevision(item: ItemEvaluacionJefatura, accion: AccionRevisionJefatura) {
        _uiState.update {
            it.copy(
                itemEnModal = item,
                accionModal = accion,
                comentarioModal = "",
                comentarioError = null
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
            cerrarModalRevision()
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
