package com.zanaaustralis.argos.drawer_menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @author esteban.rivera01
 */

sealed class DrawerItem(
    val title: String,
    val icon: ImageVector
) {
    object MyPin : DrawerItem("Mi PIN", Icons.Outlined.Lock)
    object History : DrawerItem("Mi historial", Icons.Outlined.DateRange)
    object About : DrawerItem("Acerca de", Icons.Outlined.Info)
    object Logout : DrawerItem("Cerrar Sesión", Icons.Outlined.ExitToApp)
}
