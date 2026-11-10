package cl.duoc.teqgastos.data.repository

import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para `TeqGastosRepository`.
 * Verifica autenticación, consultas por rol, transacciones y control anti-duplicidad.
 */
class TeqGastosRepositoryTest {

    private lateinit var fakeDao: FakeTeqGastosDao
    private lateinit var repository: TeqGastosRepository

    private val usuarioTecnico = UsuarioEntity(
        id = 1,
        nombre = "Carlos Muñoz",
        email = "tecnico@teqmed.cl",
        password = "123456",
        rol = UsuarioEntity.ROL_TECNICO,
        centroCostoAutorizado = "CC-104"
    )

    private val usuarioJefatura = UsuarioEntity(
        id = 2,
        nombre = "Andrea Soto",
        email = "jefatura@teqmed.cl",
        password = "123456",
        rol = UsuarioEntity.ROL_JEFATURA,
        centroCostoAutorizado = "CC-TODOS"
    )

    private val usuarioFinanzas = UsuarioEntity(
        id = 3,
        nombre = "Roberto Gómez",
        email = "finanzas@teqmed.cl",
        password = "123456",
        rol = UsuarioEntity.ROL_FINANZAS,
        centroCostoAutorizado = "CC-TODOS"
    )

    @Before
    fun setUp() = runTest {
        fakeDao = FakeTeqGastosDao()
        repository = TeqGastosRepository(fakeDao)
        fakeDao.insertarUsuarios(listOf(usuarioTecnico, usuarioJefatura, usuarioFinanzas))
    }

    @Test
    fun login_retornaUsuario_cuandoCredencialesSonCorrectas() = runTest {
        val user = repository.login("tecnico@teqmed.cl", "123456")
        assertNotNull("Login con credenciales válidas no debe ser null", user)
        assertEquals(usuarioTecnico.id, user?.id)
        assertEquals(UsuarioEntity.ROL_TECNICO, user?.rol)
    }

    @Test
    fun login_retornaNull_cuandoCredencialesSonIncorrectas() = runTest {
        val user = repository.login("tecnico@teqmed.cl", "badpass")
        assertNull("Login con clave incorrecta debe retornar null", user)
    }

    @Test
    fun obtenerUsuarioPorId_retornaUsuarioCorrespondiente() = runTest {
        val user = repository.obtenerUsuarioPorId(2)
        assertNotNull(user)
        assertEquals("Andrea Soto", user?.nombre)
    }

    @Test
    fun registrarRendicionConGasto_creaRendicionYGastoYRecalculaTotal() = runTest {
        val rendicionId = repository.registrarRendicionConGasto(
            tecnicoId = 1,
            nombreTecnico = "Carlos Muñoz",
            centroCosto = "CC-104",
            rutProveedor = "76.123.456-7",
            folioBoleta = "1001",
            fechaGastoMillis = System.currentTimeMillis(),
            categoria = GastoEntity.CAT_ALIMENTACION,
            moneda = GastoEntity.MONEDA_CLP,
            montoOriginal = 12000.0,
            montoConvertidoClp = 12000.0,
            fotoUri = "content://boletas/foto.jpg",
            latitud = -36.8270,
            longitud = -73.0503
        )

        val rendicion = repository.obtenerRendicionPorId(rendicionId.toInt()).first()
        assertNotNull(rendicion)
        assertEquals(RendicionEntity.ESTADO_PENDIENTE_JEFATURA, rendicion?.estado)
        assertEquals(12000.0, rendicion?.montoTotalClp!!, 0.001)

        val gastos = repository.obtenerGastosPorRendicion(rendicionId.toInt()).first()
        assertEquals(1, gastos.size)
        assertEquals("1001", gastos.first().folioBoleta)
    }

    @Test
    fun existeBoletaDuplicada_detectaCorrectamenteBoletasGuardadas() = runTest {
        assertFalse(repository.existeBoletaDuplicada("76.123.456-7", "5001", 15000.0))

        repository.registrarRendicionConGasto(
            tecnicoId = 1,
            nombreTecnico = "Carlos Muñoz",
            centroCosto = "CC-104",
            rutProveedor = "76.123.456-7",
            folioBoleta = "5001",
            fechaGastoMillis = System.currentTimeMillis(),
            categoria = GastoEntity.CAT_ALIMENTACION,
            moneda = GastoEntity.MONEDA_CLP,
            montoOriginal = 15000.0,
            montoConvertidoClp = 15000.0,
            fotoUri = "content://foto.jpg",
            latitud = null,
            longitud = null
        )

        assertTrue(
            "Boleta con mismo RUT, Folio y Monto debe ser identificada como duplicada",
            repository.existeBoletaDuplicada("76.123.456-7", "5001", 15000.0)
        )
    }

    @Test
    fun obtenerRendicionesPorRol_filtraEstadosCorrespondientes() = runTest {
        // Crear 3 rendiciones con estados distintos
        repository.crearRendicion(
            RendicionEntity(id = 1, tecnicoId = 1, nombreTecnico = "Carlos", centroCosto = "CC-1", medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR, estado = RendicionEntity.ESTADO_BORRADOR)
        )
        repository.crearRendicion(
            RendicionEntity(id = 2, tecnicoId = 1, nombreTecnico = "Carlos", centroCosto = "CC-1", medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR, estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA)
        )
        repository.crearRendicion(
            RendicionEntity(id = 3, tecnicoId = 1, nombreTecnico = "Carlos", centroCosto = "CC-1", medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR, estado = RendicionEntity.ESTADO_APROBADA)
        )

        val tecnicoList = repository.obtenerRendicionesPorRol(usuarioTecnico).first()
        assertEquals("Técnico debe ver todas sus rendiciones", 3, tecnicoList.size)

        val jefaturaList = repository.obtenerRendicionesPorRol(usuarioJefatura).first()
        assertEquals("Jefatura ve las enviadas a revisión y aprobadas (2 de 3)", 2, jefaturaList.size)

        val finanzasList = repository.obtenerRendicionesPorRol(usuarioFinanzas).first()
        assertEquals("Finanzas sólo ve las aprobadas o pagadas (1 de 3)", 1, finanzasList.size)
    }

    @Test
    fun procesarPagoLoteFinanzas_actualizaTodasLasRendicionesAPagadaErp() = runTest {
        val id1 = repository.crearRendicion(
            RendicionEntity(id = 10, tecnicoId = 1, nombreTecnico = "Carlos", centroCosto = "CC-1", medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR, estado = RendicionEntity.ESTADO_APROBADA)
        ).toInt()
        val id2 = repository.crearRendicion(
            RendicionEntity(id = 20, tecnicoId = 1, nombreTecnico = "Carlos", centroCosto = "CC-1", medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR, estado = RendicionEntity.ESTADO_APROBADA)
        ).toInt()

        repository.procesarPagoLoteFinanzas(listOf(id1, id2), "ERP-TRF-2026-99")

        val r1 = repository.obtenerRendicionPorId(id1).first()
        val r2 = repository.obtenerRendicionPorId(id2).first()

        assertEquals(RendicionEntity.ESTADO_PAGADA_ERP, r1?.estado)
        assertEquals("ERP-TRF-2026-99", r1?.codigoPagoFinanzas)
        assertEquals(RendicionEntity.ESTADO_PAGADA_ERP, r2?.estado)
        assertEquals("ERP-TRF-2026-99", r2?.codigoPagoFinanzas)
    }
}
