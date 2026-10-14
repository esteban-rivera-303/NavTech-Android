package com.zanaaustralis.core.domain.usescases

import android.os.Build
import android.util.Log
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.model.BaseResponse
import com.zanaaustralis.core.data.remote.model.LoginResponse
import com.zanaaustralis.core.data.remote.model.OnboardingRequest
import com.zanaaustralis.core.data.remote.model.OnboardingResponse
import com.zanaaustralis.core.data.remote.model.RegisterRequest
import com.zanaaustralis.core.data.remote.model.RegisterResponse
import com.zanaaustralis.core.data.remote.model.RequestCodeRequest
import com.zanaaustralis.core.data.remote.model.UserStatusResponse
import com.zanaaustralis.core.data.remote.model.VerifyCodeRequest
import com.zanaaustralis.core.domain.repository.AuthRepository
import com.zanaaustralis.core.secure.crypto.AndroidKeyStoreManager
import com.zanaaustralis.core.secure.crypto.UserAuthKeyStore
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.store.notification.PushTokenStore

/**
 * @author esteban.rivera01
 */

data class AuthUseCases(
    val loginUserUseCase: LoginUserUseCase,
    val registerUser: RegisterUserUseCase,
    val verifyCode: VerifyCodeUseCase,
    val onboarding: OnboardingUseCase,
    val userStatus: UserStatusUseCase,
    val requestUseCase: RequestCodeUseCase
)
class LoginUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<LoginResponse> {
        return repository.login()
    }
}

class RegisterUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(request: RegisterRequest): Resource<RegisterResponse> {
        return repository.registerUser(request)
    }
}

class VerifyCodeUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(request: VerifyCodeRequest): Resource<BaseResponse> {
        return repository.verifyCode(request)
    }
}

class RequestCodeUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        request: RequestCodeRequest
    ): Resource<BaseResponse> {
        return repository.requestCode(request)
    }
}

class OnboardingUseCase(
    private val androidKeyStoreManager: AndroidKeyStoreManager,
    private val deviceIdentityManager: DeviceIdentityManager,
    private val pushTokenStore: PushTokenStore,
    private val repository: AuthRepository,
    private val userAuthKeyStore: UserAuthKeyStore) {
    suspend operator fun invoke(rut: String): Resource<OnboardingResponse> {
        if (rut.isEmpty()) Resource.Error("Empty rut")

        val deviceUuid = deviceIdentityManager.getOrCreateDeviceUUID()
        deviceIdentityManager.saveFingerprint()
        androidKeyStoreManager.generateKeyPairIfNeeded()
        val publicPem = androidKeyStoreManager.publicKeyToPem()

        val request = OnboardingRequest(
            rut = rut,
            device_uuid = deviceUuid,
            os = "Android ${Build.VERSION.RELEASE}",
            model = Build.MODEL ?: "unknown",
            fingerprint = deviceIdentityManager.getFingerprint(),
            platform = "android",
            public_key = publicPem,
            device_info = deviceIdentityManager.getDeviceInfo(),
            fcm_token = pushTokenStore.get()?: "",
            user_auth_public_key = userAuthKeyStore.provisionForEnrollment()
        )
        return repository.onboarding(request)
    }
}

class UserStatusUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(rut :String): Resource<UserStatusResponse> {
        return repository.userStatus(rut)
    }
}



