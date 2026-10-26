# Bitácora de Cambios (CHANGELOG)

Todas las modificaciones notables realizadas al proyecto **SGNIA Carnicería & Balanza Industrial POS** se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) y este proyecto sigue el versionado semántico formal.

---

## [1.0.3] - 2026-10-25

### Añadido
- **Auto-Llenado de Peso de Caja por Código de Barras (`StockReceptionScreen.kt` & `MeatBarcodeParser.kt`)**:
  - Detección automática de peso neto directo en etiquetas de cajas empaquetadas (ejemplo: cajas de Kekén / Grupo Porcícola Mexicano con código `22.30` o `22,30`).
  - Al escanear el código de barras con la pistola o con la cámara del dispositivo, el campo **Kilos a Recibir (kg)** se auto-completa instantáneamente con el peso exacto (`22.300 kg`).
  - Soporte inteligente para códigos de barras de 4 dígitos (ej. `2230` -> `22.30 kg`) y formatos con sufijo de unidad (`22.30kg`).
  - Botón de acceso directo a escaneo de cámara integrado en el campo de texto de kilos.
  - El campo de peso permanece **100% manual y editable** en cualquier momento si el carnicero necesita ajustarlo o corregirlo.
  - Se mantiene activa la opción de capturar el peso en vivo desde la **Báscula** física mediante el botón dedicado.

---

## [1.0.2] - 2026-10-25

### Añadido
- **Módulo de Inversión de Venta / Bajas de Tickets (`SalesReversalScreen.kt`)**:
  - Búsqueda y filtrado instantáneo por **Número de Operación** (ej. `#OP-10024`) o **Código de Barras** (etiqueta EAN-13 o ticket de báscula).
  - Escáner de cámara integrado (`CameraBarcodeScannerDialog`) para lectura directa del código de barras de tickets impresos o cajas.
  - Cancelación e inversión de ventas con diálogo de confirmación y selección de motivo de reembolso/baja (*equivocación en cobro, reembolso solicitado por cliente, producto devuelto*).
  - **Reingreso automático de mercancía**: Al invertir la venta, los kilogramos exactos se reincorporan automáticamente al inventario físico mediante un registro de `StockEntryEntity`.
  - Nueva tabla en base de datos Room (`SaleTicketEntity` y `SaleTicketDao`) con migración automática `MIGRATION_11_12`.
  - Acceso directo desde el menú lateral Cerhan de la app Tienda (`Invertir Venta / Bajas`).
