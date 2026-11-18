# CentroTareas — Concurrencia y Corrutinas en Android (Jetpack Compose + MVVM)

**CentroTareas** es una aplicación Android desarrollada en **Kotlin** con **Jetpack Compose** y **Material 3** para el **Laboratorio N.° 14 (Hilos en Android)** de TECSUP. Su propósito es demostrar de forma práctica cómo ejecutar operaciones costosas (progreso progresivo, cálculo intensivo de CPU para contar números primos y cargas de datos simuladas secuenciales vs. paralelas) en segundo plano con **Corrutinas de Kotlin**, manteniendo el hilo principal (*Main / UI Thread*) 100 % libre de bloqueos, con soporte de cancelación cooperativa (`Job` + `ensureActive()`) y manejo estructurado de errores.

---

## 1. Estructura de Paquetes (`com.tecsup.centrotareas`)

```text
com.tecsup.centrotareas/
├── MainActivity.kt                  [E] Punto de entrada; conecta TareasViewModelFactory y PantallaTareas
├── model/
│   └── Estados.kt                   [E] Estados sellados de UI: PrimosUi y CargaUi
├── data/
│   ├── TareasRepository.kt          [E] Contrato (interface) de la capa de datos
│   └── TareasRepositoryLocal.kt     [G/A] Implementación con cargas simuladas y conteo de primos en Dispatchers.Default
├── viewmodel/
│   ├── TareasViewModel.kt           [G/A] Orquestación con viewModelScope, StateFlow, Job, async y manejo de errores
│   └── TareasViewModelFactory.kt    [E] Fábrica para inyectar TareasRepository en TareasViewModel
└── ui/
    ├── PantallaTareas.kt            [G/A] Interfaz declarativa en Jetpack Compose con 3 tarjetas interactivas
    └── theme/
        ├── Color.kt                 [E] Paleta Material 3 (azul institucional, verde de éxito y rojo de error)
        ├── Theme.kt                 [E] Configuración de CentroTareasTheme
        └── Type.kt                  [E] Tipografía Material 3
```

---

## 2. Arquitectura `MVVM + Repository`

```text
+-----------------------------------------------------------------------+
|                         VIEW (ui/PantallaTareas.kt)                   |
|  - Observa StateFlow mediante collectAsState()                        |
|  - Emite eventos de usuario (clicks, texto, switch) al ViewModel      |
|  - PROHIBIDO: lanzar corrutinas o usar Thread / lógica pesada         |
+-----------------------------------+-----------------------------------+
                                    | Eventos (tareaLarga, contar, cargar...)
                                    | Observación reactiva (StateFlow)
                                    v
+-----------------------------------------------------------------------+
|                   VIEWMODEL (viewmodel/TareasViewModel.kt)            |
|  - Lanza corrutinas en viewModelScope.launch                          |
|  - Gestiona cancelación con Job (trabajo, trabajoPrimos)              |
|  - Ejecuta cargas secuenciales y paralelas (coroutineScope + async)   |
|  - Expone estados inmutables StateFlow<T>                             |
|  - Solo conoce la interfaz TareasRepository (sin Context ni Compose)  |
+-----------------------------------+-----------------------------------+
                                    | Llamadas suspend
                                    v
+-----------------------------------------------------------------------+
|          REPOSITORY (data/TareasRepository.kt -> Local)               |
|  - contarPrimos(hasta): cálculo intensivo en Dispatchers.Default      |
|    con cancelación cooperativa mediante ensureActive()                |
|  - cargarPerfil() y cargarPedidos(simularError): simulación asíncrona |
+-----------------------------------------------------------------------+
```

---

## 3. Observación del Paso 1 (Bloqueo del Hilo Principal con `Thread.sleep`)

Al ejecutar `tareaLargaMala()` en el **Paso 1**:

```kotlin
fun tareaLargaMala() {
    _mensaje.value = "Trabajando..."
    Thread.sleep(4000)
    _mensaje.value = "Terminó"
}
```

1. **¿Qué pasa con la pantalla?**  
   Al pulsar `"Tarea larga (MAL)"` e inmediatamente tocar el botón `"Tócame: n"`, la interfaz queda completamente **congelada durante 4 segundos**. El botón `"Tócame"` no responde a los toques, no muestra animación de *ripple* y el contador no incrementa hasta que finaliza `Thread.sleep(4000)`.
2. **¿Por qué nunca se llega a ver el texto `"Trabajando..."`?**  
   Aunque `_mensaje.value = "Trabajando..."` actualiza el valor del `MutableStateFlow` antes del `Thread.sleep(4000)`, Compose necesita que el hilo principal (*Main Thread / Choreographer*) quede libre para procesar el siguiente fotograma (*frame*), ejecutar la recomposición y dibujar el nuevo texto en pantalla. Como `Thread.sleep(4000)` secuestra el hilo principal inmediatamente y al despertar cambia `_mensaje.value = "Terminó"` dentro del mismo bloque de ejecución antes de devolver el control al *Looper*, Android solo llega a recomponer y dibujar el estado final `"Terminó"`.
3. **¿Qué mostraría Android si la espera fuera de 10 segundos?**  
   Cuando el hilo principal permanece bloqueado y no procesa eventos de entrada (*input events*) durante **más de 5 segundos**, el sistema operativo Android dispara un error **ANR (*Application Not Responding*)**, mostrando al usuario el cuadro de diálogo del sistema: **"CentroTareas no responde"** con las opciones **Cerrar app** o **Esperar**.

