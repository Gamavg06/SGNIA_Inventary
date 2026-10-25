# 📐 Descripción Técnica de la Interfaz de Usuario (UI/UX)
**Sistema SGNIA Carnicería & Balanza Industrial POS**  
*Documento de Auditoría y Especificación de Diseño para Evaluación Externa*  
*Fecha de Extracción del Código Fuente: 24 de Octubre de 2026*  
*Autor: Desarrollador del Proyecto (SGNIA COMPANY)*  

---

> **PROPÓSITO DE ESTE DOCUMENTO**: Esta especificación técnica describe la interfaz de usuario implementada en el código fuente de Kotlin y Jetpack Compose. No contiene lenguaje publicitario ni proyecciones de lo que "debería haber". Cada medida en `dp`/`sp`, valor hexadecimal `#RRGGBB`, estado interactivo y debilidad visual corresponde a la inspección directa del código fuente en producción.

---

# PARTE 1: SISTEMA DE DISEÑO Y TOKENS

En el estado actual del repositorio coexisten **dos capas de diseño superpuestas**:
1. **Capa Legada / Temas Múltiples (`theme/Color.kt`, `theme/Theme.kt`, `theme/SgniaTheme.kt`, `ui/components/SgniaComponents.kt`)**: Sistema inicial glassmórfico con soporte para tres temas (`NOIR_CREMA`, `OBSIDIAN_GOLD`, `CYBER_VIOLET`) y componentes basados en Material 3 y tarjetas translúcidas.
2. **Capa Actual "Instrumento de Precisión" (`ui/design/NoirTokens.kt`, `ui/design/NoirComponents.kt`)**: Sistema estricto de diseño industrial Noir & Crema Bento Grid, diseñado para eliminar fatiga visual y operar con guantes o manos húmedas.

---

### 1.1 Archivos de Tema y Paleta de Colores

#### A. Archivo `ui/design/NoirTokens.kt` (Fuente de Verdad del Rediseño Noir)
| Nombre del Token | Valor Hexadecimal | Color / Tono | Dónde se usa en el código |
| :--- | :---: | :--- | :--- |
| `NoirColors.Background` | `#0F0E0D` | Carbón Mate Profundo | Fondo principal de pantallas (`Scaffold`, `LazyColumn`, visor de básculas, login). |
| `NoirColors.Surface` | `#181715` | Carbón Suave Neutro | Fondo estándar de contenedores `BentoCard` y cards inactivas. |
| `NoirColors.SurfaceRaised` | `#1F1D1A` | Carbón Elevado | Tarjetas seleccionadas, modales y botones secundarios. |
| `NoirColors.SurfaceSunken` | `#121110` | Carbón Hundido | Cajas de búsqueda, campos de texto y fondos de métricas. |
| `NoirColors.Hairline` | `#2A2723` | Borde Sutil | Separadores y bordes de celdas no seleccionadas (1 dp). |
| `NoirColors.HairlineStrong`| `#3A352F` | Borde Medio | Bordes con foco o en tarjetas interactivas. |
| `NoirColors.Cream` | `#EEDDC8` | Crema Marfil | Acento principal, `Hero BentoCard`, botones primarios `NoirPrimaryButton`, chips activos y títulos destacados. |
| `NoirColors.TextPrimary` | `#F3EFE6` | Blanco Crema | Texto principal, cifras numéricas grandes y nombres de productos. |
| `NoirColors.TextSecondary` | `#A8A196` | Crema Medio | Subtítulos, etiquetas técnicas, unidades (`kg`, `$`) y descripciones. |
| `NoirColors.TextMuted` | `#6E675E` | Crema Ceniza | Marcas de agua, labels tácticos (`NoirLabel`) y datos deshabilitados. |
| `NoirColors.OnCream` | `#141312` | Negro Grafito | Texto e iconos colocados encima de superficies `Cream` (botones primarios o hero cards). |
| `NoirColors.OnCreamSecondary`| `#4A4640` | Carbón Opaco | Subtítulos situados sobre superficies `Cream`. |
| `NoirColors.Emerald` | `#4ADE80` | Verde Esmeralda | **Exclusivo para estados positivos**: peso estable (`[ESTABLE]`), ping exitoso y badge `Al día`. |
| `NoirColors.EmeraldMuted`| `#1C2A21` | Verde Musgo Oscuro | Fondo de insignias de estado estable (`StatusPill`). |
| `NoirColors.Gold` | `#C9A96E` | Oro Champán | Acento secundario en tarjetas maestras e importes. |
| `NoirColors.GoldMuted` | `#2A241A` | Oro Viejo Fondo | Fondo de badges con tono dorado. |
| `NoirColors.Amber` | `#D9A441` | Ámbar Industrial | Indicador de lectura pendiente, aviso de peso en movimiento y botones de acción. |
| `NoirColors.AmberMuted` | `#2B2416` | Ámbar Oscuro | Fondo de status pills con estado pendiente. |
| `NoirColors.Slate` | `#9FB0C0` | Acero Claro | Etiquetas informativas neutras. |
| `NoirColors.SlateMuted` | `#1C2127` | Acero Carbón | Fondo de status pills informativas. |
| `NoirColors.Danger` | `#E07A6F` | Salmón / Carmesí | Error de desconexión, bloqueo de seguridad anti-hooking y peso inestable crítico. |
| `NoirColors.DangerMuted` | `#2C1B19` | Carmesí Oscuro | Fondo de tarjetas con error y status pills de alarma. |

#### B. Archivo `theme/SgniaTheme.kt` (Sistema Legado / Multi-Tema)
| Modo de Tema | Token | Valor Hex | Uso en el código |
| :--- | :--- | :---: | :--- |
| `NOIR_CREMA` | `backgroundPrimary` | `#0F0E0D` | Fondo general de la app si se utiliza `SgniaTheme`. |
| `NOIR_CREMA` | `backgroundSecondary`| `#171614` | Barras de navegación superior y lateral. |
| `NOIR_CREMA` | `surfaceGlass` | `#211E1B` | Tarjetas tácticas en pantallas legadas. |
| `NOIR_CREMA` | `surfaceGlassBorder`| `#423B32` | Borde de tarjetas tácticas. |
| `NOIR_CREMA` | `accentPrimary` | `#EEDDC8` | Botones de tema en `SgniaComponents`. |
| `NOIR_CREMA` | `statusStable` | `#26A69A` | Teal / Verde en componentes legados. |
| `NOIR_CREMA` | `statusUnstable` | `#FFB74D` | Naranja ámbar legado. |
| `NOIR_CREMA` | `statusAlert` | `#E57373` | Carmesí suave legado. |
| `NOIR_CREMA` | `oledDisplayBg` | `#0A0908` | Fondo negro absoluto en display OLED. |
| `NOIR_CREMA` | `oledDigitColor` | `#FFE082` | Dígitos amarillos ámbar en visor OLED. |
| `OBSIDIAN_GOLD`| `backgroundPrimary` | `#0A0B0E` | Modo alternativo activable desde Ajustes. |
| `OBSIDIAN_GOLD`| `accentPrimary` | `#D4AF37` | Oro líquido metálico. |
| `CYBER_VIOLET` | `backgroundPrimary` | `#0B0813` | Modo cyberpunk alternativo en Ajustes. |
| `CYBER_VIOLET` | `accentPrimary` | `#D946EF` | Fucsia / Neón violeta. |

