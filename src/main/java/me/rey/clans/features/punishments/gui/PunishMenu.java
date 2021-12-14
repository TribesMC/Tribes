package me.rey.clans.features.punishments.gui;

import me.rey.clans.Tribes;
import me.rey.clans.features.punishments.Punishment;
import me.rey.clans.features.punishments.PunishmentCategory;
import me.rey.clans.features.punishments.PunishmentType;
import me.rey.clans.gui.GuiEditable;
import me.rey.clans.utils.UtilTime;
import me.rey.core.gui.GuiItem;
import me.rey.core.utils.ItemBuilder;
import me.rey.core.utils.UtilText;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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

        setPunishmentButton(0, new ItemBuilder(Material.PAPER)
                .setDisplayName(green + "Warning")
                .build());
        setPunishmentButton(9, new ItemBuilder(Material.FEATHER)
                .setDisplayName(green + "Kick")
                .build());
        setPunishmentButton(18, new ItemBuilder(Material.BOOK)
                .setDisplayName(green + "Report Ban")
                .build());
        setPunishmentButton(27, new ItemBuilder(Material.BOOK_AND_QUILL)
                .setDisplayName(green + "Permanent Mute")
                .build());
        setPunishmentButton(36, new ItemBuilder(Material.REDSTONE_BLOCK)
                .setDisplayName(green + "Permanent Ban")
                .build());

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

        setPunishmentSeverityButton(33, PunishmentCategory.CLIENT, 24.0d * 7.0d, 2, (player, clickType, slot) -> {
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

        setPunishmentSeverityButton(42, PunishmentCategory.CLIENT, 24.0d * 30.0d, 3, (player, clickType, slot) -> {
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
            if (punishment.getCategory() != category || punishment.getSeverity() != severity) return;
            pastOffenses++;
        }
        setItem(new GuiItem(new ItemBuilder(stack)
                .setDisplayName(color + "" + ChatColor.BOLD + "Severity " + severity)
                .setLore(
                        ChatColor.WHITE + "Past Offenses: " + ChatColor.YELLOW + pastOffenses,
                        ChatColor.WHITE + "Duration: " + ChatColor.YELLOW + UtilTime.convert((long) time * 3600000, 0, UtilTime.getBestUnit((long) time * 3600000)) + " " + UtilTime.getBestUnit((long) time * 3600000).name().toLowerCase())
                .build()) {

            @Override
            public void onUse(Player player, ClickType type, int slot) {
                if (runner != null) runner.run(player, type, slot);
            }
        }, slot);
    }

    @Override
    public void init() {

    }

    public interface PunishRunner {
        void run(Player player, ClickType type, int slot);
    }

}