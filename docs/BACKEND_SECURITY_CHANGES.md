# Cambios de seguridad requeridos en el backend

Cambios del cliente Android que necesitan contraparte en `mobile-api` (Supabase Edge Functions, repo `Argos2FA`).
Cada entrada indica el estado en la app, el problema y lo que hay que modificar en el backend.

> Las referencias de código del backend (`index.ts`, `signature-verification.ts`, `crypto.ts`) se tomaron de la copia local de `Argos2FA/supabase/functions/mobile-api`. Esa copia **no coincide con la app actual** (p. ej. `index.ts` llama a `validateDeviceSignature`, que no existe en `signature-verification.ts`), así que hay que validar cada punto contra la versión desplegada.

## Activación en la app

La firma v4 y la clave ligada a biometría (B1, B2) están implementadas detrás de un flag de build, **desactivado por defecto**:

```bash
./gradlew assembleRelease -Pargos.signatureV4=true
```

Con el flag apagado la app sigue enviando la firma v3 actual. Activarlo solo cuando el backend acepte v4.

| # | Cambio | Estado app | Estado backend |
|---|--------|-----------|----------------|
| B1 | Firma v4 obligatoria y ligada al desafío y al payload | Implementado (flag `argos.signatureV4`) | Pendiente |
| B2 | Biometría verificable en servidor (clave `user_auth`) | Implementado (flag `argos.signatureV4`) | Pendiente |
| B3 | Validación TOTP siempre en servidor + anti-replay | Implementado en app (envía `totp_code`) | Pendiente |
| B4 | Bloqueo de PIN y restauración | Implementado en app | Recomendado |
| B5 | Envoltorio `encrypted` | Sin cambio | Pendiente de decisión |
| B6 | Attestation de clave / Play Integrity | Pendiente | Pendiente |
| B7 | Nonce en la firma de login | Pendiente (requiere contrato) | Pendiente |
| B8 | Logout desactiva el dispositivo | Pendiente (depende de B8 backend) | Pendiente |

---

## B1. Firma v4 de `POST /challenges/respond`

**Hoy (v3, flag apagado)**: la app envía header `x-argos-signature-version: 3` y firma `"{device_uuid}:{challenge_id}:{response}:{timestamp}"` con la clave de dispositivo. No hay nonce y `totp_code`, `location` y `metadata` no están firmados. En la copia local del backend la verificación además es condicional (`if (device_uuid && timestamp && signature)`) y `parseSignatureVersion` solo conoce v1/v2.

**Contrato v4 (implementado en la app)**

1. `POST /request-nonce` `{ "device_uuid": "..." }` → `{ "success": true, "nonce": "<uuid>", "timestamp": <ms>, "expires_in": 300000 }` (endpoint existente).
2. `POST /challenges/respond` con header `x-argos-signature-version: 4` y cuerpo:
   ```json
   {
     "encrypted": true,
     "data": "<Base64(JSON payload)>",
     "device_uuid": "<uuid>",
     "signature": "<Base64 SHA256withRSA PKCS#1 v1.5>",
     "signing_key": "device | user_auth"
   }
   ```
   Payload decodificado de `data` (la firma **no** va dentro):
   ```json
   { "challenge_id": "...", "response": "approved|rejected", "timestamp": 1700000000000,
     "nonce": "...", "totp_code": null, "location": null, "metadata": null }
   ```
3. Canonical firmado:
   ```
   v4:{device_uuid}:{challenge_id}:{response}:{timestamp}:{nonce}:{data_hash}
   data_hash = base64url_sin_padding(SHA-256(UTF-8 del string `data` tal como llega))
   ```
   Al hashear el string `data` recibido no hace falta canonicalizar JSON. Implementación de referencia en la app: `core/.../secure/crypto/SignatureV4.kt` (con tests).

**Cambios en backend**
1. `parseSignatureVersion`: aceptar `4`. Para v4, verificar **antes** de `decodeBase64Data`, usando `body.data` crudo y `body.signature` del envoltorio.
2. Campos obligatorios en v4: `data`, `device_uuid`, `signature`, `signing_key` y, dentro del payload, `challenge_id`, `response`, `timestamp`, `nonce` → `400` si falta alguno. Eliminar el `if` condicional: sin firma válida no se procesa la respuesta.
3. Verificación:
   - `device_uuid` del envoltorio = dispositivo `is_active`, `key_state = 'ACTIVE'` y del `session.user_id`.
   - `|now - timestamp| ≤ 5 min`.
   - `nonce` emitido por `/request-nonce` para ese `device_id`, no usado y no expirado; consumirlo (`INSERT` con `UNIQUE(device_id, nonce)`).
   - Clave pública: `devices.public_key` si `signing_key = "device"`, `devices.user_auth_public_key` si `signing_key = "user_auth"` (ver B2).
