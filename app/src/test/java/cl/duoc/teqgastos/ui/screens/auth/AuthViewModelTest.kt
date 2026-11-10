package cl.duoc.teqgastos.ui.screens.auth

import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import cl.duoc.teqgastos.data.repository.FakeTeqGastosDao
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas unitarias para `AuthViewModel`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDao: FakeTeqGastosDao
    private lateinit var repository: TeqGastosRepository
    private lateinit var viewModel: AuthViewModel

    private val usuarioTecnico = UsuarioEntity(
        id = 1,
        nombre = "Carlos Muñoz",
        email = "tecnico@teqmed.cl",
        password = "123456",
        rol = UsuarioEntity.ROL_TECNICO,
        centroCostoAutorizado = "CC-104"
    )

    @Before
    fun setUp() = runTest {
        fakeDao = FakeTeqGastosDao()
        repository = TeqGastosRepository(fakeDao)
        fakeDao.insertarUsuarios(listOf(usuarioTecnico))
        viewModel = AuthViewModel(repository)
    }

    @Test
    fun onEmailChange_actualizaEstadoYError() {
        viewModel.onEmailChange("tecnico@")
        val state = viewModel.uiState.value
        assertEquals("tecnico@", state.email)
        assertNotNull("Debe indicar error por correo incompleto", state.emailError)

        viewModel.onEmailChange("tecnico@teqmed.cl")
        val stateValido = viewModel.uiState.value
        assertEquals("tecnico@teqmed.cl", stateValido.email)
        assertNull("Un correo válido elimina el error", stateValido.emailError)
        assertEquals(PerfilDemo.TECNICO, stateValido.perfilDemoActivo)
    }

    @Test
    fun onPasswordChange_actualizaEstadoYError() {
        viewModel.onPasswordChange("123")
        val state = viewModel.uiState.value
        assertEquals("123", state.password)
        assertNotNull("Debe indicar error por contraseña corta", state.passwordError)

        viewModel.onPasswordChange("123456")
        val stateValido = viewModel.uiState.value
        assertNull("Una contraseña de 6 dígitos no tiene error", stateValido.passwordError)
    }

    @Test
    fun seleccionarCredencialesDemo_autocompletaEmailYPassword() {
        viewModel.seleccionarCredencialesDemo(PerfilDemo.TECNICO, autoIniciarSesion = false)
        val state = viewModel.uiState.value
        assertEquals("tecnico@teqmed.cl", state.email)
        assertEquals("123456", state.password)
        assertEquals(PerfilDemo.TECNICO, state.perfilDemoActivo)
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun iniciarSesion_exitoso_autenticaUsuario() = runTest {
        viewModel.onEmailChange("tecnico@teqmed.cl")
        viewModel.onPasswordChange("123456")
        viewModel.iniciarSesion()

        val state = viewModel.uiState.value
        assertNotNull("El usuario autenticado debe ser asignado", state.usuarioAutenticado)
        assertEquals(usuarioTecnico.id, state.usuarioAutenticado?.id)
        assertNull(state.authError)
    }

    @Test
    fun iniciarSesion_credencialesIncorrectas_muestraError() = runTest {
        viewModel.onEmailChange("tecnico@teqmed.cl")
        viewModel.onPasswordChange("wrongpassword")
        viewModel.iniciarSesion()

        val state = viewModel.uiState.value
        assertNull("No debe haber usuario autenticado con clave mala", state.usuarioAutenticado)
        assertNotNull("Debe mostrar mensaje de credenciales incorrectas", state.authError)
    }

    @Test
    fun cerrarSesion_restableceUiState() = runTest {
        viewModel.onEmailChange("tecnico@teqmed.cl")
        viewModel.onPasswordChange("123456")
        viewModel.iniciarSesion()

        viewModel.cerrarSesion()
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertNull(state.usuarioAutenticado)
    }
}
