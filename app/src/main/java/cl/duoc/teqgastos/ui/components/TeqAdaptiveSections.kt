package cl.duoc.teqgastos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.domain.validators.GastoValidator
import cl.duoc.teqgastos.ui.screens.jefatura.obtenerIconoYNombreCategoria
import cl.duoc.teqgastos.ui.theme.TeqApproveBg
import cl.duoc.teqgastos.ui.theme.TeqApproveText
import cl.duoc.teqgastos.ui.theme.TeqBorder
import cl.duoc.teqgastos.ui.theme.TeqCardBorder
import cl.duoc.teqgastos.ui.theme.TeqDivider
import cl.duoc.teqgastos.ui.theme.TeqLabelText
import cl.duoc.teqgastos.ui.theme.TeqMutedText
import cl.duoc.teqgastos.ui.theme.TeqNavy
import cl.duoc.teqgastos.ui.theme.TeqObserveBg
import cl.duoc.teqgastos.ui.theme.TeqObserveText
import cl.duoc.teqgastos.ui.theme.TeqPendingPillBg
import cl.duoc.teqgastos.ui.theme.TeqPendingPillText
import cl.duoc.teqgastos.ui.theme.TeqRejectBg
import cl.duoc.teqgastos.ui.theme.TeqRejectText
import cl.duoc.teqgastos.ui.theme.TeqTeal
import cl.duoc.teqgastos.ui.theme.TeqTealDark
import cl.duoc.teqgastos.ui.theme.TeqTealIconBg
import cl.duoc.teqgastos.ui.theme.TeqTealSoft

/**
 * Modos de diseño adaptable entre Android (Material Design 3) e iOS (Human Interface Guidelines / Cupertino).
 */
enum class TeqPlatformMode(
    val titulo: String,
    val subtitulo: String
) {
    ANDROID_MATERIAL(
        titulo = "Android Nativo (Material 3)",
        subtitulo = "Navegación Material 3, bordes 18dp y controles nativos Android"
    ),
    IOS_CUPERTINO(
        titulo = "iOS Adaptable (Cupertino HIG)",
        subtitulo = "Barra flotante estilo iOS con Home Indicator, bordes squircle 24dp y Segmented Pills"
    )
}

/**
 * Estado global reactivo para previsualizar en tiempo real la adaptabilidad Android / iOS
 * desde cualquier pantalla del MVP durante la defensa en Android Studio.
 */
object TeqAdaptivePlatformState {
    var mode by mutableStateOf(TeqPlatformMode.ANDROID_MATERIAL)
}

val LocalTeqPlatformMode = compositionLocalOf { TeqPlatformMode.ANDROID_MATERIAL }

/**
 * Resumen de métricas de una categoría para el panel de Reportes y Seguimiento.
 */
data class ResumenCategoriaReporte(
    val categoria: String,
    val montoAcumuladoClp: Double,
    val cantidadBoletas: Int,
    val topeUnitarioClp: Double
)

/**
 * Sección de reportes y seguimiento para Técnico, Jefatura y Finanzas.
 */