4. Actualizar el desafío con `UPDATE ... WHERE id = :id AND user_id = :uid AND status = 'pending'` y exigir `rowCount = 1` (evita doble respuesta concurrente).
5. Transición: mantener v3 mientras haya apps sin el flag, con un flag de servidor `ALLOW_SIGNATURE_V3` y registro de uso; rechazar peticiones sin firma desde el primer despliegue.

---

## B2. Biometría verificable en servidor

**Problema**: `validate_challenge_factors` recibe `p_has_biometric: true` fijo y `metadata.biometric_used` lo declara la app. La clave de dispositivo (`aegis_device_key`) firma sin autenticación del usuario.

**Implementado en la app (flag `argos.signatureV4`)**
- Segunda clave RSA 2048 `argos_user_auth_key` en el Android Keystore: `setUserAuthenticationRequired(true)`, autenticación `BIOMETRIC_STRONG` en **cada uso** y `setInvalidatedByBiometricEnrollment(true)`.
- Se genera en cada enrolamiento y su PEM se envía como `user_auth_public_key` en `onboarding`, `recovery/enroll-device` y `trusted-device-reactivate`. Si el dispositivo no tiene biometría enrolada el campo no se envía.
- En desafíos con `requires_biometric`, al aprobar se muestra un BiometricPrompt con `CryptoObject`; la firma v4 se hace con esa clave y se envía `signing_key = "user_auth"`. Rechazos y desafíos sin biometría usan `signing_key = "device"`.
- Si el usuario cambia sus huellas la clave se invalida y la app pide re-enrolar.

**Cambios en backend**
1. Migración: `ALTER TABLE devices ADD COLUMN user_auth_public_key text NULL;`
2. `onboarding`, `recovery/enroll-device`, `trusted-device-reactivate`: aceptar `user_auth_public_key` (PEM, opcional), validar que sea RSA ≥ 2048 y guardarlo; en re-enrolamiento reemplazar el anterior.
3. `/challenges/respond`: si el tipo de desafío requiere biometría y `response = approved`, exigir `signing_key = "user_auth"` y verificar con `user_auth_public_key`; `p_has_biometric` = resultado de esa verificación (eliminar el `true` fijo). Si el dispositivo no tiene `user_auth_public_key` → `403` con mensaje de re-enrolamiento.
4. Usar `metadata.*_used` solo como auditoría, nunca para decidir factores.

---

## B3. TOTP: validación obligatoria en servidor y anti-replay

**App**: en `newUI` el código TOTP se genera y compara localmente (`TotpManager`) y luego se envía `totp_code` en el payload. La comparación local no es un control de seguridad.

**Problemas en backend**
1. En `/challenges/respond` el TOTP solo se verifica si `totp_code && missing_factors && !missing_factors.includes('TOTP')`; si el desafío exige TOTP la lógica depende del resultado del RPC y puede saltarse.
2. `verifyTOTP(secret, code, windowSize = 2)` acepta ±2 pasos (±60 s, 150 s en total) y no impide reutilizar el mismo código.

**Cambios**
1. Si el tipo de desafío requiere `TOTP`: `totp_code` obligatorio y `verifyTOTP` siempre; `401` si falla.
2. Reducir `windowSize` a 1.
3. Anti-replay: guardar `last_totp_step` por dispositivo y rechazar `step <= last_totp_step`.
4. Rate limit en `/totp/validate` y en la validación dentro de `/challenges/respond` (p. ej. 5 intentos / 5 min por dispositivo).
5. Mediano plazo: el secreto TOTP no debería residir en el mismo dispositivo que firma; evaluar TOTP en otro dispositivo o eliminar TOTP como factor independiente en este flujo.

---

## B4. Bloqueo de PIN y restauración

**App (implementado)**: el PIN se valida localmente con PBKDF2; tras 3 fallos queda bloqueado de forma persistente y el usuario debe restaurarlo vía KYC (`identity-validation/*`).

**Cambios recomendados**
1. Rate limit en `identity-validation/start` y `identity-validation/verify` por usuario y dispositivo, porque son la vía de desbloqueo.
2. Registrar en `audit_logs` el evento `pin_restored` al completar la restauración.
3. Opcional (más robusto): validar el PIN en servidor (hash Argon2id/PBKDF2 en `devices` o `users`) con contador de intentos en servidor, y que la app envíe una prueba en lugar de validar localmente.

---

## B5. Envoltorio `encrypted`

