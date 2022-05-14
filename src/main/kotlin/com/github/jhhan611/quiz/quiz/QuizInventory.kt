package com.github.jhhan611.quiz.quiz

import com.github.jhhan611.quiz.plugin
import com.github.jhhan611.quiz.utils.item
import com.github.jhhan611.quiz.utils.toComponent
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

object QuizInventory : Listener {
    val inventory = Bukkit.createInventory(null, 54, "${ChatColor.GOLD}퀴즈 목록".toComponent()).apply {
        for (i in 0..8) {
            setItem(i, Material.WHITE_STAINED_GLASS_PANE.item(" "))
            setItem(i + 45, Material.WHITE_STAINED_GLASS_PANE.item(" "))
        }
        setItem(4, Material.BOOK.item("${ChatColor.GOLD}퀴즈 목록"))
        setItem(49, Material.BARRIER.item("${ChatColor.RED}나가기"))
        setItem(53, Material.COMMAND_BLOCK.item("${ChatColor.LIGHT_PURPLE}퀴즈 추가 / 삭제"))
    }

    init {
        updateList()
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun updateList() {
        for (i in 9..44) {
            inventory.setItem(i, Material.GRAY_STAINED_GLASS_PANE.item(" "))
        }

        QuizManager.quizQueue.toList().subList(0, QuizManager.quizQueue.size.coerceAtMost(36))
            .forEachIndexed { idx, quiz ->
                inventory.setItem(idx + 9, quiz.getItem())
            }
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        if (e.inventory == inventory) e.isCancelled = true
        if (e.clickedInventory != inventory) return
        val player = e.whoClicked as Player
        when (e.slot) {
            49 -> {
                player.closeInventory()
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            }
            53 -> {
                val quiz = QuizManager.getPlayerQuiz(player)
                if (quiz == null)
                    player.openInventory(QuizCreateGui(player).inventory)
                else player.openInventory(
                    QuizManageGui(quiz).inventory
                )
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            }
        }
        if (!player.hasPermission("quiz.delete")) return
        val quiz = QuizManager.quizQueue.find { it.getItem() == e.currentItem } ?: return
        player.openInventory(QuizManageGui(quiz).inventory)
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
    }
}