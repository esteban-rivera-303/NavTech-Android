package com.zanaaustralis.argos.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.zanaaustralis.argos.drawer_menu.DrawerItem
import com.zanaaustralis.argos.drawer_menu.HomeDrawerContent
import com.zanaaustralis.core.NavResultKeys
import com.zanaaustralis.core.NavRoute
import com.zanaaustralis.core.common.firstName
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.secure.biometric.BiometricHelper
import com.zanaaustralis.core.session.SessionStateNotification
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_auth.presentation.totp.TotpCardView
import com.zanaaustralis.feature_challanges.domain.AuthorizationStep
import com.zanaaustralis.feature_challanges.presentation.components.promptChallengeApproval
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.presentation.challenge_list.ChallengeViewModel
import com.zanaaustralis.feature_challanges.presentation.challenge_list.HomeUiState
import kotlinx.coroutines.launch


/**
 * @author esteban.rivera01
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: ChallengeViewModel = hiltViewModel(),
    onNotificationsClick: () -> Unit,
    onChallengeClick: (ChallengeItem) -> Unit,
    onSeeAllChallenges: () -> Unit
) {
    val pendingChallenges by viewModel.challenges.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userDataState by viewModel.uiState.collectAsState()
    val session by viewModel.session.collectAsState()
    val state by viewModel.totpState.collectAsState()


    val context = LocalContext.current
    val activity = context as FragmentActivity
    val biometricHelper = remember { BiometricHelper(context) }
    val clipboardManager = LocalClipboardManager.current

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    val savedStateHandle =
        navController.currentBackStackEntry
            ?.savedStateHandle

    val pinValidatedChallengeIdFlow =
        savedStateHandle
            ?.getStateFlow<String?>(
                NavResultKeys.PIN_VALIDATED_CHALLENGE_ID,
                null
            )
    LaunchedEffect(Unit) {
        viewModel.startPolling()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }
    LaunchedEffect(pinValidatedChallengeIdFlow) {
        pinValidatedChallengeIdFlow?.collect { challengeId ->
            if (challengeId != null) {

                viewModel.markFactorAsAuthorized(
                    challengeId,
                    AuthFactor.PIN
                )
                savedStateHandle.remove<String>(
                    NavResultKeys.PIN_VALIDATED_CHALLENGE_ID
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.authorizationStep.collect { step ->
            when (step) {
                is AuthorizationStep.RequireBiometric -> {
                    if (biometricHelper.canAuthenticate()) {
                        biometricHelper.authenticate(
                            activity = activity,
                            title = "Autorizacion Biometrica",
                            subtitle = "Autoriza con tu huella",
                            onSuccess = {
                                viewModel.markFactorAsAuthorized(
                                    step.challengeId,
                                    AuthFactor.BIOMETRIC
                                )
                            },
                            onError = { }
                        )
                    }
                }

                is AuthorizationStep.RequireBiometricSignature -> {
                    biometricHelper.promptChallengeApproval(
                        activity = activity,
                        step = step,
                        onSigned = viewModel::approveWithBiometricSignature
                    )
                }

                is AuthorizationStep.RequirePin -> {

                    navController.navigate(
                        NavRoute.PIN_SECTION.build(step.challengeId)
                    )
                }

                is AuthorizationStep.RequireTotp -> {
                    navController.navigate(
                        NavRoute.Totp.build(step.challengeId)
                    )
                }

                is AuthorizationStep.ReadyToAuthorize -> { /* UI ya muestra botón */
                }

                is AuthorizationStep.Completed -> { /* remover challenge */
                }

                is AuthorizationStep.RequireLocation -> {}
                else -> {}
            }
        }
    }

    // MODAL
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                navController = navController,
                onItemClick = { item ->
                    scope.launch { drawerState.close() }

                    when (item) {
                        DrawerItem.MyPin ->
                            navController.navigate(NavRoute.MenuPIN.route)
                        DrawerItem.History ->
                            navController.navigate(NavRoute.MenuHistory.route)
                        DrawerItem.About ->
                            navController.navigate(NavRoute.MenuAbout.route)
                        DrawerItem.Logout ->
                            navController.navigate(NavRoute.MenuLogout.route)
                    }
                }
            )
        }
    ) {
        val colors = NavTechTheme.colors
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 24.dp)
        ) {

            HomeHeader(
                state = userDataState,
                notificationState = session,
                onProfileClick = {scope.launch { drawerState.open() }},
                onNotificationsClick = onNotificationsClick
            )

            Spacer(Modifier.height(32.dp))

            PendingChallengesSection(
                challenges = pendingChallenges,
                isLoading = isLoading,
                onChallengeClick = onChallengeClick,
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Código TOTP",
                    style = NavTechTheme.typography.titleMedium
                )

            }
            Spacer(modifier = Modifier.height(12.dp))

            TotpCardView(
                code = state.code,
                secondsRemaining = state.secondsRemaining,
                onCopy = { code ->
                    clipboardManager.setText(AnnotatedString(code))
                    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                }
            )
        }

    }
}