#### C. Archivo `theme/Color.kt` y `theme/Theme.kt` (Inconsistencia Identificada)
* Contiene los colores por defecto del template de Android Studio (`Purple80 = #D0BCFF`, `PurpleGrey80 = #CCC2DC`, `Pink80 = #EFB8C8`, `Purple40 = #6650a4`, etc.).
* **Diagnóstico**: Este archivo está huérfano. No se utiliza en ninguna pantalla operativa, pero sigue compilándose en el paquete `theme`.

---

### 1.2 Tipografías y Jerarquía de Texto

#### A. Tokens Oficiales en `NoirTokens.kt` (`NoirType`)
* **`metricXL`**: `FontFamily.Monospace`, `FontWeight.Bold`, tamaño **56 sp**, interlineado `60 sp`, tracking `-2 sp`. Se usa para cifras gigantes de pesaje en tiempo real y totales consolidados de kilos.
* **`metricL`**: `FontFamily.Monospace`, `FontWeight.Bold`, tamaño **36 sp**, interlineado `40 sp`, tracking `-1 sp`. Se usa para importes de compra e importes por pagar.
* **`metricM`**: `FontFamily.Monospace`, `FontWeight.SemiBold`, tamaño **26 sp**, interlineado `30 sp`, tracking `-0.5 sp`. Se usa para subtotales en tarjetas Bento y pesos individuales en listas.
* **`display`**: `FontWeight.SemiBold`, tamaño **32 sp**, interlineado `38 sp`, tracking `-0.8 sp`. Se usa en títulos principales de pantallas maestras.
* **`title`**: `FontWeight.Bold`, tamaño **18 sp**, interlineado `22 sp`. Se usa en cabeceras de sección y modales.
* **`body`**: `FontWeight.Normal`, tamaño **15 sp**, interlineado `21 sp`. Se usa en descripciones operativas y textos explicativos.
* **`bodySmall`**: `FontWeight.Normal`, tamaño **13 sp**, interlineado `18 sp`. Se usa en subtítulos de tarjetas y leyendas de pie.
* **`label`**: `FontWeight.SemiBold`, tamaño **11 sp**, interlineado `14 sp`, tracking **`1.6 sp` (mayúsculas espaciadas)**. Se usa como placa de especificación superior (`NoirLabel`).
* **`button`**: `FontWeight.Bold`, tamaño **16 sp**, tracking `0.4 sp`. Se usa en botones de confirmación y pesaje.

#### B. Tipografía Legada en `SgniaTheme.kt` (`SgniaTypography`)
* `displayOled`: `FontFamily.Monospace`, `FontWeight.Black`, tamaño **52 sp**, tracking `2 sp`.
* `displayLarge`: `FontFamily.SansSerif`, `FontWeight.Black`, tamaño **26 sp**, tracking `0.5 sp`.
* `labelTactical`: `FontFamily.Monospace`, `FontWeight.Bold`, tamaño **11 sp**, tracking `1 sp`.

---

### 1.3 Radios de Esquina, Espaciados y Elevaciones

#### A. Radios (`NoirRadius`)
* **`NoirRadius.sm`**: **12 dp** — Chips pequeños, badges y botones secundarios.
* **`NoirRadius.md`**: **18 dp** — Tarjetas Bento secundarias, campos de texto y Floating Action Buttons.
* **`NoirRadius.lg`**: **24 dp** — Tarjeta principal Hero y diálogos modales.
* **`NoirRadius.pill`**: **100 dp** — Píldoras de estado (`StatusPill`), botones redondeados y selectores segmentados.

#### B. Espaciados de Cuadrícula (`NoirSpace`)
* `NoirSpace.xxs` = **4 dp** | `NoirSpace.xs` = **8 dp** | `NoirSpace.sm` = **12 dp**
* `NoirSpace.md` = **16 dp** | `NoirSpace.lg` = **20 dp** | `NoirSpace.xl` = **28 dp** | `NoirSpace.xxl` = **40 dp**
* **`NoirSpace.screen`**: **20 dp** — Margen horizontal estándar de pantalla.
* **`NoirSpace.gutter`**: **12 dp** — Separación vertical/horizontal entre celdas Bento.
* **`NoirSpace.touch`**: **64 dp** — **Altura mínima para cualquier botón o control táctil crítico** (ergonomía para uso con guantes y cuartos fríos).

#### C. Bordes, Sombras y Elevaciones
* En la capa Noir (`NoirComponents.kt`), **las elevaciones y sombras Material 3 tradicionales con blur se han eliminado o reducido a 0 dp**.
* La profundidad se logra mediante **contraste de color de superficie** (`Background #0F0E0D` vs. `Surface #181715` vs. `SurfaceRaised #1F1D1A`) y **bordes finos de 1 dp o 1.5 dp** con `NoirColors.Hairline (#2A2723)` o `NoirColors.Cream (#EEDDC8)`.

---

### 1.4 Catálogo de Componentes Reutilizables

#### 1. `BentoCard` ([`NoirComponents.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/design/NoirComponents.kt))
* **Qué muestra**: Contenedor rectangular con esquinas redondeadas para agrupar métricas o controles.
* **Medidas y Estilo**:
  * Si `hero = false`: fondo `NoirColors.Surface (#181715)`, borde de 1 dp `NoirColors.Hairline (#2A2723)`, radio **18 dp** (`NoirRadius.md`), padding interno **16 dp** (`NoirSpace.md`).
  * Si `hero = true`: fondo `NoirColors.Cream (#EEDDC8)`, borde sin trazo, radio **24 dp** (`NoirRadius.lg`), padding interno **20 dp** (`NoirSpace.lg`).
* **Estados**: Admite parámetro `onClick`. Al pulsarse aplica ripple discreto.
* **Dónde se usa**: Pantalla principal de Básculas, Dashboard del Dueño, Recepción, Pedidos y Sucursales.

#### 2. `StatusPill` ([`NoirComponents.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/design/NoirComponents.kt))
* **Qué muestra**: Píldora compacta con un círculo indicador de 6 dp y texto en mayúsculas (11 sp, SemiBold).
* **Medidas**: Altura aprox. 24 dp, radio **100 dp**, padding horizontal 10 dp, vertical 4 dp.
* **Estados (`PillTone`)**:
  * `Positive`: círculo `#4ADE80`, texto `#4ADE80`, fondo `#1C2A21`.
  * `Pending`: círculo `#D9A441`, texto `#D9A441`, fondo `#2B2416`.
  * `Alert`: círculo `#E07A6F`, texto `#E07A6F`, fondo `#2C1B19`.
  * `Neutral`: círculo `#9FB0C0`, texto `#9FB0C0`, fondo `#1C2127`.
