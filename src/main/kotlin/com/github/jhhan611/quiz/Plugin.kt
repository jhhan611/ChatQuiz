package com.github.jhhan611.quiz

import com.github.jhhan611.quiz.quiz.QuizCommand
import com.github.jhhan611.quiz.utils.findSimilarity
import com.github.jhhan611.quiz.utils.toComponent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.TextComponent
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin


lateinit var plugin: JavaPlugin
lateinit var economy: Economy

class Plugin : JavaPlugin() {
    init {
        plugin = this
    }

    override fun onEnable() {
        saveDefaultConfig()

        economy = server.servicesManager.getRegistration(Economy::class.java)?.provider!!
        getCommand("quiz")?.setExecutor(QuizCommand())
    }

}