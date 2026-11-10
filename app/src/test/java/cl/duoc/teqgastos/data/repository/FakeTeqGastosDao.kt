package cl.duoc.teqgastos.data.repository

import cl.duoc.teqgastos.data.local.dao.TeqGastosDao
import cl.duoc.teqgastos.data.local.entities.GastoEntity
import cl.duoc.teqgastos.data.local.entities.RendicionEntity
import cl.duoc.teqgastos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.math.abs

/**
 * Implementación Fake de `TeqGastosDao` en memoria para pruebas unitarias rápidas y deterministas.
 */
class FakeTeqGastosDao : TeqGastosDao {

    val usuarios = mutableListOf<UsuarioEntity>()
    val rendicionesFlow = MutableStateFlow<List<RendicionEntity>>(emptyList())
    val gastosFlow = MutableStateFlow<List<GastoEntity>>(emptyList())

    private var autoIncrementRendicionId = 1
    private var autoIncrementGastoId = 1

    override suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>) {
        this.usuarios.clear()
        this.usuarios.addAll(usuarios)
    }

    override suspend fun login(email: String, password: String): UsuarioEntity? {
        val emailLimpio = email.trim().lowercase()
        return usuarios.find {
            it.email.trim().lowercase() == emailLimpio && it.password == password
        }
    }

    override suspend fun obtenerUsuarioPorId(usuarioId: Int): UsuarioEntity? {
        return usuarios.find { it.id == usuarioId }
    }

    override fun obtenerTodasLasRendiciones(): Flow<List<RendicionEntity>> {
        return rendicionesFlow.map { list -> list.sortedByDescending { it.fechaCreacion } }
    }

    override fun obtenerRendicionesPorTecnico(tecnicoId: Int): Flow<List<RendicionEntity>> {
        return rendicionesFlow.map { list ->
            list.filter { it.tecnicoId == tecnicoId }.sortedByDescending { it.fechaCreacion }
        }
    }

    override fun obtenerRendicionesPorEstado(estado: String): Flow<List<RendicionEntity>> {
        return rendicionesFlow.map { list ->
            list.filter { it.estado == estado }.sortedByDescending { it.fechaCreacion }
        }
    }

    override fun obtenerRendicionesPorEstados(estados: List<String>): Flow<List<RendicionEntity>> {
        return rendicionesFlow.map { list ->
            list.filter { it.estado in estados }.sortedByDescending { it.fechaCreacion }
        }
    }

    override fun obtenerRendicionPorId(rendicionId: Int): Flow<RendicionEntity?> {
        return rendicionesFlow.map { list -> list.find { it.id == rendicionId } }
    }

    override suspend fun insertarRendicion(rendicion: RendicionEntity): Long {
        val currentList = rendicionesFlow.value.toMutableList()
        val idAsignado = if (rendicion.id == 0) autoIncrementRendicionId++ else rendicion.id
        val nueva = rendicion.copy(id = idAsignado)

        val existingIdx = currentList.indexOfFirst { it.id == idAsignado }
        if (existingIdx >= 0) {
            currentList[existingIdx] = nueva
        } else {
            currentList.add(nueva)
        }
        rendicionesFlow.value = currentList
        return idAsignado.toLong()
    }

    override suspend fun actualizarRendicion(rendicion: RendicionEntity) {
        val currentList = rendicionesFlow.value.toMutableList()
        val existingIdx = currentList.indexOfFirst { it.id == rendicion.id }
        if (existingIdx >= 0) {
            currentList[existingIdx] = rendicion
            rendicionesFlow.value = currentList
        }
    }

    override suspend fun eliminarRendicion(rendicionId: Int) {
        val currentList = rendicionesFlow.value.toMutableList()
        currentList.removeAll { it.id == rendicionId }
        rendicionesFlow.value = currentList
    }

    override suspend fun actualizarEstadoRendicion(
        rendicionId: Int,
        nuevoEstado: String,
        comentarioRevision: String?,
        codigoPagoFinanzas: String?
    ) {
        val currentList = rendicionesFlow.value.toMutableList()
        val idx = currentList.indexOfFirst { it.id == rendicionId }
        if (idx >= 0) {
            val actual = currentList[idx]
            currentList[idx] = actual.copy(
                estado = nuevoEstado,
                comentarioRevision = comentarioRevision ?: actual.comentarioRevision,
                codigoPagoFinanzas = codigoPagoFinanzas ?: actual.codigoPagoFinanzas
            )
            rendicionesFlow.value = currentList
        }
    }

    override suspend fun recalcularMontoTotalRendicion(rendicionId: Int) {
        val total = gastosFlow.value
            .filter { it.rendicionId == rendicionId }
            .sumOf { it.montoConvertidoClp }

        val currentList = rendicionesFlow.value.toMutableList()
        val idx = currentList.indexOfFirst { it.id == rendicionId }
        if (idx >= 0) {
            currentList[idx] = currentList[idx].copy(montoTotalClp = total)
            rendicionesFlow.value = currentList
        }
    }

    override fun obtenerGastosPorRendicion(rendicionId: Int): Flow<List<GastoEntity>> {
        return gastosFlow.map { list ->
            list.filter { it.rendicionId == rendicionId }.sortedByDescending { it.fechaGastoMillis }
        }
    }

    override fun obtenerTodosLosGastos(): Flow<List<GastoEntity>> {
        return gastosFlow.map { list -> list.sortedByDescending { it.fechaGastoMillis } }
    }

    override suspend fun insertarGasto(gasto: GastoEntity): Long {
        val currentList = gastosFlow.value.toMutableList()
        val idAsignado = if (gasto.id == 0) autoIncrementGastoId++ else gasto.id
        val nuevo = gasto.copy(id = idAsignado)

        val existingIdx = currentList.indexOfFirst { it.id == idAsignado }
        if (existingIdx >= 0) {
            currentList[existingIdx] = nuevo
        } else {
            currentList.add(nuevo)
        }
        gastosFlow.value = currentList
        return idAsignado.toLong()
    }

    override suspend fun eliminarGastoPorId(gastoId: Int) {
        val currentList = gastosFlow.value.toMutableList()
        currentList.removeAll { it.id == gastoId }
        gastosFlow.value = currentList
    }

    override suspend fun existeBoletaDuplicada(
        rutProveedor: String,
        folioBoleta: String,
        montoOriginal: Double
    ): Boolean {
        val rutLimpio = rutProveedor.trim().lowercase()
        val folioLimpio = folioBoleta.trim().lowercase()

        return gastosFlow.value.any { g ->
            g.rutProveedor.trim().lowercase() == rutLimpio &&
                g.folioBoleta.trim().lowercase() == folioLimpio &&
                abs(g.montoOriginal - montoOriginal) < 0.01
        }
    }
}
