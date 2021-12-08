package me.rey.clans.commands.staff;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.events.clans.ClanDisbandEvent;
import me.rey.clans.gui.GuiEditable;
import me.rey.clans.siege.Siege;
import me.rey.clans.utils.ErrorCheck;
import me.rey.core.gui.GuiItem;
import me.rey.core.gui.Item;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Disband extends SubCommand {

    public Disband() {
        super("disband", "Disband a clan", "/c x disband <Clan>", ClansRank.NONE, CommandType.STAFF, true);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            this.sendUsage();
            return;
        }

        final ClansPlayer cp = new ClansPlayer((Player) sender);

        final Clan toDelete = Tribes.getInstance().getClan(args[0]);
        if (toDelete == null) {
            ErrorCheck.clanNotExist(sender);
            return;
        }

        final ConfirmGui gui = new ConfirmGui(toDelete, cp.getPlayer());
        gui.setup();
        gui.open(cp.getPlayer());
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

    private static class ConfirmGui extends GuiEditable {

        private final Clan clan;
        private final Player opener;

        public ConfirmGui(final Clan clan, final Player opener) {
            super("Disband " + clan.getName() + "?", 3, Tribes.getInstance().getPlugin());
            this.clan = clan;
            this.opener = opener;
        }

        @Override
        public void setup() {
            this.setItem(new GuiItem(new Item(Material.EMERALD_BLOCK).setName(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM").setLore(Arrays.asList(
                    ChatColor.YELLOW + this.clan.getName() + ChatColor.GRAY + " will be",
                    ChatColor.GRAY + "disbanded immediately!",
                    "",
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Shift-Right Click" + ChatColor.GRAY + " to confirm!"
            ))) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                    if (ConfirmGui.this.opener.equals(player)) {
                        if (type.isRightClick() && type.isShiftClick()) {
                            ConfirmGui.this.clan.announceToClan("Your clan has been disbanded by a moderator!");

                            if (ConfirmGui.this.clan.isBeingSieged()) {
                                final List<Siege> raidingSelf = new ArrayList<>(ConfirmGui.this.clan.getClansSiegingSelf());

                                for (final Siege siege : raidingSelf) {
                                    siege.end();
                                }
                            }

                            if (ConfirmGui.this.clan.isSiegingOther()) {
                                final List<Siege> siegingOther = new ArrayList<>(ConfirmGui.this.clan.getClansSiegedBySelf());

                                for (final Siege siege : siegingOther) {
                                    siege.end();
                                }
                            }

                            new ClansPlayer(player).sendMessageWithPrefix("Success", String.format("You have successfully disbanded the &s%s &rclan.", ConfirmGui.this.clan.getName()));
                            Tribes.getInstance().getSQLManager().deleteClan(ConfirmGui.this.clan.getUniqueId());

                            final ClanDisbandEvent event = new ClanDisbandEvent(ConfirmGui.this.clan, player, ClanDisbandEvent.DisbandReason.STAFF);
                            Bukkit.getServer().getPluginManager().callEvent(event);

                            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1F, 0.75F);
                            player.closeInventory();
                        }
                    } else {
                        player.closeInventory();
                    }
                }
            }, 11);

            this.setItem(new GuiItem(new Item(Material.REDSTONE_BLOCK).setName(ChatColor.RED + "" + ChatColor.BOLD + "CANCEL").setLore(Arrays.asList(
                    ChatColor.YELLOW + this.clan.getName() + ChatColor.GRAY + " will not",
                    ChatColor.GRAY + "be disbanded!",
                    "",
                    ChatColor.RED + "" + ChatColor.BOLD + "Click" + ChatColor.GRAY + " to confirm!"
            ))) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                    player.closeInventory();
                }
            }, 15);
        }

        @Override
        public void init() {

        }
    }
}
