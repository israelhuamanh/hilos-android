package com.tecsup.centrotareas.model

data class AvancePrimos(
    val progreso: Float,
    val cantidadActual: Int,
    val terminado: Boolean = false
)

data class ComparacionTiempos(
    val unaPorUnaMs: Long? = null,
    val aLaVezMs: Long? = null
) {
    val ahorroMs: Long?
        get() = if (unaPorUnaMs != null && aLaVezMs != null) {
            (unaPorUnaMs - aLaVezMs).coerceAtLeast(0L)
        } else {
            null
        }

    val ahorroPorcentaje: Int?
        get() = if (unaPorUnaMs != null && unaPorUnaMs > 0L && aLaVezMs != null) {
            (((unaPorUnaMs - aLaVezMs).coerceAtLeast(0L) * 100L) / unaPorUnaMs).toInt()
        } else {
            null
        }
}

sealed interface PrimosUi {
    data object Inicial : PrimosUi
    data class Calculando(val progreso: Float = 0f) : PrimosUi
    data class Listo(val cantidad: Int, val milisegundos: Long) : PrimosUi
    data object Cancelado : PrimosUi
}

sealed interface CargaUi {
    data object Inicial : CargaUi
    data object Cargando : CargaUi
    data class Exito(
        val modo: String,
        val perfil: String,
        val pedidos: Int,
        val milisegundos: Long
    ) : CargaUi
    data class Error(val mensaje: String) : CargaUi
}
