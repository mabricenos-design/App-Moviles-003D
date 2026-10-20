package com.example.macmovilcare.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.macmovilcare.ui.screens.CargarEvidenciaScreen
import com.example.macmovilcare.ui.screens.CrearSolicitudScreen
import com.example.macmovilcare.ui.screens.DetalleOrdenScreen
import com.example.macmovilcare.ui.screens.HomeScreen
import com.example.macmovilcare.ui.screens.LoginScreen
import com.example.macmovilcare.viewmodel.AuthViewModel
import com.example.macmovilcare.viewmodel.OrdenesViewModel
import com.example.macmovilcare.viewmodel.SolicitudViewModel

sealed class Pantalla(val ruta: String) {
    object CargarEvidencia : Pantalla(ruta = "cargar_evidencia/{ordenId}") {
        fun crearRuta(ordenId: String) = "cargar_evidencia/$ordenId"
    }
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
    solicitudViewModel: SolicitudViewModel = viewModel(),
    esPantallaAncha: Boolean = false
) {
    // Obtenemos el rol activo en la aplicación
    val rolActivo by authViewModel.rolActivo.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Pantalla.Login.ruta
    ) {

        // 0. Pantalla Cargar Evidencia
        composable(route = Pantalla.CargarEvidencia.ruta) { backStackEntry ->
            val ordenId = backStackEntry.arguments?.getString("ordenId") ?: ""
            CargarEvidenciaScreen(
                ordenId = ordenId,
                onGuardarEvidencia = { id, uri, desc ->
                    navController.popBackStack()
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // 1. Pantalla de Login (Conectada con el Rol)
        composable(Pantalla.Login.ruta) {
            val loginState by authViewModel.uiState.collectAsState()

            LoginScreen(
                state = loginState,
                rolSeleccionado = rolActivo,
                onRolSeleccionado = authViewModel::seleccionarRolPrueba,
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
                rolUsuario = rolActivo,
                esPantallaAncha = esPantallaAncha,
                onOrdenClick = { ordenId ->
                    ordenesViewModel.seleccionarOrden(ordenId)
                    navController.navigate(Pantalla.DetalleOrden.crearRuta(ordenId))
                },
                onNuevaSolicitudClick = {
                    navController.navigate(Pantalla.CrearSolicitud.ruta)
                }
            )
        }

        // 3. Pantalla de Detalle de la Orden (Adaptada al Rol)
        composable(Pantalla.DetalleOrden.ruta) {
            val ordenSeleccionada by ordenesViewModel.ordenSeleccionada.collectAsState()

            DetalleOrdenScreen(
                orden = ordenSeleccionada,
                rolUsuario = rolActivo, // Se envía el rol activo
                onAprobarCotizacion = {
                    ordenSeleccionada?.let { orden ->
                        ordenesViewModel.responderCotizacion(orden.id, aprobada = true)
                    }
                },
                onRechazarCotizacion = {
                    ordenSeleccionada?.let { orden ->
                        ordenesViewModel.responderCotizacion(orden.id, aprobada = false)
                    }
                },
                onAdjuntarEvidencia = { ordenId ->
                    navController.navigate(Pantalla.CargarEvidencia.crearRuta(ordenId))
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