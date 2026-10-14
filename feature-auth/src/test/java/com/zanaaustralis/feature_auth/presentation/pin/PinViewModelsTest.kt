package com.zanaaustralis.feature_auth.presentation.pin

import com.zanaaustralis.feature_auth.data.PinRepository
import com.zanaaustralis.feature_auth.data.PinVerificationResult
import com.zanaaustralis.feature_auth.presentation.ChangePinStep
import com.zanaaustralis.feature_auth.presentation.pin.auth.PinViewModel
import com.zanaaustralis.feature_auth.presentation.pin.change.ChangePinViewModel
import com.zanaaustralis.feature_auth.presentation.pin.create.CreatePinViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PinViewModelsTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakePinRepository(pin = "123456")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- Validación del PIN ----------

    @Test
    fun `correct pin calls success and resets the state`() = runTest(dispatcher) {
        val vm = PinViewModel(repository)
        var succeeded = false

        vm.validatePin("123456") { succeeded = true }

        assertTrue(succeeded)
        assertNull(vm.uiState.value.error)
        assertEquals(3, vm.uiState.value.remainingAttempts)
    }

    @Test
    fun `wrong pin shows remaining attempts and locks on the last one`() = runTest(dispatcher) {
        val vm = PinViewModel(repository)

        vm.validatePin("000000") {}
        assertEquals(2, vm.uiState.value.remainingAttempts)
        assertEquals("PIN incorrecto. Te quedan 2 intentos.", vm.uiState.value.error)

        vm.validatePin("000000") {}
        vm.validatePin("000000") {}

        assertTrue(vm.uiState.value.blocked)
        assertEquals("Has agotado los intentos. Debes restaurar tu PIN.", vm.uiState.value.error)
    }

    @Test
    fun `blocked state survives a new view model and ignores further attempts`() = runTest(dispatcher) {
        repository.failedAttempts = 3

        val vm = PinViewModel(repository)
        var succeeded = false
        vm.validatePin("123456") { succeeded = true }

        assertTrue(vm.uiState.value.blocked)
        assertFalse(succeeded)
        assertEquals(0, repository.verifyCalls)
    }

    @Test
    fun `reset reloads persisted attempts instead of restoring them`() = runTest(dispatcher) {
        val vm = PinViewModel(repository)
        vm.validatePin("000000") {}

        vm.reset()

        assertEquals(2, vm.uiState.value.remainingAttempts)
        assertFalse(vm.uiState.value.blocked)
    }

    // ---------- Creación ----------

    @Test
    fun `create pin asks for confirmation and saves when both match`() = runTest(dispatcher) {
        val vm = CreatePinViewModel(repository)
        var created = false

        vm.onPinCompleted("654321") {}
        assertEquals(CreatePinStep.CONFIRM, vm.step.value)

        vm.onPinCompleted("654321") { created = true }

        assertTrue(created)
        assertEquals("654321", repository.pin)
    }

    @Test
    fun `create pin restarts when confirmation does not match`() = runTest(dispatcher) {
        val vm = CreatePinViewModel(repository)

        vm.onPinCompleted("654321") {}
        vm.onPinCompleted("111111") {}

        assertEquals(CreatePinStep.ENTER, vm.step.value)
        assertEquals("Los PIN no coinciden", vm.error.value)
        assertEquals("123456", repository.pin)
    }

    @Test
    fun `create pin rejects pins that are not six digits`() = runTest(dispatcher) {
        val vm = CreatePinViewModel(repository)

        vm.onPinCompleted("1234") {}
        assertEquals("El PIN debe tener 6 dígitos", vm.error.value)

        vm.onPinCompleted("12345a") {}
        assertEquals(CreatePinStep.ENTER, vm.step.value)
    }

    @Test
    fun `cancel clears the create flow`() = runTest(dispatcher) {
        val vm = CreatePinViewModel(repository)
        vm.onPinCompleted("654321") {}

        vm.onCancel()

        assertEquals(CreatePinStep.ENTER, vm.step.value)
        assertNull(vm.error.value)
    }

    // ---------- Cambio ----------

    @Test
    fun `change pin goes through verify enter confirm and done`() = runTest(dispatcher) {
        val vm = ChangePinViewModel(repository)
        var changed = false

        assertEquals(ChangePinStep.VERIFY_CURRENT, vm.step.value)
        vm.onCurrentPinValidated()
        vm.onNewPinEntered("222222")
        assertEquals(ChangePinStep.CONFIRM_NEW, vm.step.value)
        vm.onNewPinConfirmed("222222") { changed = true }

        assertTrue(changed)
        assertEquals(ChangePinStep.DONE, vm.step.value)
        assertEquals("222222", repository.pin)
    }

    @Test
    fun `change pin mismatch goes back to entering the new pin`() = runTest(dispatcher) {
        val vm = ChangePinViewModel(repository)
        vm.onCurrentPinValidated()
        vm.onNewPinEntered("222222")

        vm.onNewPinConfirmed("333333") {}

        assertEquals(ChangePinStep.ENTER_NEW, vm.step.value)
        assertEquals("Los PIN no coinciden", vm.error.value)
        assertEquals("123456", repository.pin)
    }

    @Test
    fun `change pin rejects invalid new pins and errors can be cleared`() = runTest(dispatcher) {
        val vm = ChangePinViewModel(repository)
        vm.onCurrentPinValidated()

        vm.onNewPinEntered("12")
        assertEquals("El PIN debe tener 6 dígitos", vm.error.value)
        assertEquals(ChangePinStep.ENTER_NEW, vm.step.value)

        vm.clearError()
        assertNull(vm.error.value)
    }

    private class FakePinRepository(var pin: String?) : PinRepository {
        var failedAttempts = 0
        var verifyCalls = 0
        private val maxAttempts = 3

        override suspend fun hasPin() = pin != null

        override suspend fun savePin(pin: String) {
            this.pin = pin
            failedAttempts = 0
        }

        override suspend fun verifyPin(pin: String): PinVerificationResult {
            verifyCalls++
            if (failedAttempts >= maxAttempts) return PinVerificationResult.Locked
            if (pin == this.pin) {
                failedAttempts = 0
                return PinVerificationResult.Valid
            }
            failedAttempts++
            return if (failedAttempts >= maxAttempts) PinVerificationResult.Locked
            else PinVerificationResult.Invalid(maxAttempts - failedAttempts)
        }

        override suspend fun remainingAttempts() = (maxAttempts - failedAttempts).coerceAtLeast(0)

        override suspend fun clearPin() {
            pin = null
            failedAttempts = 0
        }
    }
}
