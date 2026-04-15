package com.count.iautista.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Desempacota a cadeia de ContextWrappers até encontrar a Activity.
 * LocalContext.current em Compose retorna um ContextWrapper, não uma Activity diretamente.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