* **Dónde se usa**: Estado de báscula (`[ESTABLE]`, `[EN LÍNEA]`, `[SIN CONEXIÓN]`), estatus de sucursales (`Al día`, `Pendiente`).

#### 3. `BigMetric` ([`NoirComponents.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/design/NoirComponents.kt))
* **Qué muestra**: Valor numérico dominante con unidad tipográfica adyacente en tamaño menor (`kg`, `g`, `$`, `%`).
* **Medidas**: Tipografía monospace configurable (`metricXL` 56 sp, `metricL` 36 sp o `metricM` 26 sp).
* **Dónde se usa**: Visor de báscula (peso en vivo), total de compra consolidada, total de merma.

#### 4. `NoirPrimaryButton` y `NoirSecondaryButton` ([`NoirComponents.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/design/NoirComponents.kt))
* **Qué muestra**: Botón de llamada a la acción con altura ergonómica.
* **Medidas**: Altura fija mínima de **64 dp** (`NoirSpace.touch`), esquinas redondeadas **18 dp** (`NoirRadius.md`).
* **Estados**:
  * `Primary`: fondo `NoirColors.Cream (#EEDDC8)`, texto `NoirColors.OnCream (#141312)`.
  * `Secondary`: fondo `NoirColors.SurfaceRaised (#1F1D1A)`, borde 1.5 dp `NoirColors.HairlineStrong (#3A352F)`, texto `NoirColors.TextPrimary (#F3EFE6)`.
* **Dónde se usa**: "PESAR Y CAPTURAR", "TARAR BÁSCULA", "NUEVA SUCURSAL", "GUARDAR BÁSCULA".

#### 5. `NoirSegmented` ([`NoirComponents.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/design/NoirComponents.kt))
* **Qué muestra**: Selector horizontal tipo pastilla deslizante para alternar modos.
* **Medidas**: Fondo `#121110`, borde `#2A2723`, pastilla activa `#EEDDC8` con texto `#141312`.
* **Dónde se usa**: Selector de autenticación en Login (`PIN` vs. `Clave`), vista de tabla vs. tarjetas en Inversión.

#### 6. `NoirPinDots` y `NoirKeypad` ([`NoirComponents.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/design/NoirComponents.kt))
* **Qué muestra**: Display de 4 círculos para ingreso de PIN y teclado numérico táctil de 3x4 (dígitos 1-9, borrar, 0, confirmar).
* **Medidas**: Botones del teclado de **64 dp** de altura mínima con radio **18 dp**.
* **Dónde se usa**: `SecurityVaultScreen` (login), `AdminSecurityScreen` (cambio de PIN) y modal de peso manual.

#### 7. Componentes de la Capa Legada / Cerhan ([`SgniaComponents.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/components/SgniaComponents.kt))
* `SgniaCerhanSidebar`: Barra lateral de navegación con drawer para tabletas y teléfonos. Fondo `#171614`, ítems con padding horizontal de 16 dp, iconos de 24 dp y badges numéricas en píldora.
* `SgniaCerhanTopBar`: Barra superior con botón hamburguesa para desplegar el drawer, caja de búsqueda redondeada (fondo `#211E1B`) y selector de tema.
* `SgniaArtisanalProductCard`: Tarjeta de producto de catálogo con imagen cuadrada de 70x70 dp, nombre en negrita, código PLU e insignia de precio por kilo.
* `OptimizedScaleOledDisplay`: Caja negra pura `#0A0908` con borde degradado y cifras en amarillo ámbar `#FFE082` simulando display fluorescente de balanza.

---

# PARTE 2: DESCRIPCIÓN PANTALLA POR PANTALLA

A continuación se detalla la anatomía de cada pantalla encontrada en el proyecto:

---

### Pantalla 1: Visor Multiescala Dinámico (Pantalla Principal de Mostrador)
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/scale/IndustrialScaleVisorScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/scale/IndustrialScaleVisorScreen.kt)
* **Acceso**: Pantalla inicial tras iniciar sesión en la variante `:app (tienda)` o seleccionando "Básculas (Principal)" en el menú lateral.
* **Estructura Visual**:
  1. **Barra Superior**: Botón hamburguesa para abrir el menú lateral, título `"SGNIA | BÁSCULAS"`, indicador del número de básculas activas (`＋ BÁSCULAS (X)`), botón para abrir el escáner de códigos de barras (icono de código QR) y selector de báscula activa en chips horizontales.
  2. **Área Central (Bento Grid Dinámico)**: Cuadrícula responsiva que muestra desde 1 hasta 10+ tarjetas individuales de báscula:
     * Si hay 1 sola báscula: se renderiza como una tarjeta **Hero Bento** de gran formato ocupando todo el ancho.
     * Si hay múltiples básculas: se organizan en un grid de tarjetas Bento paralelas.
     * **Contenido de cada tarjeta**: Nombre de la báscula (ej. *Systel Cuora Max*), dirección IP y puerto (`192.168.100.40:10001`), insignia `StatusPill` (`[ESTABLE]` verde o `[EN MOVIMIENTO]` ámbar), valor del peso en tamaño gigante (`0.000 kg` con tipografía monospace `metricXL`), indicador de tara individual (`T: 0.000 kg`), botón secundario `"TARA"` y botón primario `"PESAR Y CAPTURAR"`.
  3. **Franja Inferior / Barra de Estado**: Mini-barra con telemetría de red: latencia del último paquete (ej. `● 14ms`), bytes acumulados transferidos y botón táctil `"Ingresar Peso Manual"`.
* **Origen de Datos**: `viewModel.scalesList` (catálogo persistido en SharedPreferences) y `viewModel.scaleDevicesMap` (`StateFlow` con los estados en vivo emitidos por `TcpScaleManager`).
* **Acciones del Usuario**:
  * Tarar cualquier báscula de forma independiente sin afectar a las demás.
  * Presionar "Pesar y Capturar" para abrir el modal de vinculación de corte con el peso congelado.
  * Pulsar "Agregar Báscula" para abrir el catálogo de marcas.
  * Pulsar el icono de código de barras para activar el escáner con cámara.
  * Tocar "Ingresar Peso Manual" para abrir el modal de keypad numérico.
* **Estados**:
  * *Báscula conectada y peso estable*: Cifra en `NoirColors.TextPrimary (#F3EFE6)`, badge verde `[ESTABLE]`.
  * *Peso inestable / movimiento*: Cifra en `NoirColors.Amber (#D9A441)`, badge ámbar `[INESTABLE]`.
  * *Báscula apagada o sin red*: Cifra en `--.--- kg`, badge rojo `[SIN CONEXIÓN]`, mensaje *"Tiempo agotado"*, botón de reconexión manual.
  * *Error de hardware*: Muestra códigos de error Systel decodificados (`PE1` Inestable, `PE2` Cero, `PE3` Negativo, `PE5` Sobrecarga).
