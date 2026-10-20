package cl.duoc.teqgastos.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import kotlinx.coroutines.tasks.await

/**
 * Helper de acceso seguro a los 2 recursos nativos exigidos en Fase 5 (Indicador IE 2.4.1):
 * 1. Cámara Nativa mediante `FileProvider` (`createImageUri`).
 * 2. Geolocalización GPS mediante `FusedLocationProviderClient` (`obtenerUbicacionActual`),
 *    con manejo de `SecurityException` y fallback a Concepción (`-36.8270, -73.0503`).
 */
object NativeResourcesHelper {

    // Coordenadas por defecto de Concepción (Plaza de la Independencia / Centro) para emuladores sin GPS
    const val DEFAULT_LAT_CONCEPCION = -36.8270
    const val DEFAULT_LON_CONCEPCION = -73.0503

    /**
     * Crea un archivo temporal `.jpg` dentro del directorio interno de caché (`cacheDir/boletas`)
     * y retorna su `Uri` seguro a través de `androidx.core.content.FileProvider`.
     *
     * @param context Contexto de la aplicación o actividad.
     * @return `Uri` con esquema `content://` listo para `ActivityResultContracts.TakePicture()`.
     */
    fun createImageUri(context: Context): Uri {
        val directorioBoletas = File(context.cacheDir, "boletas").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val archivoImagen = File.createTempFile(
            "boleta_teqgastos_${System.currentTimeMillis()}_",
            ".jpg",
            directorioBoletas
        )
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, archivoImagen)
    }

    /**
     * Verifica si el permiso de Cámara (`Manifest.permission.CAMERA`) fue concedido.
     */
    fun tienePermisoCamara(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica si al menos uno de los permisos de ubicación (`FINE` o `COARSE`) fue concedido.
     */
    fun tienePermisosUbicacion(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    /**
     * Obtiene de forma asíncrona la ubicación GPS actual utilizando `FusedLocationProviderClient`.
     * Maneja explícitamente `SecurityException` y retorna las coordenadas por defecto de
     * Concepción (`-36.8270, -73.0503`) cuando el emulador no tiene proveedor GPS activo.
     *
     * @param context Contexto de Android.
     * @return Par `(latitud, longitud)` en grados decimales.
     */
    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(context: Context): Pair<Double, Double> {
        if (!tienePermisosUbicacion(context)) {
            return Pair(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            // 1. Intenta obtener ubicación fresca de alta precisión
            val tokenSource = CancellationTokenSource()
            val ubicacionFresca = fusedClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .await()

            if (ubicacionFresca != null) {
                Pair(ubicacionFresca.latitude, ubicacionFresca.longitude)
            } else {
                // 2. Fallback a la última ubicación conocida en caché del dispositivo
                val ultimaUbicacion = fusedClient.lastLocation.await()
                if (ultimaUbicacion != null) {
                    Pair(ultimaUbicacion.latitude, ultimaUbicacion.longitude)
                } else {
                    // 3. Fallback seguro para emulador sin GPS activo (Concepción)
                    Pair(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
                }
            }
        } catch (secEx: SecurityException) {
            Pair(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
        } catch (ex: Exception) {
            Pair(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
        }
    }

    /**
     * Sobrecarga basada en listeners de `FusedLocationProviderClient` para invocación desde callbacks de UI.
     */
    @SuppressLint("MissingPermission")
    fun obtenerUbicacionActual(
        context: Context,
        onUbicacionResult: (latitud: Double, longitud: Double) -> Unit
    ) {
        if (!tienePermisosUbicacion(context)) {
            onUbicacionResult(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
            return
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            val tokenSource = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onUbicacionResult(location.latitude, location.longitude)
                    } else {
                        fusedClient.lastLocation
                            .addOnSuccessListener { lastLoc ->
                                if (lastLoc != null) {
                                    onUbicacionResult(lastLoc.latitude, lastLoc.longitude)
                                } else {
                                    onUbicacionResult(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
                                }
                            }
                            .addOnFailureListener {
                                onUbicacionResult(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
                            }
                    }
                }
                .addOnFailureListener {
                    onUbicacionResult(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
                }
        } catch (secEx: SecurityException) {
            onUbicacionResult(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
        } catch (ex: Exception) {
            onUbicacionResult(DEFAULT_LAT_CONCEPCION, DEFAULT_LON_CONCEPCION)
        }
    }

    /**
     * Decodifica una miniatura optimizada (`ImageBitmap`) a partir de un `fotoUri` capturado con `FileProvider`
     * para mostrar la vista previa real de la boleta en `FormularioGastoScreen`.
     */
    fun cargarMiniaturaDesdeUri(context: Context, uriString: String?): ImageBitmap? {
        if (uriString.isNullOrBlank() || !uriString.startsWith("content://")) return null
        return try {
            val uri = Uri.parse(uriString)
            val opciones = BitmapFactory.Options().apply {
                inSampleSize = 4 // Reduce consumo de memoria al renderizar la miniatura
            }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, opciones)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }
}
