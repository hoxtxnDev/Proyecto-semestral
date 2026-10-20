package cl.duoc.teqgastos.ui.screens.finanzas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.domain.validators.GastoValidator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemReembolsoFinanzas(
    val rendicionId: Int,
    val nombreTecnico: String,
    val rutProveedor: String,
    val folioBoleta: String,
    val fechaFormateada: String,
    val categoria: String,
    val montoTotalClp: Double,
    val estado: String,
    val codigoPagoFinanzas: String?
)

data class FinanzasUiState(
    val codigoPago: String = "",
    val codigoPagoError: String? = null,
    val searchQuery: String = "",
    val mostrarPagadas: Boolean = false,
    val tabActiva: String = "Reportes",
    val isExporting: Boolean = false,
    val csvGenerado: String? = null,
    val mensajeExito: String? = null
)

class FinanzasViewModel(
    private val repository: TeqGastosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanzasUiState())
    val uiState: StateFlow<FinanzasUiState> = _uiState.asStateFlow()

    val itemsFinanzas: StateFlow<List<ItemReembolsoFinanzas>> = combine(
        repository.obtenerRendicionesPorEstados(
            listOf(
                RendicionEntity.ESTADO_APROBADA,
                RendicionEntity.ESTADO_PAGADA_ERP
            )
        ),
        repository.obtenerTodosLosGastos()
    ) { rendiciones, gastos ->
        val formatoFecha = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CL"))
        val gastosPorRendicion = gastos.groupBy { it.rendicionId }

        rendiciones.map { rendicion ->
            val primerGasto = gastosPorRendicion[rendicion.id]?.firstOrNull()
            ItemReembolsoFinanzas(
                rendicionId = rendicion.id,
                nombreTecnico = rendicion.nombreTecnico,
                rutProveedor = primerGasto?.rutProveedor ?: "76.123.456-7",
                folioBoleta = primerGasto?.folioBoleta?.let {
                    if (it.startsWith("F-") || it.startsWith("B-")) it else "B-$it"
                } ?: "B-00${rendicion.id}",
                fechaFormateada = formatoFecha.format(Date(rendicion.fechaCreacion)),
                categoria = primerGasto?.categoria ?: GastoEntity.CAT_PEAJE_ESTACIONAMIENTO,
                montoTotalClp = rendicion.montoTotalClp,
                estado = rendicion.estado,
                codigoPagoFinanzas = rendicion.codigoPagoFinanzas
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onCodigoPagoChange(nuevoCodigo: String) {
        val error = GastoValidator.validarCodigoPagoErp(nuevoCodigo)
        _uiState.update {
            it.copy(
                codigoPago = nuevoCodigo,
                codigoPagoError = error,
                mensajeExito = null
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun alternarFiltroPagadas() {
        _uiState.update { it.copy(mostrarPagadas = !it.mostrarPagadas) }
    }

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(tabActiva = tab) }
    }

    /**
     * Valida el código de pago ERP con `GastoValidator`, genera el contenido CSV
     * (`RUT,Folio,Categoria,Monto,CodigoPago`) y marca las rendiciones aprobadas como `PAGADA_ERP`.
     */
    fun exportarErpCsv() {
        val actual = _uiState.value
        val errorCodigo = GastoValidator.validarCodigoPagoErp(actual.codigoPago)
        if (errorCodigo != null) {
            _uiState.update { it.copy(codigoPagoError = errorCodigo) }
            return
        }

        val aprobadas = itemsFinanzas.value.filter { it.estado == RendicionEntity.ESTADO_APROBADA }
        val listaParaExportar = if (aprobadas.isNotEmpty()) aprobadas else itemsFinanzas.value

        if (listaParaExportar.isEmpty()) {
            _uiState.update {
                it.copy(codigoPagoError = "No existen rendiciones aprobadas para exportar")
            }
            return
        }

        val codigoLimpio = actual.codigoPago.trim()
        val filasCsv = buildList {
            add("RUT,Folio,Categoria,Monto,CodigoPago")
            listaParaExportar.forEach { item ->
                add("${item.rutProveedor},${item.folioBoleta},${item.categoria},${item.montoTotalClp.toLong()},$codigoLimpio")
            }
        }.joinToString("\n")

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            if (aprobadas.isNotEmpty()) {
                repository.procesarPagoLoteFinanzas(
                    rendicionIds = aprobadas.map { it.rendicionId },
                    codigoPagoFinanzas = codigoLimpio
                )
            }
            _uiState.update {
                it.copy(
                    isExporting = false,
                    mostrarPagadas = true,
                    csvGenerado = filasCsv,
                    mensajeExito = "Archivo teqgastos_erp.csv generado y ${listaParaExportar.size} rendición(es) cerrada(s) con código $codigoLimpio."
                )
            }
        }
    }
}

class FinanzasViewModelFactory(
    private val repository: TeqGastosRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanzasViewModel::class.java)) {
            return FinanzasViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