* **Modales Asociados**:
  1. *Modal "Agregar Báscula"*: Selector de marcas (chips rápidos Systel, Torrey, Rhino, Toledo, CAS, Dibatec, Yaohua), campo de texto de IP, puerto y botón de test de ping.
  2. *Modal "Teclado Numérico de Peso Manual"*: Keypad táctil con dígitos 1-9, punto decimal, botón borrar y confirmación de kilos.
  3. *Modal "Catálogo PLU de la Balanza"* (`PluCatalogListDialog.kt`): Muestra la lista de cortes leídos de la memoria física de la balanza Systel.

---

### Pantalla 2: Catálogo de Cortes y Precios
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/AdminCatalogScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/AdminCatalogScreen.kt)
* **Acceso**: Menú lateral $\rightarrow$ "Catálogo de Cortes" (o "Catálogo Maestro" en Dueño).
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("CATÁLOGO MAESTRO DE CORTES")`, título `"Catálogo y Precios"`, total de productos registrados en badge.
  2. **Buscador y Filtros**: Campo `OutlinedTextField` de búsqueda rápida (busca por nombre, código PLU o código local) y fila de chips deslizables por categoría cárnica (`Todas`, `Res`, `Cerdo`, `Pollo`, `Embutidos`, `Otros`).
  3. **Lista de Productos (`LazyColumn`)**: Tarjetas Bento individuales para cada producto. Cada celda muestra:
     * Nombre comercial del corte (ej. *Bistec de Res Especial*).
     * Código PLU de balanza y código interno.
     * Precio actual por kilogramo (`$180.00 / kg`).
     * Existencia actual en mostrador (`Stock: 45.20 kg`).
     * Botón de edición rápida (icono lápiz) y botón de eliminar (icono bote de basura).
  4. **Floating Action Button (FAB)**: Botón crema en la esquina inferior derecha con etiqueta `"NUEVO CORTE"`.
* **Origen de Datos**: `viewModel.allProducts` (Room SQLite, tabla `products`).
* **Acciones del Usuario**: Buscar cortes en tiempo real, filtrar por categoría animal, pulsar un corte para editar su precio/stock, o presionar el FAB para agregar un corte nuevo.
* **Estados**:
  * *Cargando*: Pantalla en blanco con skeleton o lista vacía.
  * *Vacío*: Mensaje de texto central indicando que no se encontraron cortes para el criterio de búsqueda.
* **Modal Asociado**:
  * `ProductEditDialog.kt`: Modal con campos para Código PLU, Nombre, Categoría, Precio por kilo, Stock actual, Merma esperada (%) y botón "Guardar Producto".

---

### Pantalla 3: Recepción de Mercancía y Entrada de Canales
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/reception/StockReceptionScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/reception/StockReceptionScreen.kt)
* **Acceso**: Menú lateral $\rightarrow$ "Recepción Mercancía".
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("ENTRADA DE ALMACÉN Y PROVEEDORES")`, título `"Recepción de Mercancía"`.
  2. **Tarjeta de Entrada Rápida (Bento Hero)**:
     * Campo selector de producto o código de barras.
     * Botón para abrir la cámara de escaneo óptico (`CameraBarcodeScannerDialog`).
     * Bloque de pesaje conectado: muestra en tiempo real el peso de la báscula seleccionada (ej. báscula de plataforma o gancho) y botón `"CAPTURAR PESO EN VIVO"`.
     * Campo para ingresar el lote, frigorífico/proveedor y notas.
     * Botón primario `"REGISTRAR ENTRADA A STOCK"`.
  3. **Botón Especial Despiece**: Botón secundario `"CALCULAR DESPIECE DE CANAL"` que abre la calculadora de rendimiento.
  4. **Historial de Entradas Recientes**: Lista scrollable con las últimas recepciones registradas (fecha, producto, kilos ingresados, costo por kilo y proveedor).
* **Origen de Datos**: `viewModel.allProducts`, `viewModel.scaleState` y tabla Room `stock_entries`.
* **Modales Asociados**:
  * `CameraBarcodeScannerDialog.kt`: Ventana modal que activa la cámara trasera con visor cuadrado y detección de códigos EAN-13 cárnicos mediante Google ML Kit.
  * `QuickProductCreationDialog.kt`: Creación al vuelo de un producto no registrado en catálogo.
  * `DespiecePiernaDialog.kt`: Modal de control de despiece.

---

### Pantalla 4: Despiece y Control de Mermas de Canal
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/inventory/DespiecePiernaScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/inventory/DespiecePiernaScreen.kt)
* **Acceso**: Desde el botón "Despiece" en Recepción de Mercancía o Inventario.
* **Estructura Visual**:
  1. **Cabecera del Despiece**: Selector de tipo de canal (Pierna de Cerdo, Canal de Res, Cuarto Trasero, etc.).
  2. **Tarjeta de Peso Inicial Bruto**: Muestra el peso de la pieza completa al entrar al cuarto frío (ej. `48.500 kg`) con opción de captura directa desde báscula.
  3. **Lista de Sub-Cortes Obtenidos**: Tabla o lista con los cortes resultantes del despiece (ej. *Pulpa Negra, Aguayón, Hueso con Tuétano, Grasa/Sebo, Merma Inevitable*). Para cada corte se captura el peso obtenido.
  4. **Panel de Rendimiento Matemático**:
     * Sumatoria de kilos despiezados.
     * Kilos de merma neta (Diferencia = `Peso Inicial - Suma de Cortes`).
     * Porcentaje de merma real vs. merma esperada.
     * Barra de progreso `LinearProgressIndicator` con código de colores (verde si está dentro de la tolerancia, rojo `#B71C1C` si la merma supera el estándar).
  5. **Botones Inferiores**: `"REINICIAR DESPIECE"` y `"GUARDAR Y ACTUALIZAR STOCKS"`.
* **Origen de Datos**: `viewModel.despieceState` (Room SQLite, tablas `despiece_processes` y `despiece_cuts`).
* **Debilidad visual observada**: Esta pantalla aún conserva estilos de Material 3 con colores hardcodeados (`Color(0xFFB71C1C)`) y tarjetas `CardDefaults.cardColors` en lugar del componente `BentoCard`.

---

