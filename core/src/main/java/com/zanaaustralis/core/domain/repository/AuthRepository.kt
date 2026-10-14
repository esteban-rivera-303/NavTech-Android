package com.zanaaustralis.core.domain.repository

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.BaseResponse
import com.zanaaustralis.core.data.remote.model.LoginResponse
import com.zanaaustralis.core.data.remote.model.OnboardingRequest
import com.zanaaustralis.core.data.remote.model.OnboardingResponse
import com.zanaaustralis.core.data.remote.model.RefreshTokenRequest
import com.zanaaustralis.core.data.remote.model.RegisterRequest
import com.zanaaustralis.core.data.remote.model.RegisterResponse
import com.zanaaustralis.core.data.remote.model.RequestCodeRequest
import com.zanaaustralis.core.data.remote.model.UserStatusRequest
import com.zanaaustralis.core.data.remote.model.UserStatusResponse
import com.zanaaustralis.core.data.remote.model.VerifyCodeRequest

/**
 * @author esteban.rivera01
 */


interface AuthRepository {

    /**
     * Registra un nuevo usuario enviando rut, correo y teléfono.
     * Devuelve un RegisterResponse con user_id y posibles códigos de verificación.
     */
    suspend fun registerUser(request: RegisterRequest): Resource<RegisterResponse>

    /**
     * Verifica un código enviado por email o teléfono.
     * Devuelve un BaseResponse indicando éxito o error.
     */
    suspend fun verifyCode(request: VerifyCodeRequest): Resource<BaseResponse>
    suspend fun requestCode(request: RequestCodeRequest): Resource<BaseResponse>

    /**
     * Realiza el enrolamiento del usuario y el dispositivo.
     * Envía datos del dispositivo y la llave pública generada localmente.
     */
    suspend fun onboarding(request: OnboardingRequest): Resource<OnboardingResponse>

    /*
    *
    * */
    suspend fun userStatus(rut: String): Resource<UserStatusResponse>


    /**
     * LOGIN
     * */

    suspend fun login (): Resource<LoginResponse>

    suspend fun logout(): Result<Unit>



    /*
    * Refresh token
    * */
    suspend fun refreshToken(request: RefreshTokenRequest): Resource<AuthSessionResponse>

}