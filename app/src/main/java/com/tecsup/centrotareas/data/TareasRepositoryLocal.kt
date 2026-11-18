package com.tecsup.centrotareas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tecsup.centrotareas.model.AvancePrimos
import com.tecsup.centrotareas.model.ComparacionTiempos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

private val Context.centroTareasDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "centro_tareas_prefs"
)

class TareasRepositoryLocal(
    private val context: Context? = null
) : TareasRepository {

    private val historialMemoria = MutableStateFlow<List<String>>(emptyList())
    private val comparacionMemoria = MutableStateFlow(ComparacionTiempos())

    override fun ejecutarTareaConProgreso(): Flow<Float> = flow {
        for (i in 1..100) {
            delay(40)
            emit(i / 100f)
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun cargarPerfil(): String = withContext(Dispatchers.IO) {
        delay(2000)
        "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> = withContext(Dispatchers.IO) {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    override suspend fun contarPrimos(hasta: Int): Int = withContext(Dispatchers.Default) {
        var cantidad = 0
        for (n in 2..hasta) {
            ensureActive()
            if (esPrimo(n)) {
                cantidad++
            }
        }
        cantidad
    }

    override fun contarPrimosConProgreso(hasta: Int): Flow<AvancePrimos> = flow {
        var cantidad = 0
        val total = (hasta - 1).coerceAtLeast(1)
        val pasoEmision = (total / 100).coerceAtLeast(1)

        emit(AvancePrimos(progreso = 0f, cantidadActual = 0, terminado = false))

        for (n in 2..hasta) {
            currentCoroutineContext().ensureActive()
            if (esPrimo(n)) {
                cantidad++
            }
            val procesados = n - 1
            if (procesados % pasoEmision == 0 || n == hasta) {
                val fraccion = (procesados.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                emit(
                    AvancePrimos(
                        progreso = fraccion,
                        cantidadActual = cantidad,
                        terminado = (n == hasta)
                    )
                )
            }
        }
    }.flowOn(Dispatchers.Default)

    override fun observarHistorial(): Flow<List<String>> {
        val ctx = context ?: return historialMemoria
        return ctx.centroTareasDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .map { prefs ->
                val raw = prefs[CLAVE_HISTORIAL].orEmpty()
                if (raw.isBlank()) emptyList() else raw.split(SEPARADOR_HISTORIAL)
            }
            .flowOn(Dispatchers.IO)
    }

    override fun observarComparacionTiempos(): Flow<ComparacionTiempos> {
        val ctx = context ?: return comparacionMemoria
        return ctx.centroTareasDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .map { prefs ->
                ComparacionTiempos(
                    unaPorUnaMs = prefs[CLAVE_UNA_POR_UNA_MS],
                    aLaVezMs = prefs[CLAVE_A_LA_VEZ_MS]
                )
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun guardarResultadoHistorial(entrada: String) = withContext(Dispatchers.IO) {
        val ctx = context
        if (ctx == null) {
            historialMemoria.value = (listOf(entrada) + historialMemoria.value).take(MAX_HISTORIAL)
            return@withContext
        }
        ctx.centroTareasDataStore.edit { prefs ->
            val actuales = prefs[CLAVE_HISTORIAL]
                ?.takeIf { it.isNotBlank() }
                ?.split(SEPARADOR_HISTORIAL)
                ?: emptyList()
            val actualizados = (listOf(entrada) + actuales).take(MAX_HISTORIAL)
            prefs[CLAVE_HISTORIAL] = actualizados.joinToString(SEPARADOR_HISTORIAL)
        }
    }

    override suspend fun guardarTiempoCarga(modo: String, milisegundos: Long) = withContext(Dispatchers.IO) {
        val ctx = context
        if (ctx == null) {
            comparacionMemoria.value = if (modo == "Una por una") {
                comparacionMemoria.value.copy(unaPorUnaMs = milisegundos)
            } else {
                comparacionMemoria.value.copy(aLaVezMs = milisegundos)
            }
            return@withContext
        }
        ctx.centroTareasDataStore.edit { prefs ->
            if (modo == "Una por una") {
                prefs[CLAVE_UNA_POR_UNA_MS] = milisegundos
            } else if (modo == "A la vez") {
                prefs[CLAVE_A_LA_VEZ_MS] = milisegundos
            }
        }
    }

    override suspend fun limpiarHistorial() = withContext(Dispatchers.IO) {
        val ctx = context
        if (ctx == null) {
            historialMemoria.value = emptyList()
            comparacionMemoria.value = ComparacionTiempos()
            return@withContext
        }
        ctx.centroTareasDataStore.edit { prefs ->
            prefs.remove(CLAVE_HISTORIAL)
            prefs.remove(CLAVE_UNA_POR_UNA_MS)
            prefs.remove(CLAVE_A_LA_VEZ_MS)
        }
    }

    private fun esPrimo(n: Int): Boolean {
        if (n < 2) return false
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }

    private companion object {
        const val MAX_HISTORIAL = 5
        const val SEPARADOR_HISTORIAL = "\n"
        val CLAVE_HISTORIAL = stringPreferencesKey("ultimos_5_resultados")
        val CLAVE_UNA_POR_UNA_MS = longPreferencesKey("tiempo_una_por_una_ms")
        val CLAVE_A_LA_VEZ_MS = longPreferencesKey("tiempo_a_la_vez_ms")
    }
}