### Pantalla 5: Inversión Propia del Encargado (Botanas y Embolsados)
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/manager/ManagerInvestmentScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/manager/ManagerInvestmentScreen.kt)
* **Acceso**: Menú lateral $\rightarrow$ "Mi Inversión Local".
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("TIENDA LOCAL · EMBOLSADO Y BOTANAS")`, título `"Mi Inversión Local"`.
  2. **Tarjetas de Resumen Ejecutivo (2 Celdas Bento Paralelas)**:
     * Celda Izquierda: Total de bolsas/piezas por elaborar y valor de costo proyectado.
     * Celda Derecha: Total de bolsas/piezas en existencia y valor de venta proyectado (`BigMetric`).
  3. **Barra de Herramientas**: Campo de búsqueda de botanas, selector de filtro por tipo de inversión (`Todas` u nombres específicos) y conmutador `NoirSegmented` para alternar entre **Modo Tarjetas** y **Modo Tabla**.
  4. **Área Principal**:
     * En *Modo Tabla*: Renderiza una tabla compacta con columnas: Código, Producto, Piezas/Bolsa, Precio Venta, Stock Bolsas, Stock Piezas, Valor Total e Iconos de acción.
     * En *Modo Tarjetas*: Muestra tarjetas Bento individuales con botones `＋` y `－` para incrementar o decrementar bolsas en existencia de forma inmediata.
  5. **FAB Inferior**: Botón crema con etiqueta `"NUEVA INVERSIÓN"`.
* **Origen de Datos**: `viewModel.localProducts` (tabla Room `local_products`).
* **Acciones del Usuario**: Registrar nuevos productos embolsados (cacahuates, chicharrones, salsas, condimentos), ajustar existencias rápidas tocando `＋`/`－` y compartir el reporte de inversión por WhatsApp en formato de texto estructurado.
* **Modales**: Diálogo de alta/edición de producto local (nombre, piezas por bolsa, precio sugerido, existencias iniciales).

---

### Pantalla 6: Pedidos de Surtido de Tienda
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/orders/OrdersScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/orders/OrdersScreen.kt)
* **Acceso**: Menú lateral $\rightarrow$ "Pedidos de Surtido".
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("SUCURSAL MOSTRADOR · PEDIDO AL RASTRO")`, título `"Surtido de Carne"`.
  2. **Tarjeta Hero Bento**: Volumen total calculado en kilos para surtir, importe total proyectado de compra y botón para agregar corte al pedido manual.
  3. **Lista de Sugerencias Automáticas (`LazyColumn`)**: Muestra los productos cuyo stock actual está por debajo del mínimo, calculando el faltante en kilos y redondeando a cajas o piezas enteras.
  4. **Botonera Inferior**:
     * Botón secundario `"EXPORTAR PDF"` (genera documento formal del pedido de la tienda).
     * Botón primario `"ENVIAR PEDIDO POR WHATSAPP"` (abre WhatsApp con el texto desglosado listo para enviar al dueño o frigorífico).
* **Origen de Datos**: `viewModel.pendingOrders` y `viewModel.allProducts`.
* **Modal Asociado**: `ManualOrderDialog.kt` para agregar ítems no calculados automáticamente.

---

### Pantalla 7: Login, Perfil e Ingreso de PIN
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/auth/SecurityVaultScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/auth/SecurityVaultScreen.kt)
* **Acceso**: Al arrancar la aplicación si `uiState.isLoggedIn == false`.
* **Estructura Visual**:
  1. **Logo y Encabezado Superior**: Escudo oficial `sglogo.png`, `NoirLabel("SISTEMA DE SEGURIDAD INDUSTRIAL")`, título `"Bóveda de Acceso"`.
  2. **Selector de Perfil**: Conmutador segmentado para elegir entre:
     * **`ENCARGADO`**: Muestra dropdown para seleccionar la sucursal activa asignada a la tablet.
     * **`DUEÑO / ADMIN`**: Acceso con credenciales maestras.
  3. **Selector de Modo de Autenticación**: `NoirSegmented` para alternar entre **PIN de 4 Dígitos** o **Clave Alfanumérica**.
  4. **Display de PIN (`NoirPinDots`)**: Cuatro círculos que se rellenan al teclear. Si el PIN es incorrecto, los círculos parpadean en rojo (`#E07A6F`) y se muestra el mensaje de error.
  5. **Teclado Táctil (`NoirKeypad`)**: Rejilla de 12 botones grandes (1 al 9, retroceso, 0 y confirmación) con respuesta visual táctil.
* **Origen de Datos**: `viewModel.uiState.adminPin` y `BranchEntity.pin`. Fallback por defecto: `"1234"` si no hay PIN configurado.
* **Estados**:
  * *PIN correcto*: Transición inmediata a la pantalla correspondiente al rol.
  * *PIN incorrecto*: Círculos en rojo, vibración táctil y reseteo automático del input tras 4 dígitos.

---

### Pantalla 8: Dashboard Ejecutivo del Propietario (Exclusivo App Dueño)
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/dashboard/ExecutiveOwnerDashboardScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/dashboard/ExecutiveOwnerDashboardScreen.kt)
* **Acceso**: Pantalla principal en la variante `:app (dueno)`.
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("CONSOLA DE DIRECCIÓN GENERAL")`, título `"Panel Maestro"`.
  2. **Hero Bento Card (Volumen e Importe Maestro)**:
     * Total consolidado de kilos requeridos por todas las sucursales (ej. `228.5 kg` en `metricXL`).
     * Importe financiero estimado de compra al rastro en pesos mexicanos (`$48,500.00 MXN`).
     * Badge de estatus (`StatusPill` indicando el número de tiendas con pedidos pendientes).
     * Al pulsar la tarjeta navega directamente a la pantalla de Unificación Maestra.
  3. **Sección de Accesos Rápidos (Grid de 3 Tarjetas Bento)**:
     * *Unificación*: Icono `MergeType`, detalle de pedidos consolidados.
     * *Sucursales*: Icono `Business`, contador de tiendas vinculadas (ej. `3 activas`).
     * *Proveedores*: Icono `LocalShipping`, catálogo de rastros y frigoríficos.
  4. **Resumen de Mermas de la Semana**: Indicador de porcentaje global de merma registrado en tiendas.
* **Origen de Datos**: `viewModel.allBranchOrders`, `viewModel.allBranches`, `viewModel.allSuppliers`.

---

### Pantalla 9: Unificación Maestra de Pedidos Multi-Tienda (Exclusivo Dueño)
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/MasterOrderUnificationScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/MasterOrderUnificationScreen.kt)
* **Acceso**: Menú lateral de la app Dueño $\rightarrow$ "Unificación de Pedidos".
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("CONSOLIDACIÓN MULTI-SUCURSAL")`, título `"Pedido Maestro al Rastro"`.
  2. **Selector de Agrupación**: Filtro por frigorífico o rastro destinatario.
  3. **Tabla de Unificación**: Agrupa matemáticamente los pedidos de todas las sucursales por corte cárnico:
     * Columna 1: Corte (ej. *Costilla de Cerdo*).
     * Columna 2: Desglose por tienda (ej. *Matriz: 50 kg, Norte: 30 kg, Mercado: 25 kg*).
     * Columna 3: Kilos totales solicitados (`105 kg`).
     * Columna 4: Redondeo a cajas cerradas (ej. *4 cajas de 25 kg = 100 kg o 5 cajas = 125 kg*).
     * Columna 5: Importe proyectado.
  4. **Botonera Flotante**:
     * `"GENERAR PDF MAESTRO"`: Emite orden de compra formal con membrete SGNIA.
     * `"WHATSAPP AL RASTRO"`: Envía el desglose consolidado al proveedor con 1 toque.
