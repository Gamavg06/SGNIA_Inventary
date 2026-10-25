# Documento Interno de Seguridad y Arquitectura Criptográfica Privada - SGNIA

> **CONFIDENCIAL / USO EXCLUSIVO INTERNO**  
> Este documento contiene información técnica reservada sobre el modelo de amenazas, parámetros criptográficos internos y la propuesta de migración de licenciamiento para el equipo de desarrollo de SGNIA COMPANY. No debe incluirse en publicaciones externas.

---

## 1. Auditoría del Repositorio y Módulo Keygen

* **Aislamiento de Keygen**: El módulo emisor de licencias (`keygen/`) ha sido removido del control de versiones público de Git (`git rm -r keygen`), respaldado localmente en una ubicación segura fuera del repositorio y añadido formalmente a `.gitignore`.
* **Configuración de Gradle**: En `settings.gradle.kts`, la inclusión de `:keygen` es condicional (`if (file("keygen").exists()) { include(":keygen") }`), evitando que el proyecto público dependa o exponga el código del emisor.
* **Verificación de Secretos en `app/`**: En la aplicación cliente (`app/src/main/...`), la validación en tiempo de ejecución no almacena cadenas de texto plano de claves maestras; utiliza enmascaramiento con operaciones XOR por bytes en `AppSecurityGuard.kt`.

---

## 2. Parámetros Internos y Medidas de Mitigación Implementadas

### A. Política de PIN por Defecto
* **Vulnerabilidad histórica**: PIN inicial `1234`.
* **Mitigación implementada**:
  - En `SecurityVaultScreen.kt`, si el PIN almacenado o ingresado coincide con el valor de fábrica, se bloquea la navegación y se despliega un diálogo modal obligatorio exigiendo la creación de un nuevo PIN seguro de 4 a 8 dígitos numéricos distintos del default.
  - La clave de sucursal en `AdminBranchesScreen.kt` se muestra ofuscada por defecto (`••••`) con botón explícito de revelar/ocultar mediante confirmación visual.

### B. Throttling y Bloqueo Anti-Fuerza Bruta
* **Mitigación implementada**:
  - Conteo de intentos fallidos en `SecurityVaultScreen.kt`.
  - Al acumular 3 intentos erróneos, se activa una ventana de bloqueo de 30 segundos (`lockoutUntil`), deshabilitando el teclado numérico y mostrando un banner de advertencia con cuenta regresiva en tiempo real.

---

## 3. Propuesta Técnica: Migración de Licenciamiento a Firma Asimétrica (Ed25519)

### Diagnóstico del Esquema Actual (HMAC-SHA256 Simétrico)
* Actualmente, la validación fuera de línea (`LicenseManager.kt`) requiere que el algoritmo de verificación comparta el secreto simétrico con el generador de licencias.
* Aunque los bytes se encuentren ofuscados u ocultos mediante técnicas de ingeniería inversa en DEX/Smali, un analista persistente con desensambladores avanzados podría extraer el secreto y clonar licencias.

### Arquitectura Propuesta con Ed25519 (Firma Asimétrica)

```
┌─────────────────────────────────┐           ┌───────────────────────────────────┐
│     SERVIDOR SEGURO / KEYGEN    │           │      APLICACIÓN SGNIA CLIENTE     │
│   (Ambiente Privado de SGNIA)   │           │      (Instalado en Terminal)      │
├─────────────────────────────────┤           ├───────────────────────────────────┤
│ Clave Privada (Ed25519 Private) │           │ Clave Pública (Ed25519 Public)    │
│  [NUNCA SALE DEL SERVIDOR]      │           │  [EMBEBIDA LIBREMENTE EN LA APP]  │
│                                 │           │                                   │
│ Payload = HardwareID + Tier     │           │ Recibe: Licencia en Base64        │
│         + Expiración            │           │                                   │
│ Firma = Ed25519.Sign(Payload,   │ ────────> │ Valida:                           │
│                      PrivateKey)│           │ Ed25519.Verify(Payload,           │
│ Licencia = Payload + "." + Firma│           │                Signature,         │
│                                 │           │                PublicKey)         │
└─────────────────────────────────┘           └───────────────────────────────────┘
```

#### Ventajas Clave:
1. **Ruptura de Simetría**: La clave privada nunca se compila en el APK ni viaja a los dispositivos de los clientes.
2. **Inmune a Decompilación**: Incluso si un atacante desensambla el APK al 100% y extrae la clave pública, **solo puede verificar firmas, jamás generarlas**.
3. **Eficiencia y Tamaño**: Las firmas Ed25519 tienen un tamaño compacto (64 bytes) y un rendimiento ultra rápido en procesadores ARM de terminales POS.
4. **Compatibilidad nativa en Android**:
   - En Java 15+ / Android API 33+ disponible vía `java.security.KeyPairGenerator.getInstance("Ed25519")`.
   - Para Android API 26+ (compatibilidad SGNIA), se implementa mediante BouncyCastle o libsodium-jni (`org.bouncycastle.crypto.signers.Ed25519Signer`).

#### Plan de Implementación de 3 Pasos:
1. **Generación de Par de Llaves**: Generar par maestro en el servidor privado de SGNIA. Guardar clave privada en variables de entorno seguras con respaldo cifrado offline.
2. **Actualización de `LicenseManager.kt`**: Reemplazar `Mac.getInstance("HmacSHA256")` por `Ed25519Signer.verifySignature()`, consumiendo los 32 bytes de la clave pública embebida.
3. **Formato de Licencia Unificado**:
   ```
   SGNIA-<VERSION>-<BASE64_PAYLOAD>-<BASE64_ED25519_SIGNATURE>
   ```
   Donde el payload contiene `{hwid, tier, expiry_epoch, issued_epoch}` en formato binario estructurado o JSON compacto.
