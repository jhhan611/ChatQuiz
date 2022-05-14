package com.github.jhhan611.quiz.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

fun String.toComponent(): TextComponent {
    return Component.text(this)
}