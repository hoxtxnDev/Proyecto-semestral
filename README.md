# TeqGastos — Sistema Móvil de Rendición de Gastos (TEQMED SPA)

Aplicación nativa Android desarrollada en **100% Kotlin** con **Jetpack Compose (Material Design 3)** para la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105 - Evaluación Parcial 2)**.

---

## 1. Propósito del Proyecto

**TeqGastos** digitaliza y controla el proceso completo de rendición de gastos de terreno para el personal técnico, jefaturas y el área de finanzas de **TEQMED SPA**, eliminando planillas manuales, pérdida de boletas físicas y duplicidad de cobros mediante validación tributaria local, captura fotográfica y georreferenciación GPS.

---

## 2. Arquitectura y Estructura de Paquetes

El proyecto implementa el patrón arquitectónico **MVVM Estricto (Model - View - ViewModel)** con flujo de datos unidireccional (**UDF**) basado en `StateFlow` y persistencia reactiva en **Room (SQLite)**:

```text
UI (@Composable) ──► ViewModel (StateFlow) ──► Domain Validators ──► Repository ──► Room DAO (SQLite)
```

### Reglas Arquitectónicas Aplicadas
1. **100% Kotlin y Jetpack Compose (Material 3)**: Interfaz declarativa reactiva sin XML.
2. **Desacoplamiento Estricto de Validaciones**: Ningún `@Composable` contiene lógica de validación. Todas las reglas de negocio y validaciones de formularios residen en `domain/validators` y son orquestadas por el `ViewModel` mediante estados explícitos (`errorMessage: String?`, `isError: Boolean`).
3. **Single Source of Truth (SSOT)**: `TeqGastosRepository` centraliza el acceso a `TeqGastosDao`, exponiendo flujos reactivos (`Flow<List<...>>`) filtrados por rol y recalculando montos totales de forma atómica en SQLite.

### Estructura de Paquetes (`cl.duoc.teqgastos`)

```text
cl.duoc.teqgastos
├── data
│   ├── local
│   │   ├── dao
│   │   │   └── TeqGastosDao.kt          # Consultas reactivas Flow, recálculo CLP y query anti-duplicados
│   │   ├── entities
│   │   │   ├── UsuarioEntity.kt         # Usuarios corporativos y rol (TECNICO, JEFATURA, FINANZAS)
│   │   │   ├── RendicionEntity.kt       # Cabecera de rendición y máquina de estados de aprobación
│   │   │   └── GastoEntity.kt           # Ítem de gasto con FK en cascada, índice compuesto, foto y GPS
│   │   └── AppDatabase.kt               # Singleton RoomDatabase + Callback con semilla de datos
│   └── repository
│       └── TeqGastosRepository.kt       # Repositorio único de verdad para los ViewModels
├── domain
│   └── validators                       # Validadores puros (RUT Módulo 11, Folio, Montos, Políticas)
├── ui
│   ├── components                       # Componentes Material Design 3 reutilizables
│   ├── navigation                       # Grafo de navegación (Navigation Compose)
│   └── screens                          # Pantallas Compose + ViewModels con StateFlow
└── utils                                # Utilidades de formato multimoneda, Cámara y Location Services
```

---

## 3. Supuestos de Negocio (TEQMED SPA)

1. **Control de Acceso Basado en Roles (RBAC)**:
   - **TÉCNICO (`TECNICO`)**: Crea carpetas de rendición asociadas a su Centro de Costo, registra gastos individuales en terreno con respaldo fotográfico y ubicación GPS, importa gastos desde *TeqRuta* y envía la rendición a revisión.
   - **JEFATURA (`JEFATURA`)**: Audita las rendiciones en estado `PENDIENTE_JEFATURA`, revisa el detalle de boletas y decide **Aprobar (`APROBADA`)**, **Observar (`OBSERVADA`)** o **Rechazar (`RECHAZAR`)** exigiendo comentario de retroalimentación cuando existen reparos.
   - **FINANZAS (`FINANZAS`)**: Visualiza las rendiciones `APROBADA` por Jefatura y ejecuta el cierre contable registrando el código de comprobante/transferencia ERP (`PAGADA_ERP`).
2. **Ciclo de Vida de una Rendición**:
   - `BORRADOR` $\rightarrow$ `PENDIENTE_JEFATURA` $\rightarrow$ (`APROBADA` | `OBSERVADA` | `RECHAZADA`) $\rightarrow$ `PAGADA_ERP`.
   - Una rendición en estado `OBSERVADA` permite al técnico subsanar los ítems objetados y reenviarla a `PENDIENTE_JEFATURA`.
