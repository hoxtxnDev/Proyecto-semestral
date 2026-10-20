package cl.duoc.teqgastos.ui.screens.tecnico

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.domain.validators.GastoValidator
import cl.duoc.teqgastos.ui.components.TeqAppHeader
import cl.duoc.teqgastos.ui.components.TeqBottomNav
import cl.duoc.teqgastos.ui.components.TeqPrimaryButton
import cl.duoc.teqgastos.ui.components.ValidatedTextField
import cl.duoc.teqgastos.ui.theme.TeqApproveBg
import cl.duoc.teqgastos.ui.theme.TeqApproveText
import cl.duoc.teqgastos.ui.theme.TeqBackground
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
import cl.duoc.teqgastos.ui.theme.TeqTealMid
import cl.duoc.teqgastos.ui.theme.TeqTealSoft

/**
 * Pantalla de Técnico (`TechnicianScreen` de Figma `src-interface/App.tsx`).
 * Implementa registro de gastos con validaciones desacopladas (`GastoValidator`),
 * conversión multimoneda offline (CLP, UF, USD), topes por categoría, captura de comprobante
 * y visualización de historial de rendiciones en tiempo real.
 */
@Composable
fun TecnicoScreen(
    viewModel: TecnicoViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rendiciones by viewModel.misRendiciones.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TeqBackground,
        bottomBar = {
            TeqBottomNav(
                activeTab = uiState.tabActiva,
                onTabSelected = viewModel::onTabSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            TeqAppHeader(
                eyebrow = if (uiState.tabActiva == "Rendiciones") "NUEVA RENDICIÓN" else "MIS RENDICIONES",
                title = if (uiState.tabActiva == "Rendiciones") "Registrar gasto" else "Estado de envíos",
                subtitle = if (uiState.tabActiva == "Rendiciones") {
                    "Completa los datos de tu comprobante."
                } else {
                    "Seguimiento en tiempo real de tus rendiciones."
                },
                userInitials = "CM",
                searchQuery = uiState.searchQuery,
                onSearchChange = viewModel::onSearchQueryChange,
                onBack = onLogout
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 17.dp, vertical = 17.dp)
            ) {
                // Banner animado de éxito al enviar rendición
                AnimatedVisibility(
                    visible = uiState.mensajeExito != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 13.dp),
                        color = TeqApproveBg,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TeqApproveText.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TeqApproveText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.mensajeExito.orEmpty(),
                                color = TeqApproveText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Banner animado de error general / boleta duplicada
                AnimatedVisibility(
                    visible = uiState.mensajeErrorGeneral != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 13.dp),
                        color = TeqRejectBg,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TeqRejectText.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = TeqRejectText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.mensajeErrorGeneral.orEmpty(),
                                color = TeqRejectText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (uiState.tabActiva == "Rendiciones") {
                    FormularioRegistroGastoFigma(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                } else {
                    HistorialRendicionesTecnico(
                        rendiciones = rendiciones.filter {
                            uiState.searchQuery.isBlank() ||
                                it.nombreTecnico.contains(uiState.searchQuery, ignoreCase = true) ||
                                it.centroCosto.contains(uiState.searchQuery, ignoreCase = true) ||
                                it.estado.contains(uiState.searchQuery, ignoreCase = true)
                        },
                        onIrARegistrar = { viewModel.onTabSelected("Rendiciones") }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormularioRegistroGastoFigma(
    uiState: TecnicoUiState,
    viewModel: TecnicoViewModel
) {
    // =========================================================================
    // 1. TARJETA CENTRO DE COSTO ACTIVO (.cost-center-card)
    // =========================================================================
    Surface(
        onClick = viewModel::alternarCentroCosto,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, TeqCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(39.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TeqTealIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = TeqTealDark,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CENTRO DE COSTO ACTIVO (TOQUE PARA CAMBIAR)",
                    color = TeqMutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = uiState.centroCostoActivo,
                    color = TeqNavy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Cambiar centro de costo",
                tint = TeqMutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(13.dp))

    // =========================================================================
    // 2. TARJETA DE FORMULARIO (.form-card: Datos del gasto + Categorías)
    // =========================================================================
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, TeqCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(19.dp)
        ) {
            // Cabecera de tarjeta (.card-title-row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "COMPROBANTE",
                        color = TeqMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Datos del gasto",
                        color = TeqNavy,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF0F3F5)
                ) {
                    Text(
                        text = "1 DE 2",
                        color = Color(0xFF788793),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dos columnas: RUT Proveedor y Folio (.two-column)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                ValidatedTextField(
                    value = uiState.rutProveedor,
                    onValueChange = viewModel::onRutChange,
                    label = "RUT Proveedor",
                    placeholder = "76.123.456-7",
                    errorMessage = uiState.rutError,
                    modifier = Modifier.weight(1.35f)
                )

                ValidatedTextField(
                    value = uiState.folioBoleta,
                    onValueChange = viewModel::onFolioChange,
                    label = "Folio",
                    placeholder = "008321",
                    errorMessage = uiState.folioError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(13.dp))

            // Selector de Moneda (CLP / UF / USD) + Campo Monto con validación de Tope por Categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Moneda de origen",
                    color = TeqLabelText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        GastoEntity.MONEDA_CLP,
                        GastoEntity.MONEDA_UF,
                        GastoEntity.MONEDA_USD
                    ).forEach { codigoMoneda ->
                        val activa = uiState.moneda == codigoMoneda
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activa) TeqTealSoft else Color(0xFFF0F3F5))
                                .border(
                                    width = 1.dp,
                                    color = if (activa) TeqTeal else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.onMonedaChange(codigoMoneda) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = codigoMoneda,
                                color = if (activa) TeqTealDark else TeqLabelText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ValidatedTextField(
                value = uiState.montoTexto,
                onValueChange = viewModel::onMontoChange,
                label = "Monto (${uiState.categoriaSeleccionada.nombreVisible}: máx ${uiState.categoriaSeleccionada.topeEtiqueta})",
                placeholder = "0",
                suffixText = uiState.moneda,
                errorMessage = uiState.montoError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(13.dp))

            // Control de Fecha del Gasto (máx 5 días de antigüedad / no futura)
            Text(
                text = "Fecha de emisión (Control máx. 5 días)",
                color = TeqLabelText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            val ahora = System.currentTimeMillis()
            val opcionesFecha = listOf(
                "Hoy (En plazo)" to ahora,
                "Hace 3 días" to (ahora - 3L * GastoValidator.MILLIS_POR_DIA),
                "Hace 7 días (Fuera plazo)" to (ahora - 7L * GastoValidator.MILLIS_POR_DIA)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                opcionesFecha.forEach { (etiqueta, timestamp) ->
                    val diffDias = ((ahora - uiState.fechaGastoMillis) / GastoValidator.MILLIS_POR_DIA).toInt()
                    val esSeleccionada = when (etiqueta) {
                        "Hoy (En plazo)" -> diffDias <= 1
                        "Hace 3 días" -> diffDias in 2..4
                        else -> diffDias >= 5
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (esSeleccionada) TeqTealSoft else Color(0xFFFAFBFB))
                            .border(
                                width = 1.dp,
                                color = if (esSeleccionada) TeqTeal else TeqBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.onFechaChange(timestamp) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = if (esSeleccionada) TeqTealDark else TeqMutedText,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = etiqueta,
                                color = if (esSeleccionada) TeqTealDark else TeqLabelText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.fechaError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, start = 4.dp),
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
                        text = uiState.fechaError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Sección de Categorías (.category-section / .category-grid)
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TeqDivider)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Categoría",
                color = TeqLabelText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val filasCategorias = CategoriaGastoUi.entries.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filasCategorias.forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { cat ->
                            val seleccionada = uiState.categoriaSeleccionada == cat
                            CategoryGridTile(
                                categoria = cat,
                                seleccionada = seleccionada,
                                onClick = { viewModel.onCategoriaChange(cat) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (fila.size < 3) {
                            repeat(3 - fila.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(13.dp))

    // =========================================================================
    // 3. TARJETA CONVERSIÓN OFFLINE (.rates-card)
    // =========================================================================
    Surface(
        shape = RoundedCornerShape(17.dp),
        color = TeqNavy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TeqTeal.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = TeqTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(9.dp))
                Column {
                    Text(
                        text = "Conversión offline",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.montoConvertidoClp > 0) {
                            "Equivalente: ${GastoValidator.formatearClp(uiState.montoConvertidoClp)} CLP"
                        } else {
                            "Tasas estáticas disponibles"
                        },
                        color = if (uiState.montoConvertidoClp > 0) TeqTeal else Color.White.copy(alpha = 0.45f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "1 UF",
                        color = TeqTeal,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$38.000 CLP",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Color.White.copy(alpha = 0.14f))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "1 USD",
                        color = TeqTeal,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$950 CLP",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(13.dp))

    // =========================================================================
    // 4. TARJETA DE CAPTURA DE COMPROBANTE Y GPS (.capture-card)
    // =========================================================================
    val tieneFoto = !uiState.fotoUri.isNullOrBlank()
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, TeqCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(19.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "RESPALDO",
                        color = TeqMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Comprobante",
                        color = TeqNavy,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }

                if (tieneFoto) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE8F8EF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF168A59),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LISTO",
                                color = Color(0xFF168A59),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caja de captura (.receipt-capture)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9FBFB))
                    .border(
                        width = 1.dp,
                        color = if (uiState.fotoError != null) {
                            MaterialTheme.colorScheme.error
                        } else if (tieneFoto) {
                            TeqTealMid
                        } else {
                            Color(0xFFCDD7DC)
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tieneFoto) {
                    // Miniatura inclinada (.receipt-preview)
                    Surface(
                        modifier = Modifier
                            .size(width = 48.dp, height = 59.dp)
                            .rotate(-3f),
                        shape = RoundedCornerShape(5.dp),
                        color = Color.White,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "BOLETA SII",
                                fontSize = 5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TeqNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(2.dp)
                                    .background(Color(0xFFD9DFE2))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .height(2.dp)
                                    .background(Color(0xFFD9DFE2))
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if (uiState.montoConvertidoClp > 0) {
                                    GastoValidator.formatearClp(uiState.montoConvertidoClp)
                                } else {
                                    "$24.990"
                                },
                                fontSize = 6.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TeqNavy
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDFF9F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cámara",
                            tint = Color(0xFF009B92),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (tieneFoto) "Comprobante capturado" else "Fotografía tu boleta",
                        color = Color(0xFF3B4B64),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (tieneFoto) "IMG_3841.jpg · 1,2 MB" else "Asegúrate de que sea legible",
                        color = TeqMutedText,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    onClick = { viewModel.onToggleFotoComprobante() },
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xFFDFF9F7)
                ) {
                    Text(
                        text = if (tieneFoto) "Cambiar" else "Tomar foto",
                        color = TeqTealDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.fotoError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, start = 4.dp),
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
                        text = uiState.fotoError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badge de Georreferenciación GPS (.location-badge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0FAF5))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(29.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF299C69),
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(9.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UBICACIÓN CAPTURADA",
                        color = Color(0xFF52A47B),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${uiState.latitud}, ${uiState.longitud}",
                        color = Color(0xFF3F6855),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Ubicación verificada",
                    tint = Color(0xFF299C69),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(15.dp))

    // =========================================================================
    // 5. BOTÓN ENVIAR RENDICIÓN (.submit-expense)
    // =========================================================================
    TeqPrimaryButton(
        text = "Enviar rendición",
        onClick = viewModel::enviarRendicion,
        isLoading = uiState.isSubmitting
    )
}

@Composable
private fun CategoryGridTile(
    categoria: CategoriaGastoUi,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icono: ImageVector = when (categoria) {
        CategoriaGastoUi.ALIMENTACION -> Icons.Default.Restaurant
        CategoriaGastoUi.ALOJAMIENTO -> Icons.Default.Hotel
        CategoriaGastoUi.COMBUSTIBLE -> Icons.Default.LocalGasStation
        CategoriaGastoUi.PEAJE_ESTAC -> Icons.Default.LocalParking
        CategoriaGastoUi.INSUMOS -> Icons.Default.Receipt
    }

    Column(
        modifier = modifier
            .height(94.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (seleccionada) Color(0xFFEFFCFB) else Color(0xFFFAFBFC))
            .border(
                width = if (seleccionada) 1.5.dp else 1.dp,
                color = if (seleccionada) Color(0xFF00C9BD) else Color(0xFFE6EBEE),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF00BDB2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = categoria.nombreVisible,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = categoria.nombreVisible,
            color = Color(0xFF40516A),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = categoria.topeEtiqueta,
            color = TeqMutedText,
            fontSize = 8.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistorialRendicionesTecnico(
    rendiciones: List<RendicionEntity>,
    onIrARegistrar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = "HISTORIAL",
                color = TeqMutedText,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp
            )
            Text(
                text = "Rendiciones registradas (${rendiciones.size})",
                color = TeqNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            onClick = onIrARegistrar,
            shape = RoundedCornerShape(9.dp),
            color = TeqTealSoft,
            border = BorderStroke(1.dp, TeqTeal)
        ) {
            Text(
                text = "+ Nuevo gasto",
                color = TeqTealDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rendiciones.forEach { rendicion ->
            val (bgColor, textColor) = when (rendicion.estado) {
                RendicionEntity.ESTADO_APROBADA, RendicionEntity.ESTADO_PAGADA_ERP -> TeqApproveBg to TeqApproveText
                RendicionEntity.ESTADO_OBSERVADA -> TeqObserveBg to TeqObserveText
                RendicionEntity.ESTADO_RECHAZADA -> TeqRejectBg to TeqRejectText
                else -> TeqPendingPillBg to TeqPendingPillText
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, TeqCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rendicion.nombreTecnico,
                                color = TeqNavy,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = rendicion.centroCosto,
                                color = TeqMutedText,
                                fontSize = 10.sp
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = bgColor
                        ) {
                            Text(
                                text = rendicion.estado,
                                color = textColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${GastoValidator.formatearClp(rendicion.montoTotalClp)} CLP",
                        color = TeqNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (!rendicion.comentarioRevision.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Motivo Jefatura: ${rendicion.comentarioRevision}",
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!rendicion.codigoPagoFinanzas.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Código ERP: ${rendicion.codigoPagoFinanzas}",
                            color = TeqTealDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
