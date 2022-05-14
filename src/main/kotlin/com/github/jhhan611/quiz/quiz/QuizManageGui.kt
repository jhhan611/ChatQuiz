package com.github.jhhan611.quiz.quiz

import com.github.jhhan611.quiz.plugin
import com.github.jhhan611.quiz.utils.item
import com.github.jhhan611.quiz.utils.toComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class QuizManageGui(val quiz: Quiz) : Listener {
    val inventory = Bukkit.createInventory(null, 9, "${ChatColor.RED}퀴즈 삭제".toComponent()).apply {
        for (i in 0..8) setItem(i, Material.GRAY_STAINED_GLASS_PANE.item(" "))
        setItem(2, Material.LIGHT_GRAY_CONCRETE.item("${ChatColor.GRAY}취소"))
        setItem(4, quiz.getItem())
        setItem(6, Material.RED_CONCRETE.item("${ChatColor.DARK_RED}${ChatColor.BOLD}삭제"))
    }

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        if (e.inventory == inventory) e.isCancelled = true
        if (e.clickedInventory != inventory) return
        val player = e.whoClicked as Player
        when (e.slot) {
            2 -> {
                player.openInventory(QuizInventory.inventory)
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            }
            6 -> {
                if (QuizManager.quizQueue.removeIf { it == quiz }) {
                    QuizInventory.updateList()
                    player.sendMessage("${ChatColor.YELLOW}퀴즈를 삭제했습니다")
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
                    player.openInventory(QuizInventory.inventory)
                } else {
                    player.sendMessage("${ChatColor.RED}퀴즈를 찾을 수 없습니다")
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
                    player.openInventory(QuizInventory.inventory)
                }
            }
        }
    }


}