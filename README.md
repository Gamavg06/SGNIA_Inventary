<div align="center">

# 🥩 SGNIA CARNICERÍA & BALANZA INDUSTRIAL POS
### *Sistema Industrial de Pesaje en Vivo, Control de Canales, Punto de Venta y Despiece para Carnicerías*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-36-34A853?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Systel Cuora](https://img.shields.io/badge/Hardware-Systel%20Cuora%20TCP%2FIP-D9A441?style=for-the-badge&logo=scale&logoColor=white)](#-protocolo-industrial-systel-cuora-max)
[![Offline First](https://img.shields.io/badge/Architecture-Offline--First%20Room%20DB-00C853?style=for-the-badge&logo=sqlite&logoColor=white)](#-arquitectura-offline-first)
[![Design System](https://img.shields.io/badge/UI%2FUX-Noir%20%26%20Crema%20Bento-EEDDC8?style=for-the-badge&labelColor=0F0E0D&color=EEDDC8)](#-sistema-de-diseño-noir--crema)

<p align="center">
  <b>Desarrollado con orgullo por SGNIA COMPANY</b><br>
  <i>“Software que Protege y Hace Crecer tu Negocio”</i><br>
  <a href="https://sgnia.xyz"><b>🌐 sgnia.xyz</b></a> • 
  <a href="https://wa.me/522761095688"><b>📱 WhatsApp Directo</b></a>
</p>

---

</div>

## 📌 Resumen Ejecutivo del Sistema

**SGNIA Carnicería & Balanza Industrial POS** es una plataforma móvil nativa de alto rendimiento desarrollada para optimizar la operación de carnicerías, rastros, frigoríficos y puntos de venta cárnicos. Diseñada bajo la filosofía **Offline-First**, el sistema no depende de internet para operar: se conecta directamente por red local TCP/IP a básculas industriales **Systel Cuora Max**, escanea etiquetas de cajas pesadas, calcula rendimientos de canal y consolida pedidos multi-tienda con exportación a WhatsApp y reportes PDF VIP.

---

## 🎨 Sistema de Diseño: *Noir & Crema (Bento Grid Industrial)*

La interfaz visual fue concebida como un **instrumento de precisión de grado ejecutivo**, optimizada para ambientes industriales, cuartos fríos, poca iluminación y uso con manos húmedas o guantes de carnicero:

* **Fondo Principal**: Negro Carbón Mate Profundo (`#0F0E0D`).
* **Tarjetas Bento**: Carbón Superficie (`#181715`) con bordes finos de 1dp (`#2A2723` / `#3A352F`) y esquinas redondeadas ergonómicas (24dp / 18dp / 12dp).
* **Tipografía y Acentos**: Crema Marfil Cálido (`#EEDDC8` / `#F3EFE6`) para máxima legibilidad sin fatiga ocular.
* **Cifras Dominantes**: Números grandes en fuente Monospace (`56sp` y `36sp`) para leer kilos, importes y porcentajes de merma en menos de 2 segundos.
* **Verde Esmeralda (`#4ADE80`)**: Reservado estrictamente para peso estable en báscula y estados positivos.
* **Zonas Táctiles Ampliadas**: Botones con diana táctil mínima de $\ge$ 52dp y teclado numérico de 68dp.

```
┌─────────────────────────────────────────────────────────────┐
│ SGNIA · CONSOLA EJECUTIVA                   [v1.0.0]        │
├──────────────────────────────┬──────────────────────────────┤
│ 01. BÁSCULA ACTIVA           │ 02. INVERSIÓN PROYECTADA     │
│   24.580 KG                  │   $48,950.00 MXN             │
│   ● ESTABLE · MOSTRADOR      │   ● 8 CORTES UNIFICADOS      │
├──────────────────────────────┴──────────────────────────────┤
│ 03. UNIFICACIÓN MAESTRA MULTI-TIENDA                        │
│   [PLU 001] BISTEC DE RES     ▶ 145.20 kg   • Matriz + Sur  │
│   [PLU 015] PIERNA DE CERDO   ▶  80.00 kg   • 4 Cajas       │
├─────────────────────────────────────────────────────────────┤
│ [ 💬 ENVIAR WHATSAPP MAESTRO ]      [ 📄 EXPORTAR PDF VIP ] │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚖️ Protocolo Industrial Systel Cuora Max (TCP/IP)

El núcleo de comunicación (`TcpScaleManager.kt`) se comunica con módulos Lantronix y placas de red oficiales de **Systel Cuora Max V7.0 / V6.2 / V4.0**:

* **Lectura Continua de Peso y Tara (Función 1 - 0x01)**: Lectura continua sin bloqueo de UI con telemetría de peso bruto, tara, estado estable/lectura y bytes recibidos.
* **Firma Digital y Decimales (Función 2 - 0x02)**: Negociación binaria para detectar si la báscula opera con 0 o 2 decimales (`D0` vs `D2`).
* **Sectores y Familias (Función 11 - 0x0B)**: Extracción directa de departamentos (Res, Cerdo, Pollo, Embutidos) de la memoria de la balanza.
* **Extracción de Memoria PLU (Función 31 - 0x1F y Función 62 - 0x3E)**: Descarga automática de todos los códigos PLU, precios por kilo y nombres registrados físicamente en la báscula por red Wi-Fi/Ethernet.
* **Captura de Ticket / Venta (Función 72 - 0x48)**: Reconocimiento nativo de tramas delimitadas por circunflejo (`Ticket^Fecha^Hora^Vendedor^PLU^Sector^Gramos`).
* **Gestión Mono-Cliente Lantronix**: Pausa suave y reanudación automática de lectura para evitar saturar el hardware al descargar catálogos completos.

---

## 👥 Separación Estricta de Roles

| Módulo / Capacidad | Rol Dueño (`ADMIN`) | Rol Encargado (`ENCARGADO`) |
| :--- | :---: | :---: |
| **Visor de 2 Básculas en Tiempo Real** | ❌ (Solo supervisión) | ✅ **100% Pantalla Principal** |
| **Catálogo de Cortes (Precios/kg y PLUs)** | ❌ (Exclusivo de Tienda) | ✅ **Alta, Baja y Edición Local** |
| **Recepción de Mercancía y Despiece** | ❌ | ✅ **Cálculo de Mermas y Cajas** |
| **Pedidos de Tienda a Rastro** | ❌ | ✅ **Cálculo Cajas / Granel** |
| **Unificación Maestra de Pedidos Multi-Tienda** | ✅ **100% Exclusivo Dueño** | ❌ |
| **Directorio de Sucursales y PINs de Mostrador**| ✅ **100% Exclusivo Dueño** | ❌ |
| **Directorio de Proveedores y Frigoríficos** | ✅ **100% Exclusivo Dueño** | ❌ |
| **Acerca de SGNIA & Soporte Técnico** | ✅ | ✅ |

---

## 📱 Pantallas del Sistema

### 🔐 Acceso y Seguridad
1. **`SplashScreen.kt`**: Video loop institucional nativo (`logovideo.mp4`) con fallback gráfico de marca (`sglogo.png`).
2. **`LegalAcceptanceScreen.kt`**: Pantalla inicial obligatoria para primera apertura tras instalar. Bloquea el acceso hasta que el usuario lea y marque los Términos y el Aviso de Privacidad. Consentimiento almacenado en **Jetpack DataStore** para no volver a interrumpir la operación.
3. **`DocumentReaderScreen.kt`**: Lector de Términos y Condiciones / Aviso de Privacidad con secciones numeradas, placeholders legales y enlace web.
4. **`SecurityVaultScreen.kt`**: Login corporativo con teclado numérico ampliado (68dp), selector visual de rol e ingreso de PIN de 4 dígitos.

### 👔 Consola del Dueño (Admin)
5. **`ExecutiveOwnerDashboardScreen.kt`**: Métricas de red, volumen total pedido, costos estimados, distribución por tienda y accesos ejecutivos.
6. **`MasterOrderUnificationScreen.kt`**: Consolidación inteligente de todas las sucursales, desglose por corte y exportación con 1 toque a WhatsApp y PDF VIP.
7. **`AdminBranchesScreen.kt`**: Gestión de tiendas, encargados asignados, teléfonos de contacto y PIN de mostrador.
8. **`AdminSuppliersScreen.kt`**: Directorio de rastros, frigoríficos y categorías cárnicas suministradas.
9. **`AdminSecurityScreen.kt`**: Bóveda protegida para actualización del PIN maestro del Dueño.

### 🏪 Operación de Tienda (Encargado)
10. **`IndustrialScaleVisorScreen.kt`**: Visor industrial de **Báscula 1** y **Báscula 2**, tara automática, selector de corte, panel BottomSheet para ajustes rápidos de precio (`-$10`, `-$5`, `+$5`, `+$10`) e importe en tiempo real.
11. **`AdminCatalogScreen.kt`**: Catálogo local de cortes con buscador instantáneo, chips de categorías y productos derivados.
12. **`StockReceptionScreen.kt`**: Recepción con pesaje directo de báscula, escáner de códigos de barras por cámara (`CameraX` + `ML Kit`) y alta al vuelo de cortes nuevos.
13. **`OrdersScreen.kt`**: Cálculo de faltantes comparando existencias vs. stock máximo con redondeo automático hacia arriba en cajas cerradas (`ceil(shortage / pesoCaja)`).
14. **`ManagerInvestmentScreen.kt`**: Módulo independiente de inversión local (bolsitas, botanas, chicharrón, manteca) con vista conmutable de tabla o tarjetas.
15. **`SettingsScreen.kt`**: Configuración de IPs locales, puertos TCP de básculas y mantenimiento del sistema.
16. **`AboutSgniaScreen.kt`**: Identidad corporativa de **SGNIA COMPANY**, fundador Gamaliel Velazquez Galicia (`GV`), soporte técnico por WhatsApp directo, correo, Google Maps y enlace a `sgnia.xyz`.

---

## 🛠️ Tecnologías y Librerías

* **Lenguaje**: Kotlin 2.1.20
* **Framework UI**: Jetpack Compose con BOM oficial 2026 y Material 3
* **Persistencia Local**: Room SQLite con KSP (`androidx.room`)
* **Preferencias Seguras**: Jetpack DataStore Preferences (`androidx.datastore:datastore-preferences`)
* **Hardware de Red**: Sockets TCP/IP puros y protocolo binario Systel Cuora
* **Visión Artificial**: Android CameraX y Google ML Kit Barcode Scanning
* **Reportes Vectoriales**: Generador de PDF nativo Android (`PdfDocument`) y formateador de textos enriquecidos para WhatsApp

---

## 🧪 Pruebas Automatizadas y Calidad

El proyecto cuenta con una suite integral de **33 pruebas unitarias automatizadas** que certifican el protocolo binario de pesaje, la extracción de PLUs y el parseo de códigos de barras:

```powershell
# Ejecutar la suite completa de pruebas unitarias
.\gradlew.bat testDebugUnitTest

# Compilar y empaquetar el APK final de producción / depuración
.\gradlew.bat assembleDebug
```

> **Resultado**: `BUILD SUCCESSFUL` (33/33 pruebas superadas al 100%, 0 fallos).  
> **Ubicación del APK**: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🏢 Acerca de SGNIA COMPANY

```
  ____   ____ _   _ ___    _       ____ ___  __  __ ____   QUÉ PROTEGE Y HACE
 / ___| / ___| \ | |_ _|  / \     / ___/ _ \|  \/  |  _ \  CRECER TU NEGOCIO
 \___ \| |  _|  \| || |  / _ \   | |  | | | | |\/| | |_) | 
  ___) | |_| | |\  || | / ___ \  | |__| |_| | |  | |  __/  Software Industrial
 |____/ \____|_| \_|___/_/   \_\  \____\___/|_|  |_|_|     de Alto Rendimiento
```

* **Empresa**: **SGNIA COMPANY** (Software & Innovación Digital)
* **Fundador & Dirección**: **Gamaliel Velazquez Galicia**
* **Lema**: *“Software que Protege y Hace Crecer tu Negocio”*
* **Sitio Web Oficial**: [https://sgnia.xyz](https://sgnia.xyz)
* **Soporte Técnico WhatsApp**: [+52 276 109 5688](https://wa.me/522761095688)
* **Correo de Contacto**: `gmlvega0025@gmail.com`
* **Ubicación**: Juan Jiménez 405, Centro, 75120 Nopalucan de la Granja, Puebla, México.
* **Redes Sociales**: TikTok [@sgn_ia](https://www.tiktok.com/@sgn_ia) • GitHub [@Gamavg06](https://github.com/Gamavg06)

---

<div align="center">
  <sub>© 2026 SGNIA COMPANY. Todos los derechos reservados. Desarrollado con excelencia técnica para la industria cárnica.</sub>
</div>
