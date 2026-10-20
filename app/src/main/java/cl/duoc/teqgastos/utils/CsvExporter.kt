package cl.duoc.teqgastos.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import cl.duoc.teqgastos.ui.screens.finanzas.ItemReembolsoFinanzas
import java.io.File

/**
 * Resultado de la generación física del archivo CSV para el ERP de TEQMED SPA.
 */
data class ResultadoExportacionCsv(
    val archivo: File,
    val uri: Uri,
    val nombreArchivo: String,
    val rutaAbsoluta: String,
    val contenidoCsv: String,
    val cantidadRegistros: Int,
    val montoTotalExportadoClp: Double
)

/**
 * Generador de archivos CSV para exportación contable al ERP.
 */
object CsvExporter {

    /**
     * Genera físicamente el archivo CSV de reembolsos aprobados en el directorio interno de la app
     * y retorna su `Uri` seguro de `FileProvider` junto con su contenido.
     */
    fun generarArchivoCsvErp(
        context: Context,
        items: List<ItemReembolsoFinanzas>,
        codigoPagoErp: String
    ): ResultadoExportacionCsv {
        val codigoNormalizado = codigoPagoErp
            .trim()
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
            .ifEmpty { "ERP_PAGO" }

        val nombreArchivo = "teqgastos_erp_${codigoNormalizado}.csv"

        val directorio = File(context.cacheDir, "boletas").apply {
            if (!exists()) {
                mkdirs()
            }
        }

        val archivoCsv = File(directorio, nombreArchivo)

        val filas = buildList {
            add("RendicionID,Tecnico,RUTProveedor,Folio,Categoria,MontoCLP,Fecha,CodigoPagoERP")
            items.forEach { item ->
                val nombreLimpio = item.nombreTecnico.replace(",", " ")
                add(
                    "${item.rendicionId}," +
                        "\"$nombreLimpio\"," +
                        "${item.rutProveedor}," +
                        "${item.folioBoleta}," +
                        "${item.categoria}," +
                        "${item.montoTotalClp.toLong()}," +
                        "\"${item.fechaFormateada}\"," +
                        codigoPagoErp.trim()
                )
            }
        }

        val contenidoCsv = filas.joinToString("\n")
        archivoCsv.writeText(contenidoCsv, Charsets.UTF_8)

        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, archivoCsv)

        return ResultadoExportacionCsv(
            archivo = archivoCsv,
            uri = uri,
            nombreArchivo = nombreArchivo,
            rutaAbsoluta = archivoCsv.absolutePath,
            contenidoCsv = contenidoCsv,
            cantidadRegistros = items.size,
            montoTotalExportadoClp = items.sumOf { it.montoTotalClp }
        )
    }

    /**
     * Lanza el selector nativo de Android (`Intent.ACTION_SEND`) para compartir o abrir
     * el archivo `.CSV` generado con correo, Drive, WhatsApp o visores de hojas de cálculo.
     */
    fun compartirArchivoCsv(
        context: Context,
        resultado: ResultadoExportacionCsv
    ) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, resultado.uri)
            putExtra(Intent.EXTRA_SUBJECT, "Nómina ERP TeqGastos - ${resultado.nombreArchivo}")
            putExtra(Intent.EXTRA_TEXT, resultado.contenidoCsv)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Exportar o compartir nómina ERP (.CSV)").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
