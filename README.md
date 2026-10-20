# 📱 TeqGastos — Sistema Móvil de Rendición de Gastos en Terreno (TEQMED SPA)

Aplicación móvil nativa para **Android (100% Kotlin + Jetpack Compose + Material Design 3)** desarrollada para digitalizar, auditar y reembolsar el flujo completo de **Rendición de Gastos en Terreno** de los técnicos de servicio de **TEQMED SPA** (Región del Biobío / Concepción).

---

## 👥 Integrantes del Equipo

| Integrante | Rol en el Proyecto | Institución / Asignatura |
| :--- | :--- | :--- |
| **Horacio Navarrete** | Arquitectura MVVM, Persistencia Room, Recursos Nativos (Cámara/GPS) y Flujo Técnico / TeqRuta | Duoc UC — Desarrollo de Aplicaciones Móviles |
| **Maximiliano Ojeda** | Diseño UI Jetpack Compose (Figma), Validaciones Desacopladas, Flujo Jefatura y Cierre Finanzas ERP | Duoc UC — Desarrollo de Aplicaciones Móviles |

---

## 📚 Documentación de Ingeniería en Carpeta [`docs/`](docs/)

Toda la fundamentación arquitectónica, patrones de diseño, requerimientos y guía de pruebas se encuentra documentada en la carpeta [`docs/`](docs/):

1. 🏛️ [**`docs/01_ARQUITECTURA_Y_PATRONES.md`**](docs/01_ARQUITECTURA_Y_PATRONES.md) — Patrones de diseño elegidos (**MVVM, UDF + StateFlow, Repository, Pure Domain Validator, Singleton Room, Factory**), diagrama de capas y por qué el MVP funciona de forma robusta Offline-First.
2. 📋 [**`docs/02_REQUERIMIENTOS_RF_RNF.md`**](docs/02_REQUERIMIENTOS_RF_RNF.md) — Especificación completa de **Requerimientos Funcionales (`RF-01` a `RF-12`)** y **Requerimientos No Funcionales (`RNF-01` a `RNF-08`)**.
3. 🔄 [**`docs/03_FLUJO_MVP_Y_TRAZABILIDAD_IEE.md`**](docs/03_FLUJO_MVP_Y_TRAZABILIDAD_IEE.md) — Máquina de estados de la rendición, secuencia End-to-End entre los 3 roles y matriz de cumplimiento de **Indicadores de Evaluación (`IE 2.1.1` a `IE 2.4.1`)**.
4. 📱🍏 [**`docs/04_ADAPTABILIDAD_ANDROID_IOS_Y_GUIA_TESTING.md`**](docs/04_ADAPTABILIDAD_ANDROID_IOS_Y_GUIA_TESTING.md) — Adaptabilidad en vivo **Android (Material Design 3) / iOS (Cupertino HIG)**, paleta de colores corporativa y **Guía Paso a Paso de Testing en Android Studio**.

---

## 🏢 Descripción del Caso de Negocio: "TeqGastos - TEQMED SPA"

**TEQMED SPA** es una empresa de ingeniería biomédica y soporte técnico en terreno cuyos especialistas realizan mantenciones preventivas y reparaciones de equipamiento clínico en hospitales y centros de salud del sur de Chile.

Históricamente, las rendiciones de viáticos (alimentación, alojamiento, peajes, estacionamientos, combustible e insumos de emergencia) se entregaban en planillas físicas al regresar a oficina, provocando:
- Pérdida o deterioro de boletas térmicas en terreno.
- Rendiciones fuera de plazo (> 5 días de antigüedad) o con boletas duplicadas.
- Exceso sobre los topes corporativos autorizados por categoría.
- Retrasos en la revisión de **Jefatura** y en el desembolso contable de **Finanzas (ERP)**.

**TeqGastos** resuelve esta problemática mediante una solución móvil **Offline-First** con trazabilidad geográfica (GPS), respaldo fotográfico directo desde cámara y sincronización con la app hermana **TeqRuta**.

---

## ✅ Indicadores de Evaluación e Implementación Técnica (Rúbrica)

