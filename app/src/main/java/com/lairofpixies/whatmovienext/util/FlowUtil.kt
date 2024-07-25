package com.lairofpixies.whatmovienext.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T, R> StateFlow<T>.mapState(
    context: CoroutineContext = EmptyCoroutineContext,
    transform: (T) -> R,
): StateFlow<R> {
    val originalScope = this as? CoroutineScope
    return map(transform).stateIn(
        originalScope ?: CoroutineScope(context),
        SharingStarted.Eagerly,
        transform(value),
    )
}
