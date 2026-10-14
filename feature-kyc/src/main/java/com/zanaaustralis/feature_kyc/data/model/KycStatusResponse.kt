package com.zanaaustralis.feature_kyc.data.model

/**
 * @author esteban.rivera01
 */

data class KycStatusResponse(
    val kyc_status: String?, // pending | verified | rejected
    //val submissions: List<KycSubmission>
    val latest_submission: KycSubmission?
)
