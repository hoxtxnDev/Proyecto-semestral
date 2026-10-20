package cl.duoc.teqgastos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =========================================================================
// PALETA DE COLORES OFICIAL EXTRAÍDA DE FIGMA (src-interface/index.css)
// =========================================================================
val TeqNavy = Color(0xFF1B2A4A)
val TeqNavyDark = Color(0xFF0F192C)
val TeqTeal = Color(0xFF00D2C4)
val TeqTealMid = Color(0xFF00BFB3)
val TeqTealDark = Color(0xFF087F78)
val TeqTealTextDark = Color(0xFF0D3F3D)
val TeqTealSoft = Color(0xFFE8FBF9)
val TeqTealIconBg = Color(0xFFE2FAF8)
val TeqAvatarBg = Color(0xFF8EF0E9)

val TeqBackground = Color(0xFFF4F6F8)
val TeqSurfaceWhite = Color(0xFFFFFFFF)
val TeqBorder = Color(0xFFDCE3E7)
val TeqCardBorder = Color(0xFFE0E6E9)
val TeqDivider = Color(0xFFEDF0F2)

val TeqMutedText = Color(0xFF91A0AA)
val TeqLabelText = Color(0xFF617181)
val TeqSecondaryText = Color(0xFF7B8995)

// Estados Semánticos (Aprobar / Observar / Rechazar / Validaciones)
val TeqValidBorder = Color(0xFF63BD87)
val TeqValidIcon = Color(0xFF2EAA68)
val TeqApproveBg = Color(0xFFDFF6E9)
val TeqApproveText = Color(0xFF15744C)

val TeqObserveBg = Color(0xFFFFF2CE)
val TeqObserveText = Color(0xFF9A6812)
val TeqObserveBtnBg = Color(0xFFFFD565)
val TeqPendingPillBg = Color(0xFFFFF6DB)
val TeqPendingPillText = Color(0xFFAD7911)

val TeqErrorBorder = Color(0xFFE87979)
val TeqErrorBg = Color(0xFFFFFAFA)
val TeqRejectBg = Color(0xFFFDE8E8)
val TeqRejectText = Color(0xFFB54545)
val TeqRejectBtnBg = Color(0xFFD95858)

private val TeqLightColorScheme = lightColorScheme(
    primary = TeqNavy,
    onPrimary = Color.White,
    primaryContainer = TeqTealSoft,
    onPrimaryContainer = TeqTealDark,
    secondary = TeqTeal,
    onSecondary = TeqTealTextDark,
    secondaryContainer = TeqTealIconBg,
    onSecondaryContainer = TeqTealDark,
    background = TeqBackground,
    onBackground = TeqNavy,
    surface = TeqSurfaceWhite,
    onSurface = TeqNavy,
    surfaceVariant = Color(0xFFFAFBFB),
    onSurfaceVariant = TeqLabelText,
    outline = TeqBorder,
    outlineVariant = TeqCardBorder,
    error = TeqRejectBtnBg,
    onError = Color.White,
    errorContainer = TeqRejectBg,
    onErrorContainer = TeqRejectText
)

@Composable
fun TeqGastosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TeqLightColorScheme,
        content = content
    )
}
