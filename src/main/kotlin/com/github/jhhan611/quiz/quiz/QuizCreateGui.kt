package com.github.jhhan611.quiz.quiz

import com.github.jhhan611.quiz.economy
import com.github.jhhan611.quiz.plugin
import com.github.jhhan611.quiz.utils.*
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.IOException
import java.text.DecimalFormat

private val clickLore = listOf(
    "",
    "${ChatColor.YELLOW}좌클릭하여 다음 모드 선택",
    "${ChatColor.AQUA}우클릭하여 이전 모드 선택"
)
val format = DecimalFormat("#,###")

class QuizCreateGui(private val player: Player) : Listener {
    private var question: String? = null
    private var answer: String? = null
    private var flag: QuizFlag = QuizFlag.DEFAULT
    private var validate: QuizValidate = QuizValidate.PERFECT_MATCH
    private var time: QuizTime = QuizTime.ONE_MINUTE
    private var normalize: Boolean = true
    private var minPrice = plugin.config.getLong("min-price", 10000)

    val inventory = Bukkit.createInventory(null, 54, "${ChatColor.GREEN}퀴즈 제작".toComponent()).apply {
        for (i in 0..8) {
            setItem(i, Material.WHITE_STAINED_GLASS_PANE.item(" "))
            setItem(i + 45, Material.WHITE_STAINED_GLASS_PANE.item(" "))
        }
        for (i in 9..44) setItem(i, Material.GRAY_STAINED_GLASS_PANE.item(" "))

        setItem(4, Material.WRITABLE_BOOK.item("${ChatColor.GOLD}퀴즈 출제"))
        setItem(49, Material.BARRIER.item("${ChatColor.RED}나가기"))
    }

