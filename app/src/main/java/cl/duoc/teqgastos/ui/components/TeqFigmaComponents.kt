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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
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
 * Logotipo corporativo `Brand` fiel al diseño Figma (`src-interface/App.tsx`).
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
 * Cabecera superior `AppHeader` extraída de Figma (`src-interface/App.tsx`).
 * Incluye fondo `#1B2A4A` con borde inferior curvo (`34.dp`), órbita decorativa,
 * botón de avatar para cerrar sesión y barra de búsqueda integrada.
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .background(TeqNavy)
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        // Órbitas decorativas sutiles (Figma .app-header::before)
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
            // Fila superior: Brand + Avatar (Cerrar Sesión)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeqBrand(inverted = true)

                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(11.dp),
                    color = TeqAvatarBg,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userInitials,
                            color = TeqNavy,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(18.dp))

            // Barra de búsqueda integrada (.search-box)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = 0.09f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(13.dp))
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
 * Botón primario de alto contraste fiel a `.primary-button` de Figma (`src-interface/index.css`).
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
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(53.dp),
        shape = RoundedCornerShape(15.dp),
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
 * Barra de navegación inferior `BottomNav` de 4 secciones extraída de Figma (`src-interface/App.tsx`).
 */
@Composable
fun TeqBottomNav(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        "Inicio" to Icons.Default.Home,
        "Rendiciones" to Icons.Default.ReceiptLong,
        "Reportes" to Icons.Default.BarChart,
        "Ajustes" to Icons.Default.Settings
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, TeqBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) TeqTealSoft else Color.Transparent)
                        .clickable { onTabSelected(label) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
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
    }
}
