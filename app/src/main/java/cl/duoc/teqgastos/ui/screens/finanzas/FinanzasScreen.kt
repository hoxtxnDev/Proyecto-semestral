package cl.duoc.teqgastos.ui.screens.finanzas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.domain.validators.GastoValidator
import cl.duoc.teqgastos.ui.components.ResumenCategoriaReporte
import cl.duoc.teqgastos.ui.components.TeqAjustesSection
import cl.duoc.teqgastos.ui.components.TeqAppHeader
import cl.duoc.teqgastos.ui.components.TeqBottomNav
import cl.duoc.teqgastos.ui.components.TeqPrimaryButton
import cl.duoc.teqgastos.ui.components.TeqReportesSection
import cl.duoc.teqgastos.ui.components.ValidatedTextField
import cl.duoc.teqgastos.ui.screens.jefatura.obtenerIconoYNombreCategoria
import cl.duoc.teqgastos.ui.theme.TeqApproveBg
import cl.duoc.teqgastos.ui.theme.TeqApproveText
import cl.duoc.teqgastos.ui.theme.TeqBackground
import cl.duoc.teqgastos.ui.theme.TeqBorder
import cl.duoc.teqgastos.ui.theme.TeqCardBorder
import cl.duoc.teqgastos.ui.theme.TeqDivider
import cl.duoc.teqgastos.ui.theme.TeqMutedText
import cl.duoc.teqgastos.ui.theme.TeqNavy
import cl.duoc.teqgastos.ui.theme.TeqTeal
import cl.duoc.teqgastos.ui.theme.TeqTealDark
import cl.duoc.teqgastos.ui.theme.TeqTealIconBg
import cl.duoc.teqgastos.utils.CsvExporter

/**
 * Pantalla de Finanzas (`FinanceScreen` de Figma `src-interface/App.tsx`).
 * Filtra las rendiciones en estado `APROBADA`, permite registrar el código de transferencia/reembolso
 * y genera el archivo `.CSV` real en el dispositivo mediante `CsvExporter` con diálogo e Intent para compartirlo.
 */
