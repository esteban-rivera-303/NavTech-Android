package com.zanaaustralis.feature_kyc.data

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.common.parseHttpError
import com.zanaaustralis.feature_kyc.data.model.StartValidationRequest
import com.zanaaustralis.feature_kyc.data.model.StartValidationResponse
import com.zanaaustralis.feature_kyc.data.model.ValidationStatusResponse
import com.zanaaustralis.feature_kyc.data.model.VerificationData
import com.zanaaustralis.feature_kyc.data.model.VerifyValidationRequest
import com.zanaaustralis.feature_kyc.data.remote.IdentityValidationApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class IdentityValidationRepository @Inject constructor(
    private val api: IdentityValidationApi
) {

    /*  suspend fun startRestorePin(deviceUuid: String) =
          api.startValidation(
              StartValidationRequest(
                  purpose = ValidationPurpose.RESTORE_PIN,
                  validation_method = "BIOMETRIC",
                  device_uuid = deviceUuid
              )
          )*/


    suspend fun startRestorePin(
        deviceUuid: String
    ): Resource<StartValidationResponse> {
        return try {
            val response = api.startValidation(
                StartValidationRequest(
                    purpose = ValidationPurpose.RESTORE_PIN,
                    validation_method = "KYC_REVALIDATION",
                    device_uuid = deviceUuid
                )
            )
            Resource.Success(response)
        } catch (e: HttpException) {
            Resource.Error(
                error = parseHttpError(e),
                code = 0
            )
        } catch (e: IOException) {
            Resource.Error("Error de red. Revisa tu conexión.")
        } catch (e: Exception) {
            Resource.Error("Error inesperado")
        }
    }


    suspend fun verify(
        validationId: String,
        biometricToken: String,
        deviceUuid: String
    ) =
        api.verifyValidation(
            VerifyValidationRequest(
                validation_id = validationId,
                verification_data = VerificationData(
                    biometric_token = biometricToken,
                    device_uuid = deviceUuid
                )
            )
        )

    suspend fun getStatus(
        validationId: String
    ): Resource<ValidationStatusResponse> {
        return try {
            val response = api.getStatus(validationId)
            Resource.Success(response)
        } catch (e: HttpException) {
            Resource.Error(parseHttpError(e), e.code())
        } catch (e: IOException) {
            Resource.Error("Error de red")
        }
    }

}


enum class ValidationPurpose {
    RESTORE_PIN,
    DEVICE_CHANGE,
    HIGH_RISK_ACTION,
    ACCOUNT_RECOVERY
}

enum class ValidationMethod {
    BIOMETRIC,
    PIN,
    OTP,
    KYC_REVALIDATION,
    MULTI_FACTOR
}
