package cl.duoc.teqgastos.ui.screens.tecnico

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Contenedor coordinador del flujo de Técnico que alterna entre `TecnicoHomeScreen`
 * y `FormularioGastoScreen`.
 */
@Composable
fun TecnicoScreen(
    viewModel: TecnicoViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarFormulario by remember { mutableStateOf(false) }

    if (mostrarFormulario) {
        FormularioGastoScreen(
            viewModel = viewModel,
            onGastoGuardado = { mostrarFormulario = false },
            onNavigateBack = { mostrarFormulario = false },
            modifier = modifier
        )
    } else {
        TecnicoHomeScreen(
            viewModel = viewModel,
            onNavigateToFormularioGasto = { rendicionId ->
                viewModel.prepararFormularioParaRendicion(rendicionId)
                mostrarFormulario = true
            },
            onLogout = onLogout,
            modifier = modifier
        )
    }
}
