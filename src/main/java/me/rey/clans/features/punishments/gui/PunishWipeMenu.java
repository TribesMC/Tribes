package me.rey.clans.features.punishments.gui;

import me.rey.clans.Tribes;
import me.rey.clans.features.punishments.Punishment;
import me.rey.clans.gui.GuiEditable;
import me.rey.clans.utils.UtilTime;
import me.rey.core.gui.GuiItem;
import me.rey.core.utils.ItemBuilder;
import me.rey.core.utils.UtilText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.UUID;

public class PunishWipeMenu extends GuiEditable {

    private final Player opener;
    private final Punishment punishment;

    public PunishWipeMenu(Player opener, Punishment punishment) {
        super("Wipe Punishment", 5, Tribes.getInstance().getPlugin());
        this.opener = opener;
        this.punishment = punishment;
    }

    @Override
    public void setup() {

        //todo do this perm check properly at some point
        if (!opener.isOp()) {
            setItem(new GuiItem(new ItemBuilder(Material.BARRIER)
                    .setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "ERROR")
                    .setLore(UtilText.wrap(ChatColor.GRAY + "You should not be able to see this!", 36).toArray(new String[0]))
                    .addLore("", ChatColor.RED + "" + ChatColor.BOLD + "Click" + ChatColor.GRAY + " to close!")
                    .build()) {
                @Override
                public void onUse(Player player, ClickType type, int slot) {
                    player.closeInventory();
                }
            }, 30);
            return;
        }

        setHistoryRecordItem(13, punishment);