@Composable
fun FinanzasScreen(
    viewModel: FinanzasViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todosLosItems by viewModel.itemsFinanzas.collectAsStateWithLifecycle()

    val aprobadas = todosLosItems.filter { it.estado == RendicionEntity.ESTADO_APROBADA }
    val listaBase = if (uiState.mostrarPagadas || aprobadas.isEmpty()) todosLosItems else aprobadas
    val listaFiltrada = listaBase.filter { item ->
        uiState.searchQuery.isBlank() ||
            item.nombreTecnico.contains(uiState.searchQuery, ignoreCase = true) ||
            item.folioBoleta.contains(uiState.searchQuery, ignoreCase = true) ||
            item.categoria.contains(uiState.searchQuery, ignoreCase = true)
    }

    val totalDesembolso = listaFiltrada.sumOf { it.montoTotalClp }

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
                eyebrow = "FINANZAS",
                title = when (uiState.tabActiva) {
                    "Reportes" -> "Consolidado Contable"
                    "Ajustes" -> "Ajustes y Plataforma"
                    else -> "Reembolsos"
                },
                subtitle = when (uiState.tabActiva) {
                    "Reportes" -> "Resumen financiero de desembolsos, topes y estado ERP."
                    "Ajustes" -> "Adaptabilidad Android/iOS, parámetros de cambio y sesión."
                    else -> "Prepara los gastos aprobados para pago."
                },
                userInitials = "RG",
                searchQuery = uiState.searchQuery,
                onSearchChange = viewModel::onSearchQueryChange,
                onFilterClick = viewModel::alternarFiltroPagadas,
                onBack = onLogout
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 17.dp, vertical = 17.dp)
            ) {
                if (uiState.tabActiva == "Reportes") {
                    val categoriasOficiales = listOf(
                        GastoEntity.CAT_ALIMENTACION,
                        GastoEntity.CAT_ALOJAMIENTO,
                        GastoEntity.CAT_PEAJE_ESTACIONAMIENTO,
                        GastoEntity.CAT_COMBUSTIBLE,
                        GastoEntity.CAT_INSUMOS
                    )
                    val resumenFinanzas = categoriasOficiales.map { cat ->
                        val itemsCat = todosLosItems.filter { it.categoria.equals(cat, ignoreCase = true) }
                        ResumenCategoriaReporte(
                            categoria = cat,
                            montoAcumuladoClp = itemsCat.sumOf { it.montoTotalClp },
                            cantidadBoletas = itemsCat.size,
                            topeUnitarioClp = GastoValidator.obtenerTopeCategoriaClp(cat) ?: 0.0
                        )
                    }
                    TeqReportesSection(
                        rolActual = "FINANZAS",
                        totalMontoClp = todosLosItems.sumOf { it.montoTotalClp },
                        totalRendiciones = todosLosItems.size,
                        conteoBorrador = 0,
                        conteoPendientes = 0,
                        conteoObservadas = 0,
                        conteoAprobadas = todosLosItems.count { it.estado == RendicionEntity.ESTADO_APROBADA },
                        conteoPagadasErp = todosLosItems.count { it.estado == RendicionEntity.ESTADO_PAGADA_ERP },
                        conteoRechazadas = 0,
                        resumenCategorias = resumenFinanzas
                    )
                } else if (uiState.tabActiva == "Ajustes") {
                    TeqAjustesSection(
                        nombreUsuario = "Roberto Gómez",
                        rolUsuario = "FINANZAS",
                        correoUsuario = "finanzas@teqmed.cl",
                        onLogout = onLogout
                    )
                } else {
                // =============================================================
                // 1. TARJETA CONSOLIDADO DESEMBOLSO (.finance-total)
                // =============================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(TeqNavy)
                        .padding(horizontal = 19.dp, vertical = 18.dp)
                ) {
                    // Anillo decorativo superior derecho (.finance-total::after)
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 60.dp, y = (-75).dp)
                            .border(26.dp, TeqTeal.copy(alpha = 0.09f), CircleShape)
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Fila superior (.finance-total-top)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(TeqTeal.copy(alpha = 0.13f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = TeqTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Text(
                                text = "LISTO PARA DESEMBOLSO",
                                color = Color.White.copy(alpha = 0.60f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "CLP",
                                color = TeqTeal,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Monto Total (.finance-total > strong)
                        Text(
                            text = GastoValidator.formatearClp(totalDesembolso),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1.0).sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.10f))
                        )

                        Spacer(modifier = Modifier.height(11.dp))

                        // Pie de tarjeta (.finance-meta)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${listaFiltrada.size} rendiciones aprobadas",
                                color = Color.White.copy(alpha = 0.70f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF69D8A2),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Validado",
                                    color = Color(0xFF69D8A2),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // =============================================================
                // 2. ENCABEZADO DE DETALLE (.finance-list-header)
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
                            text = "APROBADOS",
                            color = TeqMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.4.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Detalle de reembolsos",
                            color = TeqNavy,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        onClick = viewModel::alternarFiltroPagadas,
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
                                text = if (uiState.mostrarPagadas) "Solo APROBADAS" else "Incluir pagadas",
                                color = Color(0xFF72818D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // =============================================================
                // 3. LISTA DE APROBADOS (.approved-list / .approved-item)
                // =============================================================
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, TeqCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (listaFiltrada.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No hay rendiciones aprobadas pendientes de pago.",
                                color = TeqMutedText,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            listaFiltrada.forEachIndexed { index, item ->
                                val (iconoCat, nombreCat) = obtenerIconoYNombreCategoria(item.categoria)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(TeqTealIconBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconoCat,
                                            contentDescription = nombreCat,
                                            tint = TeqTealDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.nombreTecnico,
                                            color = TeqNavy,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.folioBoleta} · ${item.fechaFormateada} · CC ${item.centroCosto}",
                                            color = TeqMutedText,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = if (item.codigoPagoFinanzas != null) {
                                                "$nombreCat · PAGADA (${item.codigoPagoFinanzas})"
                                            } else {
                                                "$nombreCat · ${item.estado}"
                                            },
                                            color = TeqTealDark,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = GastoValidator.formatearClp(item.montoTotalClp),
                                            color = TeqNavy,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "CLP",
                                            color = TeqMutedText,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (index < listaFiltrada.lastIndex) {
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

                Spacer(modifier = Modifier.height(13.dp))

                // =============================================================
                // 4. TARJETA DE INTEGRACIÓN ERP (.export-card)
                // =============================================================
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
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(39.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TeqTealIconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = TeqTealDark,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "INTEGRACIÓN ERP",
                                    color = TeqMutedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.4.sp
                                )
                                Text(
                                    text = "Generar archivo de pago",
                                    color = TeqNavy,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        ValidatedTextField(
                            value = uiState.codigoPago,
                            onValueChange = viewModel::onCodigoPagoChange,
                            label = "Código de Pago Final",
                            placeholder = "Ej. PAG-2025-0041",
                            errorMessage = uiState.codigoPagoError
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        TeqPrimaryButton(
                            text = "Exportar a ERP (.CSV)",
                            onClick = { viewModel.exportarErpCsv(context) },
                            enabled = uiState.codigoPago.trim().length >= 4,
                            isLoading = uiState.isExporting,
                            trailingIcon = Icons.Default.Download
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = TeqMutedText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "El archivo incluirá solo rendiciones aprobadas.",
                                color = TeqMutedText,
                                fontSize = 10.sp
                            )
                        }

                        // Vista previa del CSV exportado y confirmación de cierre ERP
                        AnimatedVisibility(
                            visible = uiState.mensajeExito != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = TeqApproveBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = TeqApproveText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = uiState.mensajeExito.orEmpty(),
                                            color = TeqApproveText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (!uiState.csvGenerado.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF4F6F8),
                                        border = BorderStroke(1.dp, TeqBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Vista previa: archivo .CSV generado en el dispositivo",
                                                color = TeqNavy,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = uiState.csvGenerado.orEmpty(),
                                                color = TeqNavy,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }

    // =========================================================================
    // DIÁLOGO DE ARCHIVO CSV GENERADO EN DISPOSITIVO + INTENT PARA COMPARTIR
    // =========================================================================
    val resultadoCsv = uiState.resultadoCsvDialog
    if (resultadoCsv != null) {
        Dialog(onDismissRequest = viewModel::cerrarDialogoCsv) {
            Surface(
                shape = RoundedCornerShape(22.dp),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ARCHIVO ERP GENERADO",
                                color = TeqTealDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = resultadoCsv.archivo.name,
                                color = TeqNavy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        IconButton(onClick = viewModel::cerrarDialogoCsv) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TeqMutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ruta física en dispositivo: ${resultadoCsv.archivo.absolutePath}",
                        color = TeqMutedText,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF4F6F8),
                        border = BorderStroke(1.dp, TeqBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = resultadoCsv.contenidoCsv,
                                color = TeqNavy,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TeqPrimaryButton(
                        text = "Compartir / Enviar a ERP (.CSV)",
                        onClick = {
                            CsvExporter.compartirArchivoCsv(context, resultadoCsv)
                        },
                        trailingIcon = Icons.Default.Share
                    )
                }
            }
        }
    }
}
