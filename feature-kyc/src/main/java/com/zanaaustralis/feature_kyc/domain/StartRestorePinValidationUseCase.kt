package com.zanaaustralis.feature_kyc.domain

import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.feature_kyc.data.IdentityValidationRepository
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class StartRestorePinValidationUseCase @Inject constructor(
    private val repo: IdentityValidationRepository,
    private val deviceIdentity: DeviceIdentityManager
) {
    suspend operator fun invoke() =
        repo.startRestorePin(deviceIdentity.getOrCreateDeviceUUID())
}
