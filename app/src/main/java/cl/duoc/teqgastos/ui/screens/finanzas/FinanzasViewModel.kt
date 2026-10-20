package cl.duoc.teqgastos.ui.screens.finanzas

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.domain.validators.GastoValidator
import cl.duoc.teqgastos.utils.CsvExporter
import cl.duoc.teqgastos.utils.ResultadoExportacionCsv
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
    val centroCosto: String,
    val medioPago: String,
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
    val resultadoCsvDialog: ResultadoExportacionCsv? = null,
    val csvGenerado: String? = null,
    val mensajeExito: String? = null
)

/**
 * ViewModel para el rol FINANZAS (Fase 7).
 * Filtra las rendiciones en estado "APROBADA", valida el código de transferencia/reembolso ERP,
 * genera un archivo `.CSV` real en el dispositivo mediante `CsvExporter` y cierra contablemente
 * las rendiciones en Room (`PAGADA_ERP`).
 */
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
                centroCosto = rendicion.centroCosto,
                medioPago = rendicion.medioPago,
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

    fun cerrarDialogoCsv() {
        _uiState.update { it.copy(resultadoCsvDialog = null) }
    }

    /**
     * Valida el código de transferencia ERP con `GastoValidator`, genera el archivo `.CSV` real
     * en el almacenamiento del dispositivo mediante `CsvExporter` y actualiza las rendiciones
     * aprobadas a `PAGADA_ERP` en Room.
     */
    fun exportarErpCsv(context: Context) {
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

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            val resultado = CsvExporter.generarArchivoCsvErp(
                context = context,
                items = listaParaExportar,
                codigoPagoErp = codigoLimpio
            )

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
                    resultadoCsvDialog = resultado,
                    csvGenerado = resultado.contenidoCsv,
                    mensajeExito = "Archivo ${resultado.nombreArchivo} guardado y ${resultado.cantidadRegistros} rendición(es) pagada(s) en ERP."
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