@Composable
fun TeqReportesSection(
    rolActual: String,
    totalMontoClp: Double,
    totalRendiciones: Int,
    conteoBorrador: Int,
    conteoPendientes: Int,
    conteoObservadas: Int,
    conteoAprobadas: Int,
    conteoPagadasErp: Int,
    conteoRechazadas: Int,
    resumenCategorias: List<ResumenCategoriaReporte>,
    modifier: Modifier = Modifier
) {
    val platformMode = TeqAdaptivePlatformState.mode
    val cardCorner = if (platformMode == TeqPlatformMode.IOS_CUPERTINO) 24.dp else 19.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // =====================================================================
        // 1. HERO CARD DE SEGUIMIENTO INTEGRAL (TeqNavy + Órbitas TeqTeal)
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cardCorner))
                .background(TeqNavy)
                .padding(19.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 55.dp, y = (-65).dp)
                    .border(22.dp, TeqTeal.copy(alpha = 0.10f), CircleShape)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(TeqTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = TeqTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SEGUIMIENTO EN TIEMPO REAL · $rolActual",
                                color = TeqTeal,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Consolidado Operacional",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (platformMode == TeqPlatformMode.IOS_CUPERTINO) "iOS HIG" else "Android M3",
                            color = TeqTeal,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${GastoValidator.formatearClp(totalMontoClp)} CLP",
                    color = Color.White,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp
                )

                Text(
                    text = "$totalRendiciones carpetas registradas en base de datos Room (SQLite)",
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mini KPIs por estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    EstadoKpiPill(
                        titulo = "Pendientes",
                        valor = conteoPendientes + conteoBorrador,
                        bgColor = TeqPendingPillBg,
                        textColor = TeqPendingPillText,
                        modifier = Modifier.weight(1f)
                    )
                    EstadoKpiPill(
                        titulo = "Aprobadas",
                        valor = conteoAprobadas + conteoPagadasErp,
                        bgColor = TeqApproveBg,
                        textColor = TeqApproveText,
                        modifier = Modifier.weight(1f)
                    )
                    EstadoKpiPill(
                        titulo = "Obs / Rech",
                        valor = conteoObservadas + conteoRechazadas,
                        bgColor = if (conteoRechazadas > 0) TeqRejectBg else TeqObserveBg,
                        textColor = if (conteoRechazadas > 0) TeqRejectText else TeqObserveText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // =====================================================================
        // 2. TRAZABILIDAD DEL FLUJO END-TO-END (Técnico -> Jefatura -> Finanzas)
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(cardCorner),
            color = Color.White,
            border = BorderStroke(1.dp, TeqCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(17.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TeqTealIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = TeqTealDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            text = "PIPELINE DE AUDITORÍA",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Trazabilidad del Flujo MVP",
                            color = TeqNavy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                FlujoStepItem(
                    paso = "1",
                    titulo = "Terreno + TeqRuta (Cámara & GPS)",
                    descripcion = "Creación de carpeta ($conteoBorrador en borrador), captura fotográfica FileProvider y coordenadas GPS.",
                    estadoBadge = "Activo",
                    badgeBg = TeqTealSoft,
                    badgeText = TeqTealDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlujoStepItem(
                    paso = "2",
                    titulo = "Validación Desacoplada (GastoValidator)",
                    descripcion = "Control de RUT, Folio único en Room, antigüedad máx. 5 días, conversión UF/USD y topes CLP.",
                    estadoBadge = "100% Reglas",
                    badgeBg = TeqApproveBg,
                    badgeText = TeqApproveText
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlujoStepItem(
                    paso = "3",
                    titulo = "Evaluación Jefatura ($conteoPendientes por evaluar)",
                    descripcion = "Inspección visual de boleta, GPS y resolución con motivo obligatorio para Observar ($conteoObservadas) o Rechazar ($conteoRechazadas).",
                    estadoBadge = "$conteoAprobadas aprob.",
                    badgeBg = TeqPendingPillBg,
                    badgeText = TeqPendingPillText
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlujoStepItem(
                    paso = "4",
                    titulo = "Cierre Finanzas y Exportación ERP (.CSV)",
                    descripcion = "Registro de código de transferencia y generación de archivo físico .CSV ($conteoPagadasErp pagadas en ERP).",
                    estadoBadge = "ERP Ready",
                    badgeBg = TeqApproveBg,
                    badgeText = TeqApproveText
                )
            }
        }

        // =====================================================================
        // 3. TOPES CORPORATIVOS Y DESGLOSE POR CATEGORÍA
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(cardCorner),
            color = Color.White,
            border = BorderStroke(1.dp, TeqCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(17.dp)
            ) {
                Text(
                    text = "CONTROL PRESUPUESTARIO TEQMED SPA",
                    color = TeqMutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Gastos por Categoría vs Tope Unitario",
                    color = TeqNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                resumenCategorias.forEachIndexed { index, cat ->
                    val (icono, nombreCat) = obtenerIconoYNombreCategoria(cat.categoria)
                    val promedioBoleta = if (cat.cantidadBoletas > 0) {
                        cat.montoAcumuladoClp / cat.cantidadBoletas
                    } else {
                        0.0
                    }
                    val ratio = if (cat.topeUnitarioClp > 0) {
                        (promedioBoleta / cat.topeUnitarioClp).toFloat().coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    val progresoAnimado by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(500),
                        label = "progresoCategoria"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(TeqTealIconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icono,
                                        contentDescription = nombreCat,
                                        tint = TeqTealDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "$nombreCat (${cat.cantidadBoletas} boleta${if (cat.cantidadBoletas == 1) "" else "s"})",
                                        color = TeqNavy,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tope máx. por boleta: ${GastoValidator.formatearClp(cat.topeUnitarioClp)} CLP",
                                        color = TeqMutedText,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Text(
                                text = "${GastoValidator.formatearClp(cat.montoAcumuladoClp)} CLP",
                                color = TeqNavy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2F5))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progresoAnimado)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(if (ratio > 0.95f) TeqRejectText else TeqTeal)
                            )
                        }
                    }

                    if (index < resumenCategorias.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(TeqDivider)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección completa de "Ajustes, Adaptabilidad Android/iOS y Diagnóstico" para todos los roles.
 */
@Composable
fun TeqAjustesSection(
    nombreUsuario: String,
    rolUsuario: String,
    correoUsuario: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val platformMode = TeqAdaptivePlatformState.mode
    val cardCorner = if (platformMode == TeqPlatformMode.IOS_CUPERTINO) 24.dp else 19.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // =====================================================================
        // 1. SELECTOR INTERACTIVO DE ADAPTABILIDAD ANDROID / iOS
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(cardCorner),
            color = Color.White,
            border = BorderStroke(1.dp, TeqCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(TeqTealIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = TeqTealDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DISEÑO MULTIPLATAFORMA ADAPTABLE",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Adaptabilidad Android & iOS",
                            color = TeqNavy,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Alterna en tiempo real entre las guías visuales de Android (Material Design 3) e iOS (Apple Human Interface Guidelines / Cupertino).",
                    color = TeqLabelText,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformOptionCard(
                        icon = Icons.Default.PhoneAndroid,
                        titulo = "Android M3",
                        subtitulo = "Material Design 3",
                        seleccionado = platformMode == TeqPlatformMode.ANDROID_MATERIAL,
                        onClick = { TeqAdaptivePlatformState.mode = TeqPlatformMode.ANDROID_MATERIAL },
                        modifier = Modifier.weight(1f)
                    )
                    PlatformOptionCard(
                        icon = Icons.Default.PhoneIphone,
                        titulo = "iOS HIG",
                        subtitulo = "Cupertino / iPhone",
                        seleccionado = platformMode == TeqPlatformMode.IOS_CUPERTINO,
                        onClick = { TeqAdaptivePlatformState.mode = TeqPlatformMode.IOS_CUPERTINO },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // =====================================================================
        // 2. DIAGNÓSTICO DE ARQUITECTURA, RECURSOS NATIVOS Y REGLAS OFFLINE
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(cardCorner),
            color = Color.White,
            border = BorderStroke(1.dp, TeqCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PARÁMETROS TÉCNICOS DEL MVP",
                    color = TeqMutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Arquitectura, Hardware y Reglas",
                    color = TeqNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                ParametroTecnicoRow(
                    icono = Icons.Default.Layers,
                    titulo = "Patrón de Diseño",
                    detalle = "MVVM + Clean Architecture + Repository + UDF (StateFlow)"
                )
                ParametroTecnicoRow(
                    icono = Icons.Default.CameraAlt,
                    titulo = "Recurso Nativo 1: Cámara",
                    detalle = "FileProvider seguro (cacheDir/boletas) + TakePicture()"
                )
                ParametroTecnicoRow(
                    icono = Icons.Default.GpsFixed,
                    titulo = "Recurso Nativo 2: GPS",
                    detalle = "FusedLocationProviderClient + Fallback Concepción (-36.8270, -73.0503)"
                )
                ParametroTecnicoRow(
                    icono = Icons.Default.CurrencyExchange,
                    titulo = "Conversor Multimoneda Offline",
                    detalle = "1 UF = $38.000 CLP · 1 USD = $950 CLP · Plazo máx. 5 días"
                )
                ParametroTecnicoRow(
                    icono = Icons.Default.Shield,
                    titulo = "Persistencia Local Relacional",
                    detalle = "Room SQLite v2 (usuarios, rendiciones, gastos con ForeignKey CASCADE)"
                )
            }
        }

        // =====================================================================
        // 3. SESIÓN ACTIVA Y CAMBIO RÁPIDO DE ROL PARA DEFENSA EN VIVO
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(cardCorner),
            color = Color.White,
            border = BorderStroke(1.dp, TeqCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SESIÓN ACTIVA EN DEMOSTRACIÓN",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = nombreUsuario,
                            color = TeqNavy,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "$correoUsuario · Rol: $rolUsuario",
                            color = TeqTealDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = TeqApproveBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = TeqApproveText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verificado",
                                color = TeqApproveText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TeqPrimaryButton(
                    text = "Cambiar de Rol / Cerrar Sesión",
                    onClick = onLogout,
                    trailingIcon = Icons.AutoMirrored.Filled.Logout
                )
            }
        }
    }
}

@Composable
private fun EstadoKpiPill(
    titulo: String,
    valor: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor.toString(),
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = titulo,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FlujoStepItem(
    paso: String,
    titulo: String,
    descripcion: String,
    estadoBadge: String,
    badgeBg: Color,
    badgeText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFB))
            .border(1.dp, TeqBorder, RoundedCornerShape(12.dp))
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(TeqNavy),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = paso,
                color = TeqTeal,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = TeqNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = descripcion,
                color = TeqMutedText,
                fontSize = 9.sp,
                lineHeight = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Surface(
            shape = CircleShape,
            color = badgeBg
        ) {
            Text(
                text = estadoBadge,
                color = badgeText,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun PlatformOptionCard(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (seleccionado) TeqTealSoft else Color(0xFFF8FAFB),
        label = "bgPlatform"
    )
    val borderColor by animateColorAsState(
        targetValue = if (seleccionado) TeqTeal else TeqBorder,
        label = "borderPlatform"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(if (seleccionado) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = titulo,
                tint = if (seleccionado) TeqTealDark else TeqMutedText,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    color = if (seleccionado) TeqTealDark else TeqNavy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitulo,
                    color = TeqMutedText,
                    fontSize = 9.sp
                )
            }
            if (seleccionado) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = TeqTealDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ParametroTecnicoRow(
    icono: ImageVector,
    titulo: String,
    detalle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFF8FAFB))
            .border(1.dp, TeqBorder, RoundedCornerShape(11.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(TeqTealIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = TeqTealDark,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = TeqNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = detalle,
                color = TeqMutedText,
                fontSize = 9.sp
            )
        }
    }
}

/**
 * Calcula el resumen por las 5 categorías oficiales de TEQMED SPA a partir de una lista de gastos.
 */
fun construirResumenCategorias(gastos: List<GastoEntity>): List<ResumenCategoriaReporte> {
    val categoriasOficiales = listOf(
        GastoEntity.CAT_ALIMENTACION,
        GastoEntity.CAT_ALOJAMIENTO,
        GastoEntity.CAT_PEAJE_ESTACIONAMIENTO,
        GastoEntity.CAT_COMBUSTIBLE,
        GastoEntity.CAT_INSUMOS
    )
    val agrupados = gastos.groupBy { it.categoria.uppercase() }
    return categoriasOficiales.map { cat ->
        val items = agrupados[cat].orEmpty()
        ResumenCategoriaReporte(
            categoria = cat,
            montoAcumuladoClp = items.sumOf { it.montoConvertidoClp },
            cantidadBoletas = items.size,
            topeUnitarioClp = GastoValidator.obtenerTopeCategoriaClp(cat) ?: 0.0
        )
    }
}
