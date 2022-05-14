package com.github.jhhan611.quiz.quiz

import com.github.jhhan611.quiz.economy
import com.github.jhhan611.quiz.plugin
import com.github.jhhan611.quiz.utils.findSimilarity
import com.github.jhhan611.quiz.utils.toComponent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object QuizManager : Listener {
    val line = "${ChatColor.GOLD}${
        StringUtils.repeat(
            "-",
            10
        )
    }${ChatColor.GRAY}<${ChatColor.GREEN}${ChatColor.BOLD}QUIZ${ChatColor.GRAY}>${ChatColor.GOLD}${
        StringUtils.repeat(
            "-",
            10
        )
    }"
    val endLine = "${ChatColor.GOLD}${StringUtils.repeat("-", 26)}"
    val quizQueue: Queue<Quiz> = LinkedList()
    private const val quizCooldown = 15
    var cooldown = 0
    var timer = 0
    var quiz: Quiz? = null
    private var timerTask: BukkitRunnable? = null

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun startQuiz() {
        val quiz = quizQueue.poll() ?: return
        QuizInventory.updateList()
        this.quiz = quiz
        timer = quiz.time.duration
        Bukkit.broadcast(
            listOf(
                line,
                "${ChatColor.AQUA}문제 출제자 ${ChatColor.GRAY}: ${ChatColor.YELLOW}${quiz.player.name}",
                "",
                "${ChatColor.YELLOW}문제 ${ChatColor.GRAY}: ${ChatColor.WHITE}${
                    ChatColor.translateAlternateColorCodes(
                        '&',
                        quiz.question
                    )
                }",
                "${ChatColor.LIGHT_PURPLE}제한시간 ${ChatColor.GRAY}: ${ChatColor.WHITE}${quiz.time.optionName}",
                endLine
            ).joinToString("\n").toComponent()
        )
        sendHint(quiz)
        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
        }
        timerTask = object : BukkitRunnable() {
            override fun run() {
                timer--
                sendHint(quiz)
                if (timer <= 0) {
                    Bukkit.getOnlinePlayers().forEach {
                        it.playSound(it.location, Sound.BLOCK_ANVIL_LAND, 1f, 1f)
                    }
                    Bukkit.broadcast(
                        listOf(
                            line,
                            "${ChatColor.RED}아무도 답을 맞추지 못했습니다!",
                            "",
                            "${ChatColor.YELLOW}답 ${ChatColor.GRAY}: ${ChatColor.WHITE}${
                                ChatColor.translateAlternateColorCodes(
                                    '&',
                                    quiz.answer
                                )
                            }",
                            endLine
                        ).joinToString("\n").toComponent()
                    )
                    stopQuiz()
                    return cancel()
                }
            }
        }.also { it.runTaskTimer(plugin, 20, 20) }
    }

    private var init = 0
    private val quizHint: MutableList<Pair<String, Boolean>> = mutableListOf()

    fun sendHint(quiz: Quiz) {
        val hintText = quiz.answer.replace("[^\\s]".toRegex(), "\uF802")
        val letterCount = hintText.count { it == '' }
        when (quiz.flag) {
            QuizFlag.SHOW_LENGTH ->
                if (quiz.time.duration == timer)
                    Bukkit.broadcast("${ChatColor.GREEN}힌트 ${ChatColor.GRAY}: ${ChatColor.GRAY}$hintText".toComponent())
            QuizFlag.SHOW_LENGTH_AFTER -> {
                if (timer == quiz.time.duration / 2)
                    Bukkit.broadcast("${ChatColor.GREEN}힌트 ${ChatColor.GRAY}: ${ChatColor.GRAY}$hintText".toComponent())
            }
            QuizFlag.REVEAL_LETTER -> {
                if (init == 0) {
                    quizHint.addAll(quiz.answer.chunked(1).map { if (it == " ") Pair(it, true) else Pair(it, false) })
                    init = 1
                }
                val count = quizHint.count { !it.second }
                val hintCount = (letterCount * 0.3).toInt().coerceAtMost(10) + 2
                if (timer > 0 && (timer % (quiz.time.duration / hintCount)) == 0 && count > 0) {
                    if (init != 2) {
                        init = 2
                        return
                    }
                    var idx: Int
                    do {
                        idx = Random().nextInt(quizHint.size)
                    } while (quizHint[idx].second)
                    quizHint[idx] = Pair(quizHint[idx].first, true)
                    val hint =
                        quizHint.joinToString(
                            "",
                            transform = { if (it.second) "${ChatColor.GOLD}${it.first}" else "${ChatColor.GRAY}\uF802" })

                    Bukkit.broadcast("${ChatColor.GREEN}힌트 ${ChatColor.GRAY}: $hint".toComponent())
                }
            }
            else -> return
        }
    }

    fun stopQuiz() {
        quiz = null
        cooldown = quizCooldown
        timer = 0
        timerTask?.cancel()
        quizHint.clear()
        init = 0
        if (quizQueue.isNotEmpty()) Bukkit.broadcast(Component.text("${ChatColor.GRAY}${ChatColor.ITALIC}다음 퀴즈가 ${quizCooldown}초 후에 출제됩니다"))
        object : BukkitRunnable() {
            override fun run() {
                cooldown--
                if (cooldown <= 0) {
                    startQuiz()
                    return cancel()
                }
            }
        }.runTaskTimer(plugin, 20, 20)
    }

    private fun checkAnswer(value: String): Boolean {
        val quiz = quiz ?: return false
        val str = if (quiz.normalize)
            ChatColor.stripColor(value).toString().lowercase()
        else value
        if (findSimilarity(
                if (quiz.normalize) quiz.answer.lowercase() else quiz.answer,
                str
            ) >= quiz.validate.value
        ) return true
        return false
    }


    fun getPlayerQuiz(player: Player): Quiz? {
        return quizQueue.find { it.player == player }
    }

    @EventHandler
    fun onChat(e: AsyncChatEvent) {
        if (quiz == null) return
        val text = (e.message() as TextComponent).content()
        if (checkAnswer(text)) {
            e.isCancelled = true
            if (e.player == quiz?.player) {
                e.player.sendMessage("${ChatColor.RED}문제 출제자는 정답을 맞출 수 없습니다")
                e.player.playSound(e.player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                return
            } else {
                Bukkit.getOnlinePlayers().forEach {
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                }
                Bukkit.broadcast(
                    listOf(
                        line,
                        "${ChatColor.YELLOW}${e.player.name}${ChatColor.GREEN}님이 답을 맞췄습니다!",
                        "",
                        "${ChatColor.YELLOW}답 ${ChatColor.GRAY}: ${ChatColor.WHITE}${
                            ChatColor.translateAlternateColorCodes(
                                '&',
                                quiz?.answer ?: return
                            )
                        }",
                        "${ChatColor.YELLOW}보상 ${ChatColor.GRAY}: ${ChatColor.AQUA}${format.format(quiz?.price)}도토리",
                        endLine
                    ).joinToString("\n").toComponent()
                )
                economy.depositPlayer(e.player, quiz?.price?.toDouble() ?: 0.0)
                stopQuiz()
            }
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        quizQueue.removeIf { it.player == e.player }
        QuizInventory.updateList()
    }
}