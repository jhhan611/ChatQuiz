package com.github.jhhan611.quiz.quiz

import com.github.jhhan611.quiz.utils.toComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class QuizCommand : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        string: String,
        args: Array<out String>
    ): Boolean {
        val player = (sender as? Player) ?: return true
        player.openInventory(QuizInventory.inventory)
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        string: String,
        strings: Array<out String>
    ): MutableList<String> {
        return mutableListOf()
    }
}