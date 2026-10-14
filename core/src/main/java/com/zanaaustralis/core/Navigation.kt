package com.zanaaustralis.core

/**
 * @author esteban.rivera01
 */

sealed class NavRoute(val route: String) {

    data object Home : NavRoute("home")
    data object HomeGate : NavRoute("home_gate")
    data object Launch : NavRoute("launch")
    data object UserStatus : NavRoute("user_status")

    data object VerificationFlow : NavRoute("verification_flow/{userJson}/{rut}") {
        fun build(userJson: String, rut: String) =
            "verification_flow/$userJson/$rut"
    }

    data object Register : NavRoute("register/{rut}") {
        fun build(rut: String) =
            "register/$rut"
    }
    data object EnrollmentDevice : NavRoute("enrollment_device/{rut}/{email}/{phone}") {
        fun build(rut: String, email: String, phone: String) =
            "enrollment_device/$rut/$email/$phone"
    }
    data object EnrollmentRecoveryDevice : NavRoute("enrollment_recovery_device/{rut}") {
        fun build(rut: String) =
            "enrollment_recovery_device/$rut"
    }
    data object EnrollmentTrustedDevice : NavRoute("enrollment_trusted_device/{rut}") {
        fun build(rut: String) =
            "enrollment_trusted_device/$rut"
    }

    data object Verify : NavRoute("verify/{type}/{target}") {
        fun build(type: String, target: String) = "verify/$type/$target"
    }

    data object Kyc : NavRoute("kyc/{rut}/{deviceId}") {
        fun build(rut: String, deviceId: String) = "kyc/$rut/$deviceId"
    }

    // menu section
    data object MenuPIN : NavRoute("menu_pin")
    data object MenuHistory : NavRoute("menu_history")
    data object MenuAbout : NavRoute("menu_about")
    data object MenuLogout : NavRoute("menu_logout")

    data object DialogSuccess : NavRoute("result_success")
    data object DialogError : NavRoute("result_error")

    // PIN
    data object CreatePIN : NavRoute("create_pin")
    data object ChangePIN : NavRoute("change_pin")
    data object ResetPIN : NavRoute("reset_pin")

    data object RecoveryStart : NavRoute("recovery_start/{userJson}"){
        fun build(userJson: String) = "recovery_start/${userJson}"
    }
    data object RecoveryVerify : NavRoute("recovery_verify/{rut}/{email}/{phone}") {
        fun build(rut: String, email: String, phone: String) = "recovery_verify/$rut/$email/$phone"
    }

    data object Challenges : NavRoute("challenges")
    object ChallengeDetail : NavRoute("challenge_detail/{challengeId}") {
        fun build(challengeId: String) = "challenge_detail/$challengeId"
    }
    data object Notifications : NavRoute("notifications")
    data object PIN_SECTION : NavRoute("pin/{challengeId}") {
        fun build(id: String) = "pin/$id"
    }
    data object Totp : NavRoute("totp/{challengeId}") {
        fun build(id: String) = "totp/$id"
    }
}

object NavResultKeys {
    const val PIN_VALIDATED_CHALLENGE_ID = "pin_validated_challenge_id"
    const val TOTP_VALIDATED_CHALLENGE_ID = "totp_validated_challenge_id"
}
