package com.example.macmovilcare

import com.example.macmovilcare.data.local.dao.OrdenesDao
import com.example.macmovilcare.data.remote.ApiService
import com.example.macmovilcare.data.remote.dto.*
import com.example.macmovilcare.data.repository.MCmovilRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class MCmovilRepositoryTest {

    private lateinit var ordenDao: OrdenesDao
    private lateinit var apiService: ApiService
    private lateinit var repository: MCmovilRepository

    @Before
    fun setUp() {
        ordenDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        every { ordenDao.obtenerTodasLasOrdenes() } returns flowOf(emptyList())
        repository = MCmovilRepository(ordenDao = ordenDao, apiService = apiService)
    }

    @Test
    fun `sincronizarOrdenes descarga desde API y guarda en Room`() = runTest {
        val listaRemota = listOf(
            OrdenDto(
                id = "101",
                equipoId = "EQ-550",
                clienteId = "CLI-01",
                fechaIngreso = "10-10-2026",
                estado = "ingresado",
                diagnostico = "Falla en pantalla"
            )
        )
        coEvery { apiService.obtenerOrdenes() } returns listaRemota

        repository.sincronizarOrdenes()

        coVerify(exactly = 1) { apiService.obtenerOrdenes() }
        coVerify(exactly = 1) {
            ordenDao.insertarOrdenes(match { entidades ->
                entidades.size == 1 &&
                        entidades[0].id == "101" &&
                        entidades[0].estado == "INGRESADO"
            })
        }
    }

    @Test
    fun `sincronizarOrdenes maneja excepcion sin caer cuando no hay internet`() = runTest {
        coEvery { apiService.obtenerOrdenes() } throws RuntimeException("Sin conexión")

        repository.sincronizarOrdenes()

        coVerify(exactly = 0) { ordenDao.insertarOrdenes(any()) }
    }

    @Test
    fun `login retorna respuesta exitosa con token y usuario`() = runTest {
        val usuarioMock = mockk<UsuarioDto>(relaxed = true)
        val expectedResponse = LoginResponse(
            token = "jwt-token-2026",
            usuario = usuarioMock
        )
        coEvery { apiService.login(any()) } returns Response.success(expectedResponse)

        val result = repository.login("tecnico@mcmovil.cl", "123456")

        assertTrue(result.isSuccessful)
        assertEquals("jwt-token-2026", result.body()?.token)
        assertNotNull(result.body()?.usuario)
    }

    @Test
    fun `enviarNuevaSolicitud guarda en Room y retorna true cuando API responde ok`() = runTest {
        val dtoCreado = OrdenDto(
            id = "ABC123",
            equipoId = "EQ-900",
            clienteId = "CLI-01",
            fechaIngreso = "21-11-2026",
            estado = "ingresado",
            diagnostico = "No enciende"
        )
        coEvery { apiService.crearSolicitud(any(), any()) } returns Response.success(dtoCreado)

        val exito = repository.enviarNuevaSolicitud(
            token = "token-test",
            equipoId = "EQ-900",
            descripcionProblema = "No enciende"
        )

        assertTrue(exito)
        coVerify(exactly = 1) { ordenDao.insertarOrden(match { it.id == "ABC123" }) }
    }

    @Test
    fun `enviarNuevaSolicitud retorna false cuando API falla`() = runTest {
        val errorResponse = mockk<Response<OrdenDto>> {
            every { isSuccessful } returns false
            every { body() } returns null
        }
        coEvery { apiService.crearSolicitud(any(), any()) } returns errorResponse

        val exito = repository.enviarNuevaSolicitud("token-test", "EQ-900", "Falla")

        assertFalse(exito)
        coVerify(exactly = 0) { ordenDao.insertarOrden(any()) }
    }

    @Test
    fun `responderCotizacionRemota actualiza estado en Room al aprobar`() = runTest {
        val dtoActualizado = OrdenDto("101", "EQ-550", "CLI-01", "10-10-2026", "EN_REPARACION", "OK")
        coEvery { apiService.actualizarEstadoOrden(any(), eq("101"), any()) } returns Response.success(dtoActualizado)

        val exito = repository.responderCotizacionRemota("token-test", "101", "APROBADO")

        assertTrue(exito)
        coVerify(exactly = 1) { ordenDao.actualizarEstado("101", "APROBADO") }
    }

    @Test
    fun `enviarCotizacionTecnico actualiza orden en Room cuando API responde ok`() = runTest {
        val dtoCotizado = OrdenDto("102", "EQ-720", "CLI-01", "14-10-2026", "COTIZADO_PENDIENTE", "Cambio batería")
        coEvery { apiService.generarCotizacion(any(), eq("102"), any()) } returns Response.success(dtoCotizado)

        val exito = repository.enviarCotizacionTecnico(
            token = "token-test",
            ordenId = "102",
            diagnostico = "Cambio batería",
            montoTotal = 35000.0,
            detallesServicios = "Repuesto original"
        )

        assertTrue(exito)
        coVerify(exactly = 1) {
            ordenDao.insertarOrden(match { it.id == "102" && it.estado == "COTIZADO_PENDIENTE" })
        }
    }

    @Test
    fun `eliminarOrden elimina tanto en API remota como en Room local`() = runTest {
        coEvery { apiService.eliminarOrden(any(), eq("101")) } returns Response.success(Unit)

        repository.eliminarOrden("token-test", "101")

        coVerify(exactly = 1) { apiService.eliminarOrden("Bearer token-test", "101") }
        coVerify(exactly = 1) { ordenDao.eliminarOrden("101") }
    }
}