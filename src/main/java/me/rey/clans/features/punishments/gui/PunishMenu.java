package me.rey.clans.features.punishments.gui;

import me.rey.clans.Tribes;
import me.rey.clans.features.punishments.Punishment;
import me.rey.clans.features.punishments.PunishmentCategory;
import me.rey.clans.features.punishments.PunishmentType;
import me.rey.clans.gui.GuiEditable;
import me.rey.clans.utils.UtilTime;
import me.rey.core.gui.GuiItem;
import me.rey.core.players.User;
import me.rey.core.utils.ItemBuilder;
import me.rey.core.utils.UtilText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishMenu extends GuiEditable {

    private final Player opener;
    private final OfflinePlayer target;
    private final String reason;

    private final String green = ChatColor.GREEN + "" + ChatColor.BOLD + "";
    private final List<Punishment> punishments;

    public PunishMenu(Player opener, OfflinePlayer target, String reason) {
        super("Punish " + target.getName(), 6, Tribes.getInstance().getPlugin());
        this.opener = opener;
        this.target = target;
        this.reason = reason;
        this.punishments = Tribes.getInstance().getPunishmentManager().getPunishments(target.getUniqueId());
    }

    @Override
    public void setup() {

        /*
         * Labels
         */

        setItem(new GuiItem(new ItemBuilder.PlayerSkull(target).getStackAsItemBuilder()
                .setDisplayName(green + target.getName())
                .setLore(UtilText.wrap(ChatColor.WHITE + "Reason: " + ChatColor.GRAY + reason, 36).toArray(new String[0]))
                .build()) {

            @Override
            public void onUse(Player player, ClickType type, int slot) {

            }
        }, 4);

        setPunishmentCategoryItem(11, PunishmentCategory.CHAT);
        setPunishmentCategoryItem(13, PunishmentCategory.GAMEPLAY);
        setPunishmentCategoryItem(15, PunishmentCategory.CLIENT);

        /*
         * Left Side Buttons
         */

        List<Punishment> warnings = new ArrayList<>();
        List<Punishment> kicks = new ArrayList<>();
        List<Punishment> reportBans = new ArrayList<>();
        List<Punishment> permanentMutes = new ArrayList<>();
        List<Punishment> permanentBans = new ArrayList<>();
        for (Punishment punishment : punishments) {
            switch (punishment.getCategory()) {
                case KICK:
                    kicks.add(punishment);
                    break;
                case WARN:
                    warnings.add(punishment);
                    break;
                case PERM_BAN:
                    permanentBans.add(punishment);
                    break;
                case PERM_MUTE:
                    permanentMutes.add(punishment);
                    break;
                case REPORT:
                    reportBans.add(punishment);
                    break;
            }
        }
        setPunishmentButton(9, new ItemBuilder(Material.PAPER)
                .setDisplayName(green + "Warning")
                .addLore(ChatColor.WHITE + "Past Warnings: " + ChatColor.YELLOW + warnings.size())
                .addLore(!warnings.isEmpty(), "")
                .addLore(!warnings.isEmpty(), UtilText.wrap(ChatColor.WHITE + "Last Warning: " + ChatColor.YELLOW + (!warnings.isEmpty() ? warnings.get(0).getReason() : "None"), 36).toArray(new String[0]))
                .addLore(!warnings.isEmpty(), UtilText.wrap(ChatColor.WHITE + "At: " + ChatColor.YELLOW + UtilTime.getTimeDate(!warnings.isEmpty() ? warnings.get(0).getTime() : 0) + " (" + UtilTime.convert(!warnings.isEmpty() ? System.currentTimeMillis() - warnings.get(0).getTime() : 0, 0, UtilTime.getBestUnit(!warnings.isEmpty() ? System.currentTimeMillis() - warnings.get(0).getTime() : 0)) + " " + UtilTime.getBestUnit(!warnings.isEmpty() ? System.currentTimeMillis() - warnings.get(0).getTime() : 0).name().toLowerCase() + " ago)", 36).toArray(new String[0]))
                .build(), (player, type, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.WARN,
                    PunishmentCategory.WARN,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    -1,
                    1,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });
        setPunishmentButton(18, new ItemBuilder(Material.FEATHER)
                .setDisplayName(green + "Kick")
                .addLore(ChatColor.WHITE + "Past Kicks: " + ChatColor.YELLOW + kicks.size())
                .addLore(!kicks.isEmpty(), "")
                .addLore(!kicks.isEmpty(), UtilText.wrap(ChatColor.WHITE + "Last Kick: " + ChatColor.YELLOW + (!kicks.isEmpty() ? kicks.get(0).getReason() : "None"), 36).toArray(new String[0]))
                .addLore(!kicks.isEmpty(), UtilText.wrap(ChatColor.WHITE + "At: " + ChatColor.YELLOW + UtilTime.getTimeDate(!kicks.isEmpty() ? kicks.get(0).getTime() : 0) + " (" + UtilTime.convert(!kicks.isEmpty() ? System.currentTimeMillis() - kicks.get(0).getTime() : 0, 0, UtilTime.getBestUnit(!kicks.isEmpty() ? System.currentTimeMillis() - kicks.get(0).getTime() : 0)) + " " + UtilTime.getBestUnit(!kicks.isEmpty() ? System.currentTimeMillis() - kicks.get(0).getTime() : 0).name().toLowerCase() + " ago)", 36).toArray(new String[0]))
                .build(), (player, type, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.KICK,
                    PunishmentCategory.KICK,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    -1,
                    1,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });
        setPunishmentButton(27, new ItemBuilder(Material.BOOK)
                .setDisplayName(green + "Report Ban")
                .addLore(ChatColor.WHITE + "Past Report Bans: " + ChatColor.YELLOW + reportBans.size())
                .addLore(!reportBans.isEmpty(), "")
                .addLore(!reportBans.isEmpty(), UtilText.wrap(ChatColor.WHITE + "Last Report Ban: " + ChatColor.YELLOW + (!reportBans.isEmpty() ? reportBans.get(0).getReason() : "None"), 36).toArray(new String[0]))
                .addLore(!reportBans.isEmpty(), UtilText.wrap(ChatColor.WHITE + "At: " + ChatColor.YELLOW + UtilTime.getTimeDate(!reportBans.isEmpty() ? reportBans.get(0).getTime() : 0) + " (" + UtilTime.convert(!reportBans.isEmpty() ? System.currentTimeMillis() - reportBans.get(0).getTime() : 0, 0, UtilTime.getBestUnit(!reportBans.isEmpty() ? System.currentTimeMillis() - reportBans.get(0).getTime() : 0)) + " " + UtilTime.getBestUnit(!reportBans.isEmpty() ? System.currentTimeMillis() - reportBans.get(0).getTime() : 0).name().toLowerCase() + " ago)", 36).toArray(new String[0]))
                .build(), (player, type, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.REPORTBAN,
                    PunishmentCategory.REPORT,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    -1,
                    1,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });
        setPunishmentButton(36, new ItemBuilder(Material.BOOK_AND_QUILL)
                .setDisplayName(green + "Permanent Mute")
                .addLore(ChatColor.WHITE + "Past Permanent Mutes: " + ChatColor.YELLOW + permanentMutes.size())
                .addLore(!permanentMutes.isEmpty(), "")
                .addLore(!permanentMutes.isEmpty(), UtilText.wrap(ChatColor.WHITE + "Last Permanent Mute: " + ChatColor.YELLOW + (!permanentMutes.isEmpty() ? permanentMutes.get(0).getReason() : "None"), 36).toArray(new String[0]))
                .addLore(!permanentMutes.isEmpty(), UtilText.wrap(ChatColor.WHITE + "At: " + ChatColor.YELLOW + UtilTime.getTimeDate(!permanentMutes.isEmpty() ? permanentMutes.get(0).getTime() : 0) + " (" + UtilTime.convert(!permanentMutes.isEmpty() ? System.currentTimeMillis() - permanentMutes.get(0).getTime() : 0, 0, UtilTime.getBestUnit(!permanentMutes.isEmpty() ? System.currentTimeMillis() - permanentMutes.get(0).getTime() : 0)) + " " + UtilTime.getBestUnit(!permanentMutes.isEmpty() ? System.currentTimeMillis() - permanentMutes.get(0).getTime() : 0).name().toLowerCase() + " ago)", 36).toArray(new String[0]))
                .build(), (player, type, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.MUTE,
                    PunishmentCategory.PERM_MUTE,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    -1,
                    1,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });
        setPunishmentButton(45, new ItemBuilder(Material.REDSTONE_BLOCK)
                .setDisplayName(green + "Permanent Ban")
                .addLore(ChatColor.WHITE + "Past Permanent Bans: " + ChatColor.YELLOW + permanentBans.size())
                .addLore(!permanentBans.isEmpty(), "")
                .addLore(!permanentBans.isEmpty(), UtilText.wrap(ChatColor.WHITE + "Last Permanent Ban: " + ChatColor.YELLOW + (!permanentBans.isEmpty() ? permanentBans.get(0).getReason() : "None"), 36).toArray(new String[0]))
                .addLore(!permanentBans.isEmpty(), UtilText.wrap(ChatColor.WHITE + "At: " + ChatColor.YELLOW + UtilTime.getTimeDate(!permanentBans.isEmpty() ? permanentBans.get(0).getTime() : 0) + " (" + UtilTime.convert(!permanentBans.isEmpty() ? System.currentTimeMillis() - permanentBans.get(0).getTime() : 0, 0, UtilTime.getBestUnit(!permanentBans.isEmpty() ? System.currentTimeMillis() - permanentBans.get(0).getTime() : 0)) + " " + UtilTime.getBestUnit(!permanentBans.isEmpty() ? System.currentTimeMillis() - permanentBans.get(0).getTime() : 0).name().toLowerCase() + " ago)", 36).toArray(new String[0]))
                .build(), (player, type, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.BAN,
                    PunishmentCategory.PERM_BAN,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    -1,
                    1,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        /*
         * Chat Severity buttons
         */

        setPunishmentSeverityButton(20, PunishmentCategory.CHAT, 4.0d, 1, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.MUTE,
                    PunishmentCategory.CHAT,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    4.0d,
                    1,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        setPunishmentSeverityButton(29, PunishmentCategory.CHAT, 24.0d * 7.0d, 2, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.MUTE,
                    PunishmentCategory.CHAT,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    24.0d * 7.0d,
                    2,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        setPunishmentSeverityButton(38, PunishmentCategory.CHAT, 24.0d * 30.0d, 3, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.MUTE,
                    PunishmentCategory.CHAT,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    24.0d * 30.0d,
                    3,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        /*
         * Gameplay Severity buttons
         */

        setPunishmentSeverityButton(22, PunishmentCategory.GAMEPLAY, 4.0d, 1, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.BAN,
                    PunishmentCategory.GAMEPLAY,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    4.0d,
                    1,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        setPunishmentSeverityButton(31, PunishmentCategory.GAMEPLAY, 24.0d * 7.0d, 2, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.BAN,
                    PunishmentCategory.GAMEPLAY,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    24.0d * 7.0d,
                    2,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        setPunishmentSeverityButton(40, PunishmentCategory.GAMEPLAY, 24.0d * 30.0d, 3, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.BAN,
                    PunishmentCategory.GAMEPLAY,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    24.0d * 30.0d,
                    3,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        /*
         * Cheating Severity buttons
         */

        setPunishmentSeverityButton(24, PunishmentCategory.CLIENT, 4.0d, 1, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.BAN,
                    PunishmentCategory.CLIENT,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    4.0d,
                    1,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        setPunishmentSeverityButton(33, PunishmentCategory.CLIENT, 24.0d * 14.0d, 2, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.BAN,
                    PunishmentCategory.CLIENT,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    24.0d * 14.0d,
                    2,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        setPunishmentSeverityButton(42, PunishmentCategory.CLIENT, 24.0d * 40.0d, 3, (player, clickType, slot) -> {
            Punishment punishment = new Punishment(
                    target.getUniqueId(),
                    PunishmentType.BAN,
                    PunishmentCategory.CLIENT,
                    reason,
                    "UUID:" + player.getUniqueId().toString(),
                    24.0d * 40.0d,
                    3,
                    System.currentTimeMillis()
            );
            Tribes.getInstance().getPunishmentManager().addPunishment(punishment, true, true);
            player.closeInventory();
        });

        /*
         * Minor history buttons
         */


        List<Punishment> shortHistory = punishments.size() == 6 ? punishments : punishments.subList(0, Math.min(5, punishments.size()));
        int i = 8;
        for (Punishment historyRecord : shortHistory) {
            setHistoryRecordItem(i, historyRecord);
            i += 9;
        }
        if (punishments.size() > 6) {
            setItem(new GuiItem(new ItemBuilder(Material.SIGN)
                    .setDisplayName(green + "More History")
                    .setLore(UtilText.wrap(ChatColor.YELLOW + target.getName() + ChatColor.GRAY + " has " + ChatColor.YELLOW + punishments.size() + ChatColor.GRAY + " punishments on their record!", 36).toArray(new String[0]))
                    .addLore("", green + "Click" + ChatColor.GRAY + " to view them!")
                    .setAmount(punishments.size())
                    .build()) {
                @Override
                public void onUse(Player player, ClickType type, int slot) {
                    PunishHistoryMenu gui = new PunishHistoryMenu(player.getPlayer(), target, punishments, new PunishSession(player, target, reason));
                    gui.setup();
                    gui.open(player.getPlayer());
                }
            }, 53);
        }

    } //todo rewrite UI stuff

    public void setPunishmentCategoryItem(int slot, PunishmentCategory category) {
        setItem(new GuiItem(new ItemBuilder(category.getItem())
                .setDisplayName(green + category.getName())
                .setLore(category.compileDescriptionAsItemLore())
                .build()) {

            @Override
            public void onUse(Player player, ClickType type, int slot) {

            }
        }, slot);
    }

    public void setPunishmentButton(int slot, ItemStack stack) {
        setPunishmentButton(slot, stack, null);
    }

    public void setPunishmentButton(int slot, ItemStack stack, PunishRunner runner) {
        setItem(new GuiItem(stack) {

            @Override
            public void onUse(Player player, ClickType type, int slot) {
                if (runner != null) runner.run(player, type, slot);
            }
        }, slot);
    }

    public void setPunishmentSeverityButton(int slot, PunishmentCategory category, double time, int severity, PunishRunner runner) {
        ChatColor color;
        ItemStack stack;
        switch (severity) {
            case 1:
                color = ChatColor.GREEN;
                stack = new ItemStack(Material.INK_SACK, 1, (short) 2);
                break;
            case 2:
                color = ChatColor.YELLOW;
                stack = new ItemStack(Material.INK_SACK, 1, (short) 11);
                break;
            case 3:
                color = ChatColor.RED;
                stack = new ItemStack(Material.INK_SACK, 1, (short) 1);
                break;
            default:
                color = ChatColor.BLUE;
                stack = new ItemStack(Material.INK_SACK, 1, (short) 4);
        }

        int pastOffenses = 0;
        for (Punishment punishment : punishments) {
            if (punishment.getCategory() != category || punishment.getSeverity() != severity) continue;
            pastOffenses++;
        }
        String duration;
        if (time > 24.0d) {
            duration = UtilTime.convert((long) time * 3600000, 0, UtilTime.TimeUnit.DAYS) + " days";
        } else {
            duration = UtilTime.convert((long) time * 3600000, 0, UtilTime.getBestUnit((long) time * 3600000)) + " " + UtilTime.getBestUnit((long) time * 3600000).name().toLowerCase();
        }
        setItem(new GuiItem(new ItemBuilder(stack)
                .setDisplayName(color + "" + ChatColor.BOLD + "Severity " + severity)
                .setLore(
                        ChatColor.WHITE + "Past Offenses: " + ChatColor.YELLOW + pastOffenses,
                        ChatColor.WHITE + "Duration: " + ChatColor.YELLOW + duration)
                .build()) {

            @Override
            public void onUse(Player player, ClickType type, int slot) {
                if (runner != null) runner.run(player, type, slot);
            }
        }, slot);
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
                .addLore(punishment.isActive() || punishment.wasRemoved(), "")
                .addLore(punishment.isActive(), UtilText.wrap(ChatColor.YELLOW + "Shift-Right Click" + ChatColor.WHITE + " to remove!", 36).toArray(new String[0]))
                .addLore(punishment.wasRemoved(), UtilText.wrap(ChatColor.YELLOW + "Shift-Right Click" + ChatColor.WHITE + " to reapply!", 36).toArray(new String[0])) // todo perms here
                .addGlow(punishment.isActive())
                .build()) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                if (type != ClickType.SHIFT_RIGHT) return;
                if (punishment.isActive()) {
                    punishment.remove(player, reason);
                    player.closeInventory();
                } else {
                    if (punishment.getHours() < 0 || (punishment.getTime() + (punishment.getHours() * 3600000) > System.currentTimeMillis())) {
                        punishment.reapply(player, reason);
                        player.closeInventory();
                    }
                }
            }
        }, slot, true);
    }

    @Override
    public void init() {

    }

    public interface PunishRunner {
        void run(Player player, ClickType type, int slot);
    }

    public static class PunishSession {
        private final Player player;
        private final OfflinePlayer target;
        private final String reason;

        public PunishSession(Player player, OfflinePlayer target, String reason) {
            this.player = player;
            this.target = target;
            this.reason = reason;
        }

        public Player getPlayer() {
            return player;
        }

        public OfflinePlayer getTarget() {
            return target;
        }

        public String getReason() {
            return reason;
        }
    }
}