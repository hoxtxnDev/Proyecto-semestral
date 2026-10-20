package cl.duoc.teqgastos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.teqgastos.ui.theme.TeqAvatarBg
import cl.duoc.teqgastos.ui.theme.TeqBorder
import cl.duoc.teqgastos.ui.theme.TeqMutedText
import cl.duoc.teqgastos.ui.theme.TeqNavy
import cl.duoc.teqgastos.ui.theme.TeqTeal
import cl.duoc.teqgastos.ui.theme.TeqTealDark
import cl.duoc.teqgastos.ui.theme.TeqTealSoft
import cl.duoc.teqgastos.ui.theme.TeqTealTextDark

/**
 * Logotipo corporativo de TeqGastos.
 */
@Composable
fun TeqBrand(
    inverted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val baseColor = if (inverted) Color.White else TeqNavy
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(31.dp)
                .rotate(-5f)
                .border(
                    width = 2.dp,
                    color = TeqTeal,
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomEnd = 10.dp,
                        bottomStart = 3.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .border(2.dp, baseColor, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = baseColor, fontWeight = FontWeight.ExtraBold)) {
                    append("Teq")
                }
                withStyle(SpanStyle(color = TeqTeal, fontWeight = FontWeight.ExtraBold)) {
                    append("Gastos")
                }
            },
            fontSize = 19.sp,
            letterSpacing = (-0.5).sp
        )
    }
}

/**
 * Cabecera superior con buscador integrado, cambio de plataforma y cierre de sesión.
 */
@Composable
fun TeqAppHeader(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    userInitials: String = "TS",
    searchQuery: String = "",
    onSearchChange: (String) -> Unit = {},
    searchPlaceholder: String = "Buscar rendiciones...",
    onFilterClick: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val platformMode = TeqAdaptivePlatformState.mode
    val isIos = platformMode == TeqPlatformMode.IOS_CUPERTINO
    val bottomRadius = if (isIos) 38.dp else 34.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = bottomRadius, bottomEnd = bottomRadius))
            .background(TeqNavy)
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(290.dp)
                .align(Alignment.TopEnd)
                .offset(x = 110.dp, y = (-120).dp)
                .border(1.dp, TeqTeal.copy(alpha = 0.17f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(210.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-80).dp)
                .border(1.dp, TeqTeal.copy(alpha = 0.10f), CircleShape)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            // Fila superior: Brand + Selector Android/iOS + Botón Cerrar Sesión (Cambio de Rol)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeqBrand(inverted = true)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chip rápido para alternar modo adaptable Android M3 / iOS HIG en vivo
                    Surface(
                        onClick = {
                            TeqAdaptivePlatformState.mode = if (isIos) {
                                TeqPlatformMode.ANDROID_MATERIAL
                            } else {
                                TeqPlatformMode.IOS_CUPERTINO
                            }
                        },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, TeqTeal.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isIos) Icons.Default.PhoneIphone else Icons.Default.PhoneAndroid,
                                contentDescription = "Modo Plataforma",
                                tint = TeqTeal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isIos) "iOS" else "Android",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Botón de Avatar + Cerrar sesión para cambiar de rol durante la defensa
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(11.dp),
                        color = TeqAvatarBg,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = userInitials,
                                color = TeqNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión / Cambiar rol",
                                tint = TeqNavy,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Eyebrow
            Text(
                text = eyebrow.uppercase(),
                color = TeqTeal,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Título principal
            Text(
                text = title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp
            )

            // Subtítulo opcional
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de búsqueda integrada (.search-box)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .clip(RoundedCornerShape(if (isIos) 16.dp else 13.dp))
                    .background(Color.White.copy(alpha = 0.09f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.10f),
                        RoundedCornerShape(if (isIos) 16.dp else 13.dp)
                    )
                    .padding(start = 14.dp, end = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = searchPlaceholder,
                            color = Color.White.copy(alpha = 0.43f),
                            fontSize = 12.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp
                        ),
                        cursorBrush = SolidColor(TeqTeal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Surface(
                    onClick = { onFilterClick?.invoke() },
                    shape = RoundedCornerShape(9.dp),
                    color = Color.White.copy(alpha = 0.11f),
                    modifier = Modifier.size(width = 34.dp, height = 32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Botón primario reutilizable de la aplicación.
 */
@Composable
fun TeqPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    trailingIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    val isIos = TeqAdaptivePlatformState.mode == TeqPlatformMode.IOS_CUPERTINO
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(53.dp),
        shape = RoundedCornerShape(if (isIos) 18.dp else 15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TeqTeal,
            contentColor = TeqTealTextDark,
            disabledContainerColor = TeqTeal.copy(alpha = 0.45f),
            disabledContentColor = TeqTealTextDark.copy(alpha = 0.55f)
        ),
        contentPadding = PaddingValues(start = 19.dp, end = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isLoading) "Procesando..." else text,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TeqTealTextDark
            )

            Box(
                modifier = Modifier
                    .size(39.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(TeqNavy.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

/**
 * Barra de navegación inferior de 4 secciones adaptable a Android e iOS.
 */
@Composable
fun TeqBottomNav(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isIos = TeqAdaptivePlatformState.mode == TeqPlatformMode.IOS_CUPERTINO
    val items = listOf(
        "Inicio" to Icons.Default.Home,
        "Rendiciones" to Icons.Default.ReceiptLong,
        "Reportes" to Icons.Default.BarChart,
        "Ajustes" to Icons.Default.Settings
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = if (isIos) 0.94f else 0.98f),
        shadowElevation = if (isIos) 16.dp else 10.dp,
        shape = if (isIos) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) else RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, TeqBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (label, icon) ->
                    val isSelected = activeTab.equals(label, ignoreCase = true)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(if (isIos) 16.dp else 12.dp))
                            .background(if (isSelected) TeqTealSoft else Color.Transparent)
                            .clickable { onTabSelected(label) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) TeqTealDark else TeqMutedText,
                            modifier = Modifier.size(21.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TeqTealDark else TeqMutedText
                        )
                    }
                }
            }

            // En modo iOS Cupertino HIG, se muestra el Home Indicator característico en la base
            if (isIos) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .width(124.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(TeqNavy.copy(alpha = 0.22f))
                )
            }
        }
    }
}
