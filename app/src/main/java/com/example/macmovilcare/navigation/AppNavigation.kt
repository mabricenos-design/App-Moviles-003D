package com.example.macmovilcare.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.macmovilcare.ui.screens.CrearSolicitudScreen
import com.example.macmovilcare.ui.screens.DetalleOrdenScreen
import com.example.macmovilcare.ui.screens.HomeScreen
import com.example.macmovilcare.ui.screens.LoginScreen
import com.example.macmovilcare.viewmodel.AuthViewModel
import com.example.macmovilcare.viewmodel.OrdenesViewModel
import com.example.macmovilcare.viewmodel.SolicitudViewModel

// Definición de las rutas de navegación
sealed class Pantalla(val ruta: String) {
    object Login : Pantalla("login")
    object Home : Pantalla("home")
    object DetalleOrden : Pantalla("detalle_orden/{ordenId}") {
        fun crearRuta(ordenId: String) = "detalle_orden/$ordenId"
    }
    object CrearSolicitud : Pantalla("crear_solicitud")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    ordenesViewModel: OrdenesViewModel = viewModel(),
    solicitudViewModel: SolicitudViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Pantalla.Login.ruta
    ) {
        // 1. Pantalla de Login
        composable(Pantalla.Login.ruta) {
            val loginState by authViewModel.uiState.collectAsState()

            LoginScreen(
                state = loginState,
                onCorreoChange = authViewModel::onCorreoChange,
                onClaveChange = authViewModel::onClaveChange,
                onLoginClick = {
                    authViewModel.iniciarSesion {
                        navController.navigate(Pantalla.Home.ruta) {
                            popUpTo(Pantalla.Login.ruta) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Pantalla Principal (Home)
        composable(Pantalla.Home.ruta) {
            val listaOrdenes by ordenesViewModel.ordenes.collectAsState()

            HomeScreen(
                ordenes = listaOrdenes,
                onOrdenClick = { ordenId ->
                    ordenesViewModel.seleccionarOrden(ordenId)
                    navController.navigate(Pantalla.DetalleOrden.crearRuta(ordenId))
                },
                onNuevaSolicitudClick = {
                    navController.navigate(Pantalla.CrearSolicitud.ruta)
                }
            )
        }

        // 3. Pantalla de Detalle de la Orden
        composable(Pantalla.DetalleOrden.ruta) {
            val ordenSeleccionada by ordenesViewModel.ordenSeleccionada.collectAsState()

            DetalleOrdenScreen(
                orden = ordenSeleccionada,
                onAprobarCotizacion = {
                    ordenSeleccionada?.let { orden ->
                        ordenesViewModel.responderCotizacion(orden.id, aprobada = true)
                    }
                },
                onRechazarCotizacion = {
                    ordenSeleccionada?.let { orden ->
                        ordenesViewModel.responderCotizacion(orden.id, aprobada = false)
                    }
                }
            )
        }

        // 4. Pantalla para Crear Nueva Solicitud
        composable(Pantalla.CrearSolicitud.ruta) {
            val solicitudState by solicitudViewModel.uiState.collectAsState()

            CrearSolicitudScreen(
                state = solicitudState,
                onEquipoIdChange = solicitudViewModel::onEquipoIdChange,
                onDescripcionChange = solicitudViewModel::onDescripcionChange,
                onEnviarSolicitud = solicitudViewModel::enviarSolicitud,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}