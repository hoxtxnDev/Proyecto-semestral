package cl.duoc.teqgastos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) principal de TeqGastos.
 * Expone flujos reactivos (Flow) para la UI y funciones suspend para transacciones asíncronas.
 */
@Dao
interface TeqGastosDao {

    // =========================================================================
    // 1. AUTENTICACIÓN Y USUARIOS
    // =========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Query("SELECT * FROM usuarios WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :usuarioId LIMIT 1")
    suspend fun obtenerUsuarioPorId(usuarioId: Int): UsuarioEntity?

    // =========================================================================
    // 2. CONSULTAS REACTIVAS (FLOW) DE RENDICIONES POR ROL Y ESTADO
    // =========================================================================

    /** Obtiene todas las rendiciones ordenadas desde la más reciente (útil para vista global/auditoría). */
    @Query("SELECT * FROM rendiciones ORDER BY fechaCreacion DESC")
    fun obtenerTodasLasRendiciones(): Flow<List<RendicionEntity>>

    /** Vista TÉCNICO: Lista únicamente las rendiciones pertenecientes al técnico autenticado. */
    @Query("SELECT * FROM rendiciones WHERE tecnicoId = :tecnicoId ORDER BY fechaCreacion DESC")
    fun obtenerRendicionesPorTecnico(tecnicoId: Int): Flow<List<RendicionEntity>>

    /** Filtra rendiciones por un estado específico (ej. "PENDIENTE_JEFATURA" o "APROBADA"). */
    @Query("SELECT * FROM rendiciones WHERE estado = :estado ORDER BY fechaCreacion DESC")
    fun obtenerRendicionesPorEstado(estado: String): Flow<List<RendicionEntity>>

    /**
     * Vista JEFATURA / FINANZAS: Permite filtrar rendiciones según una lista de estados relevantes al rol.
     * - Jefatura: ["PENDIENTE_JEFATURA", "APROBADA", "OBSERVADA", "RECHAZADA"]
     * - Finanzas: ["APROBADA", "PAGADA_ERP"]
     */
    @Query("SELECT * FROM rendiciones WHERE estado IN (:estados) ORDER BY fechaCreacion DESC")
    fun obtenerRendicionesPorEstados(estados: List<String>): Flow<List<RendicionEntity>>

    /** Observa en tiempo real la cabecera de una rendición específica por su ID. */
    @Query("SELECT * FROM rendiciones WHERE id = :rendicionId LIMIT 1")
    fun obtenerRendicionPorId(rendicionId: Int): Flow<RendicionEntity?>

    // =========================================================================
    // 3. INSERCIÓN Y ACTUALIZACIÓN DE RENDICIONES (FLUJO DE APROBACIÓN)
    // =========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRendicion(rendicion: RendicionEntity): Long

    @Update
    suspend fun actualizarRendicion(rendicion: RendicionEntity)

    @Query("DELETE FROM rendiciones WHERE id = :rendicionId")
    suspend fun eliminarRendicion(rendicionId: Int)

    /**
     * Actualiza el estado de la rendición dentro del flujo de negocio:
     * - Técnico envía a revisión -> estado = "PENDIENTE_JEFATURA"
     * - Jefatura revisa -> estado = "APROBADA" | "OBSERVADA" | "RECHAZADA" + comentarioRevision
     * - Finanzas paga en ERP -> estado = "PAGADA_ERP" + codigoPagoFinanzas
     */
    @Query(
        """
        UPDATE rendiciones 
        SET estado = :nuevoEstado,
            comentarioRevision = COALESCE(:comentarioRevision, comentarioRevision),
            codigoPagoFinanzas = COALESCE(:codigoPagoFinanzas, codigoPagoFinanzas)
        WHERE id = :rendicionId
        """
    )
    suspend fun actualizarEstadoRendicion(
        rendicionId: Int,
        nuevoEstado: String,
        comentarioRevision: String? = null,
        codigoPagoFinanzas: String? = null
    )

    /**
     * Recalcula de forma atómica el monto total en CLP de una rendición sumando todos sus gastos asociados.
     */
    @Query(
        """
        UPDATE rendiciones 
        SET montoTotalClp = COALESCE(
            (SELECT SUM(montoConvertidoClp) FROM gastos WHERE rendicionId = :rendicionId), 
            0.0
        )
        WHERE id = :rendicionId
        """
    )
    suspend fun recalcularMontoTotalRendicion(rendicionId: Int)

    // =========================================================================
    // 4. GESTIÓN DE GASTOS Y CONTROL ANTI-FRAUDE (BOLETA DUPLICADA)
    // =========================================================================

    /** Lista todos los ítems de gasto asociados a una rendición en tiempo real. */
    @Query("SELECT * FROM gastos WHERE rendicionId = :rendicionId ORDER BY fechaGastoMillis DESC")
    fun obtenerGastosPorRendicion(rendicionId: Int): Flow<List<GastoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGasto(gasto: GastoEntity): Long

    @Query("DELETE FROM gastos WHERE id = :gastoId")
    suspend fun eliminarGastoPorId(gastoId: Int)

    /**
     * QUERY CRÍTICA DE NEGOCIO (Control Anti-Duplicidad TEQMED SPA):
     * Verifica si ya existe en la base de datos una boleta con la misma combinación de:
     * RUT del Proveedor + Folio de Boleta + Monto Original.
     * Impide que un mismo documento tributario sea rendido dos veces en la misma u otra rendición.
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM gastos 
            WHERE LOWER(TRIM(rutProveedor)) = LOWER(TRIM(:rutProveedor)) 
              AND LOWER(TRIM(folioBoleta)) = LOWER(TRIM(:folioBoleta)) 
              AND ABS(montoOriginal - :montoOriginal) < 0.01
        )
        """
    )
    suspend fun existeBoletaDuplicada(
        rutProveedor: String,
        folioBoleta: String,
        montoOriginal: Double
    ): Boolean
}
