# TeqGastos — Sistema Móvil de Rendición y Auditoría de Gastos (TEQMED SPA)

Aplicación nativa Android desarrollada en **100% Kotlin** con **Jetpack Compose (Material Design 3)** para la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105 - Evaluación Parcial 2)**.

---

## 1. Propósito del Proyecto

**TeqGastos** digitaliza, valida y audita de extremo a extremo el proceso de rendición de gastos en terreno para el personal técnico, jefaturas y el área de finanzas de **TEQMED SPA**, eliminando planillas manuales, pérdida de boletas físicas, gastos fuera de plazo y duplicidad de cobros mediante validación tributaria local, conversión multimoneda offline, captura fotográfica y georreferenciación GPS.

---

## 2. Arquitectura y Estructura del Proyecto

El proyecto implementa el patrón arquitectónico **MVVM Estricto (Model - View - ViewModel)** con flujo de datos unidireccional (**UDF**) basado en `StateFlow` y persistencia local reactiva en **Room (SQLite)**:

```text
UI (@Composable Figma) ──► ViewModel (StateFlow) ──► Domain Validators (GastoValidator) ──► Repository ──► Room DAO (SQLite)
```

### Cumplimiento de Indicadores de Evaluación (Rúbrica EP2)
- **IE 2.1.1 / IE 2.1.2 (Arquitectura MVVM y Validaciones Desacopladas)**: Ningún `@Composable` contiene lógica de validación. Todas las reglas de negocio residen en `GastoValidator.kt` y son orquestadas en tiempo real desde cada `ViewModel` (`AuthViewModel`, `TecnicoViewModel`, `JefaturaViewModel`, `FinanzasViewModel`).
- **IE 2.2.1 (Componentes Visuales Validados y Animaciones Funcionales)**: `ValidatedTextField.kt` gestiona automáticamente estados `isError`, íconos semánticos (`Icons.Default.Error` en rojo y `Icons.Default.CheckCircle` en verde) y transiciones fluidas mediante `AnimatedVisibility` (`fadeIn + expandVertically` / `fadeOut + shrinkVertically`).
- **IE 2.2.2 (Autenticación y Navegación RBAC)**: `LoginScreen.kt` autentica contra Room SQLite, incorpora el **Selector Rápido de Credenciales (Modo Defensa)** y redirige automáticamente a la bandeja de trabajo según el rol (`TECNICO`, `JEFATURA`, `FINANZAS`) mediante `TeqGastosNavGraph.kt`.
- **IE 2.3.1 (Persistencia Local Offline-First)**: Almacenamiento relacional en SQLite con `AppDatabase`, claves foráneas en cascada, índice compuesto anti-duplicados y semilla automática de datos (`SeedDatabaseCallback`).

### Estructura de Paquetes (`cl.duoc.teqgastos`)

```text
cl.duoc.teqgastos
├── MainActivity.kt                              # Punto de entrada Single-Activity + inyección de dependencias Room
├── data
│   ├── local
│   │   ├── dao
│   │   │   └── TeqGastosDao.kt                  # Consultas reactivas Flow, recálculo CLP y query anti-duplicados
│   │   ├── entities
│   │   │   ├── UsuarioEntity.kt                 # Usuarios corporativos y rol (TECNICO, JEFATURA, FINANZAS)
│   │   │   ├── RendicionEntity.kt               # Cabecera de rendición y máquina de estados de aprobación
│   │   │   └── GastoEntity.kt                   # Ítem de gasto con FK en cascada, índice compuesto, foto y GPS
│   │   └── AppDatabase.kt                       # Singleton RoomDatabase + Callback con semilla de datos Figma
│   └── repository
│       └── TeqGastosRepository.kt               # Single Source of Truth para los ViewModels
├── domain
│   └── validators
│       └── GastoValidator.kt                    # Reglas puras: RUT, Folio, Fecha 5 días, Conversor UF/USD, Topes CLP
└── ui
    ├── components
    │   ├── ValidatedTextField.kt                # OutlinedTextField validado con íconos dinámicos y AnimatedVisibility
    │   └── TeqFigmaComponents.kt                # Componentes del sistema Figma: TeqBrand, TeqAppHeader, TeqBottomNav, TeqPrimaryButton
    ├── navigation
    │   └── TeqGastosNavGraph.kt                 # Grafo declarativo Navigation Compose por rol
    ├── screens
    │   ├── auth
    │   │   ├── AuthViewModel.kt                 # Validación en tiempo real de Login y perfiles Demo
    │   │   └── LoginScreen.kt                   # Pantalla de acceso con Hero curvo y selector rápido Modo Defensa
    │   ├── tecnico
    │   │   ├── TecnicoViewModel.kt              # Orquestador de registro de gastos, conversión CLP y bloqueo de duplicados
    │   │   └── TecnicoScreen.kt                 # Formulario de gasto Figma, grilla de categorías, tasas offline e historial
    │   ├── jefatura
    │   │   ├── JefaturaViewModel.kt             # Gestión de bandeja de evaluación, aprobación y observación/rechazo
    │   │   └── JefaturaScreen.kt                # Tarjeta resumen teal, tarjetas de evaluación y modal con justificación (0/240)
    │   └── finanzas
    │       ├── FinanzasViewModel.kt             # Consolidación de reembolsos aprobados y generación de archivo CSV ERP
    │       └── FinanzasScreen.kt                # Consolidado de desembolso, detalle de aprobados y exportación ERP (.CSV)
    └── theme
        └── TeqTheme.kt                          # Paleta oficial extraída de Figma (#1B2A4A Navy, #00D2C4 Teal, #F4F6F8 Surface)
```

