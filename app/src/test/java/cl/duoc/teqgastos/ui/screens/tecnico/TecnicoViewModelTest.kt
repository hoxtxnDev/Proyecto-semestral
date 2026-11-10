package cl.duoc.teqgastos.ui.screens.tecnico

import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.repository.FakeTeqGastosDao
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas unitarias para `TecnicoViewModel`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TecnicoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDao: FakeTeqGastosDao
    private lateinit var repository: TeqGastosRepository
    private lateinit var viewModel: TecnicoViewModel

    @Before
    fun setUp() = runTest {
        fakeDao = FakeTeqGastosDao()
        repository = TeqGastosRepository(fakeDao)
        viewModel = TecnicoViewModel(
            repository = repository,
            tecnicoId = 1,
            nombreTecnico = "Carlos Muñoz"
        )
    }

    @Test
    fun onRutChange_y_onFolioChange_validanEnTiempoReal() {
        viewModel.onRutChange("12345")
        assertNotNull("RUT inválido debe generar error", viewModel.uiState.value.rutError)

        viewModel.onRutChange("76.123.456-7")
        assertNull("RUT válido limpia el error", viewModel.uiState.value.rutError)

        viewModel.onFolioChange("0")
        assertNotNull("Folio 0 debe generar error", viewModel.uiState.value.folioError)

        viewModel.onFolioChange("80921")
        assertNull("Folio 80921 es válido", viewModel.uiState.value.folioError)
    }

    @Test
    fun onMontoChange_evaluaTopesPorCategoria() {
        // Categoria Alimentacion (tope $15.000 CLP)
        viewModel.onCategoriaChange(CategoriaGastoUi.ALIMENTACION)
        viewModel.onMontoChange("20000") // $20.000 CLP excede tope de $15.000
        assertNotNull("Monto $20.000 CLP excede tope de Alimentación", viewModel.uiState.value.montoError)

        viewModel.onMontoChange("12000")
        assertNull("Monto $12.000 CLP cumple tope de Alimentación", viewModel.uiState.value.montoError)
    }

    @Test
    fun crearNuevaRendicion_agregaBorradorEnRoom() = runTest {
        viewModel.abrirDialogoNuevaRendicion()
        viewModel.onNuevoCentroCostoChange("CC-104 · Operaciones Concepción")

        var rendicionCreadaId: Int? = null
        viewModel.crearNuevaRendicion { id -> rendicionCreadaId = id }

        val rendiciones = repository.obtenerTodasLasRendiciones().first()
        assertEquals(1, rendiciones.size)
        assertEquals("CC-104 · Operaciones Concepción", rendiciones.first().centroCosto)
        assertEquals(RendicionEntity.ESTADO_BORRADOR, rendiciones.first().estado)
        assertNotNull(rendicionCreadaId)
    }

    @Test
    fun importarTrasladoTeqRuta_inyectaPeajeYCombustible() = runTest {
        viewModel.importarTrasladoTeqRuta()

        val rendiciones = repository.obtenerTodasLasRendiciones().first()
        assertEquals(1, rendiciones.size)

        val rendicionId = rendiciones.first().id
        val gastos = repository.obtenerGastosPorRendicion(rendicionId).first()

        assertEquals(2, gastos.size)
        assertTrue(gastos.any { it.categoria == GastoEntity.CAT_PEAJE_ESTACIONAMIENTO })
        assertTrue(gastos.any { it.categoria == GastoEntity.CAT_COMBUSTIBLE })
        assertEquals(27300.0, rendiciones.first().montoTotalClp, 0.001)
    }

    @Test
    fun enviarRendicion_guardaGastoYRecalculaTotal() = runTest {
        // Crear rendición borrador primero
        val rId = repository.crearRendicion(
            RendicionEntity(tecnicoId = 1, nombreTecnico = "Carlos Muñoz", centroCosto = "CC-104", medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR)
        ).toInt()

        viewModel.prepararFormularioParaRendicion(rId)
        viewModel.onRutChange("76.123.456-7")
        viewModel.onFolioChange("1001")
        viewModel.onCategoriaChange(CategoriaGastoUi.ALIMENTACION)
        viewModel.onMontoChange("10000")
        viewModel.onToggleFotoComprobante("content://foto.jpg")

        var exito = false
        viewModel.enviarRendicion { exito = true }

        val gastos = repository.obtenerGastosPorRendicion(rId).first()
        assertEquals(1, gastos.size)
        assertEquals("1001", gastos.first().folioBoleta)
        assertTrue(exito)
    }

    @Test
    fun enviarRendicion_detectaBoletaDuplicada_yMuestraError() = runTest {
        // Registrar una boleta previamente en Room
        val rId = repository.crearRendicion(
            RendicionEntity(tecnicoId = 1, nombreTecnico = "Carlos Muñoz", centroCosto = "CC-104", medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR)
        ).toInt()

        repository.insertarGasto(
            GastoEntity(
                rendicionId = rId,
                rutProveedor = "76.123.456-7",
                folioBoleta = "9999",
                fechaGastoMillis = System.currentTimeMillis(),
                categoria = GastoEntity.CAT_INSUMOS,
                moneda = GastoEntity.MONEDA_CLP,
                montoOriginal = 15000.0,
                montoConvertidoClp = 15000.0,
                fotoUri = "content://foto.jpg"
            )
        )

        // Intentar registrar la misma boleta (Mismo RUT + Folio + Monto)
        viewModel.prepararFormularioParaRendicion(rId)
        viewModel.onRutChange("76.123.456-7")
        viewModel.onFolioChange("9999")
        viewModel.onCategoriaChange(CategoriaGastoUi.INSUMOS)
        viewModel.onMontoChange("15000")
        viewModel.onToggleFotoComprobante("content://foto.jpg")

        viewModel.enviarRendicion()

        val state = viewModel.uiState.value
        assertNotNull("Debe gatillar el error de boleta duplicada", state.folioError)
        assertTrue(state.folioError!!.contains("Boleta duplicada"))
    }
}
