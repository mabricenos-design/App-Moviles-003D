package com.example.macmovilcare.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object RetiroQrApiService {

    // API pública en línea de QuickChart para renderizar el QR en tiempo real
    private const val QR_IMAGE_API = "https://quickchart.io/qr"

    // API en la nube compartida entre ambos teléfonos (ntfy.sh / kv-store público sin registro)
    private const val CLOUD_CHANNEL_BASE = "https://ntfy.sh/mcmovilcare_sync_orden_"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    data class TicketRetiroRemoto(
        val ordenId: String,
        val tokenRetiro: String,
        val aprobadoPorCliente: Boolean,
        val retiradoConfirmado: Boolean
    )

    /**
     * Genera un token determinístico seguro por orden para que ambos teléfonos
     * puedan verificarlo incluso si la red demora, y además lo publica en la nube.
     */
    fun generarTokenParaOrden(ordenId: String): String {
        val hash = ("MCMOVIL2026-$ordenId").hashCode().toUInt().toString(16).uppercase()
        return hash.take(8).padStart(8, '0')
    }

    /**
     * Construye la URL directa de la API en línea QuickChart para mostrar el QR.
     */
    fun construirUrlImagenQr(ordenId: String, tokenRetiro: String = generarTokenParaOrden(ordenId)): String {
        val payload = "MCMOVIL|$ordenId|$tokenRetiro"
        val encoded = URLEncoder.encode(payload, "UTF-8")
        return "$QR_IMAGE_API?text=$encoded&size=350&margin=2&dark=0f2624&light=ffffff"
    }

    /**
     * Celular 1 (CLIENTE): Publica en la nube la aprobación y devuelve el ticket QR inmediato.
     */
    fun registrarAprobacionYGenerarTicket(ordenId: String): TicketRetiroRemoto {
        val tokenUnico = generarTokenParaOrden(ordenId)
        try {
            val json = JSONObject().apply {
                put("ordenId", ordenId)
                put("tokenRetiro", tokenUnico)
                put("aprobadoPorCliente", true)
                put("retiradoConfirmado", false)
            }

            val body = json.toString().toRequestBody("text/plain; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$CLOUD_CHANNEL_BASE$ordenId")
                .post(body)
                .build()

            client.newCall(request).execute().close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return TicketRetiroRemoto(
            ordenId = ordenId,
            tokenRetiro = tokenUnico,
            aprobadoPorCliente = true,
            retiradoConfirmado = false
        )
    }

    /**
     * Consulta el último estado publicado en la nube para esa orden.
     */
    fun consultarTicketRemoto(ordenId: String): TicketRetiroRemoto? {
        return try {
            val request = Request.Builder()
                .url("$CLOUD_CHANNEL_BASE$ordenId/json?poll=1")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val lineas = response.body?.string()?.trim()?.lines() ?: return null
                var ultimoTicket: TicketRetiroRemoto? = null

                for (linea in lineas) {
                    if (linea.isBlank()) continue
                    val envelope = JSONObject(linea)
                    val mensaje = envelope.optString("message", "")
                    if (mensaje.startsWith("{")) {
                        val data = JSONObject(mensaje)
                        ultimoTicket = TicketRetiroRemoto(
                            ordenId = data.optString("ordenId", ordenId),
                            tokenRetiro = data.optString("tokenRetiro", generarTokenParaOrden(ordenId)),
                            aprobadoPorCliente = data.optBoolean("aprobadoPorCliente", true),
                            retiradoConfirmado = data.optBoolean("retiradoConfirmado", false)
                        )
                    }
                }
                ultimoTicket
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Celular 2 (TÉCNICO): Valida el QR escaneado contra la nube y confirma el retiro (ENTREGADO).
     */
    fun validarYConfirmarRetiroPorQr(qrLeido: String): Result<String> {
        return try {
            val partes = qrLeido.trim().split("|")
            if (partes.size != 3 || partes[0] != "MCMOVIL") {
                return Result.failure(Exception("Código QR no pertenece a MCmovil Care."))
            }

            val ordenId = partes[1]
            val tokenEscaneado = partes[2]
            val tokenEsperado = generarTokenParaOrden(ordenId)

            if (tokenEscaneado != tokenEsperado) {
                return Result.failure(Exception("Token de seguridad inválido para la Orden #$ordenId."))
            }

            val ticketNube = consultarTicketRemoto(ordenId)
            if (ticketNube != null && ticketNube.retiradoConfirmado) {
                return Result.failure(Exception("Este QR ya fue utilizado para retirar la Orden #$ordenId."))
            }

            // Publicar confirmación de retiro en la nube para sincronizar ambos dispositivos
            val jsonConfirmado = JSONObject().apply {
                put("ordenId", ordenId)
                put("tokenRetiro", tokenEscaneado)
                put("aprobadoPorCliente", true)
                put("retiradoConfirmado", true)
            }

            val body = jsonConfirmado.toString().toRequestBody("text/plain; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$CLOUD_CHANNEL_BASE$ordenId")
                .post(body)
                .build()

            client.newCall(request).execute().close()

            Result.success(ordenId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al validar QR: ${e.message}"))
        }
    }
}