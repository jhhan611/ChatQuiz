package com.github.jhhan611.quiz.quiz

import com.github.jhhan611.quiz.utils.item
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Duration

data class Quiz(
    val player: Player,
    val question: String,
    val answer: String,
    val price: Long,
    val flag: QuizFlag,
    val validate: QuizValidate,
    val time: QuizTime,
    val normalize: Boolean
) {
    fun getItem(): ItemStack {
        return Material.PAPER.item("${ChatColor.AQUA}${player.name}${ChatColor.YELLOW}님의 퀴즈",
                "${ChatColor.AQUA}보상 ${ChatColor.GRAY}- ${ChatColor.GOLD}${format.format(price)}도토리"
            )
    }
}

enum class QuizFlag(val optionName: String) {
    SHOW_LENGTH("글자 수 공개"),
    SHOW_LENGTH_AFTER("제한시간의 반 경과 시 글자 수 공개"),
    REVEAL_LETTER("글자 공개"),
    DEFAULT("일반")
}

enum class QuizValidate(val optionName: String, val value: Double) {
    PERFECT_MATCH("100% 일치", 1.0),
    MOSTLY_MATCHING("95% 일치", 0.95),
    ALMOST_MATCHING("90% 일치", 0.9),
    SLIGHTLY_MATCHING("85% 일치", 0.85),
    VERY_SLIGHTLY_MATCHING("80% 일치", 0.8)
}

enum class QuizTime(val optionName: String, val duration: Int) {
    FIVE_SECOND("5초", 5),
    TEN_SECOND("10초", 10),
    THIRTY_SECOND("30초", 30),
    FORTY_FIVE_SECOND("45초", 45),
    ONE_MINUTE("60초", 60),
    ONE_MINUTE_THIRTY_SECONDS("90초", 90),
    TWO_MINUTES("120초", 120)
}