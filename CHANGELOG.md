# Bitácora de Cambios (CHANGELOG)

Todas las modificaciones notables realizadas al proyecto **SGNIA Carnicería & Balanza Industrial POS** se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) y este proyecto sigue el versionado semántico formal.

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