        setItem(new GuiItem(new ItemBuilder(Material.EMERALD_BLOCK)
                .setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM")
                .setLore(UtilText.wrap(ChatColor.GRAY + "Punishment record will be removed and any effect it had on a player will be removed too. Once this is confirmed, it can NEVER be brought back.", 36).toArray(new String[0]))
                .addLore("", ChatColor.GREEN + "" + ChatColor.BOLD + "Shift-Right Click" + ChatColor.GRAY + " to confirm!")
                .build()) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                if (type != ClickType.SHIFT_RIGHT) return;
                if (!player.isOp()) return; // todo do this properly at some point
                Tribes.getInstance().getPunishmentManager().deletePunishment(punishment, player);
                player.closeInventory();
            }
        }, 30);

        setItem(new GuiItem(new ItemBuilder(Material.REDSTONE_BLOCK)
                .setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "DISREGARD")
                .setLore(UtilText.wrap(ChatColor.GRAY + "Punishment record will not be changed!", 36).toArray(new String[0]))
                .addLore("", ChatColor.RED + "" + ChatColor.BOLD + "Click" + ChatColor.GRAY + " to disregard!")
                .build()) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                player.closeInventory();
            }
        }, 32);
    }

    public void setHistoryRecordItem(int slot, Punishment punishment) {
        String duration;
        if (punishment.getHours() > 0) {
            if (punishment.getHours() > 24.0d) {
                duration = UtilTime.convert((long) punishment.getHours() * 3600000, 0, UtilTime.TimeUnit.DAYS) + " days";
            } else {
                duration = UtilTime.convert((long) punishment.getHours() * 3600000, 0, UtilTime.getBestUnit((long) punishment.getHours() * 3600000)) + " " + UtilTime.getBestUnit((long) punishment.getHours() * 3600000).name().toLowerCase();
            }
        } else {
            duration = "Permanent";
        }
        String addedBy;
        if (punishment.getStaff().toLowerCase().startsWith("uuid:")) {
            addedBy = Bukkit.getOfflinePlayer(UUID.fromString(punishment.getStaff().substring(5))).getName();
        } else {
            addedBy = punishment.getStaff();
        }
        String removedBy = "No one";
        if (punishment.getRemoveStaff() != null) {
            if (punishment.getRemoveStaff().toLowerCase().startsWith("uuid:")) {
                removedBy = Bukkit.getOfflinePlayer(UUID.fromString(punishment.getRemoveStaff().substring(5))).getName();
            } else {
                removedBy = punishment.getRemoveStaff();
            }
        }
        String reappliedBy = "No one";
        if (punishment.getReapplyStaff() != null) {
            if (punishment.getReapplyStaff().toLowerCase().startsWith("uuid:")) {
                reappliedBy = Bukkit.getOfflinePlayer(UUID.fromString(punishment.getReapplyStaff().substring(5))).getName();
            } else {
                reappliedBy = punishment.getReapplyStaff();
            }
        }

        setItem(new GuiItem(new ItemBuilder(punishment.getCategory().getItem())
                .setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + punishment.getCategory().getName())
                // todo do proper permissions check later
                .setLore(
                        ChatColor.WHITE + "Punishment Type: " + ChatColor.YELLOW + punishment.getCategory().getName(),
                        ChatColor.WHITE + "Severity: " + ChatColor.YELLOW + punishment.getSeverity())
                .addLore(UtilText.wrap(ChatColor.WHITE + "Date: " + ChatColor.YELLOW + UtilTime.getTimeDate(punishment.getTime()) + " (" + UtilTime.convert(System.currentTimeMillis() - punishment.getTime(), 0, UtilTime.getBestUnit(System.currentTimeMillis() - punishment.getTime())) + " " + UtilTime.getBestUnit(System.currentTimeMillis() - punishment.getTime()).name().toLowerCase() + " ago)", 36).toArray(new String[0]))
                .addLore(ChatColor.WHITE + "Length: " + ChatColor.YELLOW + duration,
                        ChatColor.WHITE + "Added By: " + ChatColor.YELLOW + addedBy,
                        "")
                .addLore(UtilText.wrap(ChatColor.WHITE + "Reason: " + ChatColor.YELLOW + punishment.getReason(), 36).toArray(new String[0]))
                .addLore(punishment.wasRemovedPreviously(),
                        "",
                        ChatColor.WHITE + "Removed By: " + ChatColor.YELLOW + removedBy)
                .addLore(punishment.wasRemovedPreviously(), UtilText.wrap(ChatColor.WHITE + "Removed For: " + ChatColor.YELLOW + punishment.getRemoveReason(), 36).toArray(new String[0]))
                .addLore(punishment.wasRemovedPreviously(), UtilText.wrap(ChatColor.WHITE + "Removed At: " + ChatColor.YELLOW + UtilTime.getTimeDate(punishment.getRemovedAt()) + " (" + UtilTime.convert(System.currentTimeMillis() - punishment.getRemovedAt(), 0, UtilTime.getBestUnit(System.currentTimeMillis() - punishment.getRemovedAt())) + " " + UtilTime.getBestUnit(System.currentTimeMillis() - punishment.getRemovedAt()).name().toLowerCase() + " ago)", 36).toArray(new String[0]))
                .addLore(punishment.wasRemovedPreviously() && punishment.wasReactivated(), "")
                .addLore(punishment.wasReactivated(),
                        ChatColor.WHITE + "Reapplied By: " + ChatColor.YELLOW + reappliedBy)
                .addLore(punishment.wasReactivated(), UtilText.wrap(ChatColor.WHITE + "Reapplied For: " + ChatColor.YELLOW + punishment.getReapplyReason(), 36).toArray(new String[0]))
                .addLore(punishment.wasReactivated(), UtilText.wrap(ChatColor.WHITE + "Reapplied At: " + ChatColor.YELLOW + UtilTime.getTimeDate(punishment.getReappliedAt()) + " (" + UtilTime.convert(System.currentTimeMillis() - punishment.getRemovedAt(), 0, UtilTime.getBestUnit(System.currentTimeMillis() - punishment.getReappliedAt())) + " " + UtilTime.getBestUnit(System.currentTimeMillis() - punishment.getReappliedAt()).name().toLowerCase() + " ago)", 36).toArray(new String[0]))
                .addGlow(punishment.isActive())
                .build()) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {

            }
        }, slot, true);
    }

    @Override
    public void init() {

    }

}
