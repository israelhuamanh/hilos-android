package com.tecsup.centrotareas.data

import com.tecsup.centrotareas.model.AvancePrimos
import com.tecsup.centrotareas.model.ComparacionTiempos
import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    fun ejecutarTareaConProgreso(): Flow<Float>
    suspend fun contarPrimos(hasta: Int): Int
    fun contarPrimosConProgreso(hasta: Int): Flow<AvancePrimos>
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>

    // Mejora 4: Comparación de tiempos e historial persistente (últimos 5 resultados)
    fun observarHistorial(): Flow<List<String>>
    fun observarComparacionTiempos(): Flow<ComparacionTiempos>
    suspend fun guardarResultadoHistorial(entrada: String)
    suspend fun guardarTiempoCarga(modo: String, milisegundos: Long)
    suspend fun limpiarHistorial()
}
