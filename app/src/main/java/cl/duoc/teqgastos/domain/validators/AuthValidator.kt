package cl.duoc.teqgastos.domain.validators

/**
 * Validador de reglas de negocio para autenticación y credenciales corporativas (TEQMED SPA).
 * Funciones puras en Kotlin que retornan `null` cuando el dato es válido o el mensaje de error correspondiente.
 */
object AuthValidator {

    // Expresión regular para correo electrónico corporativo válido
    private val REGEX_EMAIL = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Longitud mínima exigida para contraseñas corporativas
    const val MIN_PASSWORD_LENGTH = 4

    /**
     * Valida el formato del correo electrónico en el formulario de inicio de sesión.
     *
     * @param email Correo electrónico ingresado por el usuario.
     * @return `null` si el correo es válido, o el texto de error para mostrar en `ValidatedTextField`.
     */
    fun validarEmail(email: String): String? {
        val limpio = email.trim()
        if (limpio.isEmpty()) {
            return "El correo corporativo es obligatorio"
        }
        if (!REGEX_EMAIL.matches(limpio)) {
            return "Ingrese un correo válido (ej: usuario@teqmed.cl)"
        }
        return null
    }

    /**
     * Valida la contraseña en el formulario de inicio de sesión.
     *
     * @param password Contraseña ingresada por el usuario.
     * @return `null` si cumple los requisitos mínimos, o el texto de error correspondiente.
     */
    fun validarPassword(password: String): String? {
        if (password.isBlank()) {
            return "La contraseña es obligatoria"
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
        }
        return null
    }

    /**
     * Verifica si el formulario completo de inicio de sesión es válido.
     *
     * @param email Correo electrónico ingresado.
     * @param password Contraseña ingresada.
     * @return `true` si ambos campos pasan las validaciones de dominio sin errores.
     */
    fun esFormularioLoginValido(email: String, password: String): Boolean {
        return validarEmail(email) == null && validarPassword(password) == null
    }
}