---

## 4. Capturas de Pantalla

Coloca las 5 capturas de prueba del emulador o dispositivo físico en la carpeta [`docs/capturas/`](docs/capturas/):

| N.° | Vista / Escenario | Archivo | Vista previa |
|---|---|---|---|
| 1 | Tarea con barra de progreso en ejecución | `docs/capturas/01-progreso.png` | ![Progreso](docs/capturas/01-progreso.png) |
| 2 | Tarea cancelada a mitad del recorrido | `docs/capturas/02-cancelado.png` | ![Cancelado](docs/capturas/02-cancelado.png) |
| 3 | Cálculo de números primos finalizado | `docs/capturas/03-primos.png` | ![Primos](docs/capturas/03-primos.png) |
| 4 | Dos cargas: comparación secuencial vs. `async` | `docs/capturas/04-dos-cargas.png` | ![Dos cargas](docs/capturas/04-dos-cargas.png) |
| 5 | Error simulado con recuadro rojo y botón Reintentar | `docs/capturas/05-error.png` | ![Error](docs/capturas/05-error.png) |

---

## 5. Respuestas a la Sección "Para pensar"

### 1. ¿Por qué no se veía `Trabajando...` en el Paso 1?
Porque el hilo principal de Android funciona con una cola de mensajes (*Event Loop / Looper*) que procesa eventos de toque y dibuja fotogramas a 60/120 Hz. Cuando asignamos `_mensaje.value = "Trabajando..."`, solo marcamos el estado como pendiente de recomposición. Al llamar enseguida a `Thread.sleep(4000)` en el mismo hilo `main`, impedimos que el *Choreographer* ejecute el pase de composición y renderizado. Cuatro segundos después, antes de que el hilo `main` pueda dibujar un solo fotograma, el valor se sobrescribe con `"Terminó"`. Por eso la UI salta directamente de `"Listo"` a `"Terminó"`.

### 2. ¿Qué diferencia hay entre `Thread.sleep` y `delay`?
* **`Thread.sleep(ms)`** es una llamada **bloqueante** de Java: detiene por completo el hilo del sistema operativo donde se ejecuta. Si se invoca en el hilo `main`, congela el dibujado de la UI y la respuesta a toques.
* **`delay(ms)`** es una función de **suspensión (`suspend`)** de las corrutinas de Kotlin: **no bloquea el hilo**. En su lugar, pausa únicamente la corrutina actual y libera el hilo subyacente para que siga dibujando la pantalla o ejecutando otras tareas, reanudando la corrutina cuando vence el temporizador.

### 3. ¿Por qué `Dispatchers.Default` para los primos y `Dispatchers.IO` para la red?
* **`Dispatchers.Default`** está optimizado para **trabajo intensivo de CPU** (cálculos matemáticos como verificar primalidad, ordenar listas grandes o parsear JSON). Su pool tiene tantos hilos como núcleos de CPU tiene el dispositivo (mínimo 2), evitando cambios de contexto innecesarios que degradarían el rendimiento del procesador.
* **`Dispatchers.IO`** está diseñado para **operaciones de Entrada/Salida (I/O)** donde el hilo pasa la mayor parte del tiempo esperando respuesta externa (consultas de red HTTP, lectura/escritura en base de datos Room, archivos o DataStore). Por ello dispone de un pool elástico mucho mayor (hasta 64 hilos o más) para que las esperas de red o disco no agoten los hilos de cómputo.

### 4. ¿Qué pasaría con la corrutina si el usuario cierra la pantalla? ¿Qué papel cumple `viewModelScope`?
* Si la pantalla rota (cambio de configuración), el `ViewModel` sobrevive y la corrutina lanzada en `viewModelScope` **continúa ejecutándose sin reiniciarse**, conservando el progreso actual.
* Si el usuario cierra definitivamente la pantalla o sale de la actividad (`onCleared()` del `ViewModel`), **`viewModelScope` cancela automáticamente todas las corrutinas hijas activas**. Esto garantiza **Concurrencia Estructurada**, evitando fugas de memoria (*memory leaks*), consumo innecesario de CPU/batería o intentos de actualizar una vista destruida.

### 5. ¿Por qué `async` tarda 3 s y no 5 s? ¿Qué pasa si una de las dos cargas falla?
* **¿Por qué tarda ≈ 3 s?** En la carga secuencial (`Una por una`), `cargarPedidos()` (3 s) recién empieza cuando termina `cargarPerfil()` (2 s), sumando $2\text{ s} + 3\text{ s} \approx 5\text{ s}$. En cambio, con `async` dentro de `coroutineScope`, ambas corrutinas se inician **al mismo tiempo en paralelo** antes de llamar a `await()`. Por tanto, el tiempo total es el máximo de ambas duraciones: $\max(2\text{ s}, 3\text{ s}) \approx 3\text{ s}$.
* **¿Qué pasa si una falla?** Gracias a la concurrencia estructurada de `coroutineScope`, si `cargarPedidos(true)` lanza una `IOException("Sin conexión")`, el `coroutineScope` cancela automáticamente a las demás corrutinas hermanas activas y propaga la excepción hacia el bloque `try / catch` del `ViewModel`, el cual actualiza el estado a `CargaUi.Error("No se pudo cargar: Sin conexión")` sin que la aplicación se cierre.
