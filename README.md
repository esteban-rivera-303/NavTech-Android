# NavTech-Android

Aplicación Android de autenticación de doble factor (**2FArgos**). Permite enrolar un dispositivo de confianza, validar la identidad del usuario (KYC) y aprobar o rechazar desafíos de autenticación recibidos por notificación push, usando biometría, PIN, TOTP y ubicación como factores.

## Funcionalidades

- **Onboarding y registro**: consulta de estado del usuario por RUT, pre-registro y verificación de código por email/teléfono.
- **Enrolamiento de dispositivo**: genera un par de claves RSA 2048 en el Android Keystore; las solicitudes se firman con nonces emitidos por el servidor.
- **KYC / validación de identidad**: flujo de verificación de identidad con cámara.
- **Desafíos de autenticación**: lista y detalle de desafíos pendientes, que se aprueban o rechazan con los factores `BIOMETRIC`, `PIN`, `TOTP` y `LOCATION`.
- **Gestión de PIN**: creación, cambio y recuperación del PIN.
- **Recuperación de cuenta**: verificación por email y teléfono, reactivación de dispositivo de confianza y re-enrolamiento.
- **Historial**: historial de desafíos respondidos y centro de notificaciones (marcar como leídas).
- **TOTP local**: generación de códigos TOTP en el dispositivo.
- **Notificaciones push** con Firebase Cloud Messaging, con navegación directa al desafío.

## Stack técnico

| Área | Tecnología |
|------|------------|
| Lenguaje | Kotlin 2.0.21 (JVM 11) |
| UI | Jetpack Compose + Material 3, Navigation Compose |
| Arquitectura | Clean Architecture modular (data / domain / presentation), MVVM |
| DI | Hilt 2.52 (kapt) |
| Red | Retrofit 2.11 + OkHttp (Gson / Moshi) |
| Asincronía | Kotlin Coroutines / Flow |
| Persistencia | DataStore Preferences, EncryptedSharedPreferences (security-crypto) |
| Seguridad | Android Keystore, BiometricPrompt, BouncyCastle, java-otp (TOTP), Play Integrity (dependencia declarada) |
| Servicios | Firebase Messaging y Analytics, Play Services Location |
| Backend | Supabase Edge Functions (`mobile-api`) |

**SDK**: `minSdk 26` · `targetSdk 35` · `compileSdk 36` · AGP 8.9.1

## Estructura de módulos

```
NavTech-Android/
├── app/                  # Punto de entrada: MainActivity, navegación, home, drawer, notificaciones (FCM)
├── core/                 # Base compartida: red, DI, sesión, seguridad/cripto, biometría, ubicación, modelos de dominio
├── feature-auth/         # Registro, verificación, enrolamiento, PIN, TOTP, recuperación de cuenta
├── feature-challenges/   # Desafíos 2FA: consulta, detalle, respuesta y validación TOTP
└── feature-kyc/          # Validación de identidad (KYC)
```

Dependencias entre módulos:

```
app ──► core, feature-auth, feature-kyc, feature-challenges
feature-auth ──► core, feature-kyc
feature-challenges ──► core, feature-auth
feature-kyc ──► core
```

Paquete base: `com.zanaaustralis.*` · applicationId: `com.zanaaustralis.argos`

## Requisitos

- Android Studio (versión compatible con AGP 8.9)
- JDK 17 para ejecutar Gradle (Gradle 8.11 no soporta Java 25, que es el JDK incluido en versiones recientes de Android Studio)
- Un proyecto de Firebase con la app `com.zanaaustralis.argos` registrada

## Configuración

1. Clona el repositorio:
   ```bash
   git clone git@github.com:esteban-rivera-303/NavTech-Android.git
   cd NavTech-Android
   ```
2. Descarga `google-services.json` desde la consola de Firebase y colócalo en `app/`. **No se versiona** (está en `.gitignore`).
3. La URL del backend se define en `BASE_URL` de `core/src/main/java/com/zanaaustralis/core/di/NetworkModule.kt`.

## Compilación y ejecución

```bash
./gradlew assembleDebug        # Genera el APK debug
./gradlew installDebug         # Instala en un dispositivo/emulador conectado
./gradlew test                 # Tests unitarios
./gradlew connectedAndroidTest # Tests instrumentados
```

## Seguridad

### Medidas implementadas en la app