* **Origen de Datos**: `viewModel.allBranchOrders` (Room DB, tabla `branch_orders`).

---

### Pantalla 10: Directorio de Sucursales y Licenciamiento de Tiendas (Exclusivo Dueño)
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/AdminBranchesScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/AdminBranchesScreen.kt)
* **Acceso**: Menú lateral Dueño $\rightarrow$ "Sucursales".
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("RED DE TIENDAS Y PUNTOS DE VENTA")`, título `"Sucursales"`.
  2. **Tarjeta de Cupo de Licencia**: Muestra el total de sucursales activas frente al límite contratado (ej. `3 de 3 permitidas` en `BigMetric`). Si se alcanza el límite, muestra badge ámbar indicando cotización de sucursal adicional.
  3. **Lista de Sucursales (`LazyColumn`)**: Tarjeta Bento para cada tienda con:
     * Nombre de la sucursal (ej. *Sucursal Centro*).
     * Encargado responsable.
     * Teléfono de contacto.
     * PIN de 4 dígitos asignado a esa terminal.
     * Botón para copiar código de vinculación o compartir credencial.
  4. **FAB**: `"＋ AGREGAR SUCURSAL"`.
* **Modales**: Diálogo para dar de alta sucursal (nombre, encargado, teléfono y PIN). Si se excede el límite de la licencia, se dispara un `AlertDialog` informativo invitando a contactar a SGNIA COMPANY.

---

### Pantalla 11: Directorio de Proveedores y Frigoríficos (Exclusivo Dueño)
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/AdminSuppliersScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/AdminSuppliersScreen.kt)
* **Acceso**: Menú lateral Dueño $\rightarrow$ "Proveedores".
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("CADENA DE SUMINISTRO")`, título `"Proveedores y Rastros"`.
  2. **Lista de Proveedores**: Tarjetas Bento con nombre del frigorífico, contacto de ventas, categoría animal que surte (Res, Cerdo, Pollo, Mixto), días de entrega y botón directo para llamar o abrir chat de WhatsApp.
  3. **FAB**: `"＋ NUEVO PROVEEDOR"`.
* **Origen de Datos**: `viewModel.allSuppliers` (tabla Room `suppliers`).

---

### Pantalla 12: Seguridad y Cambio de PIN Maestro (Exclusivo Dueño)
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/AdminSecurityScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/admin/AdminSecurityScreen.kt)
* **Acceso**: Menú lateral Dueño $\rightarrow$ "Seguridad".
* **Estructura Visual**:
  1. **Encabezado**: `NoirLabel("CREDENCIALES MAESTRAS")`, título `"PIN de Administrador"`.
  2. **Formulario de Cambio**: Pide PIN actual, nuevo PIN de 4 dígitos y confirmación del nuevo PIN mediante el componente `NoirKeypad`.
  3. **Bóveda de Estado de Protección**: Muestra checklist con los escudos activos del sistema: Enmascaramiento XOR, Detección de Root, Bloqueo Anti-Frida y Anti-Rollback de Reloj.
* **Origen de Datos**: `viewModel.updateAdminPin(newPin)`.

---

### Pantalla 13: Ajustes Generales del Sistema
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/settings/SettingsScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/settings/SettingsScreen.kt)
* **Acceso**: Menú lateral $\rightarrow$ "Ajustes de Sistema".
* **Estructura Visual**:
  1. **Encabezado Cerhan**: Componente `SgniaCerhanGreetingHeader` con nombre de la tienda y versión.
  2. **Bloque de Configuración de Básculas**: Muestra la IP y puerto activos, estado del Foreground Service y botón `"CONFIGURAR RED DE BÁSCULAS"`.
  3. **Bloque de Apariencia y Tema**: Selector de tema visual (`NOIR_CREMA`, `OBSIDIAN_GOLD`, `CYBER_VIOLET`).
  4. **Bloque de Base de Datos y Catálogos**: Botón para importación masiva de catálogo vía JSON (`QendraCatalogImportDialog`) y reinicio controlado de tablas.
  5. **Enlaces Informativos**: Botones hacia "Licencia de Software" y "Acerca de SGNIA".
* **Debilidad visual**: Es una de las pantallas híbridas que mezcla componentes Cerhan (`SgniaCerhanNumberedCard`) con botones Material 3 estándar.

---

### Pantalla 14: Activación y Estado de Licencia Criptográfica
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/licensing/LicenseActivationScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/licensing/LicenseActivationScreen.kt)
* **Acceso**: Automático si la licencia expiró o no está activa, o vía Menú lateral $\rightarrow$ "Licencia".
* **Estructura Visual**:
  1. **Icono Superior y Escudo**: Icono de seguridad con borde crema y estado actual (`ACTIVA`, `PRUEBA`, `VENCIDA`, `RELOJ_ALTERADO`).
  2. **Caja de Hardware ID**: Muestra el identificador único del dispositivo (`SGN-XXXX-XXXX`) con botón de un toque para `"COPIAR HARDWARE ID"` y `"ENVIAR POR WHATSAPP A SGNIA"`.
  3. **Campo de Entrada de Clave**: `OutlinedTextField` para pegar la clave criptográfica HMAC (formato `SGN-XXXX-XXXX-XXXX-XXXX`).
  4. **Botón de Validación**: `"ACTIVAR LICENCIA"`. Al pulsar verifica la firma HMAC en menos de 10 ms de forma offline.
  5. **Detalle de Vigencia**: Si está activa, muestra días restantes, modalidad (Renta Mensual, Anual o Vitalicia) y fecha de expiración.

---

### Pantalla 15: Pantalla de Bloqueo de Seguridad (Anti-Tamper / Anti-Hooking)
* **Archivo**: Declarada inline en [`MainNavigation.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/MainNavigation.kt)
* **Acceso**: Automático al arrancar si `AppSecurityGuard.isDebuggerOrHookActive() == true`.
* **Estructura Visual**:
  * Pantalla completa en color negro absoluto (`NoirColors.Background #0F0E0D`).
  * Icono de escudo de seguridad en color carmesí de alerta (`NoirColors.Danger #E07A6F`).
  * Título en mayúsculas: `"ENTORNO NO SEGURO DETECTADO"`.
  * Mensaje explicativo: *"Se ha detectado un depurador o inyector de memoria activo. Por seguridad del inventario y las ventas, la app ha suspendido el acceso."*
  * **Sin botones de salida ni navegación**: La app queda completamente bloqueada hasta que se cierre el depurador o se desinstale el framework de instrumentación.

---

### Pantalla 16: Términos Legales y Consentimiento de Uso
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/legal/LegalAcceptanceScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/legal/LegalAcceptanceScreen.kt)
* **Acceso**: Primera apertura de la app tras instalación limpia.
* **Estructura Visual**:
  1. Encabezado con logotipo SGNIA y título `"Términos de Servicio y Operación Offline"`.
  2. Contenedor scrollable con el texto legal de responsabilidad sobre pesaje, mermas y exclusión de telemetría de terceros.
  3. Checkbox interactivo: *"He leído y acepto los términos de uso del software SGNIA"*.
  4. Botón primario `"CONTINUAR"`, habilitado únicamente tras marcar el checkbox. Persiste el consentimiento en Jetpack DataStore.

