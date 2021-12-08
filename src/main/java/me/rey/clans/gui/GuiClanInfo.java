package me.rey.clans.gui;

import me.rey.Main;
import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClanRelations;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.utils.References;
import me.rey.core.gui.GuiItem;
import me.rey.core.gui.Item;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GuiClanInfo extends GuiEditable {

    private final Clan clan;
    private final ClansPlayer player;

    public GuiClanInfo(final Clan clan, final ClansPlayer player) {
        super("", 6, JavaPlugin.getPlugin(Main.class));

        this.clan = clan;
        this.player = player;
    }

    @Override
    public void setup() {
        final String TITLE_COLOR = "&a&l";

        final long warpointsOnClan = !this.player.hasClan() ? 0 : this.player.getClan().getWarpointsOnClan(this.clan.getUniqueId());
        final int maxChunks = this.clan.getPossibleTerritory() <= References.MAX_TERRITORY ? this.clan.getPossibleTerritory() : References.MAX_TERRITORY;

        final List<String> lore = Arrays.asList("",
                "&eFounder &f" + this.clan.getFounder(),
                "&eMembers &f" + this.clan.getOnlinePlayers(false).size() + "/" + this.clan.getPlayers(false).size(),
                "&eTerritory &f" + this.clan.getTerritory().size() + "/" + maxChunks,
                "&eYour War Points &f" + warpointsOnClan,
                "", "&eEnergy &f" + this.clan.getEnergyString());
        final Item info = new Item(Material.IRON_BARDING).setName(TITLE_COLOR + this.clan.getName()).setLore(lore);

        for (final ClanRelations relation : ClanRelations.values()) {
            if (!relation.shouldSave()) {
                continue;
            }
            final String rName = relation.getName().endsWith("y") ? relation.getName().substring(0, relation.getName().length() - 1) + "ies" :
                    relation.getName() + "s";
            String format = "&e" + rName + " &f";

            final ArrayList<String> names = new ArrayList<String>();
            for (final UUID related : this.clan.getRelations().keySet()) {
                if (this.clan.getClanRelation(related).equals(relation)) {
                    names.add(Tribes.getInstance().getClan(related).getName());
                }
            }

            final StringBuilder str = new StringBuilder();
            for (final String name : names) {
                str.append(", ").append(name);
            }
            format += str.toString().equals(", ") || str.toString().equals("") ? "None" : str.toString().trim().replaceFirst(", ", "");
            info.addLore(format);
        }
        this.setItem(new GuiItem(info) {

            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

            }

        }, 4);

        final int row = 9;
        final int[] playerPositions = {21, 22, 23, 21 + row, 22 + row, 23 + row, 21 + row * 2, 22 + row * 2, 23 + row * 2};
        int index = 0;
        /*
         *  ONLINE PLAYERS
         */
        for (int i = ClansRank.values().length - 1; i >= 0; i--) {
            final ClansRank rank = ClansRank.values()[i];
            if (rank == ClansRank.NONE || (index + 1) >= playerPositions.length) {
                continue;
            }

            final ArrayList<UUID> players = this.clan.getPlayersFromRank(rank, false);
            for (final UUID uuid : players) {
                final ClansPlayer cp = new ClansPlayer(uuid);
                if (cp.isInFakeClan() && cp.getFakeClan().compare(this.clan)) {
                    continue;
                }
                if (cp.getPlayer() != null && cp.getPlayer().isOnline()) {
                    final int position = playerPositions[index];

                    final String name = cp.getPlayer().getName();

                    final ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                    final SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setDisplayName(Text.color("&a&l" + name));
                    meta.setLore(Arrays.asList("",
                            Text.color("&eRole &f" + rank.getName()), ""));
                    meta.setOwner(name);
                    head.setItemMeta(meta);

                    this.setItem(new GuiItem(head) {

                        @Override
                        public void onUse(final Player player, final ClickType type, final int slot) {
                            // ignore
                        }

                    }, position);
                    index++;
                }
            }
        }

        /*
         * OFFLINE PLAYERS
         */
        for (int i = ClansRank.values().length - 1; i >= 0; i--) {
            final ClansRank rank = ClansRank.values()[i];
            if (rank == ClansRank.NONE || (index + 1) >= playerPositions.length) {
                continue;
            }

            final ArrayList<UUID> players = this.clan.getPlayersFromRank(rank, false);
            for (final UUID uuid : players) {
                final ClansPlayer cp = new ClansPlayer(uuid);
                if (cp.isInFakeClan() && cp.getFakeClan().compare(this.clan)) {
                    continue;
                }
                if (cp.getPlayer() == null || !cp.getPlayer().isOnline()) {
                    final int position = playerPositions[index];
                    final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                    final String name = player.getName();

                    final ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 0);
                    final SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setDisplayName(Text.color("&c&l" + name));
                    meta.setLore(Arrays.asList("",
                            Text.color("&eRole &f" + rank.getName()), ""));
                    head.setItemMeta(meta);

                    this.setItem(new GuiItem(head) {

                        @Override
                        public void onUse(final Player player, final ClickType type, final int slot) {
                            // ignore
                        }

                    }, position);
                    index++;
                }
            }
        }

        if (index < playerPositions.length - 1) {
            for (int i = index; i < playerPositions.length; i++) {
                this.setItem(new GuiItem(new Item(Material.BARRIER).setName("&c&lEMPTY SLOT!")) {

                    @Override
                    public void onUse(final Player player, final ClickType type, final int slot) {

                    }
                }, playerPositions[i]);
            }
        }
    }

    @Override
    public void init() {

    }

}
