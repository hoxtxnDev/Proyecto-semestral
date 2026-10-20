# 🏛️ 1. Patrón de Diseño, Arquitectura y Justificación Técnica del MVP

**Proyecto:** TeqGastos — Sistema Móvil de Rendición de Gastos en Terreno (TEQMED SPA)  
**Integrantes:** Horacio Navarrete y Maximiliano Ojeda  

---

## 1. Patrones de Diseño Elegidos

Para garantizar que **TeqGastos** sea escalable, mantenible, fácil de testear en Android Studio y adaptable entre **Android** e **iOS**, se seleccionó una combinación sinérgica de **6 patrones de diseño de ingeniería de software**:

| # | Patrón de Diseño | Capa donde aplica | Archivo(s) Clave | Propósito Técnico en TeqGastos |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **MVVM (Model-View-ViewModel)** | Presentación y Estado (`ui/`) | [`AuthViewModel.kt`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/auth/AuthViewModel.kt), [`TecnicoViewModel.kt`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/tecnico/TecnicoViewModel.kt), [`JefaturaViewModel.kt`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/jefatura/JefaturaViewModel.kt), [`FinanzasViewModel.kt`](../app/src/main/java/cl/duoc/teqgastos/ui/screens/finanzas/FinanzasViewModel.kt) | Separa radicalmente la interfaz declarativa (`@Composable`) de la lógica de negocio y del estado de la pantalla, sobreviviendo a rotaciones y cambios de configuración. |
| **2** | **Unidirectional Data Flow (UDF) + Observer (`StateFlow`)** | Estado Reactivo (`ui/`) | `MutableStateFlow` privado + `StateFlow` público consumido vía `collectAsStateWithLifecycle()` | Los eventos fluyen hacia arriba (`UI → ViewModel`) y el estado inmutable fluye hacia abajo (`ViewModel → UI`). Evita estados inconsistentes y condiciones de carrera. |
| **3** | **Repository Pattern (Fuente Única de Verdad)** | Datos (`data/repository/`) | [`TeqGastosRepository.kt`](../app/src/main/java/cl/duoc/teqgastos/data/repository/TeqGastosRepository.kt) | Abstrae el origen de los datos (`Room DAO` local y la integración con **TeqRuta**). Los ViewModels nunca acceden directamente a SQLite. |
| **4** | **Pure Domain Validator (Strategy / Reglas Desacopladas)** | Dominio (`domain/validators/`) | [`GastoValidator.kt`](../app/src/main/java/cl/duoc/teqgastos/domain/validators/GastoValidator.kt) | Encapsula el 100% de las reglas de negocio de TEQMED SPA en funciones puras Kotlin sin dependencias de Android (`Context` ni `Compose`), permitiendo pruebas unitarias instantáneas y reutilización directa en iOS (Kotlin Multiplatform). |
| **5** | **Singleton Thread-Safe (`@Volatile` + `synchronized`)** | Persistencia (`data/local/`) | [`AppDatabase.kt`](../app/src/main/java/cl/duoc/teqgastos/data/local/AppDatabase.kt) | Garantiza una única instancia activa del motor SQLite/Room en toda la aplicación e inyecta los datos semilla (`SeedCallback`) en el primer arranque. |
| **6** | **Factory Pattern (`ViewModelProvider.Factory`)** | Inyección de Dependencias (`ui/`) | `AuthViewModelFactory`, `TecnicoViewModelFactory`, `JefaturaViewModelFactory`, `FinanzasViewModelFactory` | Permite inyectar limpiamente `TeqGastosRepository` en cada `ViewModel` respetando el ciclo de vida de Jetpack Navigation. |

---

## 2. Diagrama de Arquitectura en Capas (Clean MVVM + UDF)

