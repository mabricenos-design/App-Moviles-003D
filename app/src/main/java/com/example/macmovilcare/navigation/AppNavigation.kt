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
    object Login : Pantalla(ruta = "login")
    object Home : Pantalla(ruta = "home")
    object DetalleOrden : Pantalla(ruta = "detalle_orden/{ordenId}") {
        fun crearRuta(ordenId: String) = "detalle_orden/$ordenId"
    }
    object CrearSolicitud : Pantalla(ruta = "crear_solicitud")
    object CargarEvidencia : Pantalla(ruta = "cargar_evidencia/{ordenId}") {
        fun crearRuta(ordenId: String) = "cargar_evidencia/$ordenId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    ordenesViewModel: OrdenesViewModel = viewModel(),
    solicitudViewModel: SolicitudViewModel = viewModel(),
    esPantallaAncha: Boolean = false
) {
    val rolActivo by authViewModel.rolActivo.collectAsState()
    val notificacionTecnico by ordenesViewModel.notificacionTecnico.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Pantalla.Login.ruta
    ) {
        // 1. Login
        composable(route = Pantalla.Login.ruta) {
            val loginState by authViewModel.uiState.collectAsState()

            LoginScreen(
                state = loginState,
                rolSeleccionado = rolActivo,
                onRolSeleccionado = authViewModel::seleccionarRolPrueba,
                onCorreoChange = authViewModel::onCorreoChange,
                onClaveChange = authViewModel::onClaveChange,
                onLoginClick = {
                    authViewModel.iniciarSesion {
                        navController.navigate(route = Pantalla.Home.ruta) {
                            popUpTo(route = Pantalla.Login.ruta) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Home
        composable(route = Pantalla.Home.ruta) {
            val listaOrdenes by ordenesViewModel.ordenes.collectAsState()

            HomeScreen(
                ordenes = listaOrdenes,
                rolUsuario = rolActivo,
                esPantallaAncha = esPantallaAncha,
                mensajeNotificacion = notificacionTecnico,
                onDescartarNotificacion = ordenesViewModel::limpiarNotificacion,
                onOrdenClick = { ordenId ->
                    ordenesViewModel.seleccionarOrden(ordenId)
                    navController.navigate(route = Pantalla.DetalleOrden.crearRuta(ordenId))
                },
                onNuevaSolicitudClick = {
                    solicitudViewModel.limpiarFormulario()
                    navController.navigate(route = Pantalla.CrearSolicitud.ruta)
                },
                onCerrarSesion = {
                    navController.navigate(route = Pantalla.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 3. Detalle de Orden
        composable(route = Pantalla.DetalleOrden.ruta) {
            val ordenSeleccionada by ordenesViewModel.ordenSeleccionada.collectAsState()

            DetalleOrdenScreen(
                orden = ordenSeleccionada,
                rolUsuario = rolActivo,
                onAprobarCotizacion = {
                    ordenSeleccionada?.let { orden ->
                        ordenesViewModel.responderCotizacion(ordenId = orden.id, aprobada = true)
                    }
                },
                onRechazarCotizacion = {
                    ordenSeleccionada?.let { orden ->
                        ordenesViewModel.responderCotizacion(ordenId = orden.id, aprobada = false)
                    }
                },
                onResponderComoTecnico = { ordenId, comentarios, uriFoto ->
                    ordenesViewModel.responderSolicitudComoTecnico(ordenId, comentarios, uriFoto)
                    navController.popBackStack()
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Crear Solicitud (Exclusivo Cliente)
        composable(route = Pantalla.CrearSolicitud.ruta) {
            val solicitudState by solicitudViewModel.uiState.collectAsState()

            CrearSolicitudScreen(
                state = solicitudState,
                onEquipoIdChange = solicitudViewModel::onEquipoIdChange,
                onDescripcionChange = solicitudViewModel::onDescripcionChange,
                onEnviarSolicitud = {
                    solicitudViewModel.enviarSolicitud { equipoId, descripcion ->
                        ordenesViewModel.dispararNotificacionNuevaSolicitud(equipoId, descripcion)
                        navController.popBackStack()
                    }
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Cargar Evidencia
        composable(route = Pantalla.CargarEvidencia.ruta) { backStackEntry ->
            val ordenId = backStackEntry.arguments?.getString("ordenId") ?: ""
            CargarEvidenciaScreen(
                ordenId = ordenId,
                onGuardarEvidencia = { _, _, _ ->
                    navController.popBackStack()
                },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}