3. **Prevención de Fraude y Boleta Duplicada**:
   - No se permite registrar dos veces un documento tributario con la misma combinación de **RUT Proveedor + Folio Boleta + Monto Original** (`existeBoletaDuplicada`).
4. **Soporte Multimoneda con Conversión a CLP**:
   - Los gastos pueden ingresarse en `CLP`, `UF` o `USD`, almacenando tanto el `montoOriginal` como el `montoConvertidoClp` para cuadrar el total contable de la rendición.
5. **Integración con Ecosistema TeqRuta**:
   - Los gastos de peaje/estacionamiento o kilometraje originados en la app hermana *TeqRuta* se identifican con el flag `origenTeqRuta = true`.

---

## 4. Especificación de Requerimientos

### Requerimientos Funcionales (RF)

| Código | Nombre | Descripción |
| :--- | :--- | :--- |
| **RF-01** | Autenticación por Rol | El sistema debe autenticar usuarios locales (`UsuarioEntity`) y redirigirlos a la bandeja de trabajo correspondiente a su rol (`TECNICO`, `JEFATURA`, `FINANZAS`). |
| **RF-02** | Creación de Rendición | El técnico debe poder crear una cabecera de rendición (`RendicionEntity`) indicando centro de costo y medio de pago (`FONDO_POR_RENDIR`, `TARJETA_CORPORATIVA`, `DINERO_PROPIO`). |
| **RF-03** | Registro de Gastos y Multimoneda | El técnico debe poder agregar gastos (`GastoEntity`) con RUT proveedor, folio, fecha, categoría (`ALIMENTACION`, `ALOJAMIENTO`, `PEAJE_ESTACIONAMIENTO`, `COMBUSTIBLE`, `INSUMOS`), moneda (`CLP`, `UF`, `USD`) y conversión automática a CLP. |
| **RF-04** | Bloqueo de Boleta Duplicada | El sistema debe verificar mediante Room (`existeBoletaDuplicada`) que la combinación de RUT proveedor, folio de boleta y monto original no haya sido rendida previamente. |
| **RF-05** | Captura de Evidencia y GPS | El registro de gasto debe permitir adjuntar URI de fotografía de la boleta (`fotoUri`) y coordenadas geográficas (`latitud`, `longitud`) mediante Google Play Services Location. |
| **RF-06** | Flujo de Aprobación Jefatura | La jefatura debe poder aprobar, observar o rechazar una rendición en estado `PENDIENTE_JEFATURA`, exigiendo comentario obligatorio en caso de observación o rechazo. |
| **RF-07** | Pago y Cierre ERP Finanzas | El perfil de Finanzas debe poder marcar como `PAGADA_ERP` una rendición previamente `APROBADA`, registrando el `codigoPagoFinanzas` obligatorio. |

### Requerimientos No Funcionales (RNF)

| Código | Categoría | Descripción |
| :--- | :--- | :--- |
| **RNF-01** | Arquitectura y Mantenibilidad | Implementación estricta del patrón **MVVM** con separación en capas (`data`, `domain`, `ui`, `utils`) y prohibición de lógica de validación dentro de funciones `@Composable`. |
| **RNF-02** | Persistencia Offline-First | Almacenamiento 100% local en SQLite mediante **Jetpack Room 2.6.1** procesado con **KSP**, garantizando operación en terreno sin conexión a internet. |
| **RNF-03** | Reactividad y Concurrencia | Uso de **Kotlin Coroutines** (`Dispatchers.IO`) para operaciones de base de datos y exposición de estado reactivo en UI mediante `Flow` y `StateFlow`. |
| **RNF-04** | Usabilidad y Diseño | Interfaz construida con **Jetpack Compose** y **Material Design 3**, presentando mensajes de error específicos por campo (`isError`, `errorMessage`). |
| **RNF-05** | Compatibilidad | Soporte para dispositivos Android con `minSdk = 26` (Android 8.0 Oreo) y `targetSdk = 35` (Android 15) bajo JDK 17. |

---

## 5. Usuarios de Prueba Pre-cargados (`AppDatabase.Callback`)

Al iniciar la aplicación por primera vez, `AppDatabase` inserta automáticamente los siguientes perfiles de prueba y una rendición de ejemplo con 2 gastos asociados:

| Rol | Nombre | Email | Contraseña | Centro de Costo Autorizado |
| :--- | :--- | :--- | :--- | :--- |
| **TÉCNICO** | Carlos Muñoz | `tecnico@teqmed.cl` | `123456` | `CC-MANT-CLINICO-01` |
| **JEFATURA** | Andrea Soto | `jefatura@teqmed.cl` | `123456` | `CC-MANT-CLINICO-01` |
| **FINANZAS** | Roberto Gómez | `finanzas@teqmed.cl` | `123456` | `CC-CORPORATIVO-ALL` |
