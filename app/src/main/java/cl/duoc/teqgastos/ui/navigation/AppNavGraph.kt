package cl.duoc.teqgastos.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
object AppRoutes {
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
 * Grafo principal de navegación Compose (`AppNavGraph`) con transiciones suaves de entrada/salida
 * y soporte de cierre de sesión desde la barra superior (`TeqAppHeader`) en todas las vistas por rol
 * para facilitar el cambio rápido de perfil durante la presentación.
 */
@Composable
fun AppNavGraph(
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
        startDestination = AppRoutes.LOGIN,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(280)) +
                slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth / 5 }, animationSpec = tween(280))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(220)) +
                slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth / 5 }, animationSpec = tween(220))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(260)) +
                slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth / 5 }, animationSpec = tween(260))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(220)) +
                slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth / 5 }, animationSpec = tween(220))
        }
    ) {
        composable(route = AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { usuario ->
                    val destino = when (usuario.rol) {
                        UsuarioEntity.ROL_TECNICO -> AppRoutes.rutaTecnicoHome(usuario.id)
                        UsuarioEntity.ROL_JEFATURA -> AppRoutes.JEFATURA
                        UsuarioEntity.ROL_FINANZAS -> AppRoutes.FINANZAS
                        else -> AppRoutes.rutaTecnicoHome(usuario.id)
                    }
                    navController.navigate(destino) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppRoutes.TECNICO_HOME,
            arguments = listOf(navArgument("usuarioId") { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt("usuarioId") ?: 1
            TecnicoHomeScreen(
                viewModel = tecnicoViewModel,
                onNavigateToFormularioGasto = { rendicionId ->
                    tecnicoViewModel.prepararFormularioParaRendicion(rendicionId)
                    navController.navigate(
                        AppRoutes.rutaFormularioGasto(
                            usuarioId = usuarioId,
                            rendicionId = rendicionId
                        )
                    )
                },
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppRoutes.FORMULARIO_GASTO,
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

        composable(route = AppRoutes.JEFATURA) {
            val jefaturaViewModel: JefaturaViewModel = viewModel(
                factory = JefaturaViewModelFactory(repository)
            )
            JefaturaScreen(
                viewModel = jefaturaViewModel,
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = AppRoutes.FINANZAS) {
            val finanzasViewModel: FinanzasViewModel = viewModel(
                factory = FinanzasViewModelFactory(repository)
            )
            FinanzasScreen(
                viewModel = finanzasViewModel,
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
