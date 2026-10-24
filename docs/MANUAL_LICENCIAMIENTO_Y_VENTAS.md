# 🔑 Manual de Licenciamiento Criptográfico, Ventas y Keygen Admin

**SGNIA COMPANY — “Software que Protege y Hace Crecer tu Negocio”**  
*Guía Comercial y Operativa para el Desarrollador (Gamaliel Velazquez)*

---

## 1. Visión General del Modelo de Negocio

El sistema comercial de **SGNIA CARNICERÍA POS** fue diseñado para permitir máxima flexibilidad al momento de negociar con propietarios de carnicerías, rastros o cadenas cárnicas:

```
                      ┌──────────────────────────────────────────────┐
                      │              CLIENTES / PROSPECTOS           │
                      └──────────────────────┬───────────────────────┘
                                             │
             ┌───────────────────────────────┼──────────────────────────────┐
             ▼                               ▼                              ▼
     [ CLIENTE TIPO A ]              [ CLIENTE TIPO B ]             [ CLIENTE TIPO C/D ]
     Compra Definitiva               Renta Mensual                  Renta Anual
     • Licencia Perpetua (LIFE)      • Pago recurrente (01M)        • Contrato anual (01Y)
     • Cobro por paquete inicial     • Cobro mensual por sucursal   • Descuento por volumen
     • Costo por sucursal extra      • Renovación vía WhatsApp      • Renovación anual
```

---

## 2. Arquitectura Criptográfica Offline (HMAC-SHA256)

El licenciamiento funciona **completamente sin conexión a internet**:

1. **Hardware ID Único (`SGN-XXXX-XXXX`)**:
   * Cada tablet genera un identificador irrepetible basado en el hardware físico de Android (`Build.BOARD`, `Build.BRAND`, `Build.DEVICE`, `Build.MANUFACTURER`, `Build.MODEL`, `Build.HARDWARE` y `ANDROID_ID`).
   * No cambia aunque se reinicie la tablet o se reinstale la aplicación.

2. **Estructura de la Clave Generada**:
   * Formato: `SGN-[DURACIÓN]-[BLOQUE_HW]-[TOKEN_ALEATORIO]-[FIRMA_HMAC]`
   * *Ejemplo de Compra Definitiva*: `SGN-LIFE-8F2A-B9C4-E2F1`
   * *Ejemplo de Renta Mensual*: `SGN-01M-8F2A-A1F2-D4E5`

3. **Mecanismos Anti-Fraude**:
   * **Firma HMAC-SHA256**: Solo el generador de claves (`SGNIA Keygen Admin`) posee la llave secreta maestra. Ningún tercero puede inventar claves válidas.
   * **Anti-Rollback de Reloj**: La app guarda en almacenamiento protegido la fecha/hora más alta observada. Si el usuario intenta atrasar el reloj del dispositivo para ganar tiempo, la app se bloquea con el mensaje `CLOCK_TAMPERED`.
   * **Token Único de Consumo**: Una clave vencida no puede volver a ingresarse para reactivar el mismo equipo.

---

## 3. Duraciones y Modalidades Disponibles

| Código | Modalidad | Duración Real | Uso Recomendado en Venta |
| :---: | :--- | :--- | :--- |
| **`10M`** | Demostración en Vivo | 10 Minutos | Demostración presencial en mostrador frente al dueño antes de cerrar el contrato. |
| **`07D`** | Prueba Comercial | 7 Días Naturales | Prueba de confianza para que el dueño evalúe el sistema con sus cortes y básculas reales. |
| **`01M`** | Renta Mensual | 30 Días Naturales | Clientes que prefieren pagar una cuota mensual recurrente (Suscripción). |
| **`01Y`** | Renta Anual | 365 Días Naturales | Clientes de largo plazo con pago anualizado. |
| **`LIFE`**| Venta Definitiva | Perpetua (Sin vencimiento) | Clientes que adquieren el software de por vida para su negocio. |

---

## 4. Gestión Multitienda en la App `SGNIA Keygen Admin`

La aplicación instalada en el teléfono del desarrollador (`com.example.sgniakeygen`) incluye un CRM comercial completo:

### A. Registro de Dueño y Sucursales
1. **Datos del Propietario**:
   * Nombre del Dueño (ej. *Gamaliel Velázquez Galicia*).
   * Empresa o Marca (ej. *Carnicería La Bendición*).
   * Teléfono con WhatsApp (ej. *+52 222 123 4567*).
   * Correo electrónico.
2. **Selección de Membresía**:
   * Selector: Renta Mensual (`01M`), Renta Anual (`01Y`) o Venta Definitiva (`LIFE`).
3. **Sucursales Vinculadas**:
   * Botón `＋ AGREGAR SUCURSAL`.
   * Nombre de cada sucursal (ej. *Matriz Centro*, *Sucursal Norte*, *Sucursal Sur*).
   * Ingreso del `Hardware ID` de la tablet de esa tienda.
   * Generación automática de la clave individual para esa sucursal con un toque.

### B. Tarifario Maestro y Control de Pagos
* **Tarifario Configurable**:
  * Precio Renta Mensual por sucursal activa.
  * Precio Renta Anual por sucursal activa.
  * Precio Paquete Base Venta Definitiva (incluye sucursales base).
  * Precio por Sucursal Adicional en venta definitiva.
* **Cálculo Automático del Total**:
  * El sistema multiplica automáticamente las sucursales por la tarifa y muestra el monto exacto a cobrar en pesos mexicanos (`$ MXN`).
* **Estado de Cobranza**:
  * Conmutador interactivo: `🔴 PENDIENTE DE PAGO` / `🟢 PAGADO`.

### C. Emisión de Comprobante Oficial de Compra
* Al pulsar **"Generar Comprobante Oficial (PDF)"**, el sistema crea un documento formal con:
  * Logotipo oficial y escudo SGNIA COMPANY.
  * Folio único de compra y fecha de emisión.
  * Datos completos del Dueño y empresa.
  * Tabla con el desglose de sucursales, Hardware IDs y claves criptográficas asignadas.
  * Total cobrado y sello de `PAGADO` o `PENDIENTE`.
  * Datos fiscales y de contacto de Gamaliel Velazquez Galicia.
* **Envío con 1 Toque a WhatsApp**:
  * Se abre la conversación de WhatsApp con el cliente adjuntando el resumen o el comprobante con un solo botón.

---

<div align="center">
  <sub>Manual de Licenciamiento y Ventas · SGNIA COMPANY © 2026</sub>
</div>
