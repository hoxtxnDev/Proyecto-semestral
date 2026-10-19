package cl.duoc.teqgastos.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un usuario corporativo de TEQMED SPA.
 * Soporta control de acceso basado en roles (RBAC): TECNICO, JEFATURA y FINANZAS.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String, // Valores: "TECNICO", "JEFATURA", "FINANZAS"
    val centroCostoAutorizado: String
) {
    companion object {
        const val ROL_TECNICO = "TECNICO"
        const val ROL_JEFATURA = "JEFATURA"
        const val ROL_FINANZAS = "FINANZAS"
    }
}
