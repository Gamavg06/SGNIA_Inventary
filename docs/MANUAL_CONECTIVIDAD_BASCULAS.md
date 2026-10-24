# ⚖️ Manual Operativo: Conectividad y Configuración de Básculas TCP/IP

**SGNIA CARNICERÍA & BALANZA INDUSTRIAL POS**  
*Módulo de Pesaje en Vivo · Soporte Dinámico de 1 a 10+ Básculas Industriales*

---

## 1. Visión General del Motor de Pesaje

El sistema incorpora un visor dinámico de alto rendimiento ([`IndustrialScaleVisorScreen.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/ui/screens/scale/IndustrialScaleVisorScreen.kt)) capaz de monitorear simultáneamente desde **1 hasta 10 o más básculas** en una misma tienda o rastro.

### Características Clave:
* **Grid Dinámico y Adaptativo**: La pantalla organiza automáticamente las básculas en tarjetas Bento individuales con visualización de peso en tiempo real (`0.000 kg`), indicador de estabilidad (`[ESTABLE]`), latencia y estado del canal.
* **Tara Independiente**: Cada báscula cuenta con su propio botón de TARA. Tarar la Báscula 1 no altera el peso ni la tara de la Báscula 2 ni de las subsiguientes.
* **Captura Directa de Pesaje**: Cada tarjeta cuenta con el botón **"PESAR Y CAPTURAR"**, el cual abre la ventana modal de corte precargando el peso exacto de la báscula pulsada.
* **Soporte Multi-Marca**: Permite mezclar en la misma tienda básculas de distintas marcas (ej. una Systel en mostrador, una Torrey en embutidos y una báscula de plataforma de gancho para canales).

---

## 2. Catálogo Oficial de Marcas y Modelos Soportados

El archivo [`ScaleBrandCatalog.kt`](file:///c:/Users/gmlve/Downloads/Inventario/app/src/main/java/com/example/inventariocarniceria/data/scale/ScaleBrandCatalog.kt) integra 19 modelos preconfigurados organizados en 8 familias:

| Marca | Modelos Soportados | Puerto por Defecto | Protocolo Industrial | Descripción / Uso Recomendado |
| :--- | :--- | :---: | :--- | :--- |
| **Systel** | Cuora Max, Cuora Neo, Croma, Maya | `10001` | `SYSTEL_CUORA` (Binario / XOR) | Básculas etiquetadoras y comerciales con memoria PLU integrada. |
| **Torrey** | W-LABEL, L-EQ / LEQ, PCR / MFQ, W-TOUCH | `5000` / `9001` | `TORREY_ASCII` (Sondeo `'W'` / `0x57`) | Básculas de mostrador y etiquetadoras comerciales Torrey. |
| **Rhino** | BAR-8 / BAC, I-PEP / PLU, BAPCA | `10001` | `RHINO_ASCII` (Continuo / `'P'`) | Básculas comerciales y de porcionado para carnicerías. |
| **Mettler Toledo** | Viva, bPlus, Tiger | `10001` | `TOLEDO_MTSICS` (`'S\r\n'`) | Balanzas de alta precisión con protocolo estándar MT-SICS. |
| **CAS** | CL-5000, PD-II | `10001` / `5000` | `CAS_AUTO` | Básculas etiquetadoras y balanzas POS de mostrador. |
| **Dibatec** | D-900 | `10001` | `DIBATEC_AUTO` | Balanza táctil de mostrador para carnicerías. |
| **Yaohua / XK3190**| A12E, A27E | `10001` | `YAOHUA_CONTINUOUS` | Indicadores de pesaje de plataforma y ganchos de canal de res/cerdo. |
| **Genérica TCP/IP**| Báscula Wi-Fi Genérica | `10001` | `AUTO` | Detección automática por expresiones regulares tolerantes. |

---

## 3. Protocolo Detallado Systel Cuora Max

Para las básculas Systel Cuora Max (las provistas por defecto en el sistema):

1. **Configuración de Red de Fábrica**:
   * **Báscula 1 (SGNIA1)**: IP `192.168.100.40`, Puerto `10001`, Identificación ID `1` o `2`.
   * **Báscula 2**: IP `192.168.100.50`, Puerto `10001`, Identificación ID `1` o `2`.
2. **Estructura del Paquete Binario (Función 1 - Solicitud de Peso)**:
   * **Trama enviada por la App**: `[ID (1B)] + [0x01] + [ID XOR 0x01]`
   * **Trama devuelta por la Balanza (16 bytes)**:
     `[ID] + [0x01] + [Peso Neto (6 dígitos ASCII)] + ['e' (Estable) / 'i' (Inestable)] + [Tara (6 dígitos ASCII)] + [XOR CRC]`
   * *Ejemplo*: `002000e000000` = `2.000 kg` estables, tara `0.000 kg`.
3. **Códigos de Advertencia y Error Systel**:
   * `PE1`: Peso inestable / movimiento en el plato.
   * `PE2`: Báscula en cero / plato vacío.
   * `PE3`: Peso negativo (requiere pulsar CERO o TARA).
   * `PE4`: Función no válida o peso inferior al mínimo (mínimo 0.100 kg en ciertos modos).
   * `PE5`: Sobrecarga en la balanza (> 30 kg).

---

## 4. Guía Paso a Paso para Agregar una Nueva Báscula

1. **Abrir el Visor de Básculas**:
   * En la pantalla principal de la tablet de mostrador, pulsa el botón superior `＋ BÁSCULAS (X)` o el banner inferior de configuración.
2. **Pulsar `＋ AGREGAR BÁSCULA`**:
   * Se abrirá el modal Bento con el catálogo de marcas y modelos.
3. **Filtrar por Marca o Buscar por Nombre**:
   * Puedes presionar los chips rápidos (`Systel`, `Torrey`, `Rhino`, etc.) o teclear en el buscador (ej. *"W-LABEL"* o *"Cuora"*).
   * Al tocar el modelo deseado, el sistema autocompleta:
     * Nombre sugerido de la báscula.
     * Puerto TCP estándar de fábrica.
     * Protocolo de comunicación recomendado.
4. **Ingresar la Dirección IP**:
   * Introduce la IP asignada en tu router local (ej. `192.168.1.120`).
5. **Realizar Prueba de Conexión (Ping)**:
   * Toca el botón **"Probar Conexión (Ping)"**. El sistema verificará la accesibilidad del socket en menos de 200 ms mostrando una insignia verde con los milisegundos de latencia.
6. **Guardar**:
   * Presiona **"Guardar Báscula"**. La báscula se sumará de inmediato al visor dinámico y empezará a recibir pesaje en tiempo real.

---

<div align="center">
  <sub>Manual de Conectividad · SGNIA COMPANY © 2026</sub>
</div>
