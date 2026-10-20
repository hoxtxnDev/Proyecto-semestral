package cl.duoc.teqgastos.ui.screens.jefatura

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.domain.validators.GastoValidator
import cl.duoc.teqgastos.ui.components.TeqAjustesSection
import cl.duoc.teqgastos.ui.components.TeqAppHeader
import cl.duoc.teqgastos.ui.components.TeqBottomNav
import cl.duoc.teqgastos.ui.components.TeqReportesSection
import cl.duoc.teqgastos.ui.components.ValidatedTextField
import cl.duoc.teqgastos.ui.components.construirResumenCategorias
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
import cl.duoc.teqgastos.ui.theme.TeqObserveBtnBg
import cl.duoc.teqgastos.ui.theme.TeqObserveText
import cl.duoc.teqgastos.ui.theme.TeqPendingPillBg
import cl.duoc.teqgastos.ui.theme.TeqPendingPillText
import cl.duoc.teqgastos.ui.theme.TeqRejectBg
import cl.duoc.teqgastos.ui.theme.TeqRejectBtnBg
import cl.duoc.teqgastos.ui.theme.TeqRejectText
import cl.duoc.teqgastos.ui.theme.TeqTeal
import cl.duoc.teqgastos.ui.theme.TeqTealDark
import cl.duoc.teqgastos.ui.theme.TeqTealIconBg
import cl.duoc.teqgastos.ui.theme.TeqTealMid
import cl.duoc.teqgastos.utils.NativeResourcesHelper

/**
 * Pantalla de evaluación de Jefatura para aprobar, observar o rechazar rendiciones.
 */
