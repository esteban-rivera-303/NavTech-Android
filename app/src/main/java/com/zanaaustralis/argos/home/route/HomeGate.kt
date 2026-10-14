package com.zanaaustralis.argos.home.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zanaaustralis.core.NavRoute
import com.zanaaustralis.feature_auth.presentation.pin.intro.PinViewModel

/**
 * @author esteban.rivera01
 */

@Composable
fun HomeGate(
    // Esto es un gate para crear PIN en caso de que no lo tenga
    navController: NavController,
    viewModel: PinViewModel = hiltViewModel()

) {
    val hasPin by viewModel.hasPin.collectAsState()

    LaunchedEffect(hasPin) {

        if (navController.currentDestination?.route == NavRoute.HomeGate.route) {

            when (hasPin) {
                true -> {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                false -> {
                    navController.navigate(NavRoute.CreatePIN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                null -> {
                    // Aún cargando → no navegar
                }
            }
        }
    }
}