package com.github.jhhan611.quiz.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag

fun Material.item(name: Component, vararg lore: Component): ItemStack {
    return this.item().lore(*lore).name(name)
}

fun Material.item(name: String, vararg lore: String): ItemStack {
    return this.item().lore(*lore).name(name)
}

fun Material.item(): ItemStack {
    return ItemStack(this)
}

fun ItemStack.name(name: String): ItemStack {
    return this.name(name.toComponent())
}

fun ItemStack.name(name: Component): ItemStack {
    return this.apply {
        itemMeta = itemMeta?.apply {
            displayName(name)
        }
    }
}

fun ItemStack.lore(vararg lore: String): ItemStack {
    return this.apply {
        itemMeta = itemMeta?.apply {
            lore(lore.map { it.toComponent() })
        }
    }
}

fun ItemStack.lore(vararg lore: Component): ItemStack {
    return this.apply {
        itemMeta = itemMeta?.apply {
            lore(lore.toList())
        }
    }
}

fun ItemStack.flags(): ItemStack {
    return this.apply {
        itemMeta = itemMeta?.apply {
            addItemFlags(*ItemFlag.values())
        }
    }
}

fun ItemStack.flags(vararg flags: ItemFlag): ItemStack {
    return this.apply {
        itemMeta = itemMeta?.apply {
            addItemFlags(*flags)
        }
    }
}

fun ItemStack.removeFlags(): ItemStack {
    return this.apply {
        itemMeta = itemMeta?.apply {
            removeItemFlags(*ItemFlag.values())
        }
    }
}

fun ItemStack.enchant(enchant: Enchantment, level: Int): ItemStack {
    return this.apply {
        addUnsafeEnchantment(enchant, level)
    }
}