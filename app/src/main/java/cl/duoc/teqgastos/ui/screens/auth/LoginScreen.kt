package cl.duoc.teqgastos.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import cl.duoc.teqgastos.ui.components.TeqBrand
import cl.duoc.teqgastos.ui.components.TeqPrimaryButton
import cl.duoc.teqgastos.ui.components.ValidatedTextField
import cl.duoc.teqgastos.ui.theme.TeqBackground
import cl.duoc.teqgastos.ui.theme.TeqBorder
import cl.duoc.teqgastos.ui.theme.TeqMutedText
import cl.duoc.teqgastos.ui.theme.TeqNavy
import cl.duoc.teqgastos.ui.theme.TeqSecondaryText
import cl.duoc.teqgastos.ui.theme.TeqTeal
import cl.duoc.teqgastos.ui.theme.TeqTealDark
import cl.duoc.teqgastos.ui.theme.TeqTealSoft

/**
 * Pantalla de inicio de sesión con validación de credenciales y selector de perfiles de prueba.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (UsuarioEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (uiState.email.isEmpty() && uiState.perfilDemoActivo == null) {
            viewModel.seleccionarCredencialesDemo(PerfilDemo.TECNICO)
        }
    }

    LaunchedEffect(uiState.usuarioAutenticado) {
        uiState.usuarioAutenticado?.let { usuario ->
            onLoginSuccess(usuario)
        }
    }

    val etiquetaRolBoton = when (uiState.perfilDemoActivo) {
        PerfilDemo.TECNICO -> "Técnico"
        PerfilDemo.JEFATURA -> "Jefatura"
        PerfilDemo.FINANZAS -> "Finanzas"
        null -> "Usuario"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TeqBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabecera principal de acceso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(315.dp)
                    .clip(RoundedCornerShape(bottomStart = 42.dp, bottomEnd = 42.dp))
                    .background(TeqNavy)
                    .padding(horizontal = 26.dp, vertical = 28.dp)
            ) {
                // Órbitas decorativas (.orbit-one, .orbit-two y glow inferior)
                Box(
                    modifier = Modifier
                        .size(330.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 110.dp, y = (-100).dp)
                        .border(1.dp, TeqTeal.copy(alpha = 0.16f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 66.dp, y = (-57).dp)
                        .border(1.dp, TeqTeal.copy(alpha = 0.16f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 45.dp, y = 80.dp)
                        .clip(CircleShape)
                        .background(TeqTeal.copy(alpha = 0.09f))
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    // Fila Brand + Tag TEQMED SPA (.login-brand-row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeqBrand(inverted = true)

                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                        ) {
                            Text(
                                text = "TEQMED SPA",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Copy Hero (.hero-copy)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF56E5DC),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ACCESO SEGURO",
                            color = Color(0xFF56E5DC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.4.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                                append("Gastos simples.\n")
                            }
                            withStyle(SpanStyle(color = TeqTeal, fontWeight = FontWeight.ExtraBold)) {
                                append("Control total.")
                            }
                        },
                        fontSize = 35.sp,
                        lineHeight = 37.sp,
                        letterSpacing = (-1.2).sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Rinde, aprueba y procesa gastos desde un solo lugar.",
                        color = Color.White.copy(alpha = 0.63f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            // =================================================================
            // 2. PANEL DE FORMULARIO Y SELECTOR DEMO (.login-panel)
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 28.dp)
            ) {
                // Cabecera de sección (.section-heading)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "BIENVENIDO",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Inicia sesión",
                            color = TeqNavy,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.6).sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .border(1.dp, TeqBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "01",
                            color = TeqMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Selector rápido de perfil de prueba (.profile-block / .role-selector)
                Text(
                    text = "PERFIL DE PRUEBA (SELECTOR RÁPIDO DEFENSA)",
                    color = TeqMutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    PerfilDemo.entries.forEach { perfil ->
                        val seleccionado = uiState.perfilDemoActivo == perfil
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (seleccionado) TeqTealSoft else Color.White)
                                .border(
                                    width = if (seleccionado) 1.5.dp else 1.dp,
                                    color = if (seleccionado) TeqTeal else TeqBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !uiState.isLoading) {
                                    focusManager.clearFocus()
                                    viewModel.seleccionarCredencialesDemo(perfil)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = perfil.tituloBoton,
                                color = if (seleccionado) TeqTealDark else TeqSecondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Campos ValidatedTextField conectados al AuthViewModel en tiempo real
                ValidatedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Email Corporativo",
                    placeholder = "nombre@teqmed.cl",
                    errorMessage = uiState.emailError,
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(14.dp))

                ValidatedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Contraseña",
                    placeholder = "Ingresa tu contraseña",
                    errorMessage = uiState.passwordError,
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.iniciarSesion()
                        }
                    ),
                    enabled = !uiState.isLoading
                )

                // Alerta animada para errores de autenticación
                AnimatedVisibility(
                    visible = uiState.authError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error de autenticación",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.authError.orEmpty(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                TeqPrimaryButton(
                    text = "Ingresar como $etiquetaRolBoton",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.iniciarSesion()
                    },
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Plataforma de uso interno · Teqmed SPA",
                    color = TeqMutedText,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
