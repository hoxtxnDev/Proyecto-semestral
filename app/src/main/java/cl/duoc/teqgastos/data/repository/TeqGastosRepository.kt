package cl.duoc.teqgastos.data.repository

import cl.duoc.teqgastos.data.local.dao.TeqGastosDao
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio único de verdad (Single Source of Truth) para TeqGastos.
 * Desacopla a los ViewModels de la implementación concreta de Room (TeqGastosDao).
 */
class TeqGastosRepository(private val dao: TeqGastosDao) {

    // =========================================================================
    // 1. AUTENTICACIÓN
    // =========================================================================

    suspend fun login(email: String, password: String): UsuarioEntity? {
        return dao.login(email, password)
    }

    suspend fun obtenerUsuarioPorId(usuarioId: Int): UsuarioEntity? {
        return dao.obtenerUsuarioPorId(usuarioId)
    }

    // =========================================================================
    // 2. CONSULTAS REACTIVAS DE RENDICIONES (FLOW)
    // =========================================================================

    /**
     * Retorna el flujo reactivo de rendiciones filtrado automáticamente según el rol del usuario:
     * - TECNICO: Sus propias rendiciones (en cualquier estado).
     * - JEFATURA: Rendiciones enviadas a revisión y su historial de aprobaciones/rechazos.
     * - FINANZAS: Rendiciones aprobadas por jefatura listas para pago ERP y las ya pagadas.
     */
    fun obtenerRendicionesPorRol(usuario: UsuarioEntity): Flow<List<RendicionEntity>> {
        return when (usuario.rol) {
            UsuarioEntity.ROL_TECNICO -> dao.obtenerRendicionesPorTecnico(usuario.id)
            UsuarioEntity.ROL_JEFATURA -> dao.obtenerRendicionesPorEstados(
                listOf(
                    RendicionEntity.ESTADO_PENDIENTE_JEFATURA,
                    RendicionEntity.ESTADO_APROBADA,
                    RendicionEntity.ESTADO_OBSERVADA,
                    RendicionEntity.ESTADO_RECHAZADA,
                    RendicionEntity.ESTADO_PAGADA_ERP
                )
            )
            UsuarioEntity.ROL_FINANZAS -> dao.obtenerRendicionesPorEstados(
                listOf(
                    RendicionEntity.ESTADO_APROBADA,
                    RendicionEntity.ESTADO_PAGADA_ERP
                )
            )
            else -> dao.obtenerTodasLasRendiciones()
        }
    }

    fun obtenerTodasLasRendiciones(): Flow<List<RendicionEntity>> =
        dao.obtenerTodasLasRendiciones()

    fun obtenerRendicionesPorTecnico(tecnicoId: Int): Flow<List<RendicionEntity>> =
        dao.obtenerRendicionesPorTecnico(tecnicoId)

    fun obtenerRendicionesPorEstado(estado: String): Flow<List<RendicionEntity>> =
        dao.obtenerRendicionesPorEstado(estado)

    fun obtenerRendicionesPorEstados(estados: List<String>): Flow<List<RendicionEntity>> =
        dao.obtenerRendicionesPorEstados(estados)

    fun obtenerRendicionPorId(rendicionId: Int): Flow<RendicionEntity?> =
        dao.obtenerRendicionPorId(rendicionId)

    // =========================================================================
    // 3. GESTIÓN DE RENDICIONES Y GASTOS
    // =========================================================================

    fun obtenerGastosPorRendicion(rendicionId: Int): Flow<List<GastoEntity>> =
        dao.obtenerGastosPorRendicion(rendicionId)

    suspend fun crearRendicion(rendicion: RendicionEntity): Long {
        return dao.insertarRendicion(rendicion)
    }

    /**
     * Inserta un nuevo gasto y actualiza automáticamente el montoTotalClp de la rendición padre.
     */
    suspend fun insertarGasto(gasto: GastoEntity): Long {
        val idGenerado = dao.insertarGasto(gasto)
        dao.recalcularMontoTotalRendicion(gasto.rendicionId)
        return idGenerado
    }

    /**
     * Elimina un gasto de una rendición en borrador/observada y recalcula el total en CLP.
     */
    suspend fun eliminarGasto(gastoId: Int, rendicionId: Int) {
        dao.eliminarGastoPorId(gastoId)
        dao.recalcularMontoTotalRendicion(rendicionId)
    }

    /**
     * Verifica en Room si la boleta (RUT + Folio + Monto Original) ya fue registrada previamente.
     */
    suspend fun existeBoletaDuplicada(
        rutProveedor: String,
        folioBoleta: String,
        montoOriginal: Double
    ): Boolean {
        return dao.existeBoletaDuplicada(
            rutProveedor = rutProveedor,
            folioBoleta = folioBoleta,
            montoOriginal = montoOriginal
        )
    }

    // =========================================================================
    // 4. TRANSICIONES DE ESTADO (FLUJO DE APROBACIÓN Y PAGO ERP)
    // =========================================================================

    suspend fun actualizarEstadoRendicion(
        rendicionId: Int,
        nuevoEstado: String,
        comentarioRevision: String? = null,
        codigoPagoFinanzas: String? = null
    ) {
        dao.actualizarEstadoRendicion(
            rendicionId = rendicionId,
            nuevoEstado = nuevoEstado,
            comentarioRevision = comentarioRevision,
            codigoPagoFinanzas = codigoPagoFinanzas
        )
    }

    /** Acción de TÉCNICO: Envía una rendición (BORRADOR u OBSERVADA) a revisión de Jefatura. */
    suspend fun enviarRendicionAJefatura(rendicionId: Int) {
        dao.actualizarEstadoRendicion(
            rendicionId = rendicionId,
            nuevoEstado = RendicionEntity.ESTADO_PENDIENTE_JEFATURA
        )
    }

    /** Acción de JEFATURA: Aprueba, Observa o Rechaza una rendición dejando comentario de auditoría. */
    suspend fun resolverRevisionJefatura(
        rendicionId: Int,
        nuevoEstado: String,
        comentarioRevision: String?
    ) {
        dao.actualizarEstadoRendicion(
            rendicionId = rendicionId,
            nuevoEstado = nuevoEstado,
            comentarioRevision = comentarioRevision
        )
    }

    /** Acción de FINANZAS: Registra el código de transferencia/comprobante ERP y cierra la rendición. */
    suspend fun registrarPagoFinanzas(
        rendicionId: Int,
        codigoPagoFinanzas: String
    ) {
        dao.actualizarEstadoRendicion(
            rendicionId = rendicionId,
            nuevoEstado = RendicionEntity.ESTADO_PAGADA_ERP,
            codigoPagoFinanzas = codigoPagoFinanzas
        )
    }
}
