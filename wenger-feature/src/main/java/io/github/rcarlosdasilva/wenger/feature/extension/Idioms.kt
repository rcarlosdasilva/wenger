package io.github.rcarlosdasilva.wenger.feature.extension

public inline fun <T, R> T.runIf(condition: Boolean, block: T.() -> R): R? = if (condition) block() else null

public inline fun <R> Boolean.runIf(block: () -> R): R? = if (this) block() else null