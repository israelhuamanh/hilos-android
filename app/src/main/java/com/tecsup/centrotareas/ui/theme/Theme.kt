package com.tecsup.centrotareas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerBlue,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondarySlate,
    onSecondary = OnSecondaryWhite,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerDark,
    error = ErrorRedText,
    errorContainer = ErrorRedBg,
    onErrorContainer = ErrorRedText,
    surface = SurfaceLight,
    surfaceVariant = CardSurfaceWhite,
    outline = OutlineLight
)

val CentroTareasShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp)
)

@Composable
fun CentroTareasTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        shapes = CentroTareasShapes,
        typography = CentroTareasTypography,
        content = content
    )
}
