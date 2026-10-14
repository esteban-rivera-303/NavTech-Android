package com.zanaaustralis.argos.drawer_menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.zanaaustralis.core.NavRoute
import com.zanaaustralis.core.common.firstName
import com.zanaaustralis.core.common.ui.theme.NavTechTheme

/**
 * @author esteban.rivera01
 */

@Composable
fun HomeDrawerContent(
    navController: NavHostController,
    onItemClick: (DrawerItem) -> Unit,
) {

    val drawerViewModel: DrawerViewModel = hiltViewModel()
    val logoutState by drawerViewModel.logoutState.collectAsState()
    val state by drawerViewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }



    LaunchedEffect(logoutState) {
        if (logoutState is LogoutState.Success) {
            drawerViewModel.resetState()
            navController.navigate(NavRoute.Launch.route) {
                popUpTo(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
            .padding(top = 48.dp)
    ) {

        // 👤 Header usuario
        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
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

            Spacer(Modifier.height(12.dp))

            if (state.isLoading) {
                Text(
                    text = "Hola",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    text = state.userName?.let { "Hola, ${it.firstName()}" } ?: "Hola",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "Dispositivo activo",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(32.dp))

        // 📋 Opciones
        DrawerItemRow(DrawerItem.MyPin, onItemClick)
        DrawerItemRow(DrawerItem.History, onItemClick)
        DrawerItemRow(DrawerItem.About, onItemClick)
        DrawerItemRow(
            item = DrawerItem.Logout,
            onItemClick = {
                showLogoutDialog = true
            }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                drawerViewModel.logout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun DrawerItemRow(
    item: DrawerItem,
    onItemClick: (DrawerItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = NavTechTheme.colors.primary
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cerrar sesión")
        },
        text = {
            Text("¿Estás seguro de que deseas cerrar sesión?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
