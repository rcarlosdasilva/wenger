package io.github.rcarlosdasilva.wenger.feature.extension

/**
 * 表达式为true时，执行代码块
 */
inline fun <T, R> T.runIf(condition: Boolean, block: T.() -> R): R? = if (condition) block() else null

/**
 * 为true时，执行代码块
 */
inline fun <R> Boolean.runIf(block: () -> R): R? = if (this) block() else null

/**
 * 表达式为false时，执行代码块
 */
inline fun <T, R> T.runUnless(condition: Boolean, block: T.() -> R): R? = if (!condition) block() else null

/**
 * 为false时，执行代码块
 */
inline fun <R> Boolean.runUnless(block: () -> R): R? = if (!this) block() else null