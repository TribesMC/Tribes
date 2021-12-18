package me.rey.clans.features.punishments.gui;

import me.rey.clans.Tribes;
import me.rey.clans.features.punishments.Punishment;
import me.rey.clans.gui.GuiEditable;
import me.rey.clans.utils.UtilTime;
import me.rey.core.gui.GuiItem;
import me.rey.core.gui.Item;
import me.rey.core.utils.ItemBuilder;
import me.rey.core.utils.UtilText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class PunishHistoryMenu extends GuiEditable {

    private final Player opener;
    private final OfflinePlayer target;
    private final List<Punishment> punishments;
    private final PunishMenu.PunishSession session;

    private final String green = ChatColor.GREEN + "" + ChatColor.BOLD + "";
    private int page;

    public PunishHistoryMenu(Player opener, OfflinePlayer target, List<Punishment> punishments) {
        super((opener.getUniqueId().equals(target.getUniqueId()) ? "Your" : target.getName() + "'s") + " Punishment History", 6, Tribes.getInstance().getPlugin());
        this.opener = opener;
        this.target = target;
        this.punishments = punishments;
        this.session = null;
        this.page = 0;
    }

    public PunishHistoryMenu(Player opener, OfflinePlayer target, List<Punishment> punishments, PunishMenu.PunishSession session) {
        super((opener.getUniqueId().equals(target.getUniqueId()) ? "Your" : target.getName() + "'s") + " Punishment History", 6, Tribes.getInstance().getPlugin());
        this.opener = opener;
        this.target = target;
        this.punishments = punishments;
        this.session = session;
        this.page = 0;
    }

    @Override
    public void setup() {
        clear();

        setItem(new GuiItem(new ItemBuilder.PlayerSkull(target).getStackAsItemBuilder()
                .setDisplayName(green + target.getName())
                .setLore(ChatColor.GRAY + "Viewing " + ChatColor.YELLOW + Math.max(page * 28, 1) + ChatColor.GRAY + "-" + ChatColor.YELLOW + Math.min((page + 1) * 28, punishments.size()) + ChatColor.GRAY + " of " + ChatColor.YELLOW + punishments.size())
                .build()) {

            @Override
            public void onUse(Player player, ClickType type, int slot) {

            }
        }, 4, true);

        int i = 10;
        List<Punishment> concise = punishments.subList(page * 28, Math.min(punishments.size(), (page + 1) * 28));
        for (Punishment punishment : concise) {
            setHistoryRecordItem(i, punishment);
            if (i++ % 9 == 7) {
                i += 2;
            }
        }

        double exactMaxPage = (double) (punishments.size() == 28 ? 27 : 28) / 28;
        int maxPage;
        if (exactMaxPage > 0 && exactMaxPage < 1) {
            maxPage = 0;
        } else {
            maxPage = (int) Math.ceil(exactMaxPage);
        }

        if (page > 0) {
            setItem(new GuiItem(new ItemBuilder(Material.ARROW)
                    .setDisplayName(green + "Previous Page")
                    .build()) {
                @Override
                public void onUse(Player player, ClickType type, int slot) {
                    page--;
                    setup();
                }
            }, 45, true);
        }

        if (maxPage > 0 && page < maxPage) {
            setItem(new GuiItem(new ItemBuilder(Material.ARROW)
                    .setDisplayName(green + "Next Page")
                    .build()) {
                @Override
                public void onUse(Player player, ClickType type, int slot) {
                    page++;
                    setup();
                }
            }, 53, true);
        }

        if (session != null) {
            setItem(new GuiItem(new ItemBuilder(Material.ARROW)
                    .setDisplayName(green + "Return To Punish Menu")
                    .build()) {
                @Override
                public void onUse(Player player, ClickType type, int slot) {
                    PunishMenu gui = new PunishMenu(session.getPlayer(), session.getTarget(), session.getReason());
                    gui.setup();
                    gui.open(player);
                }
            }, 0, true);
        }
    }

    public void clear() {
        for (int i = 0; i < getSize(); i++) {
            setItem(new GuiItem(new ItemStack(Material.AIR)) {
                @Override
                public void onUse(Player player, ClickType type, int slot) {

                }
            }, i, true);
        }
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
                .setDisplayName(green + punishment.getCategory().getName())
                // todo do proper permissions check later
                .setLore(opener.isOp(), ChatColor.WHITE + "ID: " + ChatColor.YELLOW + punishment.getId())
                .addLore(
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
                .addLore(session != null && (punishment.isActive() || punishment.wasRemoved()), "")
                .addLore(session != null && punishment.isActive(), UtilText.wrap(ChatColor.YELLOW + "Shift-Right Click" + ChatColor.WHITE + " to remove!", 36).toArray(new String[0]))
                .addLore(session != null && punishment.wasRemoved(), UtilText.wrap(ChatColor.YELLOW + "Shift-Right Click" + ChatColor.WHITE + " to reapply!", 36).toArray(new String[0])) // todo perms here
                .addGlow(punishment.isActive())
                .build()) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                if (session == null) return;
                if (type != ClickType.SHIFT_RIGHT) return;
                if (punishment.isActive()) {
                    punishment.remove(player, session.getReason());
                    player.closeInventory();
                } else {
                    if (punishment.getHours() < 0 || (punishment.getTime() + (punishment.getHours() * 3600000) > System.currentTimeMillis())) {
                        punishment.reapply(player, session.getReason());
                        player.closeInventory();
                    }
                }
            }
        }, slot, true);
    }

    @Override
    public void init() {

    }

}