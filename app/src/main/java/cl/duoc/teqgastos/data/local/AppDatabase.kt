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

            // 2. Carga inicial de rendiciones de prueba
            val ahora = System.currentTimeMillis()

            // Rendición 1 (Pendiente Jefatura - Camila Torres / Carlos Muñoz)
            val r1 = dao.insertarRendicion(
                RendicionEntity(
                    id = 1,
                    tecnicoId = 1,
                    nombreTecnico = "Camila Torres",
                    centroCosto = "CC-104 · Operaciones Norte",
                    medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
                    fechaCreacion = ahora - 36_000_000L,
                    montoTotalClp = 14990.0,
                    estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA
                )
            ).toInt()
            dao.insertarGasto(
                GastoEntity(
                    rendicionId = r1,
                    rutProveedor = "17.982.341-2",
                    folioBoleta = "008321",
                    fechaGastoMillis = ahora - 36_000_000L,
                    categoria = GastoEntity.CAT_ALIMENTACION,
                    moneda = GastoEntity.MONEDA_CLP,
                    montoOriginal = 14990.0,
                    montoConvertidoClp = 14990.0,
                    fotoUri = "content://teqgastos/boletas/IMG_3841.jpg",
                    latitud = -33.4489,
                    longitud = -70.6693,
                    origenTeqRuta = false
                )
            )

            // Rendición 2 (Pendiente Jefatura - Diego Soto)
            val r2 = dao.insertarRendicion(
                RendicionEntity(
                    id = 2,
                    tecnicoId = 1,
                    nombreTecnico = "Diego Soto",
                    centroCosto = "CC-104 · Operaciones Norte",
                    medioPago = RendicionEntity.MEDIO_TARJETA_CORPORATIVA,
                    fechaCreacion = ahora - 72_000_000L,
                    montoTotalClp = 58700.0,
                    estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA
                )
            ).toInt()
            dao.insertarGasto(
                GastoEntity(
                    rendicionId = r2,
                    rutProveedor = "16.440.828-6",
                    folioBoleta = "114209",
                    fechaGastoMillis = ahora - 72_000_000L,
                    categoria = GastoEntity.CAT_COMBUSTIBLE,
                    moneda = GastoEntity.MONEDA_CLP,
                    montoOriginal = 58700.0,
                    montoConvertidoClp = 58700.0,
                    fotoUri = "content://teqgastos/boletas/IMG_3842.jpg",
                    latitud = -33.4372,
                    longitud = -70.6506,
                    origenTeqRuta = false
                )
            )

            // Rendición 3 (Pendiente Jefatura - Martín Rojas)
            val r3 = dao.insertarRendicion(
                RendicionEntity(
                    id = 3,
                    tecnicoId = 1,
                    nombreTecnico = "Martín Rojas",
                    centroCosto = "CC-MANT-CLINICO-01",
                    medioPago = RendicionEntity.MEDIO_FONDO_POR_RENDIR,
                    fechaCreacion = ahora - 86_400_000L,
                    montoTotalClp = 65000.0,
                    estado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA
                )
            ).toInt()
            dao.insertarGasto(
                GastoEntity(
                    rendicionId = r3,
                    rutProveedor = "18.223.901-4",
                    folioBoleta = "003118",
                    fechaGastoMillis = ahora - 86_400_000L,
                    categoria = GastoEntity.CAT_ALOJAMIENTO,
                    moneda = GastoEntity.MONEDA_CLP,
                    montoOriginal = 65000.0,
                    montoConvertidoClp = 65000.0,
                    fotoUri = "content://teqgastos/boletas/IMG_3843.jpg",
                    latitud = -33.4489,
                    longitud = -70.6693,
                    origenTeqRuta = false
                )
            )

            // Rendición 4 (Aprobada lista para Finanzas - Paula Díaz)
            val r4 = dao.insertarRendicion(
                RendicionEntity(
                    id = 4,
                    tecnicoId = 1,
                    nombreTecnico = "Paula Díaz",
                    centroCosto = "CC-104 · Operaciones Norte",
                    medioPago = RendicionEntity.MEDIO_DINERO_PROPIO,
                    fechaCreacion = ahora - 120_000_000L,
                    montoTotalClp = 18500.0,
                    estado = RendicionEntity.ESTADO_APROBADA,
                    comentarioRevision = "Aprobado conforme a política de peajes TeqRuta"
                )
            ).toInt()
            dao.insertarGasto(
                GastoEntity(
                    rendicionId = r4,
                    rutProveedor = "19.118.220-1",
                    folioBoleta = "006742",
                    fechaGastoMillis = ahora - 120_000_000L,
                    categoria = GastoEntity.CAT_PEAJE_ESTACIONAMIENTO,
                    moneda = GastoEntity.MONEDA_CLP,
                    montoOriginal = 18500.0,
                    montoConvertidoClp = 18500.0,
                    fotoUri = "content://teqgastos/boletas/IMG_3844.jpg",
                    latitud = -33.4489,
                    longitud = -70.6693,
                    origenTeqRuta = true
                )
            )
        }
    }
}
