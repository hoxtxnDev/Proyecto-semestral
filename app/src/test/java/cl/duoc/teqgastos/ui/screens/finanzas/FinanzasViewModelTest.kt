package cl.duoc.teqgastos.ui.screens.finanzas

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
 * Pruebas unitarias para `FinanzasViewModel`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FinanzasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDao: FakeTeqGastosDao
    private lateinit var repository: TeqGastosRepository
    private lateinit var viewModel: FinanzasViewModel

    @Before
    fun setUp() = runTest {
        fakeDao = FakeTeqGastosDao()
        repository = TeqGastosRepository(fakeDao)
        viewModel = FinanzasViewModel(repository)
    }

    @Test
    fun itemsFinanzas_combinaRendicionesAprobadasYPagadas() = runTest {
        val rId1 = repository.crearRendicion(
            RendicionEntity(
                id = 1,
                tecnicoId = 10,
                nombreTecnico = "Carlos Muñoz",
                centroCosto = "CC-104",
                medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
                estado = RendicionEntity.ESTADO_APROBADA,
                montoTotalClp = 25000.0
            )
        ).toInt()

        repository.insertarGasto(
            GastoEntity(
                rendicionId = rId1,
                rutProveedor = "76.123.456-7",
                folioBoleta = "500",
                fechaGastoMillis = System.currentTimeMillis(),
                categoria = GastoEntity.CAT_PEAJE_ESTACIONAMIENTO,
                moneda = GastoEntity.MONEDA_CLP,
                montoOriginal = 25000.0,
                montoConvertidoClp = 25000.0,
                fotoUri = "content://foto.jpg"
            )
        )

        val items = viewModel.itemsFinanzas.first()
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(1, item.rendicionId)
        assertEquals("Carlos Muñoz", item.nombreTecnico)
        assertEquals(25000.0, item.montoTotalClp, 0.001)
        assertEquals(RendicionEntity.ESTADO_APROBADA, item.estado)
    }

    @Test
    fun onCodigoPagoChange_validaFormatoCodigoErp() {
        viewModel.onCodigoPagoChange("123")
        assertNotNull("Código corto de 3 caracteres debe generar error", viewModel.uiState.value.codigoPagoError)

        viewModel.onCodigoPagoChange("PAG-2026-0089")
        assertNull("Código válido limpia el error", viewModel.uiState.value.codigoPagoError)
        assertEquals("PAG-2026-0089", viewModel.uiState.value.codigoPago)
    }

    @Test
    fun alternarFiltroPagadas_y_onSearchQueryChange_actualizanEstado() {
        viewModel.onSearchQueryChange("Carlos")
        assertEquals("Carlos", viewModel.uiState.value.searchQuery)

        viewModel.alternarFiltroPagadas()
        assertTrue(viewModel.uiState.value.mostrarPagadas)
    }
}
