package cl.duoc.teqgastos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una carpeta/cabecera de Rendición de Gastos.
 * Modela el flujo completo del ciclo de vida en TEQMED SPA:
 * BORRADOR -> PENDIENTE_JEFATURA -> (APROBADA | OBSERVADA | RECHAZADA) -> PAGADA_ERP.
 */
@Entity(tableName = "rendiciones")
data class RendicionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tecnicoId: Int,
    val nombreTecnico: String,
    val centroCosto: String,
    val medioPago: String, // "FONDO_POR_RENDIR", "TARJETA_CORPORATIVA", "DINERO_PROPIO"
    val fechaCreacion: Long = System.currentTimeMillis(),
    val montoTotalClp: Double = 0.0,
    val estado: String = ESTADO_BORRADOR,
    val comentarioRevision: String? = null,
    val codigoPagoFinanzas: String? = null
) {
    companion object {
        // Medios de Pago permitidos
        const val MEDIO_FONDO_POR_RENDIR = "FONDO_POR_RENDIR"
        const val MEDIO_TARJETA_CORPORATIVA = "TARJETA_CORPORATIVA"
        const val MEDIO_DINERO_PROPIO = "DINERO_PROPIO"

        // Estados del flujo de aprobación y pago
        const val ESTADO_BORRADOR = "BORRADOR"
        const val ESTADO_PENDIENTE_JEFATURA = "PENDIENTE_JEFATURA"
        const val ESTADO_APROBADA = "APROBADA"
        const val ESTADO_OBSERVADA = "OBSERVADA"
        const val ESTADO_RECHAZADA = "RECHAZADA"
        const val ESTADO_PAGADA_ERP = "PAGADA_ERP"
    }
}
