package cl.duoc.teqgastos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un ítem de gasto individual (boleta/factura) asociado a una Rendición.
 * Incluye evidencia multimedia (fotoUri), georreferenciación GPS (latitud/longitud)
 * e integración simulada con la app hermana TeqRuta (origenTeqRuta).
 */
@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = RendicionEntity::class,
            parentColumns = ["id"],
            childColumns = ["rendicionId"],
            onDelete = ForeignKey.CASCADE // Si se elimina una rendición borrador, se eliminan sus gastos en cascada
        )
    ],
    indices = [
        Index(value = ["rendicionId"]),
        Index(value = ["rutProveedor", "folioBoleta", "montoOriginal"]) // Índice compuesto para optimizar detección de boletas duplicadas
    ]
)
data class GastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rendicionId: Int,
    val rutProveedor: String,
    val folioBoleta: String,
    val fechaGastoMillis: Long,
    val categoria: String, // "ALIMENTACION", "ALOJAMIENTO", "PEAJE_ESTACIONAMIENTO", "COMBUSTIBLE", "INSUMOS"
    val moneda: String, // "CLP", "UF", "USD"
    val montoOriginal: Double,
    val montoConvertidoClp: Double,
    val fotoUri: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val origenTeqRuta: Boolean = false
) {
    companion object {
        // Categorías de Gasto
        const val CAT_ALIMENTACION = "ALIMENTACION"
        const val CAT_ALOJAMIENTO = "ALOJAMIENTO"
        const val CAT_PEAJE_ESTACIONAMIENTO = "PEAJE_ESTACIONAMIENTO"
        const val CAT_COMBUSTIBLE = "COMBUSTIBLE"
        const val CAT_INSUMOS = "INSUMOS"

        // Monedas soportadas
        const val MONEDA_CLP = "CLP"
        const val MONEDA_UF = "UF"
        const val MONEDA_USD = "USD"
    }
}
