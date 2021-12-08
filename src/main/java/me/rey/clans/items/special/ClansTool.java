package me.rey.clans.items.special;

import me.rey.clans.items.special.epics.EpicItem;
import me.rey.clans.items.special.legends.LegendaryItem;
import me.rey.clans.items.special.miscs.MiscItem;
import me.rey.core.utils.ItemBuilder;
import me.rey.core.utils.UtilText;
import me.rey.core.utils.UtilTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public enum ClansTool {
    ALLIGATORS_TOOTH(new ItemBuilder(Material.RECORD_3)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.GOLD + "Alligator's Tooth")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.ALLIGATORS_TOOTH.archetype, UtilTool.NBTTagType.STRING);
    }),
    GIANTS_BROADSWORD(new ItemBuilder(Material.RECORD_4)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.GOLD + "Giant's Broadsword")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.GIANTS_BROADSWORD.archetype, UtilTool.NBTTagType.STRING);
    }),
    HYPERAXE(new ItemBuilder(Material.RECORD_5)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.GOLD + "Hyperaxe")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.HYPERAXE.archetype, UtilTool.NBTTagType.STRING);
    }),
    MAGNETIC_MAUL(new ItemBuilder(Material.RECORD_6)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.GOLD + "Magnetic Maul")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.MAGNETIC_MAUL.archetype, UtilTool.NBTTagType.STRING);
    }),
    MERIDIAN_SCEPTER(new ItemBuilder(Material.RECORD_7)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.GOLD + "Meridian Scepter")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.MERIDIAN_SCEPTER.archetype, UtilTool.NBTTagType.STRING);
    }),
    WINDBLADE(new ItemBuilder(Material.RECORD_8)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.GOLD + "Windblade")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.WINDBLADE.archetype, UtilTool.NBTTagType.STRING);
    }),
    SCYTHE_OF_THE_FALLEN_LORD(new ItemBuilder(Material.RECORD_9)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.GOLD + "Scythe of the Fallen Lord")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.SCYTHE_OF_THE_FALLEN_LORD.archetype, UtilTool.NBTTagType.STRING);
    }),
    KNIGHTS_GREATLANCE(new ItemBuilder(Material.RECORD_10)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.GOLD + "Knight's Greatlance")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.KNIGHTS_GREATLANCE.archetype, UtilTool.NBTTagType.STRING);
    }),

    ALLIGATORS_TOOTH_DISABLED(new ItemBuilder(Material.RECORD_3)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.GOLD + "Alligator's Tooth")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.ALLIGATORS_TOOTH.archetype, UtilTool.NBTTagType.STRING);
    }),
    GIANTS_BROADSWORD_DISABLED(new ItemBuilder(Material.RECORD_4)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.GOLD + "Giant's Broadsword")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.GIANTS_BROADSWORD.archetype, UtilTool.NBTTagType.STRING);
    }),
    HYPERAXE_DISABLED(new ItemBuilder(Material.RECORD_5)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.GOLD + "Hyperaxe")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.HYPERAXE.archetype, UtilTool.NBTTagType.STRING);
    }),
    MAGNETIC_MAUL_DISABLED(new ItemBuilder(Material.RECORD_6)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.GOLD + "Magnetic Maul")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.MAGNETIC_MAUL.archetype, UtilTool.NBTTagType.STRING);
    }),
    MERIDIAN_SCEPTER_DISABLED(new ItemBuilder(Material.RECORD_7)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.GOLD + "Meridian Scepter")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.MERIDIAN_SCEPTER.archetype, UtilTool.NBTTagType.STRING);
    }),
    WINDBLADE_DISABLED(new ItemBuilder(Material.RECORD_8)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.GOLD + "Windblade")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.WINDBLADE.archetype, UtilTool.NBTTagType.STRING);
    }),
    SCYTHE_OF_THE_FALLEN_LORD_DISABLED(new ItemBuilder(Material.RECORD_9)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.GOLD + "Scythe of the Fallen Lord")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.SCYTHE_OF_THE_FALLEN_LORD.archetype, UtilTool.NBTTagType.STRING);
    }),
    KNIGHTS_GREATLANCE_DISABLED(new ItemBuilder(Material.RECORD_10)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.GOLD + "Knight's Greatlance")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", LegendaryItem.LegendType.KNIGHTS_GREATLANCE.archetype, UtilTool.NBTTagType.STRING);
    }),

    RUNED_PICKAXE(new ItemBuilder(Material.RECORD_11)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.DARK_PURPLE + "Runed Pickaxe")
            .addLore("")
            .addLore(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "EPIC")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", EpicItem.EpicType.RUNED_PICKAXE.archetype, UtilTool.NBTTagType.STRING);
    }),
    MYSTIC_HOE(new ItemBuilder(Material.RECORD_12)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.DARK_PURPLE + "Mystic Hoe")
            .addLore("")
            .addLore(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "EPIC")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", EpicItem.EpicType.MYSTIC_HOE.archetype, UtilTool.NBTTagType.STRING);
    }),
    PRIMORDIAL_AXE(new ItemBuilder(Material.GOLD_RECORD)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setDisplayName(ChatColor.DARK_PURPLE + "Primordial Axe")
            .addLore("")
            .addLore(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "EPIC")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", EpicItem.EpicType.PRIMORDIAL_AXE.archetype, UtilTool.NBTTagType.STRING);
    }),

    RUNED_PICKAXE_DISABLED(new ItemBuilder(Material.RECORD_11)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.RED + "Runed Pickaxe")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "EPIC")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", EpicItem.EpicType.RUNED_PICKAXE.archetype, UtilTool.NBTTagType.STRING);
    }),
    MYSTIC_HOE_DISABLED(new ItemBuilder(Material.RECORD_12)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.RED + "Mystic Hoe")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "EPIC")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", EpicItem.EpicType.MYSTIC_HOE.archetype, UtilTool.NBTTagType.STRING);
    }),
    PRIMORDIAL_AXE_DISABLED(new ItemBuilder(Material.GOLD_RECORD)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .addGlow()
            .setDisplayName(ChatColor.RED + "Primordial Axe")
            .addLore("", ChatColor.RED + "DISABLED")
            .addLore("")
            .addLore(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "EPIC")
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 1, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", EpicItem.EpicType.PRIMORDIAL_AXE.archetype, UtilTool.NBTTagType.STRING);
    }),

    ///////////////////
    // GENERAL TOOLS //
    ///////////////////


    DIAMOND_PICKAXE(new ItemBuilder(Material.DIAMOND_PICKAXE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    DIAMOND_SPADE(new ItemBuilder(Material.DIAMOND_SPADE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    DIAMOND_HOE(new ItemBuilder(Material.DIAMOND_HOE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    GOLD_PICKAXE(new ItemBuilder(Material.GOLD_PICKAXE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    GOLD_SPADE(new ItemBuilder(Material.GOLD_SPADE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    GOLD_HOE(new ItemBuilder(Material.GOLD_HOE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    IRON_PICKAXE(new ItemBuilder(Material.IRON_PICKAXE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    IRON_SPADE(new ItemBuilder(Material.IRON_SPADE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    IRON_HOE(new ItemBuilder(Material.IRON_HOE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    STONE_SWORD(new ItemBuilder(Material.STONE_SWORD)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),
    STONE_PICKAXE(new ItemBuilder(Material.STONE_PICKAXE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    STONE_SPADE(new ItemBuilder(Material.STONE_SPADE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    STONE_AXE(new ItemBuilder(Material.STONE_AXE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    STONE_HOE(new ItemBuilder(Material.STONE_HOE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    WOOD_SWORD(new ItemBuilder(Material.WOOD_SWORD)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),
    WOOD_PICKAXE(new ItemBuilder(Material.WOOD_PICKAXE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    WOOD_SPADE(new ItemBuilder(Material.WOOD_SPADE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    WOOD_AXE(new ItemBuilder(Material.WOOD_AXE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    WOOD_HOE(new ItemBuilder(Material.WOOD_HOE)
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .build(), item -> {

        item = UtilTool.setNBTTag(item, "ClansDurability", 900, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "ClansMaxDurability", 900, UtilTool.NBTTagType.INT);
    }),

    DEBUG_STICK(new ItemBuilder(Material.STICK)
            .setDisplayName(ChatColor.RED + "Debug Stick")
            .addGlow()
            .addLore("")
            .addLore(UtilText.wrap(
            ChatColor.WHITE + "I mean if you're not either a staff member or a QA person, I literally have no idea how you've got this, but eh...",
                    36
    ).toArray(new String[0]))
            .addLore("")
            .addLore(ChatColor.YELLOW + "" + ChatColor.MAGIC + "" + ChatColor.BOLD + "AA" + ChatColor.YELLOW + " " + ChatColor.BOLD + "SPECIAL" + ChatColor.YELLOW + " " + ChatColor.MAGIC + "" + ChatColor.BOLD + "AA")
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .build(), item -> {
        item = UtilTool.setNBTTag(item, "DebugStickID", UtilText.randomString(32), UtilTool.NBTTagType.STRING);
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", MiscItem.MiscType.DEBUG_STICK.archetype, UtilTool.NBTTagType.STRING);
    }),
    DEBUG_WEAPON(UtilTool.setDamage(new ItemBuilder(Material.BLAZE_ROD)
            .setDisplayName(ChatColor.RED + "Debug Weapon")
            .addGlow()
            .addLore("")
            .addLore(UtilText.wrap(
            ChatColor.WHITE + "We'll give you a reward if you turn this in. :eyes:",
                    36
    ).toArray(new String[0]))
            .addLore("")
            .addLore(ChatColor.YELLOW + "" + ChatColor.MAGIC + "" + ChatColor.BOLD + "AA" + ChatColor.YELLOW + " " + ChatColor.BOLD + "SPECIAL" + ChatColor.YELLOW + " " + ChatColor.MAGIC + "" + ChatColor.BOLD + "AA")
            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
            .build(), 7), item -> {
        item = UtilTool.setNBTTag(item, "ClansDisabled", 0, UtilTool.NBTTagType.INT);
        return UtilTool.setNBTTag(item, "LegendArchetype", MiscItem.MiscType.DEBUG_WEAPON.archetype, UtilTool.NBTTagType.STRING);
    }),;




    public final ItemStack stack;

    ClansTool(ItemStack stack) {
        this.stack = stack;
    }

    ClansTool(ItemStack stack, Function<ItemStack, ItemStack> actions) {
        this.stack = actions.apply(stack);
    }
}
