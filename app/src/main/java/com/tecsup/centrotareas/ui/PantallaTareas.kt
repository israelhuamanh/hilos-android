package com.tecsup.centrotareas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.centrotareas.ui.theme.CardSurfaceWhite
import com.tecsup.centrotareas.ui.theme.ErrorRedBg
import com.tecsup.centrotareas.ui.theme.ErrorRedBorder
import com.tecsup.centrotareas.ui.theme.ErrorRedText
import com.tecsup.centrotareas.ui.theme.OutlineLight
import com.tecsup.centrotareas.ui.theme.SuccessGreenBg
import com.tecsup.centrotareas.ui.theme.SuccessGreenBorder
import com.tecsup.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val primos by vm.primos.collectAsState()
    val carga by vm.carga.collectAsState()

    var toques by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CentroTareas",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta 1: Tarea con progreso (Pasos 1 a 4)
            TarjetaSeccion(titulo = "Tarea con progreso") {
                Button(onClick = { toques++ }) {
                    Text(text = "Tócame: $toques")
                }

                Button(onClick = { vm.tareaLarga() }) {
                    Text(text = "Iniciar")
                }

                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${(progreso * 100).toInt()} %",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Tarjeta 2: Contar números primos (Paso 5)
            TarjetaSeccion(titulo = "Contar números primos") {
                // TODO Paso 5: campo numérico, botones Contar y Cancelar, y estado PrimosUi
                Text(
                    text = "Estado: ${primos::class.simpleName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Tarjeta 3: Dos cargas (Pasos 6 y 7)
            TarjetaSeccion(titulo = "Dos cargas") {
                // TODO Paso 6: botones Una por una y A la vez, y estado CargaUi
                // TODO Paso 7: Switch Simular error y botón Reintentar
                Text(
                    text = "Estado: ${carga::class.simpleName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun TarjetaSeccion(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        border = BorderStroke(1.dp, OutlineLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            contenido()
        }
    }
}

@Composable
fun RecuadroExito(
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = SuccessGreenBg),
        border = BorderStroke(1.dp, SuccessGreenBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = contenido
        )
    }
}

@Composable
fun RecuadroError(
    mensaje: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = ErrorRedBg),
        border = BorderStroke(1.dp, ErrorRedBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mensaje,
                color = ErrorRedText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
