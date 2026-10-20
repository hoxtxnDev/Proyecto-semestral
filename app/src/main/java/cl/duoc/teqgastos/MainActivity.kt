package cl.duoc.teqgastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cl.duoc.teqgastos.data.local.AppDatabase
import cl.duoc.teqgastos.data.repository.TeqGastosRepository
import cl.duoc.teqgastos.ui.navigation.AppNavGraph
import cl.duoc.teqgastos.ui.theme.TeqBackground
import cl.duoc.teqgastos.ui.theme.TeqGastosTheme

/**
 * Actividad principal (Single-Activity Architecture) de TeqGastos.
 * Inicializa la base de datos local Room (`AppDatabase`) y el repositorio (`TeqGastosRepository`)
 * e inyecta el grafo declarativo `AppNavGraph` con transiciones suaves y el tema visual (`TeqGastosTheme`).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TeqGastosRepository(database.teqGastosDao())

        setContent {
            TeqGastosTheme {
                val repoRemembered = remember { repository }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TeqBackground
                ) {
                    AppNavGraph(repository = repoRemembered)
                }
            }
        }
    }
}