@Composable
fun PendingChallengesSection(
    challenges: List<ChallengeItem>,
    isLoading: Boolean,
    onChallengeClick: (ChallengeItem) -> Unit,
) {
    Column {
        SectionHeader(
            title = "Desafíos pendientes",
            action = "Ir a todos",
            onActionClick = null// TODO hacer una pantalla para los desafios a aprobar
        )

        when {
            isLoading -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(2) {
                        ChallengePlaceholderCard()
                    }
                }
            }
            challenges.isNotEmpty() -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(challenges) { challenge ->
                        ChallengeHorizontalCard(
                            challenge = challenge,
                            onClick = { onChallengeClick(challenge) }
                        )
                    }
                }
            }

            else -> {
                EmptyChallengesState()
            }
        }
    }
}

@Composable
fun EmptyChallengesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No tienes desafíos pendientes",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Todo está bajo control 👍",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun ChallengePlaceholderCard() {
    val colors = NavTechTheme.colors
    Card(
        modifier = Modifier
            .width(290.dp)
            .height(170.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.6f)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            )

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            )

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.4f)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            )
        }
    }
}


@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            style = NavTechTheme.typography.titleMedium
        )

        if (action != null && onActionClick != null) {
            Text(
                text = action,
                style = NavTechTheme.typography.bodyMedium,
                color = NavTechTheme.colors.primary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
fun ChallengeHorizontalCard(
    challenge: ChallengeItem,
    onClick: () -> Unit
) {

    val colors = NavTechTheme.colors

    Card(
        modifier = Modifier
            .width(290.dp)
            .height(170.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface // 🔥 fondo gris claro
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // 🔹 TÍTULO
            Text(
                text = challenge.title,
                style = NavTechTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C2C2C),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = challenge.description ?: "Desafio para autorizar",
                style = NavTechTheme.typography.bodySmall,
                color = colors.primary
            )

            Spacer(Modifier.height(24.dp))

            // 🔵 BOTÓN PRINCIPAL
            NavTechButton(
                text = "Ir a autorizar",
                onClick = onClick,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
fun HomeHeader(
    state: HomeUiState,
    notificationState: SessionStateNotification,
    deviceStatus: String = "Dispositivo activo",
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {

    val colors = NavTechTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(colors.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        // 🔵 LOGO CENTRADO
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(com.zanaaustralis.argos.R.drawable.logo_navtech_color),
                contentDescription = null,
                modifier = Modifier.height(28.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🟢 AVATAR (imagen + sombra)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NavTechTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {

                if (state.isLoading) {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.titleLarge,
                        color = NavTechTheme.colors.primary
                    )
                } else {
                    Text(
                        text = state.userName?.let { " ${it.take(1)}" } ?: "U",
                        style = MaterialTheme.typography.titleLarge,
                        color = NavTechTheme.colors.primary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = state.userName?.let { "Hola ${it.firstName()}" } ?: "Hola",
                    style = NavTechTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = deviceStatus,
                    style = NavTechTheme.typography.bodySmall,
                    color = colors.primary.copy(alpha = 0.9f)
                )
            }

            // 🔔 NOTIFICACIONES + BADGE
            Box {

                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones",
                        tint = colors.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if(notificationState.hasUnreadNotifications){
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFFF8A00), CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }

            }
        }
    }
}


/**
 * Preview
 * */

private fun fakeChallenge(
) = ChallengeItem(
    id = "1",
    title = "Transferencia bancaria",
    description = "Autorización de transferencia a cuenta externa",
    date = "Hace 14 minutos",
    isCritical = true,
    status = "pendiente",
    appName = "Banco Global",
    amount = 1250000.0,
    requiresBiometric = true,
    requiresTotp = true,
    requiresLocation = true,
    createdAt = "2025-11-27T10:00:00Z",
    expiresAt = "2025-11-27T10:05:00Z",
    risk_level = "high"
)

/*@Preview
@Composable
private fun ChallengeHorizontalCardPreview() {
    NavTechTheme {
        ChallengeHorizontalCard(
            challenge = fakeChallenge(),
            onClick = {},
        )
    }
}*/

/*@Preview(showBackground = true)
@Composable
fun SectionHeaderPreview() {
    NavTechTheme {
        SectionHeader(
            title = "Desafíos pendientes",
            action = "Ver todos",
            onActionClick = {}
        )
    }
}*/