@Composable
fun JefaturaScreen(
    viewModel: JefaturaViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todasLasItems by viewModel.itemsEvaluacion.collectAsStateWithLifecycle()

    val pendientes = todasLasItems.filter { it.estado == RendicionEntity.ESTADO_PENDIENTE_JEFATURA }
    val listaBase = if (uiState.mostrarSoloPendientes) pendientes else todasLasItems
    val listaFiltrada = listaBase.filter { item ->
        uiState.searchQuery.isBlank() ||
            item.nombreTecnico.contains(uiState.searchQuery, ignoreCase = true) ||
            item.rutProveedorPrincipal.contains(uiState.searchQuery, ignoreCase = true) ||
            item.folioPrincipal.contains(uiState.searchQuery, ignoreCase = true) ||
            item.categoriaPrincipal.contains(uiState.searchQuery, ignoreCase = true)
    }

    val montoTotalPendientes = pendientes.sumOf { it.montoTotalClp }
    val todosLosGastosJefatura = remember(todasLasItems) {
        todasLasItems.flatMap { item -> item.gastosAuditados.map { it.gasto } }
    }
    val resumenCategorias = remember(todosLosGastosJefatura) {
        construirResumenCategorias(todosLosGastosJefatura)
    }

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
                eyebrow = "JEFATURA",
                title = when (uiState.tabActiva) {
                    "Reportes" -> "Analítica y Topes"
                    "Ajustes" -> "Ajustes y Plataforma"
                    else -> "Por evaluar"
                },
                subtitle = when (uiState.tabActiva) {
                    "Reportes" -> "Trazabilidad completa de aprobaciones, observaciones y topes."
                    "Ajustes" -> "Adaptabilidad Android/iOS, reglas corporativas y sesión."
                    else -> "Revisa los gastos, fotos, GPS y cumplimiento de topes."
                },
                userInitials = "AS",
                searchQuery = uiState.searchQuery,
                onSearchChange = viewModel::onSearchQueryChange,
                onFilterClick = viewModel::alternarFiltroPendientes,
                onBack = onLogout
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 17.dp, vertical = 17.dp)
            ) {
                if (uiState.tabActiva == "Reportes") {
                    TeqReportesSection(
                        rolActual = "JEFATURA",
                        totalMontoClp = todasLasItems.sumOf { it.montoTotalClp },
                        totalRendiciones = todasLasItems.size,
                        conteoBorrador = todasLasItems.count { it.estado == RendicionEntity.ESTADO_BORRADOR },
                        conteoPendientes = todasLasItems.count { it.estado == RendicionEntity.ESTADO_PENDIENTE_JEFATURA },
                        conteoObservadas = todasLasItems.count { it.estado == RendicionEntity.ESTADO_OBSERVADA },
                        conteoAprobadas = todasLasItems.count { it.estado == RendicionEntity.ESTADO_APROBADA },
                        conteoPagadasErp = todasLasItems.count { it.estado == RendicionEntity.ESTADO_PAGADA_ERP },
                        conteoRechazadas = todasLasItems.count { it.estado == RendicionEntity.ESTADO_RECHAZADA },
                        resumenCategorias = resumenCategorias
                    )
                } else if (uiState.tabActiva == "Ajustes") {
                    TeqAjustesSection(
                        nombreUsuario = "Andrea Soto",
                        rolUsuario = "JEFATURA",
                        correoUsuario = "jefatura@teqmed.cl",
                        onLogout = onLogout
                    )
                } else {
                // Banner animado de confirmación de decisión
                AnimatedVisibility(
                    visible = uiState.mensajeFeedback != null,
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
                            modifier = Modifier.padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TeqApproveText,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.mensajeFeedback.orEmpty(),
                                color = TeqApproveText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // =============================================================
                // 1. TARJETA RESUMEN GRADIENTE TEAL (.summary-card)
                // =============================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(117.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(TeqTealDark, TeqTealMid)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 55.dp, y = 75.dp)
                            .border(22.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PENDIENTES",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = pendientes.size.toString().padStart(2, '0'),
                                    color = Color.White,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 36.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "rendiciones",
                                    color = Color.White.copy(alpha = 0.70f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(Color.White.copy(alpha = 0.22f))
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MONTO TOTAL",
                                color = Color.White.copy(alpha = 0.70f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.0.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = GastoValidator.formatearClp(montoTotalPendientes),
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "CLP",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(39.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // =============================================================
                // 2. ENCABEZADO DE LISTA (.list-header)
                // =============================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "BANDEJA DE ENTRADA",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.4.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (uiState.mostrarSoloPendientes) {
                                "Rendiciones recientes"
                            } else {
                                "Historial de auditoría"
                            },
                            color = TeqNavy,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        onClick = viewModel::alternarFiltroPendientes,
                        shape = RoundedCornerShape(9.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, TeqBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtrar",
                                tint = Color(0xFF72818D),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (uiState.mostrarSoloPendientes) "Ver todas" else "Solo pendientes",
                                color = Color(0xFF72818D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(13.dp))

                // =============================================================
                // 3. LISTA DE RENDICIONES CON DESGLOSE DE GASTOS (.expense-list)
                // =============================================================
                if (listaFiltrada.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(19.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, TeqCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(55.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5F8ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF15915C),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(13.dp))
                            Text(
                                text = "Todo al día",
                                color = TeqNavy,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "No quedan rendiciones pendientes.",
                                color = TeqMutedText,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        listaFiltrada.forEach { item ->
                            val expandida = uiState.rendicionExpandidaId == item.rendicionId
                            ExpenseReviewCard(
                                item = item,
                                expandida = expandida,
                                onToggleExpand = {
                                    viewModel.alternarExpansionRendicion(item.rendicionId)
                                },
                                onInspeccionarGasto = { gastoAuditado ->
                                    viewModel.inspeccionarGasto(gastoAuditado)
                                },
                                onApprove = { viewModel.aprobarRendicion(item) },
                                onObserve = {
                                    viewModel.abrirModalRevision(
                                        item = item,
                                        accion = AccionRevisionJefatura.OBSERVAR
                                    )
                                },
                                onReject = {
                                    viewModel.abrirModalRevision(
                                        item = item,
                                        accion = AccionRevisionJefatura.RECHAZAR
                                    )
                                }
                            )
                        }
                    }
                }
                }
            }
        }
    }

    // =========================================================================
    // 4. VISOR DE BOLETA, GPS Y CUMPLIMIENTO DE TOPES (Inspección Jefatura)
    // =========================================================================
    uiState.gastoInspeccionado?.let { gastoAuditado ->
        InspeccionBoletaDialog(
            gastoAuditado = gastoAuditado,
            onDismiss = { viewModel.inspeccionarGasto(null) }
        )
    }

    // =========================================================================
    // 5. MODAL DE OBSERVACIÓN / RECHAZO CON ValidatedTextField OBLIGATORIO
    // =========================================================================
    val itemModal = uiState.itemEnModal
    val accionModal = uiState.accionModal
    if (itemModal != null && accionModal != null) {
        val esRechazo = accionModal == AccionRevisionJefatura.RECHAZAR
        Dialog(onDismissRequest = viewModel::cerrarModalRevision) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 18.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(43.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (esRechazo) Color(0xFFFDE5E5) else Color(0xFFFFF1C8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (esRechazo) Icons.Default.Close else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (esRechazo) Color(0xFFB74646) else Color(0xFFA87312),
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        Surface(
                            onClick = viewModel::cerrarModalRevision,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0F3F5),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color(0xFF8B98A1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${accionModal.etiqueta.uppercase()} RENDICIÓN #${itemModal.rendicionId}",
                        color = TeqMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Cuéntanos el motivo",
                        color = TeqNavy,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tu comentario será enviado a ${itemModal.nombreTecnico} para que pueda revisar este gasto.",
                        color = Color(0xFF7F8D97),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de comentario obligatorio validado con ValidatedTextField (ícono de error + AnimatedVisibility)
                    ValidatedTextField(
                        value = uiState.comentarioModal,
                        onValueChange = viewModel::onComentarioModalChange,
                        label = "Justificación obligatoria (${uiState.comentarioModal.length}/240)",
                        placeholder = "Escribe una explicación clara (mín. 5 caracteres)...",
                        errorMessage = uiState.comentarioError,
                        leadingIcon = Icons.Default.Comment,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = viewModel::confirmarAccionModal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esRechazo) TeqRejectBtnBg else TeqObserveBtnBg,
                            contentColor = if (esRechazo) Color.White else Color(0xFF7A5310)
                        )
                    ) {
                        Text(
                            text = "Confirmar y enviar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseReviewCard(
    item: ItemEvaluacionJefatura,
    expandida: Boolean,
    onToggleExpand: () -> Unit,
    onInspeccionarGasto: (GastoAuditadoUi) -> Unit,
    onApprove: () -> Unit,
    onObserve: () -> Unit,
    onReject: () -> Unit
) {
    val (iconoCat, nombreCat) = obtenerIconoYNombreCategoria(item.categoriaPrincipal)
    val esPendiente = item.estado == RendicionEntity.ESTADO_PENDIENTE_JEFATURA

    val (pillBg, pillText, pillLabel) = when (item.estado) {
        RendicionEntity.ESTADO_PENDIENTE_JEFATURA -> Triple(TeqPendingPillBg, TeqPendingPillText, "PENDIENTE")
        RendicionEntity.ESTADO_APROBADA -> Triple(TeqApproveBg, TeqApproveText, "APROBADO")
        RendicionEntity.ESTADO_OBSERVADA -> Triple(TeqObserveBg, TeqObserveText, "OBSERVADO")
        RendicionEntity.ESTADO_RECHAZADA -> Triple(TeqRejectBg, TeqRejectText, "RECHAZADO")
        RendicionEntity.ESTADO_PAGADA_ERP -> Triple(TeqApproveBg, TeqApproveText, "PAGADO ERP")
        else -> Triple(TeqPendingPillBg, TeqPendingPillText, item.estado)
    }

    Surface(
        shape = RoundedCornerShape(19.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (expandida) TeqTealMid else TeqCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            // 1. Cabecera persona (.expense-person)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFDEF8F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.iniciales,
                        color = TeqTealDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(9.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nombreTecnico,
                        color = TeqNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.rutProveedorPrincipal} · ${item.centroCosto}",
                        color = TeqMutedText,
                        fontSize = 9.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = pillBg
                ) {
                    Text(
                        text = pillLabel,
                        color = pillText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TeqDivider)
            )

            // 2. Cuerpo principal del gasto (.expense-body)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón miniatura recibo (.receipt-thumb) -> Abre visor de boleta, foto, GPS y tope
                Column(
                    modifier = Modifier
                        .size(width = 60.dp, height = 67.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFEEF1F3))
                        .clickable {
                            item.gastosAuditados.firstOrNull()?.let(onInspeccionarGasto)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Ver boleta",
                        tint = TeqTealDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "VER FOTO",
                        color = TeqTealDark,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Color(0xFFE6FAF8)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = iconoCat,
                                    contentDescription = null,
                                    tint = TeqTealDark,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = nombreCat,
                                    color = TeqTealDark,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Badge de auditoría de topes
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = if (item.todosCumplenTope) TeqApproveBg else TeqRejectBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.todosCumplenTope) {
                                        Icons.Default.VerifiedUser
                                    } else {
                                        Icons.Default.ErrorOutline
                                    },
                                    contentDescription = null,
                                    tint = if (item.todosCumplenTope) TeqApproveText else TeqRejectText,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (item.todosCumplenTope) "Cumple tope" else "Excede tope",
                                    color = if (item.todosCumplenTope) TeqApproveText else TeqRejectText,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "FOLIO ${item.folioPrincipal} · ${item.gastosAuditados.size} ítem(s)",
                        color = TeqMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = GastoValidator.formatearClp(item.montoTotalClp),
                                color = TeqNavy,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CLP",
                                color = TeqMutedText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.clickable(onClick = onToggleExpand),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (expandida) "Ocultar desglose" else "Desglose",
                                color = TeqTealDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TeqTealDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 3. Desglose expandible de gastos (Foto, GPS, Topes)
            if (expandida && item.gastosAuditados.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TeqDivider)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "DESGLOSE DE ÍTEMS Y AUDITORÍA DE TOPES",
                    color = TeqMutedText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.0.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    item.gastosAuditados.forEach { auditado ->
                        val g = auditado.gasto
                        val (_, catNombre) = obtenerIconoYNombreCategoria(g.categoria)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color(0xFFF8FAFB))
                                .border(1.dp, TeqBorder, RoundedCornerShape(11.dp))
                                .clickable { onInspeccionarGasto(auditado) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$catNombre · Folio ${g.folioBoleta}",
                                        color = TeqNavy,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (g.origenTeqRuta) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = TeqNavy
                                        ) {
                                            Text(
                                                text = "TeqRuta",
                                                color = TeqTeal,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "RUT: ${g.rutProveedor} · ${auditado.detalleTope}",
                                    color = if (auditado.cumpleTope) TeqApproveText else TeqRejectText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "GPS: ${g.latitud ?: -36.8270}, ${g.longitud ?: -73.0503}",
                                    color = TeqMutedText,
                                    fontSize = 8.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = GastoValidator.formatearClp(g.montoConvertidoClp),
                                    color = TeqNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Ver foto",
                                        tint = TeqTealDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Ver foto",
                                        color = TeqTealDark,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 4. Botones de Acción (.expense-actions)
            if (esPendiente) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1.15f)
                            .height(34.dp),
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TeqApproveBg,
                            contentColor = TeqApproveText
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Aprobar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = onObserve,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TeqObserveBg,
                            contentColor = TeqObserveText
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Observar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TeqRejectBg,
                            contentColor = TeqRejectText
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rechazar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else if (!item.comentarioRevision.isNullOrBlank()) {
                Text(
                    text = "Resolución Jefatura: ${item.comentarioRevision}",
                    color = TeqLabelText,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Diálogo de inspección detallada para que Jefatura verifique la fotografía de la boleta,
 * las coordenadas GPS de captura en terreno y el cumplimiento del tope por categoría.
 */
@Composable
private fun InspeccionBoletaDialog(
    gastoAuditado: GastoAuditadoUi,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val gasto = gastoAuditado.gasto
    val (_, nombreCat) = obtenerIconoYNombreCategoria(gasto.categoria)
    val bitmapReal = remember(gasto.fotoUri) {
        NativeResourcesHelper.cargarMiniaturaDesdeUri(context, gasto.fotoUri)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AUDITORÍA DE COMPROBANTE",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.3.sp
                        )
                        Text(
                            text = "Boleta Folio ${gasto.folioBoleta}",
                            color = TeqNavy,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0F3F5),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TeqMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vista de la fotografía de la boleta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF4F6F8))
                        .border(1.dp, TeqBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmapReal != null) {
                        Image(
                            bitmap = bitmapReal,
                            contentDescription = "Fotografía de boleta",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = TeqTealDark,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "COMPROBANTE ELECTRÓNICO SII",
                                color = TeqNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "RUT Proveedor: ${gasto.rutProveedor} · Folio: ${gasto.folioBoleta}",
                                color = TeqLabelText,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "URI: ${gasto.fotoUri ?: "Sin adjunto"}",
                                color = TeqMutedText,
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verificación de Tope por Categoría
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (gastoAuditado.cumpleTope) TeqApproveBg else TeqRejectBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (gastoAuditado.cumpleTope) {
                                Icons.Default.VerifiedUser
                            } else {
                                Icons.Default.ErrorOutline
                            },
                            contentDescription = null,
                            tint = if (gastoAuditado.cumpleTope) TeqApproveText else TeqRejectText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "$nombreCat · ${GastoValidator.formatearClp(gasto.montoConvertidoClp)} CLP (${gasto.moneda})",
                                color = if (gastoAuditado.cumpleTope) TeqApproveText else TeqRejectText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = gastoAuditado.detalleTope,
                                color = if (gastoAuditado.cumpleTope) TeqApproveText else TeqRejectText,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Coordenadas GPS capturadas en terreno
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FAF5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF299C69),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GEORREFERENCIACIÓN GPS EN TERRENO",
                                color = Color(0xFF52A47B),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Lat: ${gasto.latitud ?: -36.8270}, Lon: ${gasto.longitud ?: -73.0503}",
                                color = Color(0xFF3F6855),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TeqNavy,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Cerrar inspección",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

internal fun obtenerIconoYNombreCategoria(categoria: String): Pair<ImageVector, String> {
    return when (categoria.trim().uppercase()) {
        GastoEntity.CAT_ALIMENTACION -> Icons.Default.Restaurant to "Alimentación"
        GastoEntity.CAT_ALOJAMIENTO -> Icons.Default.Hotel to "Alojamiento"
        GastoEntity.CAT_COMBUSTIBLE -> Icons.Default.LocalGasStation to "Combustible"
        GastoEntity.CAT_PEAJE_ESTACIONAMIENTO -> Icons.Default.LocalParking to "Peaje / Estac."
        GastoEntity.CAT_INSUMOS -> Icons.Default.Receipt to "Insumos"
        else -> Icons.Default.Receipt to categoria
    }
}
