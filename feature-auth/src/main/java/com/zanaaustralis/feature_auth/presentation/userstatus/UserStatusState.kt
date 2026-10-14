package com.zanaaustralis.feature_auth.presentation.userstatus

import com.zanaaustralis.core.data.remote.model.UserStatusResponse

/**
 * @author esteban.rivera01
 */
data class UserStatusState(
    val rut: String = "",
    val isLoading :Boolean = false,
    val data: UserStatusResponse? = null,
    val error: String ? = null,
/*    val isRutValid: Boolean = false,
    val termsAccepted: Boolean = false,
    val showTermsDialog: Boolean = false,*/
    val phase: UserStatusPhase = UserStatusPhase.INIT
)

data class UserStatusUiState(
    val rut: String = "",
    val isRutValid: Boolean = false,
    val termsAccepted: Boolean = false,
    val showTermsDialog: Boolean = false
)

enum class UserStatusPhase {
    NOT_REGISTER,       // No tiene cuenta registrada
    PENDING_EMAIL,      // Falta validar email
    PENDING_PHONE,      // Falta validar teléfono
    PENDING_KYC,        // Falta verificación de identidad
    PENDING_DEVICE,        // Falta enrolar el dispositivo
    DEVICE_MISMATCH,    // El dispositivo no coincide con el enrolado
    DEVICE_TRUSTED,    // El dispositivo  ya ha sido enrolado anteriormente
    READY_TO_LOGIN,        // Ya enrolado y activo
    SESSION_ACTIVE,         // Ya enrolado y activo
    INIT,            // Inicial
    ERROR            // Inicial
}




