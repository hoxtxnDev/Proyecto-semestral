package cl.duoc.teqgastos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Color corporativo de validación exitosa (Verde Semántico Material).
 */
private val ValidSuccessGreen = Color(0xFF2E7D32)

/**
 * Componente visual reutilizable basado en `OutlinedTextField` (Fase 4 - Indicadores IE 2.1.2, IE 2.2.1).
 *
 * Totalmente desacoplado de la lógica de validación: recibe `errorMessage: String?` calculado
 * desde el `ViewModel` y gestiona automáticamente:
 * - Estado de error (`isError = errorMessage != null`).
 * - `trailingIcon` reactivo: `Icons.Default.Error` en rojo cuando hay error, o
 *   `Icons.Default.CheckCircle` en verde cuando el campo es válido y no está vacío.
 * - Transición fluida mediante `AnimatedVisibility` debajo del campo para mostrar u ocultar
 *   el mensaje de error sin saltos bruscos en la interfaz.
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    val isError = errorMessage != null
    val isValidAndNotEmpty = !isError && value.isNotBlank()

    // Conserva el último texto de error durante la animación de salida (shrinkVertically + fadeOut)
    var ultimoMensajeError by remember { mutableStateOf(errorMessage.orEmpty()) }
    if (errorMessage != null) {
        ultimoMensajeError = errorMessage
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = label) },
            placeholder = placeholder?.let { textoPlaceholder ->
                { Text(text = textoPlaceholder) }
            },
            leadingIcon = leadingIcon?.let { icono ->
                {
                    Icon(
                        imageVector = icono,
                        contentDescription = label,
                        tint = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            },
            trailingIcon = {
                when {
                    isError -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error de validación en $label",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    isValidAndNotEmpty -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "$label válido",
                            tint = ValidSuccessGreen
                        )
                    }
                }
            },
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            enabled = enabled,
            readOnly = readOnly,
            shape = RoundedCornerShape(12.dp)
        )

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = ultimoMensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
