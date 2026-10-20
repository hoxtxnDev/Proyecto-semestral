package cl.duoc.teqgastos.ui.screens.tecnico

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import cl.duoc.teqgastos.ui.components.TeqPrimaryButton
import cl.duoc.teqgastos.ui.components.TeqReportesSection
import cl.duoc.teqgastos.ui.components.ValidatedTextField
import cl.duoc.teqgastos.ui.components.construirResumenCategorias
import cl.duoc.teqgastos.ui.screens.jefatura.obtenerIconoYNombreCategoria
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
 * Pantalla Principal de Técnico en Terreno (Fase 6 - Indicador IE 2.4.1).
 *
 * Incluye:
 * - Lista reactiva de rendiciones con tarjetas animadas (`Modifier.animateContentSize`).
 * - Botón para crear nueva rendición seleccionando Centro de Costo y Medio de Pago.
 * - Botón "Importar Traslado TeqRuta" que inyecta automáticamente 1 gasto de Peaje ($4.800)
 *   y 1 gasto de Combustible ($22.500) pre-calculados.
 * - Navegación directa a `FormularioGastoScreen` para capturar boletas con Cámara y GPS.
 */
@Composable
fun TecnicoHomeScreen(
    viewModel: TecnicoViewModel,
    onNavigateToFormularioGasto: (rendicionId: Int?) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rendiciones by viewModel.misRendiciones.collectAsStateWithLifecycle()
    val gastosPorRendicion by viewModel.gastosPorRendicion.collectAsStateWithLifecycle()

    // Control local de qué tarjeta está expandida (por defecto la seleccionada o la más reciente)
    var rendicionExpandidaId by remember(uiState.rendicionSeleccionadaId) {
        mutableStateOf(uiState.rendicionSeleccionadaId ?: rendiciones.firstOrNull()?.id)
    }

    val rendicionesFiltradas = rendiciones.filter { rendicion ->
        uiState.searchQuery.isBlank() ||
            rendicion.centroCosto.contains(uiState.searchQuery, ignoreCase = true) ||
            rendicion.estado.contains(uiState.searchQuery, ignoreCase = true) ||
            rendicion.medioPago.contains(uiState.searchQuery, ignoreCase = true)
    }

    val todosLosGastosTecnico = remember(gastosPorRendicion) {
        gastosPorRendicion.values.flatten()
    }
    val resumenCategorias = remember(todosLosGastosTecnico) {
        construirResumenCategorias(todosLosGastosTecnico)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TeqBackground,
        bottomBar = {
            TeqBottomNav(
                activeTab = uiState.tabActiva,
                onTabSelected = { tab ->
                    if (tab == "Rendiciones") {
                        onNavigateToFormularioGasto(uiState.rendicionSeleccionadaId)
                    } else {
                        viewModel.onTabSelected(tab)
                    }
                }
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
                eyebrow = "TÉCNICO EN TERRENO",
                title = when (uiState.tabActiva) {
                    "Reportes" -> "Seguimiento y Topes"
                    "Ajustes" -> "Ajustes y Plataforma"
                    else -> "Mis Rendiciones"
                },
                subtitle = when (uiState.tabActiva) {
                    "Reportes" -> "Métricas de tus carpetas, trazabilidad y topes por categoría."
                    "Ajustes" -> "Adaptabilidad Android/iOS, permisos nativos y sesión."
                    else -> "Gestiona carpetas, traslados TeqRuta y boletas con GPS."
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
                when (uiState.tabActiva) {
                    "Reportes" -> {
                        TeqReportesSection(
                            rolActual = "TÉCNICO",
                            totalMontoClp = rendiciones.sumOf { it.montoTotalClp },
                            totalRendiciones = rendiciones.size,
                            conteoBorrador = rendiciones.count { it.estado == RendicionEntity.ESTADO_BORRADOR },
                            conteoPendientes = rendiciones.count { it.estado == RendicionEntity.ESTADO_PENDIENTE_JEFATURA },
                            conteoObservadas = rendiciones.count { it.estado == RendicionEntity.ESTADO_OBSERVADA },
                            conteoAprobadas = rendiciones.count { it.estado == RendicionEntity.ESTADO_APROBADA },
                            conteoPagadasErp = rendiciones.count { it.estado == RendicionEntity.ESTADO_PAGADA_ERP },
                            conteoRechazadas = rendiciones.count { it.estado == RendicionEntity.ESTADO_RECHAZADA },
                            resumenCategorias = resumenCategorias
                        )
                    }

                    "Ajustes" -> {
                        TeqAjustesSection(
                            nombreUsuario = "Carlos Muñoz",
                            rolUsuario = "TÉCNICO",
                            correoUsuario = "tecnico@teqmed.cl",
                            onLogout = onLogout
                        )
                    }

                    else -> {
                        // =============================================================
                        // 1. BANNER DE FEEDBACK REACTIVO (ÉXITO / IMPORTACIÓN TEQRUTA)
                        // =============================================================
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
                                    modifier = Modifier.padding(13.dp),
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
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // =============================================================
                        // 2. ACCIONES PRINCIPALES: NUEVA RENDICIÓN + IMPORTAR TEQRUTA
                        // =============================================================
                        TeqPrimaryButton(
                            text = "Crear Nueva Rendición",
                            onClick = viewModel::abrirDialogoNuevaRendicion,
                            trailingIcon = Icons.Default.Add
                        )

                        Spacer(modifier = Modifier.height(11.dp))

                        // Tarjeta/Botón "Importar Traslado TeqRuta"
                        Surface(
                            onClick = {
                                viewModel.importarTrasladoTeqRuta(rendicionExpandidaId)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = TeqNavy,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(TeqTeal.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Route,
                                            contentDescription = "TeqRuta",
                                            tint = TeqTeal,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Importar Traslado TeqRuta",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "Inyecta Peaje ($4.800) + Combustible ($22.500) pre-calculados",
                                            color = Color.White.copy(alpha = 0.65f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = TeqTeal
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = TeqNavy,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Importar",
                                            color = TeqNavy,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // =============================================================
                        // 3. ENCABEZADO Y LISTA DE TARJETAS CON animateContentSize
                        // =============================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "CARPETAS DE RENDICIÓN",
                                    color = TeqMutedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.4.sp
                                )
                                Text(
                                    text = "Mis rendiciones (${rendicionesFiltradas.size})",
                                    color = TeqNavy,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Surface(
                                onClick = { onNavigateToFormularioGasto(rendicionExpandidaId) },
                                shape = RoundedCornerShape(9.dp),
                                color = TeqTealSoft,
                                border = BorderStroke(1.dp, TeqTeal)
                            ) {
                                Text(
                                    text = "+ Registrar Boleta",
                                    color = TeqTealDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                            rendicionesFiltradas.forEach { rendicion ->
                                val gastosAsociados = gastosPorRendicion[rendicion.id].orEmpty()
                                val expandida = rendicionExpandidaId == rendicion.id

                                RendicionAnimatedCard(
                                    rendicion = rendicion,
                                    gastos = gastosAsociados,
                                    expandida = expandida,
                                    onToggleExpand = {
                                        rendicionExpandidaId = if (expandida) null else rendicion.id
                                    },
                                    onAgregarGasto = {
                                        onNavigateToFormularioGasto(rendicion.id)
                                    },
                                    onImportarTeqRutaAqui = {
                                        rendicionExpandidaId = rendicion.id
                                        viewModel.importarTrasladoTeqRuta(rendicion.id)
                                    },
                                    onEnviarAJefatura = {
                                        viewModel.enviarRendicionAJefatura(rendicion.id)
                                    },
                                    onEliminarGasto = { gastoId ->
                                        viewModel.eliminarGastoDeRendicion(gastoId, rendicion.id)
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
    // 4. MODAL PARA CREAR NUEVA RENDICIÓN (Centro de Costo + Medio de Pago)
    // =========================================================================
    if (uiState.mostrarDialogoNuevaRendicion) {
        Dialog(onDismissRequest = viewModel::cerrarDialogoNuevaRendicion) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 18.dp,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(TeqTealIconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = TeqTealDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NUEVA CARPETA",
                                    color = TeqMutedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.3.sp
                                )
                                Text(
                                    text = "Crear Rendición",
                                    color = TeqNavy,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Surface(
                            onClick = viewModel::cerrarDialogoNuevaRendicion,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector rápido de Centros de Costo corporativos
                    Text(
                        text = "Centros de Costo frecuentes",
                        color = TeqLabelText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        viewModel.centrosCostoDisponibles.forEach { cc ->
                            val activo = uiState.nuevoCentroCostoInput == cc
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (activo) TeqTealSoft else Color(0xFFFAFBFB))
                                    .border(
                                        width = 1.dp,
                                        color = if (activo) TeqTeal else TeqBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.onNuevoCentroCostoChange(cc) }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = cc,
                                    color = if (activo) TeqTealDark else TeqNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ValidatedTextField(
                        value = uiState.nuevoCentroCostoInput,
                        onValueChange = viewModel::onNuevoCentroCostoChange,
                        label = "Código de Centro de Costo",
                        placeholder = "CC-104 · Operaciones Concepción",
                        errorMessage = uiState.nuevoCentroCostoError
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selector de Medio de Pago
                    Text(
                        text = "Medio de Pago",
                        color = TeqLabelText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        viewModel.mediosPagoDisponibles.forEach { (codigo, etiqueta) ->
                            val seleccionado = uiState.nuevoMedioPagoInput == codigo
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (seleccionado) TeqTealSoft else Color.White)
                                    .border(
                                        width = if (seleccionado) 1.5.dp else 1.dp,
                                        color = if (seleccionado) TeqTeal else TeqBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.onNuevoMedioPagoChange(codigo) }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = etiqueta,
                                        color = if (seleccionado) TeqTealDark else TeqNavy,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = codigo,
                                        color = TeqMutedText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    TeqPrimaryButton(
                        text = "Crear Carpeta de Rendición",
                        onClick = {
                            viewModel.crearNuevaRendicion { nuevoId ->
                                rendicionExpandidaId = nuevoId
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de rendición con transición fluida `Modifier.animateContentSize()` exigida en Fase 6.
 */
@Composable
private fun RendicionAnimatedCard(
    rendicion: RendicionEntity,
    gastos: List<GastoEntity>,
    expandida: Boolean,
    onToggleExpand: () -> Unit,
    onAgregarGasto: () -> Unit,
    onImportarTeqRutaAqui: () -> Unit,
    onEnviarAJefatura: () -> Unit,
    onEliminarGasto: (Int) -> Unit
) {
    val (bgColor, textColor) = when (rendicion.estado) {
        RendicionEntity.ESTADO_APROBADA, RendicionEntity.ESTADO_PAGADA_ERP -> TeqApproveBg to TeqApproveText
        RendicionEntity.ESTADO_OBSERVADA -> TeqObserveBg to TeqObserveText
        RendicionEntity.ESTADO_RECHAZADA -> TeqRejectBg to TeqRejectText
        else -> TeqPendingPillBg to TeqPendingPillText
    }

    val permiteEdicion = rendicion.estado == RendicionEntity.ESTADO_BORRADOR ||
        rendicion.estado == RendicionEntity.ESTADO_OBSERVADA

    Surface(
        shape = RoundedCornerShape(19.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (expandida) 1.5.dp else 1.dp,
            color = if (expandida) TeqTealMid else TeqCardBorder
        ),
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
                .clickable(onClick = onToggleExpand)
                .padding(16.dp)
        ) {
            // Cabecera de la Rendición
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rendición #${rendicion.id}",
                            color = TeqTealDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "· ${rendicion.medioPago.replace("_", " ")}",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rendicion.centroCosto,
                        color = TeqNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = bgColor
                ) {
                    Text(
                        text = rendicion.estado,
                        color = textColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "TOTAL ACUMULADO (${gastos.size} boleta${if (gastos.size == 1) "" else "s"})",
                        color = TeqMutedText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${GastoValidator.formatearClp(rendicion.montoTotalClp)} CLP",
                        color = TeqNavy,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expandida) "Ocultar detalle" else "Ver boletas",
                        color = TeqTealDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TeqTealDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!rendicion.comentarioRevision.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Retroalimentación Jefatura: ${rendicion.comentarioRevision}",
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Sección expandible animada por animateContentSize
            if (expandida) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TeqDivider)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (gastos.isEmpty()) {
                    Text(
                        text = "Aún no hay boletas en esta carpeta. Agrega un gasto manual o importa desde TeqRuta.",
                        color = TeqMutedText,
                        fontSize = 11.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        gastos.forEach { gasto ->
                            val (iconoCat, nombreCat) = obtenerIconoYNombreCategoria(gasto.categoria)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFB))
                                    .border(1.dp, TeqBorder, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(TeqTealIconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconoCat,
                                        contentDescription = nombreCat,
                                        tint = TeqTealDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(9.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = nombreCat,
                                            color = TeqNavy,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (gasto.origenTeqRuta) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(5.dp),
                                                color = TeqNavy
                                            ) {
                                                Text(
                                                    text = "TeqRuta",
                                                    color = TeqTeal,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "RUT ${gasto.rutProveedor} · Folio ${gasto.folioBoleta}",
                                        color = TeqMutedText,
                                        fontSize = 9.sp
                                    )
                                    if (gasto.latitud != null && gasto.longitud != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFF299C69),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "GPS: ${gasto.latitud}, ${gasto.longitud}",
                                                color = Color(0xFF3F6855),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = GastoValidator.formatearClp(gasto.montoConvertidoClp),
                                    color = TeqNavy,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                if (permiteEdicion) {
                                    IconButton(
                                        onClick = { onEliminarGasto(gasto.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Eliminar gasto",
                                            tint = TeqRejectText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botones de acción dentro de la tarjeta expandida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAgregarGasto,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, TeqTealMid),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = TeqTealDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Agregar Boleta",
                            color = TeqTealDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (permiteEdicion && gastos.isNotEmpty()) {
                        Button(
                            onClick = onEnviarAJefatura,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TeqTeal,
                                contentColor = TeqNavy
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Enviar a Jefatura",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else if (permiteEdicion) {
                        OutlinedButton(
                            onClick = onImportarTeqRutaAqui,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, TeqNavy),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "+ TeqRuta aquí",
                                color = TeqNavy,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