- **Visualización en Vivo de Producto y Precio por PLU en Báscula (`IndustrialScaleVisorScreen.kt`)**:
  - Reconocimiento y despliegue prominente del corte en venta (ej. **Menudo de Res**, PLU #1) directamente sobre la tarjeta de báscula al recibir el pesaje o código PLU.
  - Cálculo e indicador en tiempo real de **Importe de Venta** (`peso × precio/kg`) junto al indicador digital de peso y estabilidad.
  - Resolución automática de cortes en catálogo para Báscula 1 y Báscula 2 simultáneamente.
  - Auto-registro de Menudo de Res (PLU #1) para pruebas operativas inmediatas en mostrador.

---

## [1.0.1] - 2026-10-25

### Seguridad
- **Aislamiento del Generador de Licencias**: Módulo `keygen/` extraído del repositorio público e incorporado a `.gitignore` y configuración condicional en `settings.gradle.kts`.
- **Throttling y Bloqueo Anti-Fuerza Bruta en PIN**: Bloqueo temporal restrictivo de 30 segundos tras 3 intentos erróneos consecutivos con contador regresivo en `SecurityVaultScreen.kt`.
- **Política de PIN Obligatorio**: Forzado de cambio obligatorio de PIN inicial al detectar credenciales de fábrica.
- **Ofuscación de Claves Operativas**: Ofuscación por defecto de PINs de sucursales en `AdminBranchesScreen.kt` con selector visual de revelación/ocultamiento.
- **Sanitización Pública**: Eliminación de referencias a vectores de ataque en `README.md` y creación de `docs/SEGURIDAD_INTERNA_PRIVADA.md` con propuesta formal de migración a Ed25519.

### Corregido
- **Persistencia Permanente de PIN en Disco**: Se solucionó el problema donde el PIN de Dueño y de Sucursales solo residía en memoria volátil y se reseteaba a "1234" al cerrar las aplicaciones. Se integró almacenamiento persistente síncrono en disco (`sgnia_security_prefs` mediante `.commit()`) y sincronización con SQLite en Room.
- **Unificación de Entrada a 4 Dígitos Exactos**: Se eliminó la discrepancia donde el modal permitía de 4 a 8 dígitos mientras que el teclado táctil de la pantalla de acceso operaba con 4 círculos. Ahora la política exige exactamente 4 dígitos numéricos, garantizando acceso directo.
- **Eliminación del Bucle de Cambio Obligatorio**: Se ajustó la validación condicional para que, al registrar el nuevo PIN seguro, el valor inicial "1234" quede anulado inmediatamente y no vuelva a solicitar el cambio de clave en cada inicio de sesión.

### Cambiado
- **Estandarización de Campos de Texto**: Implementación unificada de `sgniaTextFieldColors()` (fondo `SurfaceSunken`, borde `Hairline`, foco `Cream`) en búsqueda de Inversión, clave de licencia y diálogo Qendra.
- **Retiro Definitivo de Temas Legados**: Eliminación formal de `OBSIDIAN_GOLD`, `CYBER_VIOLET` y el archivo huérfano `Color.kt`. La aplicación opera exclusivamente con `Noir & Crema`.
- **Ergonomía Táctil y Tipografía**: Botones secundarios (compartir, limpiar, cargar ejemplo, chips de báscula) elevados a una altura táctil mínima de 48 dp. Subtítulos y descripciones de 11 sp elevados a 13 sp mínimo según norma de legibilidad industrial.

---

## [1.0.0] - 2026-10-24

### Añadido
- **Blindaje de Seguridad Multicapa (`AppSecurityGuard.kt` y `KeygenSecurityGuard.kt`)**:
  - Enmascaramiento XOR (`OBF_SEED ^ MASK`) en memoria de las semillas criptográficas y firmas HMAC para mitigar la extracción estática mediante JADX o Ghidra.
  - Detección activa de root (binarios `su`, `busybox`, `magisk`, `daemonsu`, flags `test-keys` en el build del sistema).
  - Detección en tiempo real de depuradores adjuntos (`Debug.isDebuggerConnected()`) y hooks de instrumentación dinámica (puerto 27042 de Frida y paquetes Xposed).
  - Bloqueo y pantalla de advertencia ante alteraciones de seguridad en el dispositivo.
  - Protección anti-retraso de reloj (*Anti-Clock Rollback*) mediante el seguimiento de `SystemClock.elapsedRealtime()` y marcas de tiempo persistentes.
- **Motor Multiescala TCP/IP (`TcpScaleManager.kt`)**:
  - Soporte de conexión simultánea a múltiples básculas mediante `ConcurrentHashMap` y corrutinas dedicadas en `Dispatchers.IO`.
  - Implementación del protocolo binario Systel Cuora Max y Cuora 2 (Funciones 1, 2, 11, 23, 31, 62 y 3) con cálculo y validación de sumas de verificación XOR.
  - Decodificación de tramas de peso, estado de estabilidad (`'e'` estable, `'i'` inestable), tara y errores operativos de la balanza (`PE1` a `PE5`).
  - Reconexión automática no bloqueante ante pérdida de enlace en red local.
  - Medición y telemetría de latencia (ping) en tiempo real con aislamiento de socket mono-conexión.
- **Lector de Códigos de Barras y QR (`CameraBarcodeScannerDialog.kt` y `MeatBarcodeParser.kt`)**:
  - Integración de CameraX y Google ML Kit para escaneo con la cámara del dispositivo.
  - Soporte para lectores ópticos externos (pistolas USB / Bluetooth) actuando como teclado físico en Android.
  - Decodificación de códigos EAN-13 y GS1 con peso o precio embebido para productos de carnicería de peso variable.
- **Módulo de Inversión Propia del Encargado (`ManagerInvestmentScreen.kt`)**:
  - Registro independiente de productos de embolsado, condimentos y botanas pertenecientes al encargado de mostrador.
  - Control de piezas por bolsa, precio por bolsa, existencias actuales, valor en piso y valor proyectado.
  - Exportación de resumen y pedidos por mensajería.
- **Teclado Numérico de Peso Manual**:
  - Modal táctil en el visor de básculas para captura manual de pesaje cuando el hardware se encuentra desconectado o en mantenimiento.
- **Auditoría Histórica de Precios (`PriceAuditEntity.kt`)**:
  - Registro en Room SQLite de cada modificación de precio por kilo, asociando código de producto, precio anterior, precio nuevo, fecha/hora, sucursal y encargado responsable.
- **Servicio en Segundo Plano (`ScaleBackgroundService.kt`)**:
  - Foreground Service con notificación persistente, `WifiLock` y `WakeLock` para evitar que Android interrumpa las conexiones de socket TCP en suspensión.
- **SGNIA Keygen Admin**:
  - Generador de claves criptográficas HMAC-SHA256 con duraciones configurables (`10M`, `07D`, `01M`, `01Y`, `LIFE`).
  - Activación automática de sucursales y consola del dueño en un solo paso al marcar un contrato como `PAGADO`.
  - Generación de comprobantes y pólizas en PDF.

### Cambiado
- **Refactorización del Sistema de Diseño (`NoirTokens.kt`)**:
  - Unificación de la paleta bajo el estándar Noir & Crema: Fondo `#0F0E0D`, Superficie `#181715`, Crema Marfil `#EEDDC8`, Verde Esmeralda `#4ADE80` (estabilidad), Oro `#C9A96E` / Ámbar `#D9A441` y Alerta `#E07A6F`.
  - Dimensiones ergonómicas para botones y celdas táctiles con uso de guantes en cuartos fríos (altura mínima de 64 dp).
- **Consolidación de Pedidos del Dueño (`MasterOrderUnificationScreen.kt`)**:
  - Agrupación por corte y redondeo configurable de faltantes a cajas cerradas.
- **Migración a Gradle 9.0.1 y Android SDK 36 (VanillaIceCream)**:
  - Compatibilidad completa con Kotlin 2.1.20 y Compose BOM 2026.03.01.

### Corregido
- Bloqueo de la interfaz de usuario cuando una báscula no respondía en la red local.
- Colisión de socket al realizar pruebas de ping sobre básculas con módulos de red de cliente único (mono-conexión).
- Cálculo de checksum XOR en paquetes de solicitud de peso extendido en balanzas Systel.
