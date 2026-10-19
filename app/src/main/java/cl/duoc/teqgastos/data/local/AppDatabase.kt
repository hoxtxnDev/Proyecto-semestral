package cl.duoc.teqgastos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.teqgastos.data.local.dao.TeqGastosDao
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Base de datos local SQLite gestionada por Room para TeqGastos.
 * Implementa el patrón Singleton thread-safe y un RoomDatabase.Callback para poblar
 * datos semilla (3 usuarios por rol y 1 rendición de ejemplo) en la primera ejecución.
 */
@Database(
    entities = [
        UsuarioEntity::class,
        RendicionEntity::class,
        GastoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun teqGastosDao(): TeqGastosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teqgastos_database"
                )
                    .addCallback(SeedDatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback que se ejecuta al crear la base de datos por primera vez.
     * Pre-carga los 3 perfiles exigidos (TECNICO, JEFATURA, FINANZAS) y 1 rendición de prueba.
     */
    private class SeedDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    preCargarDatosIniciales(database.teqGastosDao())
                }
            }
        }

        private suspend fun preCargarDatosIniciales(dao: TeqGastosDao) {
            // 1. Pre-carga de los 3 usuarios de prueba según rol corporativo TEQMED SPA
            val usuariosSemilla = listOf(
                UsuarioEntity(
                    id = 1,
                    nombre = "Carlos Muñoz (Técnico Terreno)",
                    email = "tecnico@teqmed.cl",
                    password = "123456",
                    rol = UsuarioEntity.ROL_TECNICO,
                    centroCostoAutorizado = "CC-MANT-CLINICO-01"
                ),
                UsuarioEntity(
                    id = 2,
                    nombre = "Andrea Soto (Jefatura Técnica)",
                    email = "jefatura@teqmed.cl",
                    password = "123456",
                    rol = UsuarioEntity.ROL_JEFATURA,
                    centroCostoAutorizado = "CC-MANT-CLINICO-01"
                ),
                UsuarioEntity(
                    id = 3,
                    nombre = "Roberto Gómez (Analista Finanzas)",
                    email = "finanzas@teqmed.cl",
                    password = "123456",
                    rol = UsuarioEntity.ROL_FINANZAS,
                    centroCostoAutorizado = "CC-CORPORATIVO-ALL"
                )
            )
            dao.insertarUsuarios(usuariosSemilla)

            // 2. Pre-carga de 1 Rendición de ejemplo para demostración inmediata
            val ahora = System.currentTimeMillis()
            val rendicionId = dao.insertarRendicion(
                RendicionEntity(
                    id = 1,
                    tecnicoId = 1,
                    nombreTecnico = "Carlos Muñoz (Técnico Terreno)",
                    centroCosto = "CC-MANT-CLINICO-01",
                    medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
                    fechaCreacion = ahora - 86_400_000L, // Creada hace 24 horas
                    montoTotalClp = 28500.0,
                    estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA,
                    comentarioRevision = null,
                    codigoPagoFinanzas = null
                )
            ).toInt()

            // Ítems de gasto asociados a la rendición de ejemplo
            dao.insertarGasto(
                GastoEntity(
                    rendicionId = rendicionId,
                    rutProveedor = "76.123.456-7",
                    folioBoleta = "10452",
                    fechaGastoMillis = ahora - 90_000_000L,
                    categoria = GastoEntity.CAT_PEAJE_ESTACIONAMIENTO,
                    moneda = GastoEntity.MONEDA_CLP,
                    montoOriginal = 3500.0,
                    montoConvertidoClp = 3500.0,
                    fotoUri = null,
                    latitud = -33.4489,
                    longitud = -70.6693,
                    origenTeqRuta = true // Gasto importado desde TeqRuta
                )
            )

            dao.insertarGasto(
                GastoEntity(
                    rendicionId = rendicionId,
                    rutProveedor = "99.520.000-7",
                    folioBoleta = "88410",
                    fechaGastoMillis = ahora - 86_400_000L,
                    categoria = GastoEntity.CAT_COMBUSTIBLE,
                    moneda = GastoEntity.MONEDA_CLP,
                    montoOriginal = 25000.0,
                    montoConvertidoClp = 25000.0,
                    fotoUri = null,
                    latitud = -33.4372,
                    longitud = -70.6506,
                    origenTeqRuta = false
                )
            )

            dao.recalcularMontoTotalRendicion(rendicionId)
        }
    }
}