> **Referencia de Diseño Figma**: La carpeta [`app/src-interface/`](file:///Users/hoxtxndev/Desktop/proyecto-semestral/Proyecto-semestral/app/src-interface/App.tsx) conserva la maqueta original exportada desde Figma (`App.tsx` e `index.css`) a partir de la cual se construyó la interfaz nativa en Jetpack Compose.

---

## 3. Reglas de Negocio y Validaciones (`GastoValidator.kt`)

| Regla | Método en `GastoValidator` | Criterio de Validación |
| :--- | :--- | :--- |
| **RUT Proveedor** | `validarRutProveedor(rut)` | Obligatorio; acepta formato con puntos (`XX.XXX.XXX-X`) o sin puntos con guion (`XXXXXXXX-X`). |
| **Folio de Boleta** | `validarFolioBoleta(folio)` | Obligatorio, exclusivamente numérico y mayor a `0`. |
| **Fecha del Gasto** | `validarFechaGasto(fechaMillis)` | No puede ser futura ni superar los **5 días de antigüedad** respecto a `System.currentTimeMillis()`. |
| **Conversor Offline** | `convertirAClp(monto, moneda)` | Convierte montos en `UF` (**\$38.000 CLP** configurable/fijo), `USD` (**\$950 CLP**) o `CLP` (`1:1`) antes de evaluar topes. |
| **Topes por Categoría** | `validarTopePorCategoria(...)` | Evalúa el monto convertido a CLP según la categoría seleccionada:<br>• `ALIMENTACION`: máx **\$15.000 CLP**<br>• `ALOJAMIENTO`: máx **\$65.000 CLP**<br>• `PEAJE_ESTACIONAMIENTO`: máx **\$25.000 CLP**<br>• `COMBUSTIBLE`: máx **\$60.000 CLP**<br>• `INSUMOS`: máx **\$50.000 CLP** |
| **Evidencia Fotográfica** | `validarFotoComprobante(fotoUri)` | Exige que exista `fotoUri` adjunta del comprobante o boleta. |
| **Control Anti-Fraude** | `repository.existeBoletaDuplicada(...)` | Bloquea en SQLite cualquier documento que repita la combinación **RUT Proveedor + Folio Boleta + Monto Original**. |
| **Auditoría Jefatura** | `validarComentarioRevision(...)` | Exige justificación obligatoria (mínimo 5 caracteres, máx 240) al **Observar** o **Rechazar** una rendición. |
| **Cierre Contable ERP** | `validarCodigoPagoErp(codigo)` | Exige un código de transferencia/pago ERP válido (ej. `PAG-2025-0041`) antes de exportar el `.CSV` y cerrar las rendiciones. |

---

## 4. Especificación de Requerimientos

### Requerimientos Funcionales (RF)

| Código | Nombre | Descripción |
| :--- | :--- | :--- |
| **RF-01** | Autenticación por Rol y Modo Defensa | El sistema autentica usuarios locales (`UsuarioEntity`), incluye botones rápidos (**Demo Técnico**, **Demo Jefatura**, **Demo Finanzas**) y redirige a la vista de su rol. |
| **RF-02** | Selección de Centro de Costo | El técnico puede alternar su centro de costo activo (`CC-104 · Operaciones Norte`, `CC-MANT-CLINICO-01`, `CC-208`) al registrar gastos. |
| **RF-03** | Registro de Gastos y Multimoneda | El técnico ingresa RUT proveedor, folio, fecha, moneda (`CLP`, `UF`, `USD`) y categoría con validación en tiempo real de conversión y topes en CLP. |
| **RF-04** | Bloqueo de Boleta Duplicada | El sistema verifica en Room (`existeBoletaDuplicada`) que la combinación de RUT proveedor, folio y monto original no haya sido rendida previamente. |
| **RF-05** | Captura de Comprobante y GPS | El registro adjunta respaldo fotográfico (`fotoUri`) con vista previa de boleta y coordenadas geográficas (`-33.4489, -70.6693`). |
| **RF-06** | Flujo de Evaluación Jefatura | La jefatura visualiza el consolidado de pendientes y puede **Aprobar**, **Observar** o **Rechazar** exigiendo motivo obligatorio en el modal de revisión. |
| **RF-07** | Desembolso y Exportación ERP Finanzas | Finanzas revisa las rendiciones `APROBADA`, ingresa el **Código de Pago Final**, genera el archivo `teqgastos_erp.csv` y marca las rendiciones como `PAGADA_ERP`. |

### Requerimientos No Funcionales (RNF)

| Código | Categoría | Descripción |
| :--- | :--- | :--- |
| **RNF-01** | Arquitectura y Mantenibilidad | Implementación estricta del patrón **MVVM** con separación en capas (`data`, `domain`, `ui`) y validaciones 100% desacopladas de los `@Composable`. |
| **RNF-02** | Persistencia Offline-First | Almacenamiento 100% local en SQLite mediante **Jetpack Room 2.6.1** procesado con **KSP**, garantizando operación en terreno sin internet. |
| **RNF-03** | Reactividad y Concurrencia | Uso de **Kotlin Coroutines** y exposición de estado reactivo en UI mediante `Flow` y `StateFlow`. |
| **RNF-04** | Usabilidad, Diseño Figma y Animaciones | Interfaz construida en **Jetpack Compose (Material 3)** fiel al diseño de Figma, con `AnimatedVisibility` para feedback de errores y confirmaciones. |
| **RNF-05** | Compatibilidad | Soporte para dispositivos Android con `minSdk = 26` (Android 8.0 Oreo) y `targetSdk = 35` (Android 15) bajo JDK 17. |

---

## 5. Guía Rápida para Defensa Académica (`AppDatabase.Callback`)

Al instalar o iniciar la aplicación por primera vez, `AppDatabase` pre-carga automáticamente los **3 perfiles corporativos** y **4 rendiciones de demostración** alineadas con el prototipo de Figma:

### Credenciales de Prueba (Autocompletables desde la Pantalla de Login)

| Botón Rápido | Rol | Nombre | Email | Contraseña |
| :--- | :--- | :--- | :--- | :--- |
| **Demo Técnico** | `TECNICO` | Carlos Muñoz | `tecnico@teqmed.cl` | `123456` |
| **Demo Jefatura** | `JEFATURA` | Andrea Soto | `jefatura@teqmed.cl` | `123456` |
| **Demo Finanzas** | `FINANZAS` | Roberto Gómez | `finanzas@teqmed.cl` | `123456` |

### Datos Semilla Pre-cargados para Demostración Inmediata

1. **Bandeja Jefatura (`PENDIENTE_JEFATURA`)**:
   - **Camila Torres** — RUT `17.982.341-2` · Folio `008321` · Categoría `Alimentación` · **\$14.990 CLP**
   - **Diego Soto** — RUT `16.440.828-6` · Folio `114209` · Categoría `Combustible` · **\$58.700 CLP**
   - **Martín Rojas** — RUT `18.223.901-4` · Folio `003118` · Categoría `Alojamiento` · **\$65.000 CLP**
2. **Bandeja Finanzas (`APROBADA`)**:
   - **Paula Díaz** — RUT `19.118.220-1` · Folio `006742` · Categoría `Peaje / Estac.` · **\$18.500 CLP**
