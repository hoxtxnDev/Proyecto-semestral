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
import cl.duoc.teqgastos.ui.screens.tecnico.TecnicoScreen
import cl.duoc.teqgastos.ui.screens.tecnico.TecnicoViewModel
import cl.duoc.teqgastos.ui.screens.tecnico.TecnicoViewModelFactory

/**
 * Rutas declarativas del grafo de navegación de TeqGastos.
 */
object TeqGastosRoutes {
    const val LOGIN = "login"
    const val TECNICO = "tecnico/{usuarioId}"
    const val JEFATURA = "jefatura"
    const val FINANZAS = "finanzas"

    fun rutaTecnico(usuarioId: Int) = "tecnico/$usuarioId"
}

/**
 * Grafo principal de navegación Compose que conecta el Login con las 3 bandejas RBAC
 * implementadas desde Figma (`TechnicianScreen`, `ManagerScreen`, `FinanceScreen`).
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
                        UsuarioEntity.ROL_TECNICO -> TeqGastosRoutes.rutaTecnico(usuario.id)
                        UsuarioEntity.ROL_JEFATURA -> TeqGastosRoutes.JEFATURA
                        UsuarioEntity.ROL_FINANZAS -> TeqGastosRoutes.FINANZAS
                        else -> TeqGastosRoutes.rutaTecnico(usuario.id)
                    }
                    navController.navigate(destino) {
                        popUpTo(TeqGastosRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = TeqGastosRoutes.TECNICO,
            arguments = listOf(navArgument("usuarioId") { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt("usuarioId") ?: 1
            val tecnicoViewModel: TecnicoViewModel = viewModel(
                factory = TecnicoViewModelFactory(
                    repository = repository,
                    tecnicoId = usuarioId,
                    nombreTecnico = "Carlos Muñoz"
                )
            )
            TecnicoScreen(
                viewModel = tecnicoViewModel,
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(TeqGastosRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
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
