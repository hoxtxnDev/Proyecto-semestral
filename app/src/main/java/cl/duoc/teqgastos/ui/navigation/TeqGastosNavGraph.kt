package cl.duoc.teqgastos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.ui.screens.auth.AuthViewModel
import cl.duoc.teqgastos.ui.screens.auth.AuthViewModelFactory
import cl.duoc.teqgastos.ui.screens.auth.LoginScreen
import cl.duoc.teqgastos.ui.screens.finanzas.FinanzasScreen
import cl.duoc.teqgastos.ui.screens.finanzas.FinanzasViewModel
import cl.duoc.teqgastos.ui.screens.finanzas.FinanzasViewModelFactory
import cl.duoc.teqgastos.ui.screens.jefatura.JefaturaScreen
import cl.duoc.teqgastos.ui.screens.jefatura.JefaturaViewModel
import cl.duoc.teqgastos.ui.screens.jefatura.JefaturaViewModelFactory
import cl.duoc.teqgastos.ui.screens.tecnico.FormularioGastoScreen
import cl.duoc.teqgastos.ui.screens.tecnico.TecnicoHomeScreen
import cl.duoc.teqgastos.ui.screens.tecnico.TecnicoViewModel
import cl.duoc.teqgastos.ui.screens.tecnico.TecnicoViewModelFactory

/**
 * Rutas declarativas del grafo de navegación de TeqGastos.
 */
object TeqGastosRoutes {
    const val LOGIN = "login"
    const val TECNICO_HOME = "tecnico/{usuarioId}"
    const val FORMULARIO_GASTO = "tecnico/{usuarioId}/formulario?rendicionId={rendicionId}"
    const val JEFATURA = "jefatura"
    const val FINANZAS = "finanzas"

    fun rutaTecnicoHome(usuarioId: Int) = "tecnico/$usuarioId"

    fun rutaFormularioGasto(usuarioId: Int, rendicionId: Int? = null): String {
        val idParam = rendicionId ?: -1
        return "tecnico/$usuarioId/formulario?rendicionId=$idParam"
    }
}

/**
 * Grafo principal de navegación Compose que conecta:
 * - `LoginScreen`
 * - `TecnicoHomeScreen` (Carpetas de rendición animadas + Importación TeqRuta)
 * - `FormularioGastoScreen` (Registro de boleta con Cámara FileProvider + GPS FusedLocation)
 * - `JefaturaScreen` (Evaluación y aprobación/observación/rechazo)
 * - `FinanzasScreen` (Desembolso y exportación CSV ERP)
 */
@Composable
fun TeqGastosNavGraph(
    repository: TeqGastosRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(repository)
    )

    // ViewModel compartido para el flujo del Técnico en terreno (Home + FormularioGasto)
    val tecnicoViewModel: TecnicoViewModel = viewModel(
        factory = TecnicoViewModelFactory(
            repository = repository,
            tecnicoId = 1,
            nombreTecnico = "Carlos Muñoz"
        )
    )

    NavHost(
        navController = navController,
        startDestination = TeqGastosRoutes.LOGIN,
        modifier = modifier
    ) {
        composable(route = TeqGastosRoutes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { usuario ->
                    val destino = when (usuario.rol) {
                        UsuarioEntity.ROL_TECNICO -> TeqGastosRoutes.rutaTecnicoHome(usuario.id)
                        UsuarioEntity.ROL_JEFATURA -> TeqGastosRoutes.JEFATURA
                        UsuarioEntity.ROL_FINANZAS -> TeqGastosRoutes.FINANZAS
                        else -> TeqGastosRoutes.rutaTecnicoHome(usuario.id)
                    }
                    navController.navigate(destino) {
                        popUpTo(TeqGastosRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = TeqGastosRoutes.TECNICO_HOME,
            arguments = listOf(navArgument("usuarioId") { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt("usuarioId") ?: 1
            TecnicoHomeScreen(
                viewModel = tecnicoViewModel,
                onNavigateToFormularioGasto = { rendicionId ->
                    tecnicoViewModel.prepararFormularioParaRendicion(rendicionId)
                    navController.navigate(
                        TeqGastosRoutes.rutaFormularioGasto(
                            usuarioId = usuarioId,
                            rendicionId = rendicionId
                        )
                    )
                },
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(TeqGastosRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = TeqGastosRoutes.FORMULARIO_GASTO,
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.IntType },
                navArgument("rendicionId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            FormularioGastoScreen(
                viewModel = tecnicoViewModel,
                onGastoGuardado = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = TeqGastosRoutes.JEFATURA) {
            val jefaturaViewModel: JefaturaViewModel = viewModel(
                factory = JefaturaViewModelFactory(repository)
            )
            JefaturaScreen(
                viewModel = jefaturaViewModel,
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(TeqGastosRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = TeqGastosRoutes.FINANZAS) {
            val finanzasViewModel: FinanzasViewModel = viewModel(
                factory = FinanzasViewModelFactory(repository)
            )
            FinanzasScreen(
                viewModel = finanzasViewModel,
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(TeqGastosRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