| Área | Medida |
|------|--------|
| Logs | El logging HTTP (`secureHttpLoggingInterceptor()` en `core`) solo registra cuerpos en builds **debug**; en release el nivel es `NONE` y el header `Authorization` siempre se redacta. No se registran tokens, códigos TOTP, URLs de KYC ni ubicación. |
| PIN | Hash PBKDF2-HMAC-SHA256 (`PinHasher`, 60.000 iteraciones, salt aleatorio, comparación en tiempo constante) guardado en EncryptedSharedPreferences. Máximo 3 intentos con **contador persistente**: el bloqueo sobrevive al reinicio de la app y solo se levanta restaurando el PIN. Los PINs del esquema anterior (SHA-256 en DataStore) se migran al validarse. Si el almacén cifrado se pierde por un error del Keystore, el PIN queda **bloqueado** (no se puede crear otro sin restaurarlo vía KYC), gracias a un marcador no cifrado que recuerda que existía. |
| Factores por desafío | Los factores validados localmente y los códigos TOTP por desafío se guardan cifrados y **expiran a los 5 minutos** (`AuthFactorsCodec`, `ChallengeLocalAuthStoreImpl`). |
| Backup | `allowBackup="false"` y reglas que excluyen todos los datos del backup en la nube y de la transferencia entre dispositivos (Android 12+). Los módulos de librería no declaran `allowBackup`. |
| Claves | Clave de dispositivo RSA 2048 en el Android Keystore (no exportable); tokens, identidad y secreto TOTP en EncryptedSharedPreferences. |
| Pantalla | `FLAG_SECURE` en la única activity: sin capturas ni vista previa en recientes. |
| WebView KYC | Navegación y permisos de cámara/micrófono solo para `https://*.didit.me` (`KycWebPolicy`); sin contenido mixto ni puente JavaScript. |
| WebView de términos | Navegación limitada al origen HTTPS de `URL_TC` (`TermsWebPolicy`). Ambos WebViews tienen deshabilitado el acceso a archivos y a content providers. |
| Notificaciones | Se abren por un `activity-alias` no exportado; `MainActivity` ignora `challenge_id` si llega desde otra app. |
| Release | R8 activo (`isMinifyEnabled = true`), conservando solo los campos de los modelos serializados. |
| Dependencias | OkHttp 4.12.0, biometric 1.1.0 y security-crypto 1.1.0 estables (antes versiones alpha). |
| Firma v4 *(flag)* | Respuesta a desafíos firmada sobre `v4:device:challenge:response:timestamp:nonce:sha256(data)` con nonce de un solo uso del servidor (`SignatureV4`). |
| Biometría verificable *(flag)* | Clave `argos_user_auth_key` que exige biometría fuerte en cada uso; al aprobar un desafío con biometría, el BiometricPrompt (con `CryptoObject`) desbloquea la firma. |

### Flag `argos.signatureV4`

La firma v4 y la clave ligada a biometría requieren cambios en el backend, por eso están **desactivadas por defecto** y la app sigue usando la firma v3 actual. Para activarlas cuando el backend las soporte:

```bash
./gradlew assembleRelease -Pargos.signatureV4=true
```

### Pendiente (requiere cambios en el backend)

Lo que hay que modificar en `mobile-api` para cada cambio está en [`docs/BACKEND_SECURITY_CHANGES.md`](docs/BACKEND_SECURITY_CHANGES.md):

- B1/B2: verificar la firma v4 y la clave `user_auth` (app lista detrás del flag).
- B3: validación TOTP obligatoria en servidor con anti-replay.
- B6: attestation de clave y Play Integrity.
- B7: nonce en la firma de login.
- B8: logout que desactive el dispositivo en servidor (requisito para borrar la identidad local al cerrar sesión).

Pendiente en la app: certificate pinning, que se implementará con el servidor de producción (TODO en `NetworkModule.kt`).

## Permisos

`INTERNET`, `POST_NOTIFICATIONS`, `CAMERA`, `READ_MEDIA_IMAGES`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`.

## API

Endpoints que consume la app (relativos a `BASE_URL`):

| Dominio | Endpoints |
|---------|-----------|
| Auth | `user-status`, `pre-register`, `request`, `verify-code`, `onboarding`, `login`, `refresh`, `logout` |
| Recuperación | `account-recovery/{initiate,status,resend-code,verify-email,verify-phone,complete}`, `trusted-device-reactivate`, `recovery/enroll-device` |
| Desafíos | `GET challenges`, `GET challenges/history`, `challenges/respond`, `totp/validate` |
| Notificaciones | `GET notifications/history`, `notifications/mark-read`, `notifications/mark-all-read` |
| KYC | `kyc/initiate`, `GET kyc/status`, `identity-validation/{start,verify}`, `GET identity-validation/status/{id}` |