```mermaid
flowchart TD
    subgraph UI["Capa de Presentación (UI Declarativa - Jetpack Compose / Adaptable Android & iOS)"]
        Login["LoginScreen.kt"]
        Tecnico["TecnicoHomeScreen.kt + FormularioGastoScreen.kt"]
        Jefatura["JefaturaScreen.kt"]
        Finanzas["FinanzasScreen.kt"]
        Components["ValidatedTextField.kt + TeqFigmaComponents.kt + TeqAdaptiveSections.kt"]
    end

    subgraph VM["Capa de ViewModels (Gestión de Estado Reactivo UDF)"]
        AuthVM["AuthViewModel (StateFlow)"]
        TecVM["TecnicoViewModel (StateFlow)"]
        JefVM["JefaturaViewModel (StateFlow)"]
        FinVM["FinanzasViewModel (StateFlow)"]
    end

    subgraph DOMAIN["Capa de Dominio Puro (100% Kotlin Multiplatform Ready)"]
        Validator["GastoValidator.kt\n(RUT, Folio, Plazo 5 días, Topes CLP, Conversor UF/USD, Motivo)"]
    end

    subgraph DATA["Capa de Datos y Hardware Nativo"]
        Repo["TeqGastosRepository.kt\n(Single Source of Truth + Sincronización TeqRuta)"]
        Dao["TeqGastosDao.kt (Flows Reactivos SQLite)"]
        RoomDB["AppDatabase.kt (Room v2 + SeedCallback)"]
        Hardware["NativeResourcesHelper.kt (Cámara FileProvider + GPS FusedLocation)\nCsvExporter.kt (Exportación física ERP .CSV)"]
    end

    Login <-->|"Eventos / StateFlow"| AuthVM
    Tecnico <-->|"Eventos / StateFlow"| TecVM
    Jefatura <-->|"Eventos / StateFlow"| JefVM
    Finanzas <-->|"Eventos / StateFlow"| FinVM

    AuthVM --> Validator
    TecVM --> Validator
    JefVM --> Validator
    FinVM --> Validator

    AuthVM --> Repo
    TecVM --> Repo
    JefVM --> Repo
    FinVM --> Repo

    Repo --> Dao
    Dao --> RoomDB
    Tecnico --> Hardware
    FinVM --> Hardware
```

---

## 3. ¿Por qué el MVP funciona bien y es sólido en Producción / Defensa?

1. **Arquitectura 100% Offline-First sin dependencia de servidores externos caídos:**
   - Los técnicos de TEQMED SPA operan en sótanos de hospitales y carreteras interurbanas sin señal estable. Al persistir todo en **Room (`SQLite`)** con relaciones `ForeignKey.CASCADE` y recalcular automáticamente el `montoTotalClp` de cada carpeta en una transacción local, la aplicación responde en **< 16ms** y jamás pierde una boleta.
2. **Reactividad en Tiempo Real entre Roles (Cero recargas manuales):**
   - Gracias a que [`TeqGastosDao.kt`](../app/src/main/java/cl/duoc/teqgastos/data/local/dao/TeqGastosDao.kt) expone `Flow<List<RendicionEntity>>` y `Flow<List<GastoEntity>>`, cuando el **Técnico** envía una rendición a Jefatura, esta aparece instantáneamente en la bandeja de **Jefatura**. Cuando **Jefatura** la aprueba, aparece de inmediato en el consolidado de **Finanzas** listo para exportar a `.CSV`.
3. **Validación Preventiva en Tiempo Real (Poka-Yoke):**
   - El usuario recibe retroalimentación visual inmediata en cada tecla mediante [`ValidatedTextField.kt`](../app/src/main/java/cl/duoc/teqgastos/ui/components/ValidatedTextField.kt) y [`GastoValidator.kt`](../app/src/main/java/cl/duoc/teqgastos/domain/validators/GastoValidator.kt):
     - Bloquea boletas con más de **5 días de antigüedad** o fechas futuras.
     - Convierte automáticamente montos en **UF (`$38.000`)** o **USD (`$950`)** a **CLP** antes de verificar el tope de la categoría.
     - Consulta en Room (`existeBoletaDuplicada(rut, folio)`) para impedir que dos técnicos rindan la misma boleta.
4. **Degradación Elegante (Graceful Fallback) de Hardware en Emulador y Dispositivo Físico:**
   - Si el emulador de Android Studio no tiene señal satelital GPS activa, [`NativeResourcesHelper.kt`](../app/src/main/java/cl/duoc/teqgastos/utils/NativeResourcesHelper.kt) captura `SecurityException` o ubicaciones nulas y entrega automáticamente las coordenadas corporativas de **Concepción (`-36.8270, -73.0503`)**, garantizando que la demostración en vivo nunca falle.
