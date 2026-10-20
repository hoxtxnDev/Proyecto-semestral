package cl.duoc.teqgastos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cl.duoc.teqgastos.data.repository.TeqGastosRepository

/**
 * Alias de compatibilidad hacia `AppNavGraph`.
 */
@Composable
fun TeqGastosNavGraph(
    repository: TeqGastosRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    AppNavGraph(
        repository = repository,
        modifier = modifier,
        navController = navController
    )
}
