package com.zanaaustralis.feature_challanges.presentation.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.core.R
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem

/**
 * @author esteban.rivera01
 */

@Composable
fun ChallengeHistoryScreen(
    viewModel: ChallengeHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {

    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPaginating by viewModel.isPaginating.collectAsState()
    val error by viewModel.error.collectAsState()
    val colors = NavTechTheme.colors

   /* SetStatusBarColor(
        color = colors.background,
        darkIcons = true
    )*/
    Column(
        modifier = Modifier
            .fillMaxSize()
            //.windowInsetsPadding(WindowInsets.systemBars)
            .background(colors.background)
    ) {

        // 🔵 HEADER NUEVO
        HistoryHeader(onBack = { onBack() })

        // 🔵 FILTROS
        HistoryFilters(
            onFilter = { viewModel.filter(it) }
        )

        Spacer(Modifier.height(8.dp))

        when {

            isLoading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null && history.isEmpty() -> {
                ErrorState(
                    message = error ?: "Error desconocido",
                    onRetry = { viewModel.loadHistory(reset = true) }
                )
            }

            else -> {
                if (history.isEmpty() && !isPaginating) {

                    EmptyHistoryPlaceholder()

                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(history) { challenge ->
                            ChallengeHistoryCard(challenge)
                        }

                        item {
                            when {
                                isPaginating -> {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                error != null -> {
                                    PaginationError(
                                        onRetry = { viewModel.loadHistory() }
                                    )
                                }

                                else -> {
                                    LaunchedEffect(Unit) {
                                        viewModel.loadHistory()
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
fun HistoryHeader(
    title: String = "Mi historial",
    onBack: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(120.dp)
            .background(Color.White)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {

        Spacer(Modifier.height(48.dp))
        // 🔙 BACK (izquierda)
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Volver"
            )
        }

        // 🔵 CONTENIDO CENTRADO
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.logo_navtech_color),
                contentDescription = null,
                modifier = Modifier.height(28.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HistoryFilters(
    onFilter: (String?) -> Unit
) {

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            FilterChip("Todos") { onFilter(null) }
        }

        item {
            FilterChip("Aprobados") { onFilter("approved") }
        }

        item {
            FilterChip("Rechazados") { onFilter("rejected") }
        }

        item {
            FilterChip("Expirados") { onFilter("expired") }
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
/*@Composable
fun ChallengeHistoryScreen(
    viewModel: ChallengeHistoryViewModel = hiltViewModel()
) {

    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPaginating by viewModel.isPaginating.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {

        // 🔵 HEADER SIMPLE
        Text(
            text = "Historial",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        // 🔵 FILTROS
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            FilterChip("Todos") { viewModel.filter(null) }
            FilterChip("Aprobados") { viewModel.filter("approved") }
            FilterChip("Rechazados") { viewModel.filter("rejected") }
            FilterChip("Expirados") { viewModel.filter("expired") }
        }

        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(history) { challenge ->
                    ChallengeHistoryCard(challenge)
                }

                item {
                    if (isPaginating) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LaunchedEffect(Unit) {
                            viewModel.loadHistory()
                        }
                    }
                }
            }
        }
    }
}*/

@Composable
fun ChallengeHistoryCard(
    challenge: ChallengeItem
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = challenge.appName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Text(
                    text = challenge.statusLabel(),
                    color = challenge.statusColor(),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = challenge.description ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Riesgo: ${challenge.risk_level}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White,
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(48.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        NavTechButton(
            text = "Reintentar",
            onClick = onRetry
        )
    }
}

@Composable
fun PaginationError(
    onRetry: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Error al cargar más resultados",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

// Preview
private fun mockHistory(): List<ChallengeItem> {
    return listOf(
        ChallengeItem(
            id = "1",
            title = "Transferencia bancaria",
            description = "Transferencia a Juan Pérez",
            date = "2026-03-20",
            isCritical = true,
            status = "approved",
            appName = "Banco Estado",
            amount = 25000.0,
            requiresBiometric = true,
            requiresTotp = false,
            requiresLocation = false,
            createdAt = "2026-03-20T10:30:00Z",
            expiresAt = "2026-03-20T10:35:00Z",
            risk_level = "High"
        ),
        ChallengeItem(
            id = "2",
            title = "Pago servicio",
            description = "Pago de luz",
            date = "2026-03-19",
            isCritical = false,
            status = "rejected",
            appName = "Enel",
            amount = 12000.0,
            requiresBiometric = false,
            requiresTotp = true,
            requiresLocation = false,
            createdAt = "2026-03-19T10:30:00Z",
            expiresAt = "2026-03-19T10:35:00Z",
            risk_level = "Medium"
        ),
        ChallengeItem(
            id = "3",
            title = "Login sospechoso",
            description = "Intento de login",
            date = "2026-03-18",
            isCritical = true,
            status = "expired",
            appName = "Google",
            amount = null,
            requiresBiometric = false,
            requiresTotp = true,
            requiresLocation = true,
            createdAt = "2026-03-18T10:30:00Z",
            expiresAt = "2026-03-18T10:35:00Z",
            risk_level = "High"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewChallengeHistoryCard() {
    MaterialTheme {
        ChallengeHistoryCard(
            challenge = mockHistory().first()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHistoryPagination() {

    val mockList = mockHistory()

    MaterialTheme {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(mockList) {
                ChallengeHistoryCard(it)
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}