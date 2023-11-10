package io.github.thisisthepy.pycomposeui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


var moduleNamePreset: String = ""
    set(name) {
        if (field.isNotEmpty()) {
            throw IllegalStateException("Python Module Name Preset can be set only once.")
        }
        field = name
    }


@Composable
fun PythonLauncher(
    content: @Composable () -> Unit
) {
    if (!Python.isStarted()) {
        val platform = AndroidPlatform(LocalContext.current)
        platform.redirectStdioToLogcat()
        Python.start(platform)
        println("PythonLauncher: Python is started...")
        val runtime = Python.getInstance().getModule("pycomposeui.runtime")
        val composable = runtime["Composable"]
        if (composable != null) {
            composable.callAttr("register_composer", currentComposer)
            println("PythonLauncher: Composer registered.")
        } else {
            throw RuntimeException("PythonLauncher: Failed to register Composer. Cannot find Composable class in python runtime.")
        }
    }
    content()
}

private fun getPyModule(name: String): PyObject {
    if (!Python.isStarted()) throw IllegalStateException("Python is not started. Please run PythonLauncher Composable first.")
    return Python.getInstance().getModule(name)
}

val sys: PyObject
    get() = getPyModule("sys")
val version: String
    get() = sys["version"].toString()
val os: PyObject
    get() = getPyModule("os")

@Composable
fun PythonWidget(
    modifier: Modifier = Modifier,
    composableName: String,
    moduleName: String = moduleNamePreset,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable (args: Array<Any>) -> Unit = {}
) {
    val module = getPyModule(moduleName)
    Surface(modifier, shape, color, contentColor, tonalElevation, shadowElevation, border) {
        module.callAttr(composableName, content)
    }
}

@Composable
fun PythonAppView(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null
) {
    PythonWidget(modifier, moduleNamePreset, "App",
        shape, color, contentColor, tonalElevation, shadowElevation, border)
}

fun runPy(functionName: String, moduleName: String = moduleNamePreset): String {
    val module = getPyModule(moduleName)
    val result = module.callAttr(functionName)
    return result.toString()
}

@Composable
fun BoxBinding(modifier: Modifier = Modifier,
               contentAlignment: Alignment = Alignment.TopStart,
               propagateMinConstraints: Boolean = false,
               content: @Composable BoxScope.() -> Unit) {
    Box(modifier, contentAlignment, propagateMinConstraints, content)
}

fun getModifier() = Modifier

fun getAlignment() = Alignment
