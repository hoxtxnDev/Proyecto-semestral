package cl.duoc.teqgastos.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.domain.validators.GastoValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Perfiles preconfigurados para selección rápida durante la defensa del proyecto (Fase 4).
 * Coinciden con los usuarios semilla insertados por `AppDatabase.SeedDatabaseCallback`.
 */
enum class PerfilDemo(
    val tituloBoton: String,
    val email: String,
    val password: String,
    val rol: String,
    val subtitulo: String
) {
    TECNICO(
        tituloBoton = "Demo Técnico",
        email = "tecnico@teqmed.cl",
        password = "123456",
        rol = UsuarioEntity.ROL_TECNICO,
        subtitulo = "Carlos Muñoz · Terreno"
    ),
    JEFATURA(
        tituloBoton = "Demo Jefatura",
        email = "jefatura@teqmed.cl",
        password = "123456",
        rol = UsuarioEntity.ROL_JEFATURA,
        subtitulo = "Andrea Soto · Aprobador"
    ),
    FINANZAS(
        tituloBoton = "Demo Finanzas",
        email = "finanzas@teqmed.cl",
        password = "123456",
        rol = UsuarioEntity.ROL_FINANZAS,
        subtitulo = "Roberto Gómez · ERP"
    )
}

/**
 * Estado inmutable de la pantalla de inicio de sesión expuesto mediante `StateFlow`.
 */
data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val authError: String? = null,
    val usuarioAutenticado: UsuarioEntity? = null,
    val perfilDemoActivo: PerfilDemo? = null
) {
    val esFormularioValido: Boolean
        get() = email.isNotBlank() &&
            password.isNotBlank() &&
            emailError == null &&
            passwordError == null
}

/**
 * ViewModel responsable del flujo de autenticación y validación reactiva en tiempo real
 * (Fase 4 - Indicadores IE 2.1.2, IE 2.2.1 e IE 2.2.2).
 *
 * Delega todas las reglas de validación a `GastoValidator` y consulta a `TeqGastosRepository`
 * para autenticar al usuario según su rol corporativo (`TECNICO`, `JEFATURA`, `FINANZAS`).
 */
class AuthViewModel(
    private val repository: TeqGastosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el correo electrónico y ejecuta validación en tiempo real desde el dominio.
     */
    fun onEmailChange(nuevoEmail: String) {
        val error = GastoValidator.validarEmail(nuevoEmail)
        _uiState.update { estadoActual ->
            estadoActual.copy(
                email = nuevoEmail,
                emailError = error,
                authError = null,
                perfilDemoActivo = PerfilDemo.entries.find {
                    it.email.equals(nuevoEmail.trim(), ignoreCase = true)
                }
            )
        }
    }

    /**
     * Actualiza la contraseña y ejecuta validación en tiempo real desde el dominio.
     */
    fun onPasswordChange(nuevaPassword: String) {
        val error = GastoValidator.validarPassword(nuevaPassword)
        _uiState.update { estadoActual ->
            estadoActual.copy(
                password = nuevaPassword,
                passwordError = error,
                authError = null
            )
        }
    }

    /**
     * Carga instantáneamente las credenciales de prueba para agilizar la demostración
     * ante el docente evaluador ("Demo Técnico", "Demo Jefatura", "Demo Finanzas").
     *
     * @param perfil Perfil corporativo de prueba a autocompletar.
     * @param autoIniciarSesion Si es `true`, ejecuta además el login inmediatamente.
     */
    fun seleccionarCredencialesDemo(
        perfil: PerfilDemo,
        autoIniciarSesion: Boolean = false
    ) {
        val emailError = GastoValidator.validarEmail(perfil.email)
        val passwordError = GastoValidator.validarPassword(perfil.password)

        _uiState.update { estadoActual ->
            estadoActual.copy(
                email = perfil.email,
                emailError = emailError,
                password = perfil.password,
                passwordError = passwordError,
                authError = null,
                perfilDemoActivo = perfil
            )
        }

        if (autoIniciarSesion) {
            iniciarSesion()
        }
    }

    /**
     * Valida el formulario completo y autentica contra la base de datos local Room.
     */
    fun iniciarSesion() {
        val estadoActual = _uiState.value
        val errorEmail = GastoValidator.validarEmail(estadoActual.email)
        val errorPassword = GastoValidator.validarPassword(estadoActual.password)

        if (errorEmail != null || errorPassword != null) {
            _uiState.update {
                it.copy(
                    emailError = errorEmail,
                    passwordError = errorPassword,
                    authError = "Por favor corrija los campos marcados en rojo antes de continuar."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            try {
                val usuario = repository.login(
                    email = estadoActual.email.trim(),
                    password = estadoActual.password
                )
                if (usuario != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            usuarioAutenticado = usuario,
                            authError = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authError = "Credenciales incorrectas. Verifique su correo y contraseña o seleccione un perfil Demo."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = "Error al consultar la base de datos local: ${e.localizedMessage ?: "Error desconocido"}"
                    )
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error general de autenticación.
     */
    fun limpiarErrorAutenticacion() {
        _uiState.update { it.copy(authError = null) }
    }

    /**
     * Cierra la sesión activa y restablece el formulario de login.
     */
    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }
}

/**
 * Factory para inyectar `TeqGastosRepository` en `AuthViewModel` sin requerir frameworks externos.
 */
class AuthViewModelFactory(
    private val repository: TeqGastosRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