| Indicador | Descripción en Pauta | Implementación en TeqGastos |
| :--- | :--- | :--- |
| **IE 2.1.1 / 2.1.2** | **Arquitectura MVVM y Estado Reactivo (`StateFlow`)** | Separación estricta en capas `data` (`Room` + `Repository`), `domain` (`GastoValidator`) y `ui` (`ViewModels` con `StateFlow` inmutable consumido vía `collectAsStateWithLifecycle()`). Ninguna validación ni consulta de datos reside en los `@Composable`. |
| **IE 2.2.1** | **Validaciones Desacopladas de Reglas de Negocio** | Objeto puro [`GastoValidator.kt`](app/src/main/java/cl/duoc/teqgastos/domain/validators/GastoValidator.kt) que valida: RUT de proveedor chileno, Folio numérico obligatorio, Antigüedad máxima de **5 días** (bloqueo de fechas futuras o fuera de plazo), Conversor offline multimoneda (`CLP`, `USD = $950`, `UF = $38.000`), Topes máximos por categoría en CLP, Unicidad de boleta (`RUT + Folio`) en Room y Comentario obligatorio de auditoría en Jefatura. |
| **IE 2.2.2** | **Componentes Visuales Validados y Animaciones Funcionales** | Componente reutilizable [`ValidatedTextField.kt`](app/src/main/java/cl/duoc/teqgastos/ui/components/ValidatedTextField.kt) con `isError`, íconos dinámicos (`Icons.Default.Error` / `Icons.Default.CheckCircle`), transición `AnimatedVisibility` (`fadeIn + expandVertically`), tarjetas expandibles con `animateContentSize()` y transiciones de navegación suaves en [`AppNavGraph.kt`](app/src/main/java/cl/duoc/teqgastos/ui/navigation/AppNavGraph.kt). |
| **IE 2.3.1** | **Persistencia Local Relacional con Room (`SQLite`)** | Base de datos [`AppDatabase.kt`](app/src/main/java/cl/duoc/teqgastos/data/local/AppDatabase.kt) con 3 entidades ([`UsuarioEntity`](app/src/main/java/cl/duoc/teqgastos/data/local/entities/UsuarioEntity.kt), [`RendicionEntity`](app/src/main/java/cl/duoc/teqgastos/data/local/entities/RendicionEntity.kt), [`GastoEntity`](app/src/main/java/cl/duoc/teqgastos/data/local/entities/GastoEntity.kt)), llave foránea en cascada (`ForeignKey.CASCADE`), `SeedCallback` de datos iniciales y consultas reactivas `Flow<List<...>>` en [`TeqGastosDao.kt`](app/src/main/java/cl/duoc/teqgastos/data/local/dao/TeqGastosDao.kt). |
| **IE 2.4.1** | **Acceso Seguro a 2 Recursos Nativos (Cámara y GPS)** | 1. **Cámara Nativa**: Configurada con `androidx.core.content.FileProvider` en [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) y [`file_paths.xml`](app/src/main/res/xml/file_paths.xml), disparada con `ActivityResultContracts.TakePicture()` y renderizada en miniatura con `Coil`.<br>2. **GPS Nativo (`FusedLocationProviderClient`)**: Solicitud en tiempo de ejecución con `RequestMultiplePermissions` (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`) y fallback seguro a coordenadas de Concepción (`-36.8270, -73.0503`) en [`NativeResourcesHelper.kt`](app/src/main/java/cl/duoc/teqgastos/utils/NativeResourcesHelper.kt). |

---

## 📏 Reglas de Negocio y Topes Corporativos Evaluados

| Categoría | Tope Máximo (CLP) | Validación Aplicada |
| :--- | :---: | :--- |
| **Alimentación** (`ALIMENTACION`) | `$15.000 CLP` | Conversión previa a CLP (`CLP`, `USD` o `UF`) + validación de tope |
| **Alojamiento** (`ALOJAMIENTO`) | `$65.000 CLP` | Conversión previa a CLP + validación de tope |
| **Peaje / Estacionamiento** (`PEAJE_ESTACIONAMIENTO`) | `$25.000 CLP` | Conversión previa a CLP + validación de tope |
| **Combustible** (`COMBUSTIBLE`) | `$60.000 CLP` | Conversión previa a CLP + validación de tope |
| **Insumos de Emergencia** (`INSUMOS`) | `$50.000 CLP` | Conversión previa a CLP + validación de tope |

---

## 🔑 Credenciales de Prueba por Rol (Defensa en Vivo)

La pantalla de inicio de sesión ([`LoginScreen.kt`](app/src/main/java/cl/duoc/teqgastos/ui/screens/auth/LoginScreen.kt)) incluye el **Selector Rápido de Rol** que autocompleta las credenciales para agilizar la demostración ante el docente:

| Rol | Usuario | Correo Electrónico | Contraseña | Funcionalidades Principales |
| :--- | :--- | :--- | :--- | :--- |
| **TÉCNICO** | Carlos Muñoz | `tecnico@teqmed.cl` | `123456` | Crear carpetas de rendición, **Importar Traslado TeqRuta** (Peaje + Combustible), capturar **Foto de Boleta con Cámara**, capturar **Coordenadas GPS**, validar topes/duplicados y enviar a Jefatura. |
| **JEFATURA** | Andrea Soto | `jefatura@teqmed.cl` | `123456` | Auditar rendiciones enviadas, **Inspeccionar desglose de gastos** (ver foto de boleta, coordenadas GPS y cumplimiento de topes), **Aprobar**, **Observar** o **Rechazar** con comentario obligatorio validado. |
| **FINANZAS** | Roberto Gómez | `finanzas@teqmed.cl` | `123456` | Filtrar rendiciones en estado `APROBADA`, registrar el código de transferencia/reembolso y **Exportar a ERP (`.CSV`)** generando un archivo físico en el dispositivo con diálogo e `Intent.ACTION_SEND` para compartirlo. |

> 💡 **Cambio de Rol en Vivo:** Todas las pantallas cuentan con el botón `←` en la barra superior (`TeqAppHeader`) para cerrar sesión instantáneamente y cambiar de rol durante la presentación.

---

## 📂 Estructura de Paquetes del Proyecto

```text
app/src/main/java/cl/duoc/teqgastos/
├── MainActivity.kt                        # Actividad principal que monta AppNavGraph y Room
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt                 # Room Database + SeedCallback con usuarios y rendiciones demo
│   │   ├── dao/TeqGastosDao.kt            # Consultas Flow, control de duplicados y estados
│   │   └── entities/                      # UsuarioEntity, RendicionEntity, GastoEntity
│   └── repository/TeqGastosRepository.kt  # Fuente única de verdad + integración simulada TeqRuta
├── domain/
│   └── validators/GastoValidator.kt       # Validaciones puras (RUT, Folio, Plazo 5 días, Topes CLP, Comentario)
├── ui/
│   ├── components/
│   │   ├── TeqFigmaComponents.kt          # Header corporativo, BottomNav, Badges de estado y Botones
│   │   └── ValidatedTextField.kt          # Input reactivo con ícono Error/CheckCircle y AnimatedVisibility
│   ├── navigation/
│   │   └── AppNavGraph.kt                 # Navegación declarativa con transiciones suaves fade/slide
│   ├── screens/
│   │   ├── auth/                          # AuthViewModel.kt + LoginScreen.kt
│   │   ├── tecnico/                       # TecnicoViewModel.kt + TecnicoHomeScreen.kt + FormularioGastoScreen.kt
│   │   ├── jefatura/                      # JefaturaViewModel.kt + JefaturaScreen.kt (Auditoría de boletas/GPS)
│   │   └── finanzas/                      # FinanzasViewModel.kt + FinanzasScreen.kt (Cierre y exportación CSV)
│   └── theme/TeqTheme.kt                  # Tokens de color y tipografía fieles al diseño de Figma
└── utils/
    ├── NativeResourcesHelper.kt           # FileProvider URI para Cámara + FusedLocationProviderClient GPS
    └── CsvExporter.kt                     # Generador de archivo físico .CSV en dispositivo + Intent.ACTION_SEND
```

---

## 🛠️ Pasos para Compilar y Ejecutar en Android Studio

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/hoxtxnDev/Proyecto-semestral.git
   cd Proyecto-semestral
   ```
2. **Abrir en Android Studio:**
   - Seleccionar **File → Open** y elegir la carpeta raíz del proyecto.
   - Esperar a que finalice la sincronización de **Gradle (AGP 8.7.3 / Kotlin 2.0.21 / KSP)**.
3. **Configurar Dispositivo o Emulador:**
   - Usar un dispositivo físico o un emulador con **API 26 (Android 8.0)** o superior (recomendado API 34/35 con cámara emulada configurada en *Virtual Scene* o *Webcam0*).
4. **Compilar desde Terminal (opcional):**
   ```bash
   ./gradlew assembleDebug
   ```
5. **Ejecutar la aplicación:**
   - Presionar **Run (`Shift + F10` / `Ctrl + R`)** en Android Studio.
