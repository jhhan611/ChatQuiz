package com.github.jhhan611.quiz.utils

import net.minecraft.network.chat.IChatBaseComponent
import java.text.Normalizer
import kotlin.math.max
import kotlin.math.min

fun String.asJsonText(): String {
    return IChatBaseComponent.ChatSerializer.a(IChatBaseComponent.a(this))
}

inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}

inline fun <reified T : Enum<T>> T.before(): T {
    val values = enumValues<T>()
    val nextOrdinal = if (ordinal - 1 < 0) values.size - 1 else ordinal - 1
    return values[nextOrdinal]
}

private fun getLevenshteinDistance(X: String, Y: String): Int {
    val m = X.length
    val n = Y.length
    val t = Array(m + 1) { IntArray(n + 1) }
    for (i in 1..m) {
        t[i][0] = i
    }
    for (j in 1..n) {
        t[0][j] = j
    }
    var cost: Int
    for (i in 1..m) {
        for (j in 1..n) {
            cost = if (X[i - 1] == Y[j - 1]) 0 else 1
            t[i][j] = min(
                min(t[i - 1][j] + 1, t[i][j - 1] + 1),
                t[i - 1][j - 1] + cost
            )
        }
    }
    return t[m][n]
}

fun findSimilarity(x: String, y: String): Double {
    val maxLength = max(x.length, y.length)
    return if (maxLength > 0) {
        (maxLength * 1.0 - getLevenshteinDistance(x, y)) / maxLength * 1.0
    } else 1.0
}