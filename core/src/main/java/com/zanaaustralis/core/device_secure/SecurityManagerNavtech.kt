package com.zanaaustralis.core.device_secure

import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

data class SecuritySignals(
    val isRooted: Boolean,
    val isEmulator: Boolean,
    val isHooked: Boolean,
    //val playIntegrityPassed: Boolean
)

class SecurityManagerNavtech @Inject constructor(
    private val rootDetector: RootDetector,
    private val emulatorDetector: EmulatorDetector,
    private val hookDetector: HookDetector,
) {
    suspend fun evaluate(): Pair<RiskLevel, SecuritySignals> {
        val signals = SecuritySignals(
            isRooted = rootDetector.isDeviceRooted(),
            isEmulator = emulatorDetector.isEmulator(),
            isHooked = hookDetector.isHooked(),
            //playIntegrityPassed = integrityChecker.check()
        )

        val risk = when {
            signals.isHooked -> RiskLevel.CRITICAL
            signals.isRooted -> RiskLevel.HIGH
            //!signals.playIntegrityPassed -> RiskLevel.HIGH
            signals.isEmulator -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return risk to signals
    }
}