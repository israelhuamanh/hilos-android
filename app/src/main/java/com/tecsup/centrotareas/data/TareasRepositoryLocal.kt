package com.tecsup.centrotareas.data

import com.tecsup.centrotareas.model.AvancePrimos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {

    override fun ejecutarTareaConProgreso(): Flow<Float> = flow {
        for (i in 1..100) {
            delay(40)
            emit(i / 100f)
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
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

    private fun esPrimo(n: Int): Boolean {
        if (n < 2) return false
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
