package com.lairofpixies.whatmovienext.views.state

enum class ListMode {
    ALL,
    WATCHED,
    PENDING,
    ;

    fun next(): ListMode = entries[(ordinal + 1) % entries.size]
}
