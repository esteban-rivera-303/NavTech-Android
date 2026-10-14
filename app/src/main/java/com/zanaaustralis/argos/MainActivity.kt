package com.zanaaustralis.argos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.zanaaustralis.argos.about.AboutScreen
import com.zanaaustralis.argos.challenges.ChallengeScreen
import com.zanaaustralis.argos.home.HomeScreen
import com.zanaaustralis.argos.home.route.HomeGate
import com.zanaaustralis.argos.MainActivity.Companion.trustedExtra
import com.zanaaustralis.argos.launch.LaunchScreen
import com.zanaaustralis.argos.notification.NotificationEventBus
import com.zanaaustralis.argos.notification.ui.NotificationScreen
import com.zanaaustralis.core.NavResultKeys
import com.zanaaustralis.core.NavRoute
import com.zanaaustralis.core.common.ui.component.ErrorIcon
import com.zanaaustralis.core.common.ui.component.NavResultScreen
import com.zanaaustralis.core.common.ui.component.SuccessIcon
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.common.ui.theme.SystemBarStyle
import com.zanaaustralis.core.common.ui.theme.SystemBarsEffect
import com.zanaaustralis.core.domain.model.User
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.session.SessionState
import com.zanaaustralis.feature_auth.presentation.enrollment.EnrollmentRecoveryScreen
import com.zanaaustralis.feature_auth.presentation.enrollment.EnrollmentScreen
import com.zanaaustralis.feature_auth.presentation.enrollment.EnrollmentTrustedDeviceScreen
import com.zanaaustralis.feature_auth.presentation.pin.PinEntryPointScreen
import com.zanaaustralis.feature_auth.presentation.pin.auth.PinScreen
import com.zanaaustralis.feature_auth.presentation.pin.change.ChangePinScreen
import com.zanaaustralis.feature_auth.presentation.pin.create.CreatePinScreen
import com.zanaaustralis.feature_auth.presentation.pin.recovery.RestorePinScreen
import com.zanaaustralis.feature_auth.presentation.recovery.RecoveryStartScreen
import com.zanaaustralis.feature_auth.presentation.recovery.RecoveryVerificationFlowScreen
import com.zanaaustralis.feature_auth.presentation.register.RegisterScreen
import com.zanaaustralis.feature_auth.presentation.userstatus.UserStatusScreen
import com.zanaaustralis.feature_auth.presentation.verification.VerificationFlowScreen
import com.zanaaustralis.feature_auth.presentation.verify.VerificationType
import com.zanaaustralis.feature_auth.presentation.verify.VerifyScreen
import com.zanaaustralis.feature_challanges.presentation.challenge_list.ChallengeViewModel
import com.zanaaustralis.feature_challanges.presentation.historial.ChallengeHistoryScreen
import com.zanaaustralis.feature_challanges.presentation.navigation.ChallengeDetailRoute
import com.zanaaustralis.feature_challanges.presentation.navigation.ChallengeNavigationBus
import com.zanaaustralis.feature_kyc.presentation.KyCScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    companion object {
        /** activity-alias no exportado: solo la propia app puede abrirlo (notificaciones). */
        private const val NOTIFICATION_ENTRY = "com.zanaaustralis.argos.NotificationEntryActivity"
        const val EXTRA_CHALLENGE_ID = "challenge_id"

        /** Intent para PendingIntents de notificaciones; pasa por el alias no exportado. */
        fun notificationIntent(context: Context): Intent =
            Intent().setClassName(context, NOTIFICATION_ENTRY)

        /** Devuelve el extra solo si el intent llegó por el alias interno, nunca desde otra app. */
        fun Intent?.trustedExtra(key: String): String? =
            this?.takeIf { it.component?.className == NOTIFICATION_ENTRY }?.getStringExtra(key)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Evita capturas de pantalla y la vista previa en recientes (PIN, TOTP, KYC, desafíos).
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        handleIntent(intent)
        requestNotificationPermissionIfNeeded()

        enableEdgeToEdge()

        setContent {
            NavTechTheme {
                AppRoot(intent = intent)
            }
        }
    }

    @Composable
    fun AppRoot(
        intent: Intent?
    ) {
        val navController = rememberNavController()

        val sessionManager: SessionManagerImpl = hiltViewModel<AppRootViewModel>().sessionManager
        val sessionState by sessionManager.sessionState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        // 🔥 manejo global de sesión
        LaunchedEffect(sessionState) {

            if (sessionState is SessionState.Expired || sessionState is SessionState.InvalidToken) {
                snackbarHostState.showSnackbar(
                    message = "Tu sesión expiró"
                )
                navController.navigate(NavRoute.Launch.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { padding ->

            Box(
                modifier = Modifier.padding(0.dp)
            ) {
                AppNavHost(
                    navController = navController,
                    intent = intent
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val challengeId = intent.trustedExtra(EXTRA_CHALLENGE_ID)
        if (challengeId != null) {
            ChallengeNavigationBus.emit(challengeId)
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Permiso concedido
                Log.d("Permissions", "Notifications enabled")
            } else {
                // Permiso denegado (no bloquea la app)
                Log.d("Permissions", "Notifications denied")
            }
        }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return // No aplica
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS

        val isGranted = ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            notificationPermissionLauncher.launch(permission)
        }
    }

}

fun handleNotificationIntent(
    intent: Intent?,
    navController: NavHostController
) {
    val challengeId = intent.trustedExtra(MainActivity.EXTRA_CHALLENGE_ID) ?: return

    navController.navigate(
        NavRoute.ChallengeDetail.build(challengeId)
    ) {
        popUpTo(NavRoute.Home.route)
        launchSingleTop = true
    }
}

fun NavHostController.toHome() {
    this.navigate(NavRoute.Home.route) {
        popUpTo(0) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), intent: Intent?) {

    LaunchedEffect(Unit) {
        handleNotificationIntent(intent, navController)
    }

    LaunchedEffect(Unit) {
        NotificationEventBus.challengeEvents.collect { challengeId ->
            navController.navigate(
                NavRoute.ChallengeDetail.build(challengeId)
            ) {
                launchSingleTop = true
            }
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val style = screens.find { it.route == currentRoute }?.systemBarStyle
        ?: SystemBarStyle.LIGHT

    SystemBarsEffect(style)

    NavHost(
        navController = navController,
        startDestination = NavRoute.Launch.route // Cambiar el punto de la navegacionx
    ) {

        // Launch
        composable(NavRoute.Launch.route) {
            LaunchScreen(navController = navController)
        }

        // Home
        composable(NavRoute.Home.route) {
            HomeScreen(
                navController = navController,

                onNotificationsClick = {
                    navController.navigate(NavRoute.Notifications.route)
                },

                onChallengeClick = { challenge ->
                    navController.navigate(NavRoute.ChallengeDetail.build(challenge.id))
                },

                onSeeAllChallenges = {
                    navController.navigate(NavRoute.Challenges.route)
                }
            )
        }
        // HomeGate
        composable(NavRoute.HomeGate.route) {
            HomeGate(
                navController
            )
        }
        // Register
        composable(
            route = NavRoute.Register.route,
            arguments = listOf(
                navArgument("rut") { type = NavType.StringType },
            )
        ) { backStackEntry ->

            val rut = backStackEntry.arguments?.getString("rut") ?: ""

            RegisterScreen(
                rutPrefill = rut,
                onDone = { email, phone, rut ->
                    val user = User(
                        id = "",
                        rut = rut,
                        email = email,
                        phone = phone,
                        verifiedPhone = false,
                        verifiedEmail = false,

                        )
                    val userJson = Uri.encode(Gson().toJson(user))
                    navController.navigate(
                        NavRoute.VerificationFlow.build(userJson, rut)
                    ) {
                        popUpTo(NavRoute.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Flujo principal
        composable(NavRoute.UserStatus.route) {
            UserStatusScreen(
                navController = navController,
                onNavigateToVerification = { userJson, rut ->
                    navController.navigate(
                        NavRoute.VerificationFlow.build(userJson, rut)
                    ) {
                        popUpTo(NavRoute.Register.route) { inclusive = true }
                    }
                },
                onNavigateToRecoveryVerification = { userJson ->

                    /* navController.navigate(NavRoute.RecoveryStart.route) {
                         popUpTo(NavRoute.UserStatus.route) { inclusive = true }
                     }*/
                    navController.navigate(
                        NavRoute.RecoveryStart.build(userJson)
                    ) {
                        popUpTo(NavRoute.Register.route) { inclusive = true }
                    }
                },
                onComplete = {
                    navController.navigate(NavRoute.HomeGate.route)
                }
            )
        }
        // Verification
        composable(
            route = NavRoute.VerificationFlow.route,
            arguments = listOf(
                navArgument("userJson") { type = NavType.StringType },
                navArgument("rut") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val userJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val rut = backStackEntry.arguments?.getString("rut") ?: ""

            val user = Gson().fromJson(userJson, User::class.java)

            VerificationFlowScreen(
                user = user,
                rut = rut,
                onComplete = {
                    navController.navigate(
                        NavRoute.EnrollmentDevice.build(
                            rut = user.rut,
                            email = user.email,
                            phone = user.phone
                        )
                    )
                }
            )
        }

        // Enrollment
        composable(
            route = NavRoute.EnrollmentDevice.route,
            arguments = listOf(
                navArgument("rut") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("phone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rut = backStackEntry.arguments?.getString("rut") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            EnrollmentScreen(
                rut = rut,
                email = email,
                phone = phone,
                onDone = {
                    navController.navigate(NavRoute.CreatePIN.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        // Enrollment Recovery
        composable(
            route = NavRoute.EnrollmentRecoveryDevice.route,
            arguments = listOf(
                navArgument("rut") { type = NavType.StringType },
            )
        ) { backStackEntry ->

            val rut = backStackEntry.arguments?.getString("rut") ?: ""

            EnrollmentRecoveryScreen(
                rut = rut,
                onDone = {
                    navController.navigate(NavRoute.CreatePIN.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        // Enrollment Trusted Device
        composable(
            route = NavRoute.EnrollmentTrustedDevice.route,
            arguments = listOf(
                navArgument("rut") { type = NavType.StringType },
            )
        ) { backStackEntry ->

            val rut = backStackEntry.arguments?.getString("rut") ?: ""

            EnrollmentTrustedDeviceScreen(
                rut = rut,
                onDone = {
                    navController.navigate(NavRoute.CreatePIN.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        //Verificación
        composable(
            route = NavRoute.Verify.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("target") { type = NavType.StringType }
            )
        ) { backStack ->
            val type = backStack.arguments?.getString("type")!!
            val target = backStack.arguments?.getString("target")!!

            VerifyScreen(
                verificationType = if (type == "email") VerificationType.EMAIL else VerificationType.PHONE,
                targetValue = target,
                onNext = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("VERIFY_RESULT", Pair(type, true))

                    navController.popBackStack(
                        NavRoute.UserStatus.route,
                        inclusive = false
                    )
                }
            )
        }

        // Validación de identidad
        composable(
            route = NavRoute.Kyc.route,
            arguments = listOf(
                navArgument("rut") { type = NavType.StringType },
                navArgument("deviceId") { type = NavType.StringType }
            )
        ) { backStack ->

            val rut = backStack.arguments?.getString("rut")!!
            KyCScreen(
                rut = rut,
                onSuccess = {
                    navController.navigate(NavRoute.UserStatus.route)
                }
            )
        }

        // Dialogs
        composable(route = NavRoute.DialogSuccess.route) {
            NavResultScreen(
                title = "Operación exitosa",
                subtitle = "Tu desafío fue aprobado correctamente",
                buttonText = "Continuar",
                onClick = {
                    navController.toHome()
                },
                icon = { SuccessIcon() }
            )
        }

        composable(route = NavRoute.DialogError.route) {
            NavResultScreen(
                title = "Algo salió mal",
                subtitle = "No pudimos completar la operación",
                buttonText = "Intentar nuevamente",
                onClick = {
                    navController.toHome()
                },
                icon = { ErrorIcon() }
            )
        }
        // MENU
        composable(route = NavRoute.MenuHistory.route) {
            ChallengeHistoryScreen {
                navController.popBackStack()
            }
        }


        // PIN
        composable(
            route = NavRoute.PIN_SECTION.route,
            arguments = listOf(
                navArgument("challengeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val challengeId =
                backStackEntry.arguments?.getString("challengeId")
                    ?: return@composable

            PinScreen(
                onPinValidated = {
                    // ✅ DEVOLVER RESULTADO AL HOME
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            NavResultKeys.PIN_VALIDATED_CHALLENGE_ID,
                            challengeId
                        )

                    navController.popBackStack()
                },
                onCancel = {

                    navController.navigate(NavRoute.ResetPIN.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }


        // Recovery

        composable(
            route = NavRoute.RecoveryStart.route,
            arguments = listOf(
                navArgument("userJson") { type = NavType.StringType },
            )
        ) { backStackEntry ->

            val userJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val user = Gson().fromJson(userJson, User::class.java)

            RecoveryStartScreen(
                user = user,
                { rut, email, phone ->
                    navController.navigate(NavRoute.RecoveryVerify.build(rut, email, phone))
                }
            )
        }


        // Verify Recovery
        composable(NavRoute.RecoveryVerify.route) { backStack ->
            val rut = backStack.arguments?.getString("rut")!!
            val email = backStack.arguments?.getString("email")!!
            val phone = backStack.arguments?.getString("phone")!!

            RecoveryVerificationFlowScreen(rut, email, phone) {
                navController.navigate(NavRoute.EnrollmentRecoveryDevice.build(rut))
            }
        }

        /**
         * FLUJO DE DESAFIO
         *
         * */

        // TODO REVISAR ESTA RUTA
        composable("challenges") {
            ChallengeScreen(
                viewModel = hiltViewModel(),
                onValidateWithPIN = { id ->
                    navController.navigate("pin/$id")
                },
                onValidateWithTOTP = { id ->
                    navController.navigate("totp/$id")
                },
                onValidateLocation = {}
            )
        }

        composable(
            route = NavRoute.ChallengeDetail.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->

            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: return@composable
            val challengeViewModel: ChallengeViewModel = hiltViewModel()
            ChallengeDetailRoute(
                navController = navController,
                viewModel = challengeViewModel,
                challengeId = challengeId
            )
        }

        /**
         * PIN Section
         */
        composable(NavRoute.MenuPIN.route) {
            PinEntryPointScreen(
                onCreatePin = { navController.navigate(NavRoute.CreatePIN.route) },
                onChangePin = { navController.navigate(NavRoute.ChangePIN.route) },
                onForgotPin = { navController.navigate(NavRoute.ResetPIN.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoute.CreatePIN.route) {
            CreatePinScreen(
                onPinCreated = {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoute.ChangePIN.route) {
            ChangePinScreen(
                navController = navController
            )
        }

        composable(NavRoute.ResetPIN.route) {
            RestorePinScreen(
                navController = navController
            )
        }
        composable(NavRoute.Notifications.route) {
            NotificationScreen{
                navController.popBackStack()
            }
        }

        // Drawer Menu
        composable(NavRoute.MenuAbout.route) {
            AboutScreen() {
                navController.popBackStack()
            }
        }
    }
}
data class ScreenConfig(
    val route: String,
    val systemBarStyle: SystemBarStyle
)

val screens = listOf(
    ScreenConfig(NavRoute.Home.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.HomeGate.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.Launch.route, SystemBarStyle.PRIMARY),
    ScreenConfig(NavRoute.UserStatus.route, SystemBarStyle.PRIMARY_BLACK),
    ScreenConfig(NavRoute.EnrollmentTrustedDevice.route, SystemBarStyle.PRIMARY_BLACK),
    ScreenConfig(NavRoute.EnrollmentDevice.route, SystemBarStyle.PRIMARY_BLACK),
    ScreenConfig(NavRoute.EnrollmentRecoveryDevice.route, SystemBarStyle.PRIMARY_BLACK),

    ScreenConfig(NavRoute.VerificationFlow.route, SystemBarStyle.PRIMARY),
    ScreenConfig(NavRoute.Verify.route, SystemBarStyle.PRIMARY),
    ScreenConfig(NavRoute.Kyc.route, SystemBarStyle.PRIMARY),

    ScreenConfig(NavRoute.MenuPIN.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.MenuHistory.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.MenuAbout.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.MenuLogout.route, SystemBarStyle.PRIMARY),

    ScreenConfig(NavRoute.Notifications.route, SystemBarStyle.LIGHT),

    ScreenConfig(NavRoute.DialogSuccess.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.DialogError.route, SystemBarStyle.LIGHT),

    ScreenConfig(NavRoute.CreatePIN.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.ChangePIN.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.ResetPIN.route, SystemBarStyle.LIGHT),

    ScreenConfig(NavRoute.RecoveryStart.route, SystemBarStyle.PRIMARY),
    ScreenConfig(NavRoute.RecoveryVerify.route, SystemBarStyle.PRIMARY),

    ScreenConfig(NavRoute.Challenges.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.ChallengeDetail.route, SystemBarStyle.LIGHT),

    ScreenConfig(NavRoute.Totp.route, SystemBarStyle.LIGHT),
    ScreenConfig(NavRoute.PIN_SECTION.route, SystemBarStyle.LIGHT),

    ScreenConfig(NavRoute.Register.route, SystemBarStyle.PRIMARY),


)