**Estado**: la app envía `{ "encrypted": true, "data": Base64(JSON), "device_uuid": ... }` y el backend lo decodifica con `decodeBase64Data` (`atob`). No hay cifrado; solo TLS.

**Cambios (elegir una)**
1. Renombrar el campo a `encoded` (o eliminar el envoltorio) para no dar una falsa sensación de seguridad, aceptando ambos formatos durante la transición.
2. O cifrado real: el backend publica una clave pública (X25519/RSA-OAEP), la app cifra con AES-GCM + clave efímera, el backend descifra antes de `decodeBase64Data`.

`decodeBase64Data` hace `{ ...body, ...parsed }`: los campos del `data` decodificado sobrescriben a los del envoltorio. Validar que `device_uuid` del envoltorio y del payload coincidan.

---

## B6. Attestation de clave y Play Integrity

**App**: la clave del dispositivo se genera en el Android Keystore pero se envía solo el PEM público; `PlayIntegrityChecker.kt` está vacío aunque la dependencia está declarada.

**Cambios**
1. En enrolamiento, aceptar `attestation_chain` (array de certificados X.509 Base64) y verificar:
   - cadena hasta la raíz de Google Hardware Attestation;
   - `attestationChallenge` = nonce emitido por el servidor;
   - `attestationSecurityLevel` ≥ `TrustedEnvironment`, `purpose = SIGN`, `noAuthRequired = false` para `user_auth_public_key`;
   - revocación vía la lista de estado de Google.
2. Play Integrity: endpoint que reciba el token, lo descifre/verifique con la API `decodeIntegrityToken` y exija `MEETS_DEVICE_INTEGRITY` y `PLAY_RECOGNIZED` en enrolamiento y en aprobaciones de desafíos críticos.
3. Guardar `security_level` y el veredicto en `devices` para usar en reglas de riesgo.

---

## B7. Nonce en la firma de login

**App (hoy)**: `POST /login` envía `{ device_uuid, timestamp, signature }` con la firma v1 sobre `"{device_uuid}:{timestamp}"` (`DeviceCryptoSigner.signLogin`). No hay nonce: una firma capturada (logs de un proxy, dispositivo comprometido) se puede reutilizar para obtener una sesión nueva mientras el `timestamp` esté dentro de la ventana de 5 minutos.

**Cambios en backend**
1. Aceptar una firma de login v2 con nonce de `/request-nonce` (reutiliza el mecanismo de B1):
   ```
   login-v2:{device_uuid}:{timestamp}:{nonce}
   ```
   Cuerpo: `{ "device_uuid", "timestamp", "nonce", "signature" }` y header `x-argos-signature-version: 2` (o el valor que se acuerde para login).
2. Verificar el nonce igual que en B1 (emitido para ese dispositivo, no usado, no expirado) y consumirlo.
3. Mientras existan apps con v1, rechazar al menos firmas v1 repetidas: guardar `(device_id, timestamp)` usados en la ventana de 5 minutos.

**App**: una vez acordado el contrato, pedir el nonce antes del login y firmarlo, detrás del mismo flag `argos.signatureV4`.

---

## B8. Logout: desactivar el dispositivo en servidor

**App (hoy)**: el logout llama a `POST /logout` y localmente solo borra la sesión, los tokens y el flag `enrolled` (`DeviceIdentityManager.clear()`). El UUID, la clave de dispositivo, la clave `user_auth` y el secreto TOTP quedan en el teléfono.

**Por qué no se borran solo en la app**: el re-enrolamiento depende de que el UUID sobreviva. Si `onboarding` responde `"User already has a device enrolled"`, la app reutiliza el mismo UUID y la misma clave para iniciar sesión (`EnrollmentViewModel.enrollDevice`). Si la app borra la identidad y el servidor mantiene el dispositivo activo, el usuario no podría volver a entrar desde el mismo teléfono.

**Cambios en backend**
1. Definir qué significa logout: (a) solo cerrar la sesión, con el dispositivo enrolado (comportamiento actual), o (b) desenrolar el dispositivo.
2. Si es (b): `POST /logout` debe marcar el dispositivo como inactivo (`is_active = false`, `key_state = 'REVOKED'`), invalidar sus refresh tokens y dejar de enviarle desafíos. Después, el re-enrolamiento desde el mismo teléfono debe aceptar un UUID y una clave nuevos.
3. Confirmar si `/login` siempre devuelve `totp_secret`. La app solo puede borrar el secreto TOTP en el logout si el login siguiente lo vuelve a entregar.

**App (cuando el backend implemente la opción b)**: en el logout, borrar el UUID, la clave de dispositivo, la clave `user_auth`, el secreto TOTP y los factores por desafío.
