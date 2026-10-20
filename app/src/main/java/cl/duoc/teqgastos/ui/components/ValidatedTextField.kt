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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.teqgastos.ui.theme.TeqBorder
import cl.duoc.teqgastos.ui.theme.TeqErrorBg
import cl.duoc.teqgastos.ui.theme.TeqErrorBorder
import cl.duoc.teqgastos.ui.theme.TeqLabelText
import cl.duoc.teqgastos.ui.theme.TeqMutedText
import cl.duoc.teqgastos.ui.theme.TeqNavy
import cl.duoc.teqgastos.ui.theme.TeqTealMid
import cl.duoc.teqgastos.ui.theme.TeqValidBorder
import cl.duoc.teqgastos.ui.theme.TeqValidIcon

/**
 * Campo de texto reutilizable con indicador visual de validación y mensaje de error animado.
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
    suffixText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    val isError = errorMessage != null
    val isValidAndNotEmpty = !isError && value.isNotBlank()

    var ultimoMensajeError by remember { mutableStateOf(errorMessage.orEmpty()) }
    if (errorMessage != null) {
        ultimoMensajeError = errorMessage
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TeqLabelText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { textoPlaceholder ->
                {
                    Text(
                        text = textoPlaceholder,
                        color = TeqMutedText,
                        fontSize = 13.sp
                    )
                }
            },
            leadingIcon = leadingIcon?.let { icono ->
                {
                    Icon(
                        imageVector = icono,
                        contentDescription = label,
                        tint = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            TeqMutedText
                        },
                        modifier = Modifier.size(19.dp)
                    )
                }
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    if (!suffixText.isNullOrBlank()) {
                        Text(
                            text = suffixText,
                            color = TeqLabelText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                        if (isError || isValidAndNotEmpty) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                    when {
                        isError -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error en $label",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        isValidAndNotEmpty -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "$label válido",
                                tint = TeqValidIcon,
                                modifier = Modifier.size(19.dp)
                            )
                        }
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
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TeqNavy,
                unfocusedTextColor = TeqNavy,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = TeqErrorBg,
                focusedBorderColor = if (isValidAndNotEmpty) TeqValidBorder else TeqTealMid,
                unfocusedBorderColor = if (isValidAndNotEmpty) TeqValidBorder else TeqBorder,
                errorBorderColor = TeqErrorBorder,
                cursorColor = TeqTealMid
            )
        )

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, top = 5.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = ultimoMensajeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
