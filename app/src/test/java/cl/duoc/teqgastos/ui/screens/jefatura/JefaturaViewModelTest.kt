package cl.duoc.teqgastos.ui.screens.jefatura

import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.repository.FakeTeqGastosDao
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas unitarias para `JefaturaViewModel`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JefaturaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDao: FakeTeqGastosDao
    private lateinit var repository: TeqGastosRepository
    private lateinit var viewModel: JefaturaViewModel

    @Before
    fun setUp() = runTest {
        fakeDao = FakeTeqGastosDao()
        repository = TeqGastosRepository(fakeDao)
        viewModel = JefaturaViewModel(repository)
    }

    @Test
    fun itemsEvaluacion_combinaRendicionesYAuditaGastos() = runTest {
        val rId = repository.crearRendicion(
            RendicionEntity(
                id = 1,
                tecnicoId = 10,
                nombreTecnico = "Carlos Muñoz",
                centroCosto = "CC-104",
                medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
                estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA,
                montoTotalClp = 12000.0
            )
        ).toInt()

        repository.insertarGasto(
            GastoEntity(
                rendicionId = rId,
                rutProveedor = "76.123.456-7",
                folioBoleta = "101",
                fechaGastoMillis = System.currentTimeMillis(),
                categoria = GastoEntity.CAT_ALIMENTACION,
                moneda = GastoEntity.MONEDA_CLP,
                montoOriginal = 12000.0,
                montoConvertidoClp = 12000.0,
                fotoUri = "content://foto.jpg"
            )
        )

        val items = viewModel.itemsEvaluacion.first()
        assertEquals(1, items.size)

        val item = items.first()
        assertEquals("Carlos Muñoz", item.nombreTecnico)
        assertEquals("CM", item.iniciales)
        assertEquals(1, item.gastosAuditados.size)
        assertTrue("Monto $12.000 CLP cumple el tope de $15.000 CLP", item.todosCumplenTope)
    }

    @Test
    fun aprobarRendicion_actualizaEstadoAAprobadaEnRoom() = runTest {
        val rId = repository.crearRendicion(
            RendicionEntity(
                id = 1,
                tecnicoId = 10,
                nombreTecnico = "Carlos Muñoz",
                centroCosto = "CC-104",
                medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
                estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA
            )
        ).toInt()

        val item = ItemEvaluacionJefatura(
            rendicionId = rId,
            nombreTecnico = "Carlos Muñoz",
            iniciales = "CM",
            centroCosto = "CC-104",
            medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
            rutProveedorPrincipal = "76.123.456-7",
            folioPrincipal = "101",
            categoriaPrincipal = GastoEntity.CAT_ALIMENTACION,
            montoTotalClp = 12000.0,
            estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA,
            comentarioRevision = null,
            gastosAuditados = emptyList(),
            todosCumplenTope = true
        )

        viewModel.aprobarRendicion(item)

        val actual = repository.obtenerRendicionPorId(rId).first()
        assertEquals(RendicionEntity.ESTADO_APROBADA, actual?.estado)
        assertNotNull(viewModel.uiState.value.mensajeFeedback)
    }

    @Test
    fun abrirYConfirmarModalRevision_observaRendicionConMotivo() = runTest {
        val rId = repository.crearRendicion(
            RendicionEntity(
                id = 2,
                tecnicoId = 10,
                nombreTecnico = "Carlos Muñoz",
                centroCosto = "CC-104",
                medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
                estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA
            )
        ).toInt()

        val item = ItemEvaluacionJefatura(
            rendicionId = rId,
            nombreTecnico = "Carlos Muñoz",
            iniciales = "CM",
            centroCosto = "CC-104",
            medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
            rutProveedorPrincipal = "76.123.456-7",
            folioPrincipal = "102",
            categoriaPrincipal = GastoEntity.CAT_ALIMENTACION,
            montoTotalClp = 18000.0,
            estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA,
            comentarioRevision = null,
            gastosAuditados = emptyList(),
            todosCumplenTope = false
        )

        viewModel.abrirModalRevision(item, AccionRevisionJefatura.OBSERVAR)
        assertEquals(item, viewModel.uiState.value.itemEnModal)

        // Intentar confirmar sin comentario (falla validación)
        viewModel.confirmarAccionModal()
        assertNotNull("Exige motivo de observación", viewModel.uiState.value.comentarioError)

        // Escribir motivo válido
        viewModel.onComentarioModalChange("Favor adjuntar detalle legible del proveedor")
        assertNull(viewModel.uiState.value.comentarioError)

        viewModel.confirmarAccionModal()

        val actual = repository.obtenerRendicionPorId(rId).first()
        assertEquals(RendicionEntity.ESTADO_OBSERVADA, actual?.estado)
        assertEquals("Favor adjuntar detalle legible del proveedor", actual?.comentarioRevision)
    }
}
