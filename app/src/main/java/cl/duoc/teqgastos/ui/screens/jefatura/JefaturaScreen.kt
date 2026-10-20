package cl.duoc.teqgastos.ui.screens.jefatura

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.domain.validators.GastoValidator
import cl.duoc.teqgastos.ui.components.TeqAppHeader
import cl.duoc.teqgastos.ui.components.TeqBottomNav
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
import cl.duoc.teqgastos.ui.theme.TeqTealDark
import cl.duoc.teqgastos.ui.theme.TeqTealMid

/**
 * Pantalla de Jefatura (`ManagerScreen` de Figma `src-interface/App.tsx`).
 * Permite auditar rendiciones pendientes, aprobarlas directamente o abrir el modal
 * de Observación/Rechazo con justificación obligatoria validada por `GastoValidator`.
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
            item.rutProveedor.contains(uiState.searchQuery, ignoreCase = true) ||
            item.folioBoleta.contains(uiState.searchQuery, ignoreCase = true) ||
            item.categoria.contains(uiState.searchQuery, ignoreCase = true)
    }

    val montoTotalPendientes = pendientes.sumOf { it.montoTotalClp }

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
                title = "Por evaluar",
                subtitle = "Revisa los gastos enviados por tu equipo.",
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
                    // Anillo decorativo inferior derecho (.summary-card::after)
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
                        // Columna izquierda: Cantidad de pendientes
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

                        // Divisor vertical
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(Color.White.copy(alpha = 0.22f))
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        // Columna central: Monto Total
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

                    // Ícono de reloj superior derecho (.summary-icon)
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
                                "Todas las rendiciones"
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
                                text = if (uiState.mostrarSoloPendientes) "Ver historial" else "Solo pendientes",
                                color = Color(0xFF72818D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(13.dp))

                // =============================================================
                // 3. LISTA DE RENDICIONES (.expense-list / .expense-card)
                // =============================================================
                if (listaFiltrada.isEmpty()) {
                    // Estado vacío fiel a `.empty-state` de Figma
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
                            ExpenseReviewCardFigma(
                                item = item,
                                onApprove = { viewModel.aprobarRendicion(item.rendicionId) },
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

    // =========================================================================
    // 4. MODAL DE JUSTIFICACIÓN OBLIGATORIA (.action-modal en Figma)
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
                    // Fila superior: Ícono de acción + botón cerrar
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
                        text = "${accionModal.etiqueta.uppercase()} RENDICIÓN",
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

                    Text(
                        text = "Justificación obligatoria",
                        color = TeqLabelText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.comentarioModal,
                            onValueChange = viewModel::onComentarioModalChange,
                            placeholder = {
                                Text(
                                    text = "Escribe una explicación clara...",
                                    color = TeqMutedText,
                                    fontSize = 12.sp
                                )
                            },
                            isError = uiState.comentarioError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(108.dp),
                            shape = RoundedCornerShape(13.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFBFCFC),
                                unfocusedContainerColor = Color(0xFFFBFCFC),
                                focusedBorderColor = TeqTealMid,
                                unfocusedBorderColor = TeqBorder
                            )
                        )
                        Text(
                            text = "${uiState.comentarioModal.length}/240",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 10.dp, bottom = 8.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = uiState.comentarioError != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = uiState.comentarioError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = viewModel::confirmarAccionModal,
                        enabled = uiState.comentarioModal.trim().isNotEmpty(),
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
private fun ExpenseReviewCardFigma(
    item: ItemEvaluacionJefatura,
    onApprove: () -> Unit,
    onObserve: () -> Unit,
    onReject: () -> Unit
) {
    val (iconoCat, nombreCat) = obtenerIconoYNombreCategoria(item.categoria)
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
        border = BorderStroke(1.dp, TeqCardBorder),
        modifier = Modifier.fillMaxWidth()
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
                        text = item.rutProveedor,
                        color = TeqMutedText,
                        fontSize = 10.sp
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

            // 2. Cuerpo del gasto (.expense-body)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Miniatura recibo (.receipt-thumb)
                Column(
                    modifier = Modifier
                        .size(width = 60.dp, height = 67.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFEEF1F3)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Ver boleta",
                        tint = Color(0xFF81909B),
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "VER",
                        color = Color(0xFF81909B),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Categoría pill (.expense-category)
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

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "FOLIO ${item.folioBoleta}",
                        color = TeqMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

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
                }
            }

            // 3. Botones de Acción (.expense-actions) si está pendiente
            if (esPendiente) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Botón Aprobar
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

                    // Botón Observar
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

                    // Botón Rechazar
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
                    text = "Observación: ${item.comentarioRevision}",
                    color = TeqLabelText,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