    init {
        updateInventory()
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun createQuiz() {
        if (QuizManager.quizQueue.any { it.player == player }) {
            player.sendMessage("${ChatColor.RED}이미 퀴즈를 등록했습니다")
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            return
        }
        val q = question
        val a = answer
        if (q == null || a == null) {
            player.sendMessage("${ChatColor.RED}문제와 정답을 모두 입력해주세요")
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            return
        }
        val price = getPrice()
        if (price > economy.getBalance(player)) {
            player.sendMessage("${ChatColor.RED}도토리가 부족합니다")
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            return
        }
        economy.withdrawPlayer(player, price.toDouble())
        QuizManager.quizQueue.offer(Quiz(player, q, a, (price * 0.75).toLong(), flag, validate, time, normalize))
        if (QuizManager.quizQueue.size == 1 && QuizManager.cooldown == 0 && QuizManager.timer == 0) {
            QuizManager.startQuiz()
            player.closeInventory()
            return
        }
        QuizInventory.updateList()
        player.sendMessage("${ChatColor.YELLOW}퀴즈를 만들었습니다!")
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
        player.openInventory(QuizInventory.inventory)
    }

    private fun getPrice(): Long {
        var price = (economy.getBalance(player) * 0.01).toLong()
        price = price.coerceAtLeast(minPrice)
        price += ((time.ordinal - 2).coerceAtLeast(0) * 0.005 * economy.getBalance(player)).toLong()
        price += (answer?.length?.minus(10))?.coerceAtLeast(0)?.times(50) ?: 0
        return price
    }

    private val changeLore = "${ChatColor.YELLOW}클릭하여 변경"
    private fun updateInventory() {
        inventory.apply {
            setItem(
                22,
                Material.WRITABLE_BOOK.item(
                    "${ChatColor.GOLD}퀴즈 출제",
                    "${ChatColor.AQUA}비용 ${ChatColor.GRAY}- ${ChatColor.GOLD}도토리 ${format.format(getPrice())}개"
                )
            )

            val q = question
            if (q == null)
                setItem(20, Material.PAPER.item("${ChatColor.YELLOW}문제를 입력하세요"))
            else
                setItem(
                    20,
                    Material.PAPER.item(
                        "${ChatColor.WHITE}${ChatColor.translateAlternateColorCodes('&', q)}",
                        changeLore
                    )
                )

            val a = answer
            if (a == null)
                setItem(24, Material.PAPER.item("${ChatColor.GREEN}정답을 입력하세요"))
            else
                setItem(
                    24,
                    Material.PAPER.item(
                        "${ChatColor.WHITE}${ChatColor.translateAlternateColorCodes('&', a)}",
                        changeLore
                    )
                )

            setItem(
                29,
                Material.GOLD_INGOT.item(
                    "${ChatColor.AQUA}판정 변경",
                    "${ChatColor.WHITE}판정 ${ChatColor.GRAY}- ${ChatColor.GOLD}${validate.optionName}",
                    *clickLore.toTypedArray()
                )
            )
            setItem(
                33,
                Material.IRON_INGOT.item(
                    "${ChatColor.AQUA}표준화 여부",
                    "${ChatColor.WHITE}표준화 ${ChatColor.GRAY}- ${ChatColor.GOLD}${if (normalize) "켜짐" else "꺼짐"}",
                    "",
                    "${ChatColor.YELLOW}클릭하여 전환",
                )
            )
            setItem(
                30,
                Material.COMMAND_BLOCK.item(
                    "${ChatColor.LIGHT_PURPLE}모드 변경",
                    "${ChatColor.WHITE}모드 ${ChatColor.GRAY}- ${ChatColor.GOLD}${flag.optionName}",
                    *clickLore.toTypedArray()
                )
            )
            setItem(
                32,
                Material.CLOCK.item(
                    "${ChatColor.LIGHT_PURPLE}제한시간 변경",
                    "${ChatColor.WHITE}제한시간 ${ChatColor.GRAY}- ${ChatColor.GOLD}${time.optionName}",
                    *clickLore.toTypedArray()
                )
            )
        }
    }


    private val slots = listOf(20, 24, 22, 29, 30, 32, 33, 49)

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        if (e.inventory == inventory) e.isCancelled = true
        if (e.clickedInventory != inventory) return
        val player = e.whoClicked as Player
        val right = e.click.isRightClick
        if (slots.contains(e.slot)) player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        when (e.slot) {
            20 -> {
                waitMode = ChatWaitMode.QUESTION
                player.sendMessage("${ChatColor.GRAY}- ${ChatColor.YELLOW}문제를 채팅창에 입력해주세요 ${ChatColor.GRAY}-")
                player.closeInventory()
            }
            24 -> {
                waitMode = ChatWaitMode.ANSWER
                player.sendMessage("${ChatColor.GRAY}- ${ChatColor.AQUA}정답을 채팅창에 입력해주세요 ${ChatColor.GRAY}-")
                player.closeInventory()
            }
            22 -> createQuiz()
            29 -> validate = if (right) validate.next() else validate.before()
            30 -> flag = if (right) flag.before() else flag.next()
            32 -> time = if (right) time.before() else time.next()
            33 -> normalize = !normalize
            49 -> player.openInventory(QuizInventory.inventory)
            else -> return
        }
        updateInventory()
    }

    private var waitMode: ChatWaitMode? = null

    @EventHandler
    fun onChat(e: AsyncChatEvent) {
        val message = (e.message() as TextComponent).content().trim()
        if (e.player != player) return
        if (waitMode == null) return
        if (message.length < 5 && waitMode == ChatWaitMode.QUESTION) {
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            player.sendMessage("${ChatColor.RED}문제의 길이가 너무 짧습니다".toComponent())
        } else {
            if (waitMode == ChatWaitMode.QUESTION) question = message
            else if (waitMode == ChatWaitMode.ANSWER) answer = message
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
        }
        waitMode = null
        e.isCancelled = true
        updateInventory()
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) {
            player.openInventory(inventory)
        }
    }

    enum class ChatWaitMode {
        QUESTION,
        ANSWER
    }
}

