package cl.duoc.teqgastos.domain.validators

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para `AuthValidator`.
 * Valida las reglas de dominio para credenciales corporativas (TEQMED SPA).
 */
class AuthValidatorTest {

    @Test
    fun validarEmail_retornaNull_cuandoEmailEsValido() {
        val emailValido = "tecnico@teqmed.cl"
        val resultado = AuthValidator.validarEmail(emailValido)
        assertNull("Un correo válido debe retornar null", resultado)
    }

    @Test
    fun validarEmail_retornaNull_cuandoEmailTieneEspaciosAlInicioOFinal() {
        val emailConEspacios = "  jefatura@teqmed.cl  "
        val resultado = AuthValidator.validarEmail(emailConEspacios)
        assertNull("El correo debe ser limpiado con trim() y ser válido", resultado)
    }

    @Test
    fun validarEmail_retornaError_cuandoEmailEsVacio() {
        val resultado = AuthValidator.validarEmail("")
        assertEquals("El correo corporativo es obligatorio", resultado)
    }

    @Test
    fun validarEmail_retornaError_cuandoEmailEsBlanco() {
        val resultado = AuthValidator.validarEmail("   ")
        assertEquals("El correo corporativo es obligatorio", resultado)
    }

    @Test
    fun validarEmail_retornaError_cuandoFormatoEsInvalido() {
        val emailsInvalidos = listOf(
            "tecnico",
            "tecnico@",
            "tecnico@teqmed",
            "tecnico.cl",
            "@teqmed.cl"
        )
        emailsInvalidos.forEach { email ->
            val resultado = AuthValidator.validarEmail(email)
            assertEquals(
                "Ingrese un correo válido (ej: usuario@teqmed.cl)",
                resultado
            )
        }
    }

    @Test
    fun validarPassword_retornaNull_cuandoPasswordEsValida() {
        val resultado = AuthValidator.validarPassword("123456")
        assertNull("Una contraseña de 6 caracteres debe ser válida", resultado)
    }

    @Test
    fun validarPassword_retornaNull_cuandoCumpleLongitudMinimaExacta() {
        val resultado = AuthValidator.validarPassword("1234")
        assertNull("Una contraseña de 4 caracteres (mínimo) debe ser válida", resultado)
    }

    @Test
    fun validarPassword_retornaError_cuandoPasswordEsVacíaOBlanca() {
        val resultadoVacio = AuthValidator.validarPassword("")
        val resultadoBlanco = AuthValidator.validarPassword("   ")
        assertEquals("La contraseña es obligatoria", resultadoVacio)
        assertEquals("La contraseña es obligatoria", resultadoBlanco)
    }

    @Test
    fun validarPassword_retornaError_cuandoLongitudEsMenorAlMinimo() {
        val resultado = AuthValidator.validarPassword("123")
        assertEquals("La contraseña debe tener al menos 4 caracteres", resultado)
    }

    @Test
    fun esFormularioLoginValido_retornaTrue_cuandoEmailYPasswordSonValidos() {
        val esValido = AuthValidator.esFormularioLoginValido("finanzas@teqmed.cl", "123456")
        assertTrue("El formulario completo debe ser válido", esValido)
    }

    @Test
    fun esFormularioLoginValido_retornaFalse_cuandoAlgunCampoEsInvalido() {
        assertFalse(AuthValidator.esFormularioLoginValido("invalido", "123456"))
        assertFalse(AuthValidator.esFormularioLoginValido("tecnico@teqmed.cl", "12"))
        assertFalse(AuthValidator.esFormularioLoginValido("", ""))
    }
}
