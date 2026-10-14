package com.zanaaustralis.feature_challanges.presentation.challenge_list

import android.location.Location
import android.util.Log
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.location.LocationProvider
import com.zanaaustralis.core.secure.SecurityFlags
import com.zanaaustralis.core.secure.crypto.UserAuthKeyStore
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.session.SessionStateNotification
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_auth.ChallengeLocalAuthStore
import com.zanaaustralis.feature_auth.data.local.TotpManager
import com.zanaaustralis.feature_auth.presentation.totp.ObserveTotpUseCase
import com.zanaaustralis.feature_challanges.data.model.LocationData
import com.zanaaustralis.feature_challanges.domain.AuthorizationStep
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.domain.usecase.GetPendingChallengesUseCase
import com.zanaaustralis.feature_challanges.domain.usecase.RespondChallengeUseCase
import com.zanaaustralis.feature_challanges.presentation.challenge_detail.ChallengePermissionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val sessionManager = mockk<SessionManagerImpl>(relaxed = true)
    private val getPending = mockk<GetPendingChallengesUseCase>()
    private val localAuthStore = FakeLocalAuthStore()
    private val respond = mockk<RespondChallengeUseCase>()
    private val observeTotp = mockk<ObserveTotpUseCase>()
    private val totpManager = mockk<TotpManager>()
    private val userAuthKeyStore = mockk<UserAuthKeyStore>()

    private val pinChallenge = challenge(id = "c1", pin = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { sessionManager.sessionNotificationState } returns MutableStateFlow(SessionStateNotification())
        coEvery { sessionManager.getSession() } returns AuthSessionResponse("token", "refresh", 0L)
        coEvery { sessionManager.getUserData() } returns null
        every { observeTotp.invoke() } returns emptyFlow()
        coEvery { getPending.invoke("token") } returns Resource.Success(listOf(pinChallenge))
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        Dispatchers.resetMain()
    }

    private fun challenge(
        id: String,
        pin: Boolean = false,
        location: Boolean = false,
        biometric: Boolean = false
    ) = ChallengeItem(
        id = id, title = "t", description = null, date = "", appName = "app", amount = null,
        requiresBiometric = biometric, requiresTotp = false, requiresLocation = location, requiresPin = pin,
        createdAt = "", expiresAt = "", risk_level = "low"
    )

    private fun viewModel(): ChallengeViewModel =
        ChallengeViewModel(
            sessionManager, getPending, localAuthStore, respond, observeTotp, totpManager, userAuthKeyStore
        ).also { it.stopPolling() }

    private fun TestScope.collectSteps(vm: ChallengeViewModel): MutableList<AuthorizationStep> {
        val steps = mutableListOf<AuthorizationStep>()
        backgroundScope.launch(dispatcher) { vm.authorizationStep.toList(steps) }
        return steps
    }

    // ---------- Carga ----------

    @Test
    fun `load merges locally validated factors into server challenges`() = runTest(dispatcher) {
        localAuthStore.factors["c1"] = setOf(AuthFactor.PIN)

        val vm = viewModel()

        assertEquals(setOf(AuthFactor.PIN), vm.challenges.value.single().authorizedFactors)
        assertNull(vm.errorMessage.value)
        assertEquals(false, vm.isLoading.value)
    }

    @Test
    fun `load without session shows session error`() = runTest(dispatcher) {
        coEvery { sessionManager.getSession() } returns null

        val vm = viewModel()

        assertEquals("Sesión no encontrada. Inicia sesión nuevamente.", vm.errorMessage.value)
        assertTrue(vm.challenges.value.isEmpty())
    }

    @Test
    fun `load with 403 invalidates the session`() = runTest(dispatcher) {
        coEvery { getPending.invoke("token") } returns Resource.Error("forbidden", 403)

        val vm = viewModel()

        coVerify { sessionManager.onInvalidSessionDetected() }
        assertEquals("forbidden", vm.errorMessage.value)
    }

    @Test
    fun `load with other errors keeps the session`() = runTest(dispatcher) {
        coEvery { getPending.invoke("token") } returns Resource.Error("timeout", 500)

        val vm = viewModel()

        coVerify(exactly = 0) { sessionManager.onInvalidSessionDetected() }
        assertEquals("timeout", vm.errorMessage.value)
    }

    // ---------- Autorización ----------

    @Test
    fun `authorize with missing factors asks for the next factor and does not respond`() = runTest(dispatcher) {
        val vm = viewModel()
        val steps = collectSteps(vm)

        vm.authorizeChallenge("c1")

        assertEquals(listOf(AuthorizationStep.RequirePin("c1")), steps)
        coVerify(exactly = 0) { respond.approve(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `authorize when ready approves with pin metadata and removes the challenge`() = runTest(dispatcher) {
        coEvery { respond.approve("c1", null, null, mapOf("pin_used" to "true"), null) } returns Resource.Success(true)
        val vm = viewModel()
        val steps = collectSteps(vm)

        vm.markFactorAsAuthorized("c1", AuthFactor.PIN)
        vm.authorizeChallenge("c1")

        assertEquals(listOf(AuthorizationStep.Completed("c1")), steps)
        assertTrue(vm.challenges.value.isEmpty())
        assertTrue("c1" in localAuthStore.cleared)
    }

    @Test
    fun `failed approval restores pending status and reports the error`() = runTest(dispatcher) {
        coEvery { respond.approve(any(), any(), any(), any(), any()) } returns Resource.Error("rechazado")
        val vm = viewModel()
        val steps = collectSteps(vm)

        vm.markFactorAsAuthorized("c1", AuthFactor.PIN)
        vm.authorizeChallenge("c1")

        assertEquals(listOf(AuthorizationStep.Failed("rechazado")), steps)
        assertEquals("pending", vm.challenges.value.single().status)
        assertEquals("rechazado", vm.errorMessage.value)
    }

    @Test
    fun `reject sends no factors location or metadata`() = runTest(dispatcher) {
        coEvery { respond.reject("c1", null, null, null) } returns Resource.Success(true)
        val vm = viewModel()

        vm.rejectChallenge("c1")

        coVerify { respond.reject("c1", null, null, null) }
        assertTrue(vm.challenges.value.isEmpty())
    }

    @Test
    fun `unknown challenge ids are ignored`() = runTest(dispatcher) {
        val vm = viewModel()
        val steps = collectSteps(vm)

        vm.authorizeChallenge("missing")
        vm.rejectChallenge("missing")

        assertTrue(steps.isEmpty())
    }

    // ---------- Factores ----------

    @Test
    fun `marking factors persists them except location`() = runTest(dispatcher) {
        coEvery { getPending.invoke("token") } returns Resource.Success(listOf(challenge("c1", pin = true, location = true)))
        val vm = viewModel()

        vm.markFactorAsAuthorized("c1", AuthFactor.LOCATION)
        vm.markFactorAsAuthorized("c1", AuthFactor.PIN)

        assertEquals(setOf(AuthFactor.PIN), localAuthStore.factors["c1"])
        assertEquals(setOf(AuthFactor.PIN, AuthFactor.LOCATION), vm.challenges.value.single().authorizedFactors)
    }

    @Test
    fun `correct totp marks the factor and is sent on approval`() = runTest(dispatcher) {
        every { totpManager.getCurrentCode() } returns "123456"
        coEvery { respond.approve(any(), "123456", any(), any(), any()) } returns Resource.Success(true)
        val vm = viewModel()

        vm.onTotpEntered("c1", "123456")
        assertTrue(AuthFactor.TOTP in localAuthStore.factors.getValue("c1"))

        vm.markFactorAsAuthorized("c1", AuthFactor.PIN)
        vm.authorizeChallenge("c1")

        coVerify { respond.approve("c1", "123456", any(), any(), any()) }
    }

    @Test
    fun `wrong totp is ignored`() = runTest(dispatcher) {
        every { totpManager.getCurrentCode() } returns "123456"
        val vm = viewModel()

        vm.onTotpEntered("c1", "000000")

        assertTrue(vm.challenges.value.single().authorizedFactors.isEmpty())
    }

    @Test
    fun `forced factor maps to its step and default follows missing factors`() = runTest(dispatcher) {
        assumeFalse(SecurityFlags.signatureV4Enabled)
        val vm = viewModel()
        val steps = collectSteps(vm)

        vm.requestNextStep("c1", AuthFactor.TOTP)
        vm.requestNextStep("c1", AuthFactor.BIOMETRIC)
        vm.requestNextStep("c1")
        vm.markFactorAsAuthorized("c1", AuthFactor.PIN)
        vm.requestNextStep(vm.challenges.value.single())

        assertEquals(
            listOf(
                AuthorizationStep.RequireTotp("c1"),
                AuthorizationStep.RequireBiometric("c1"),
                AuthorizationStep.RequirePin("c1"),
                AuthorizationStep.ReadyToAuthorize("c1")
            ),
            steps
        )
    }

    // ---------- Ubicación ----------

    @Test
    fun `entering a location challenge asks for permission or location`() = runTest(dispatcher) {
        coEvery { getPending.invoke("token") } returns Resource.Success(listOf(challenge("c1", location = true)))
        val vm = viewModel()

        vm.onEnterChallengeDetail("c1", hasLocationPermission = false)
        assertEquals(ChallengePermissionState.RequireLocationPermission, vm.permissionState.value)

        vm.onEnterChallengeDetail("c1", hasLocationPermission = true)
        assertEquals(ChallengePermissionState.RequestLocation, vm.permissionState.value)

        vm.onLocationPermissionResult(granted = false)
        assertTrue(vm.permissionState.value is ChallengePermissionState.Error)
    }

    @Test
    fun `fetched location authorizes the factor and is sent on approval`() = runTest(dispatcher) {
        coEvery { getPending.invoke("token") } returns Resource.Success(listOf(challenge("c1", location = true)))
        val location = mockk<Location> {
            every { latitude } returns -33.4
            every { longitude } returns -70.6
            every { accuracy } returns 5f
        }
        val provider = mockk<LocationProvider> { coEvery { getLastLocation() } returns location }
        val sentLocation = slot<LocationData>()
        val sentMetadata = slot<Map<String, String>>()
        coEvery { respond.approve("c1", null, capture(sentLocation), capture(sentMetadata), null) } returns Resource.Success(true)
        val vm = viewModel()

        vm.fetchLocation("c1", provider)
        assertEquals(ChallengePermissionState.Ready, vm.permissionState.value)

        vm.authorizeChallenge("c1")

        assertEquals(LocationData(-33.4, -70.6, 5.0), sentLocation.captured)
        assertEquals("true", sentMetadata.captured["location"])
    }

    @Test
    fun `missing location reports an error`() = runTest(dispatcher) {
        val provider = mockk<LocationProvider> { coEvery { getLastLocation() } returns null }
        val vm = viewModel()

        vm.fetchLocation("c1", provider)

        assertEquals(ChallengePermissionState.Error("No se pudo obtener la ubicación"), vm.permissionState.value)
    }

    private class FakeLocalAuthStore : ChallengeLocalAuthStore {
        val factors = mutableMapOf<String, Set<AuthFactor>>()
        val cleared = mutableListOf<String>()

        override suspend fun locallyValidatedFactors(challengeId: String) = factors[challengeId].orEmpty()
        override suspend fun setAuthorizedFactors(challengeId: String, factors: Set<AuthFactor>) {
            this.factors[challengeId] = factors
        }
        override suspend fun clear(challengeId: String) {
            cleared += challengeId
            factors.remove(challengeId)
        }
        override suspend fun setTotpCode(challengeId: String, code: String) = Unit
        override suspend fun getTotpCode(challengeId: String): String? = null
        override suspend fun clearTotpCode(challengeId: String) = Unit
    }
}
