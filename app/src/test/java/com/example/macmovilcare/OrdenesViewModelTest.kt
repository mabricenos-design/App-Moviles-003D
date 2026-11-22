package com.example.macmovilcare

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.example.macmovilcare.data.local.AppDatabase
import com.example.macmovilcare.data.local.dao.OrdenesDao
import com.example.macmovilcare.data.local.entity.OrdenEntity
import com.example.macmovilcare.data.remote.IndicadoresApiService
import com.example.macmovilcare.data.remote.IndicadoresResponse
import com.example.macmovilcare.data.remote.RetrofitIndicadoresClient
import com.example.macmovilcare.viewmodel.OrdenesViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdenesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var application: Application
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var prefsEditor: SharedPreferences.Editor
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var ordenDao: OrdenesDao
    private lateinit var ordenesFlow: MutableStateFlow<List<OrdenEntity>>
    private lateinit var indicadoresService: IndicadoresApiService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        application = mockk(relaxed = true)
        sharedPrefs = mockk(relaxed = true)
        prefsEditor = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        ordenDao = mockk(relaxed = true)
        indicadoresService = mockk(relaxed = true)

        every { application.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.getFloat("ultimo_dolar", 950.0f) } returns 950.0f
        every { sharedPrefs.edit() } returns prefsEditor
        every { prefsEditor.putFloat(any(), any()) } returns prefsEditor
        every { application.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager

        ordenesFlow = MutableStateFlow(
            listOf(
                OrdenEntity(
                    id = "101",
                    equipoId = "EQ-550",
                    clienteId = "CLI-01",
                    fechaIngreso = "10-10-2026",
                    estado = "COTIZADO_PENDIENTE",
                    diagnostico = "Falla en tarjeta de video"
                )
            )
        )
        every { ordenDao.obtenerTodasLasOrdenes() } returns ordenesFlow

        mockkObject(AppDatabase)
        val mockDb = mockk<AppDatabase>()
        every { AppDatabase.getDatabase(any()) } returns mockDb
        every { mockDb.ordenDao() } returns ordenDao

        mockkObject(RetrofitIndicadoresClient)
        every { RetrofitIndicadoresClient.apiService } returns indicadoresService
        val mockResponse = mockk<IndicadoresResponse> {
            every { dolar.valor } returns 995.5
            every { uf.valor } returns 38000.0
        }
        coEvery { indicadoresService.obtenerIndicadores() } returns mockResponse
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private suspend fun <T> awaitRealTime(block: suspend () -> T): T =
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(5000) { block() }
        }

    @Test
    fun `al iniciar carga ordenes desde Room y actualiza valor del dolar desde API externa`() = runTest {
        val viewModel = OrdenesViewModel(application)
        val job = backgroundScope.launch { viewModel.ordenes.collect {} }

        val lista = awaitRealTime { viewModel.ordenes.first { it.isNotEmpty() } }
        val dolar = awaitRealTime { viewModel.valorDolar.first { it == 995.5 } }

        assertEquals(1, lista.size)
        assertEquals("101", lista[0].id)
        assertEquals(995.5, dolar, 0.01)
        job.cancel()
    }

    @Test
    fun `seleccionarOrden actualiza ordenSeleccionada con el id indicado`() = runTest {
        val viewModel = OrdenesViewModel(application)
        val job = backgroundScope.launch { viewModel.ordenes.collect {} }

        awaitRealTime { viewModel.ordenes.first { it.isNotEmpty() } }
        viewModel.seleccionarOrden("101")

        assertNotNull(viewModel.ordenSeleccionada.value)
        assertEquals("EQ-550", viewModel.ordenSeleccionada.value?.equipoId)
        job.cancel()
    }

    @Test
    fun `dispararNotificacionNuevaSolicitud y limpiarNotificacion gestionan el banner correctamente`() = runTest {
        val viewModel = OrdenesViewModel(application)

        viewModel.dispararNotificacionNuevaSolicitud("EQ-999", "Pantalla rota")
        assertTrue(viewModel.notificacionTecnico.value?.contains("EQ-999") == true)

        viewModel.limpiarNotificacion()
        assertNull(viewModel.notificacionTecnico.value)
    }

    @Test
    fun `responderSolicitudComoTecnico genera cotizacion y adjunta evidencia fotografica`() = runTest {
        val viewModel = OrdenesViewModel(application)
        val job = backgroundScope.launch { viewModel.ordenes.collect {} }

        awaitRealTime { viewModel.ordenes.first { it.isNotEmpty() } }

        viewModel.responderSolicitudComoTecnico(
            ordenId = "101",
            diagnostico = "Placa madre sulfatada",
            monto = 65000.0,
            detalles = "Limpieza ultrasónica + soldadura",
            uriFoto = "content://media/external/images/media/42"
        )

        awaitRealTime {
            Thread.sleep(150)
            ordenesFlow.value = listOf(
                OrdenEntity("101", "EQ-550", "CLI-01", "10-10-2026", "COTIZADO_PENDIENTE", "Placa madre sulfatada")
            )
            val actualizada = viewModel.ordenes.first { lista ->
                lista.any { it.id == "101" && it.cotizacion != null }
            }.find { it.id == "101" }

            assertEquals(65000.0, actualizada?.cotizacion?.montoTotal ?: 0.0, 0.01)
            assertEquals(1, actualizada?.evidencias?.size)
        }
        job.cancel()
    }

    @Test
    fun `responderCotizacion cambia estado de autorizacion a APROBADO`() = runTest {
        val viewModel = OrdenesViewModel(application)
        val job = backgroundScope.launch { viewModel.ordenes.collect {} }

        awaitRealTime { viewModel.ordenes.first { it.isNotEmpty() } }

        viewModel.responderCotizacion("101", aprobada = true)

        awaitRealTime {
            Thread.sleep(150)
            ordenesFlow.value = listOf(
                OrdenEntity("101", "EQ-550", "CLI-01", "10-10-2026", "EN_REPARACION", "Falla en tarjeta de video")
            )
            val orden = viewModel.ordenes.first { lista ->
                lista.any { it.id == "101" && it.cotizacion?.estadoAutorizacion == "APROBADO" }
            }.find { it.id == "101" }

            assertEquals("APROBADO", orden?.cotizacion?.estadoAutorizacion)
        }
        job.cancel()
    }
}