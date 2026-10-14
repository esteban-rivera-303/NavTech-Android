package com.zanaaustralis.argos.notification.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.domain.model.NotificationItem
import com.zanaaustralis.feature_challanges.domain.model.formatCreatedAt
import com.zanaaustralis.feature_challanges.presentation.historial.EmptyHistoryPlaceholder
import com.zanaaustralis.feature_challanges.presentation.historial.ErrorState
import com.zanaaustralis.feature_challanges.presentation.historial.HistoryHeader
import com.zanaaustralis.feature_challanges.presentation.historial.PaginationError

/**
 * @author esteban.rivera01
 */

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPaginating by viewModel.isPaginating.collectAsState()
    val error by viewModel.error.collectAsState()
    val colors = NavTechTheme.colors


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // 🔵 HEADER reutilizado
        HistoryHeader("Mis Notificaciones", onBack = onBack)

        Spacer(Modifier.height(8.dp))

        when {

            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null && notifications.isEmpty() -> {
                ErrorState(
                    message = error ?: "",
                    onRetry = { viewModel.loadNotifications(true) }
                )
            }

            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Marcar todas como leídas",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.isMarkingAll || notifications.isEmpty() )
                            NavTechTheme.colors.textDisabled
                        else
                            NavTechTheme.colors.primary,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable(
                                enabled = !uiState.isMarkingAll
                            ) {
                                viewModel.markAllAsRead()
                            }
                    )

                }
                if (notifications.isEmpty() && !isPaginating && error == null) {
                    EmptyHistoryPlaceholder()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(notifications) {
                            NotificationCard(it, viewModel)
                        }

                        item {

                            when {
                                isPaginating -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                error != null -> {
                                    PaginationError {
                                        viewModel.loadNotifications()
                                    }
                                }

                                else -> {
                                    LaunchedEffect(Unit) {
                                        viewModel.loadNotifications()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationItem, viewModel: NotificationViewModel) {
    val backgroundColor = if (item.isRead) {
        NavTechTheme.colors.surface.copy(alpha = 0.7f) // más oscuro
    } else {
        NavTechTheme.colors.surface // normal
    }

    val elevation = CardDefaults.cardElevation(
        defaultElevation = if (item.isRead) 0.dp else 4.dp
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = elevation,
        colors = CardDefaults.cardColors(backgroundColor),
        onClick = {
            viewModel.markNotificationAsRead(item.id)
        }
    ) {


        Column(Modifier.padding(16.dp)) {

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.body,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.appName ?: "",
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = item.createdAt.formatCreatedAt(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )


            }
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder() {

    val colors = NavTechTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Aún no tienes actividad",
            style = NavTechTheme.typography.titleMedium,
            color = colors.textPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Aquí verás el historial de tus autorizaciones.",
            style = NavTechTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}