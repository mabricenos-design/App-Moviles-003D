package com.example.macmovilcare.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.screens.CrearSolicitudScreen
import com.example.macmovilcare.ui.screens.DetalleOrdenScreen
import com.example.macmovilcare.ui.screens.HomeScreen
import com.example.macmovilcare.ui.screens.LoginScreen
import com.example.macmovilcare.viewmodel.AuthViewModel
import com.example.macmovilcare.viewmodel.OrdenesViewModel
import com.example.macmovilcare.viewmodel.SolicitudViewModel

@Composable
fun AppNavigation(
    esPantallaAncha: Boolean = false,
    authViewModel: AuthViewModel = viewModel(),
    ordenesViewModel: OrdenesViewModel = viewModel(),
    solicitudViewModel: SolicitudViewModel = viewModel()
) {
    val navController = rememberNavController()

    val loginState by authViewModel.uiState.collectAsState()
    val rolActivo by authViewModel.rolActivo.collectAsState()

    val ordenes by ordenesViewModel.ordenes.collectAsState()
    val mensajeNotificacion by ordenesViewModel.notificacionTecnico.collectAsState()
    val valorDolar by ordenesViewModel.valorDolar.collectAsState()

    val solicitudState by solicitudViewModel.uiState.collectAsState()

    var nombreUsuarioActual by remember { mutableStateOf("Usuario") }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                state = loginState,
                onCorreoChange = { authViewModel.onCorreoChange(it) },
                onClaveChange = { authViewModel.onClaveChange(it) },
                onLoginExitoso = { rolDetectado, nombreExtraido ->
                    authViewModel.seleccionarRolPrueba(rolDetectado)
                    nombreUsuarioActual = nombreExtraido
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                ordenes = ordenes,
                rolUsuario = rolActivo,
                nombreUsuario = nombreUsuarioActual,
                esPantallaAncha = esPantallaAncha,
                mensajeNotificacion = mensajeNotificacion,
                onDescartarNotificacion = { ordenesViewModel.limpiarNotificacion() },
                onOrdenClick = { ordenId ->
                    ordenesViewModel.seleccionarOrden(ordenId)
                    navController.navigate("detalle/$ordenId")
                },
                onNuevaSolicitudClick = {
                    solicitudViewModel.limpiarFormulario()
                    navController.navigate("crear_solicitud")
                },
                onEliminarOrden = { ordenId ->
                    ordenesViewModel.eliminarOrden(ordenId)
                },
                onCerrarSesion = {
                    authViewModel.onClaveChange("")
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("crear_solicitud") {
            CrearSolicitudScreen(
                state = solicitudState,
                onEquipoIdChange = { solicitudViewModel.onEquipoIdChange(it) },
                onDescripcionChange = { solicitudViewModel.onDescripcionChange(it) },
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

        composable(
            route = "detalle/{ordenId}",
            arguments = listOf(navArgument("ordenId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ordenId = backStackEntry.arguments?.getString("ordenId")
            val ordenSeleccionada = ordenes.find { it.id == ordenId }

            DetalleOrdenScreen(
                orden = ordenSeleccionada,
                rolUsuario = rolActivo,
                valorDolar = valorDolar,
                onAprobarCotizacion = {
                    ordenId?.let { ordenesViewModel.responderCotizacion(it, true) }
                },
                onRechazarCotizacion = {
                    ordenId?.let { ordenesViewModel.responderCotizacion(it, false) }
                },
                onResponderComoTecnico = { id, comentarios, monto, detalle, uriFoto ->
                    ordenesViewModel.responderSolicitudComoTecnico(
                        ordenId = id,
                        diagnostico = comentarios,
                        monto = monto,
                        detalles = detalle,
                        uriFoto = uriFoto
                    )
                },
                onCambiarEstadoAdmin = { id, nuevoEstado ->
                    ordenesViewModel.cambiarEstadoOrdenAdmin(id, nuevoEstado)
                },
                onEliminarOrdenAdmin = { id ->
                    ordenesViewModel.eliminarOrden(id)
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}