---

### Pantalla 17: Acerca de SGNIA y Créditos Oficiales
* **Archivo**: [`app/src/main/java/com/example/inventariocarniceria/ui/screens/about/AboutSgniaScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/about/AboutSgniaScreen.kt)
* **Acceso**: Desde Ajustes $\rightarrow$ "Acerca de SGNIA".
* **Estructura Visual**:
  * Logotipo SGNIA centrado con versión compilada (`v1.0.0`).
  * Lema institucional: *“Software que Protege y Hace Crecer tu Negocio”*.
  * Ficha del desarrollador: Gamaliel Velázquez Galicia.
  * Tarjetas táctiles con enlaces directos (`Intent.ACTION_VIEW`):
    * Abrir WhatsApp oficial (+52 276 109 5688).
    * Abrir portal web (`https://sgnia.xyz`).
    * Enviar correo electrónico (`gmlvega0025@gmail.com`).
  * Nota de derechos reservados © 2026 SGNIA COMPANY.

---

# PARTE 3: ARQUITECTURA DE NAVEGACIÓN

### 3.1 Mapa de Navegación por Perfil

```
                               ┌─────────────────────────┐
                               │       SPLASH SCREEN     │
                               └────────────┬────────────┘
                                            │
                       ┌────────────────────┴────────────────────┐
                       ▼                                         ▼
            [ ENTORNO VULNERADO ]                      [ DISPOSITIVO SEGURO ]
            Lockdown Screen (Bloqueo)                            │
                                                                 ▼
                                                       [ TÉRMINOS LEGALES ]
                                                    (Solo primera instalación)
                                                                 │
                                                                 ▼
                                                       [ LICENCIA DE SOFTWARE ]
                                                      (Si está vencida o inactiva)
                                                                 │
                                                                 ▼
                                                       [ SECURITY VAULT LOGIN ]
                                                      Selección de Rol y PIN
                                                                 │
                                ┌────────────────────────────────┴────────────────────────────────┐
                                ▼                                                                 ▼
                   ROL: ENCARGADO (APP TIENDA)                                       ROL: DUEÑO (APP CONSOLA)
                   Paquete: .inventariocarniceria.pos                                Paquete: .inventariocarniceria.admin
                   Drawer Lateral con 8 secciones:                                   Drawer Lateral con 7 secciones:
                   ├── 1. Básculas (Principal) ──► IndustrialScaleVisorScreen        ├── 1. Panel Maestro ───────► ExecutiveOwnerDashboardScreen
                   │      ├── Modal Agregar Báscula                                  ├── 2. Unificación ─────────► MasterOrderUnificationScreen
                   │      ├── Modal Keypad Manual                                    │      ├── Generar PDF Orden Rastro
                   │      └── Modal Escáner Cámara                                   │      └── Enviar Pedido a WhatsApp
                   ├── 2. Catálogo de Cortes ────► AdminCatalogScreen                ├── 3. Sucursales ──────────► AdminBranchesScreen
                   │      └── Modal ProductEditDialog                                │      └── Modal Agregar/Editar Tienda
                   ├── 3. Recepción Mercancía ───► StockReceptionScreen              ├── 4. Proveedores ─────────► AdminSuppliersScreen
                   │      ├── Modal DespiecePiernaDialog                             │      └── Modal Nuevo Frigorífico
                   │      └── Modal Escáner ML Kit                                   ├── 5. Catálogo Maestro ────► AdminCatalogScreen
                   ├── 4. Pedidos de Surtido ────► OrdersScreen                      ├── 6. Seguridad ───────────► AdminSecurityScreen
                   │      ├── Modal ManualOrderDialog                                │      └── Cambio de PIN Administrador
                   │      └── Exportar PDF / WhatsApp                                └── 7. Ajustes ─────────────► SettingsScreen
                   ├── 5. Mi Inversión Local ────► ManagerInvestmentScreen
                   │      └── Modal Alta Producto Embolsado
                   ├── 6. Ajustes de Sistema ────► SettingsScreen
                   ├── 7. Licencia de Software ──► LicenseActivationScreen
                   └── 8. Acerca de SGNIA ───────► AboutSgniaScreen
```

### 3.2 Diferencias Visuales y Funcionales entre Consola Dueño y Panel Tienda

| Aspecto | SGNIA Carnicería POS (Tienda) | SGNIA Consola Dueño (Propietario) |
| :--- | :--- | :--- |
| **Identificador de Paquete** | `com.example.inventariocarniceria.pos` | `com.example.inventariocarniceria.admin` |
| **Pantalla de Inicio** | **Visor Multiescala Dinámico** (prioridad inmediata al plato de pesaje y mostrador). | **Panel Maestro Ejecutivo** (prioridad al volumen consolidado y costo financiero total). |
| **Hardware de Básculas** | Control directo de sockets TCP/IP en vivo con tara, estabilidad y lectura de peso continuo. | Vista de supervisión; no requiere estar conectado por cable a las balanzas físicas. |
| **Gestión Multi-Tienda** | Solo visualiza la información y el inventario de su propia sucursal. | Visualiza y unifica los pedidos de todas las sucursales vinculadas a su cuenta. |
| **Control de Precios** | Consulta de precios y auditoría de cambios. | Edición maestra de precios y márgenes de ganancia. |
| **Inversión Propia** | Presente (`ManagerInvestmentScreen`) para botanas y embolsados del encargado. | No aplicable; el dueño supervisa el negocio central. |

---

# PARTE 4: ANÁLISIS DE USO REAL EN ENTORNO INDUSTRIAL

### 4.1 Legibilidad en Poca Luz y Cuartos Fríos
* **Fondo Negro Carbón (`#0F0E0D`)**: Erradica el encandilamiento en cámaras de refrigeración oscuras y reduce drásticamente el consumo de batería en pantallas OLED/AMOLED de tablets industriales.
* **Tipografía Monospace Gigante (56 sp)**: Las cifras de peso (`0.000 kg`) y totales de compra se leen claramente a una distancia de **3 a 4 metros**, permitiendo al carnicero observar el visor mientras maniobra piezas en el tajo o mesa de despiece.
* **Doble Codificación Semántica**: La estabilidad no depende solo del color. Además de cambiar de ámbar (`#D9A441`) a verde (`#4ADE80`), el sistema altera la etiqueta de texto explícita de `[EN MOVIMIENTO]` a `[ESTABLE]`, evitando confusiones en operadores con daltonismo o fatiga visual.

### 4.2 Ergonomía con Manos Húmedas y Guantes
* **Target Táctil Mínimo (`NoirSpace.touch = 64 dp`)**: Todos los botones de confirmación, cambio de tara, captura de pesaje y dígitos del teclado numérico superan con creces los 48 dp recomendados por Material Design. Esto permite pulsaciones con guantes térmicos o protectores de malla metálica.
* **Separación de Botones Críticos**: Los botones de `"TARA"` y `"PESAR Y CAPTURAR"` están separados por un gutter de 12 dp con colores de alto contraste para evitar pulsaciones accidentales de tara cuando se intentaba capturar un corte.

### 4.3 Comportamiento en Teléfono vs. Tableta
* **En Tableta (8" a 10" horizontal - Modo Mostrador)**:
  * El visor multiescala organiza las balanzas en un grid de tarjetas Bento paralelas de dos o tres columnas.
  * El cajero puede ver simultáneamente la báscula de mostrador 1 y la báscula de porcionado 2 sin cambiar de pantalla.
* **En Teléfono Inteligente (Modo Vertical - Dueño o Encargado en Piso)**:
  * Las tarjetas Bento se apilan verticalmente con scroll fluido (`LazyColumn`).
  * Los menús se adaptan al ancho completo sin desbordamientos de texto gracias al uso de `TextOverflow.Ellipsis`.

---

# PARTE 5: AUTOEVALUACIÓN HONESTA PARA EL DISEÑADOR EXTERNO

### 5.1 Las 5 Pantallas Mejor Logradas (Fortalezas Visuales)
1. **`IndustrialScaleVisorScreen.kt`**: La pantalla más sólida del sistema. Implementa al 100% los tokens Noir & Crema, tarjeta Hero adaptativa, tipografía de precisión (56 sp monospace), botones de 64 dp y telemetría de red integrada.
2. **`SecurityVaultScreen.kt` (Login)**: Excelente atmósfera industrial. El conmutador segmentado, los `NoirPinDots` reactivos y el keypad numérico de 64 dp crean una experiencia de instrumental militar muy profesional.
3. **`ExecutiveOwnerDashboardScreen.kt`**: Jerarquía de información impecable. La tarjeta Hero en crema marfil con el volumen maestro consolidado destaca inmediatamente la métrica más valiosa para el dueño.
4. **`ManagerInvestmentScreen.kt`**: Muy versátil. Resuelve el dolor real del encargado de tienda con alternancia instantánea entre vista de tabla compacta y tarjetas de ajuste rápido de existencias (`＋`/`－`).
5. **`MasterOrderUnificationScreen.kt`**: Solución matemática limpia con presentación clara del desglose multi-sucursal y redondeo a cajas cerradas para proveedores cárnicos.

---

### 5.2 Las 5 Pantallas Más Débiles o Inconsistentes (Deuda Visual Identificada)
1. **`InventoryScreen.kt` (Pantalla de Inventario Antigua)**:
   * **Problema**: Es una pantalla de la Fase 1 que no fue migrada al sistema Noir. Utiliza componentes estándar de Material 3, colores quemados en código como verde bosque (`#2E7D32`) y rojo oscuro (`#B71C1C`), y tarjetas `CardDefaults.cardColors` en lugar de `BentoCard`.
2. **`DespiecePiernaScreen.kt` (Calculadora de Rendimiento)**:
   * **Problema**: Diseñada como un modal de Material 2/3 estándar. Tiene tipografía genérica, la barra de progreso de merma no utiliza los tokens de `NoirTokens.kt` y los campos de entrada de kilos tienen paddings inconsistentes.
3. **`SettingsScreen.kt` (Ajustes de Sistema)**:
   * **Problema**: Es un híbrido que combina componentes de la plantilla Cerhan (`SgniaCerhanNumberedCard`) con botones outlined comunes de Android, rompiendo la coherencia con el visor de básculas.
4. **`AboutSgniaScreen.kt` (Acerca de SGNIA)**:
   * **Problema**: Lista vertical simple de tarjetas clicables. No aprovecha la cuadrícula Bento ni la riqueza estética del logotipo institucional.
5. **`LicenseActivationScreen.kt`**:
   * **Problema**: Aunque es funcional y limpia, el campo de texto donde se introduce la clave criptográfica es un `OutlinedTextField` genérico sin el estilo industrial oscuro de la app.

---

### 5.3 Elementos que Rompen la Paleta Noir & Crema
* **Colores quemados en pantallas legadas**:
  * `Color(0xFF2E7D32)` (Verde bosque) en `InventoryScreen.kt`.
  * `Color(0xFFB71C1C)` (Rojo granate) en `InventoryScreen.kt` y `DespiecePiernaScreen.kt`.
  * `Color(0xFF26A69A)` (Teal) en `SgniaTheme.kt`.
* **Archivo huérfano `theme/Color.kt`**: Contiene paleta morada de Compose (`Purple80`, `Purple40`) que no tiene relación con la identidad de la marca.

---

### 5.4 Inconsistencias de Espaciado y Tipografía
* Mientras que las pantallas rediseñadas usan `NoirSpace.screen = 20.dp` y `NoirSpace.gutter = 12.dp`, pantallas secundarias usan `padding(16.dp)` o `padding(horizontal = 16.dp, vertical = 8.dp)`.
* En algunas pantallas se usa `MaterialTheme.typography.headlineMedium` con fuentes proporcionales del sistema, mientras que las pantallas principales usan estrictamente `NoirType.metricXL` con tipografías monospace.

---

### 5.5 Qué Debería Cambiarse Primero (Plan de 1 Hora de Refactorización)
1. **Migrar `InventoryScreen.kt` a `BentoCard` y `NoirColors`**: Reemplazar los colores hardcodeados `#2E7D32` y `#B71C1C` por `NoirColors.Emerald` y `NoirColors.Danger`, adoptando las tarjetas Bento.
2. **Eliminar el archivo huérfano `theme/Color.kt`**: Evitar confusiones en desarrolladores externos limpiando las declaraciones de púrpura/rosa del template.
3. **Unificar los modales de Diálogo**: Asegurar que `DespiecePiernaDialog.kt` y `ProductEditDialog.kt` utilicen fondo `NoirColors.SurfaceRaised (#1F1D1A)`, esquinas `NoirRadius.lg (24 dp)` y botones `NoirPrimaryButton`.
4. **Homogeneizar el padding de contenedor raíz**: Estandarizar todas las vistas en `Modifier.padding(horizontal = NoirSpace.screen, vertical = NoirSpace.sm)`.

---
*Fin del Documento de Especificación de UI · SGNIA COMPANY © 2026*
