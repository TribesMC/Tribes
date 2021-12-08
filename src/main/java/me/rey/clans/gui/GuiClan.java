package me.rey.clans.gui;

import me.rey.Main;
import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClanRelations;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.base.Kick;
import me.rey.clans.commands.base.Promote;
import me.rey.clans.events.clans.ClanHierarchyEvent;
import me.rey.clans.utils.ErrorCheck;
import me.rey.clans.utils.References;
import me.rey.core.gui.GuiItem;
import me.rey.core.gui.Item;
import me.rey.core.players.User;
import me.rey.core.utils.Text;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GuiClan extends GuiEditable {

    private final Clan clan;
    private final Player opener;
    //	private SQLManager sql;
    private final ClansRank rank;

    public GuiClan(final Player opener, final Clan clan, final ClansRank rank) {
        super("", 6, JavaPlugin.getPlugin(Main.class));
        this.clan = clan;
        this.rank = rank;
        this.opener = opener;
//		this.sql = Main.getInstance().getSQLManager();
    }

    @Override
    public void setup() {
        final String TITLE_COLOR = "&a&l";

        // Invites item
        final List<String> invitesLore = Arrays.asList("",
                "&7Clans have a max size of &e" + References.MAX_MEMBERS + "&7 members",
                "&7You currently have &e" + this.clan.getPlayers(false).size() + "&7 members",
                "&7More members in your clan will allow you to",
                "&7claim more land, but it will also increase",
                "&7your Energy drain per minute.",
                "",
                "&eLeft Click &fInvite Player");
        this.setItem(new GuiItem(new Item(Material.PRISMARINE).setDurability(1).setName(TITLE_COLOR + "Invites").setLore(invitesLore)) {

            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {
                // TODO Invites GUI
            }
        }, 0);

        // TERRITORY
        final int maxChunks = this.clan.getPossibleTerritory() <= References.MAX_TERRITORY ? this.clan.getPossibleTerritory() : References.MAX_TERRITORY;
        final List<String> territoryLore = Arrays.asList("",
                "&7Every land claim represents a 16x16 chunk",
                "&7Your clan can claim a maximum of &e" + maxChunks + "&7 chunks",
                "&7You currently have &e" + this.clan.getTerritory().size() + "&7 chunk(s) claimed",
                "&7Increase max claims with more clan members",
                "&7Energy cost will increase with more land claimed",
                "",
                "&eLeft Click &fClaim Land",
                "&eShift-Left Click &fUnclaim Land",
                "&eShift-Right Click &fUnclaim All Land");
        this.setItem(new GuiItem(new Item(Material.PRISMARINE).setName(TITLE_COLOR + "Territory").setLore(territoryLore)) {

            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

            }
        }, 2);

        // ENERGY
        final List<String> energylore = Arrays.asList("",
                "&7Energy is the currency used to upkeep",
                "&7your clan. Energy drains over time and",
                "&7you will need to refill it at the NPC in",
                "&7the shops. More clan members and more land",
                "&7increased the rate energy drains at.",
                "",
                "&eEnergy &f" + this.clan.getEnergy() + "/" + References.MAX_ENERGY);
        this.setItem(new GuiItem(new Item(Material.SEA_LANTERN).setName(TITLE_COLOR + "Energy").setLore(energylore)) {

            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

            }
        }, 4);

        // LEAVE
        final List<String> leaveLore = Arrays.asList("", "&eShift-Left Click &fLeave Clan", "&eShift-Right Click &fDisband Clan");
        this.setItem(new GuiItem(new Item(Material.PRISMARINE).setDurability(2).setName(TITLE_COLOR + "Leave").setLore(leaveLore)) {

            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {
                final ClansPlayer cp = new ClansPlayer(player.getUniqueId());
                switch (type) {
                    case SHIFT_RIGHT:
                        if (GuiClan.this.rank == ClansRank.LEADER) {
                            cp.disbandClan();
                            cp.getPlayer().closeInventory();
                        }
                        break;
                    case SHIFT_LEFT:
                        if (GuiClan.this.rank != ClansRank.LEADER) {
                            GuiClan.this.clan.announceToClan("&s" + cp.getPlayer().getName() + " &rleft the Clan!", cp);
                            cp.leaveClan();
                            cp.getPlayer().closeInventory();
                        }
                        break;
                    default:
                        break;
                }
            }

        }, 6);

        // COMMANDS
        final List<String> commandsLore = Arrays.asList("",
                "&e/c help &fLists Clans Commands",
                "&e/c ally <clan> &fRequest Ally",
                "&e/c neutral <clan> &fRevoke Ally or Truce",
                "&e/c sethome &fSet Home Bed",
                "&e/c home &fTeleport to Home Bed",
                "&e/c map &fGive yourself a World Map");
        this.setItem(new GuiItem(new Item(Material.LAVA_BUCKET).setName(TITLE_COLOR + "Commands").setLore(commandsLore)) {

            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

            }
        }, 8);


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
                    final int x = cp.getPlayer().getLocation().getBlockX();
                    final int y = cp.getPlayer().getLocation().getBlockY();
                    final int z = cp.getPlayer().getLocation().getBlockZ();

                    final List<String> lore = new ArrayList<>(Arrays.asList(
                            "",
                            Text.color("&eRole &f" + rank.getName()),
                            Text.color(String.format("&eLocation &f(%s, %s, %s)", x, y, z)),
                            ""
                    ));

                    if (this.rank.getPower() >= ClansRank.ADMIN.getPower() && this.rank.getPower() > rank.getPower()) {
                        lore.add(Text.color("&eLeft Click &fPromote"));
                        lore.add(Text.color("&eRight Click &fDemote"));
                        lore.add(Text.color("&eShift-Right Click &fKick"));
                    }

                    final ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                    final SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setDisplayName(Text.color("&a&l" + name));
                    meta.setLore(lore);
                    meta.setOwner(name);
                    head.setItemMeta(meta);

                    this.setItem(new GuiItem(head) {

                        @Override
                        public void onUse(final Player p, final ClickType type, final int slot) {
                            if (slot != position) {
                                return;
                            }

                            GuiClan.this.handleHierarchyAction(type, p, cp.getPlayer());
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
                    final List<String> lore = new ArrayList<>(Arrays.asList(
                            "",
                            Text.color("&eRole &f" + rank.getName()),
                            ""
                    ));

                    if (this.rank.getPower() >= ClansRank.ADMIN.getPower() && this.rank.getPower() > rank.getPower()) {
                        lore.add(Text.color("&eLeft Click &fPromote"));
                        lore.add(Text.color("&eRight Click &fDemote"));
                        lore.add(Text.color("&eShift-Right Click &fKick"));
                    }

                    final ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 0);
                    final SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setDisplayName(Text.color("&c&l" + name));
                    meta.setLore(lore);
                    head.setItemMeta(meta);

                    this.setItem(new GuiItem(head) {

                        @Override
                        public void onUse(final Player p, final ClickType type, final int slot) {
                            if (slot != position) {
                                return;
                            }

                            GuiClan.this.handleHierarchyAction(type, p, player);
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

        final List<Material> materials = Arrays.asList(Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD);

        /* 4 LEFT SLOTS (+ WPs) */
        for (int i = 0; i < 4; i++) {

            Clan c = null;
            long wp = 0;

            if (this.getWarPointClans() == null || this.getWarPointClans().size() == 0 || this.getWarPointClans().size() < (i + 1)) {
                break;
            }

            for (final Clan cc : this.getWarPointClans().get(i).keySet()) {
                c = cc;
                wp = this.getWarPointClans().get(i).get(cc);
            }

            if (wp <= 0) {
                break;
            }

            this.setItem(new GuiItem(new Item(materials.get(materials.size() - (i + 1))).setName(ChatColor.GREEN + c.getName()).setAmount((int) wp)) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {

                }
            }, 45 + i);
        }


        /* 4 LEFT SLOTS (- WPs) */
        for (int i = 0; i < 4; i++) {
            Clan c = null;
            long wp = 0;

            if (this.getWarPointClans() == null || this.getWarPointClans().size() == 0 || this.getWarPointClans().size() < i + 1) {
                break;
            }

            for (final Clan cc : this.getWarPointClans().get(this.getWarPointClans().size() - (i + 1)).keySet()) {
                c = cc;
                wp = this.getWarPointClans().get(this.getWarPointClans().size() - (i + 1)).get(cc);
            }

            if (wp >= 0) {
                break;
            }

            this.setItem(new GuiItem(new Item(materials.get(materials.size() - (i + 1))).setName(ClanRelations.ENEMY.getClanColor() + c.getName()).setAmount((int) wp)) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {

                }
            }, 53 - i);
        }

    }

    public void promote(final Player promoter, final OfflinePlayer promotee) {

        final ClansPlayer cp = new ClansPlayer(promoter);
        if (!cp.hasClan()) {
            ErrorCheck.noClan(promoter);
            return;
        }

        final Clan toPromote = cp.getClan();
        if (!toPromote.isInClan(promotee.getName())) {
            ErrorCheck.specifiedNotInClan(promoter);
            return;
        }

        final ClansPlayer toProm = toPromote.getPlayer(promotee.getName());
        final String name = toProm.isOnline() ? toProm.getPlayer().getName() : toProm.getOfflinePlayer().getName();

        final ClansRank origin = toPromote.getPlayerRank(toProm.getUniqueId());
        if (toPromote.getPlayerRank(toProm.getUniqueId()).getPower() >= toPromote.getPlayerRank(cp.getUniqueId()).getPower()) {
            ErrorCheck.playerNotOurank(promoter);
            return;
        }

        if (!toPromote.promotable(toProm.getUniqueId())) {
            new User(promoter).sendMessageWithPrefix("Tribe", "This player has the highest rank!");
            return;
        }

        if (origin == ClansRank.ADMIN) {

            cp.confirm_toPromote = toPromote;
            cp.confirm_toProm = toProm;
            cp.confirm_name = name;
            cp.confirm_origin = origin;
            Promote.cpCache.put((promoter).getUniqueId(), cp);

            promoter.openInventory(new ConfirmationGUI("Make " + name + " Leader?").getInv());

        } else {

            toPromote.promote(toProm.getUniqueId());

            final ClansRank destination = toPromote.getPlayerRank(toProm.getUniqueId());

            toPromote.announceToClan(String.format("&s%s&r has promoted &s%s &rto %s%s&r!", cp.getPlayer().getName(),
                    name, destination.getColor(), destination.getName()));

            Tribes.getInstance().getSQLManager().saveClan(toPromote);

            final ClansPlayer p = new ClansPlayer(promoter);
            final Clan clan = p.getRealClan();
            this.updateInventory();

            for (final ClansPlayer clansPlayer : clan.getOnlinePlayers().keySet()) {
                final Player pp = clansPlayer.getPlayer();
                pp.playSound(pp.getLocation(), Sound.NOTE_PLING, 1F, 2F);
            }

            /*
             * EVENT HANDLING
             */
            final ClanHierarchyEvent event = new ClanHierarchyEvent(toPromote, cp.getPlayer(), ClanHierarchyEvent.HierarchyAction.PROMOTE, ClanHierarchyEvent.HierarchyReason.NORMAL, toProm, origin, destination);
            Bukkit.getServer().getPluginManager().callEvent(event);

        }

    }

    public void demote(final Player demoter, final OfflinePlayer demotee) {

        final ClansPlayer cp = new ClansPlayer(demoter);
        if (!cp.hasClan()) {
            ErrorCheck.noClan(demoter);
            return;
        }

        final Clan toPromote = cp.getClan();
        if (!toPromote.isInClan(demotee.getName())) {
            ErrorCheck.specifiedNotInClan(demoter);
            return;
        }

        final ClansPlayer toProm = toPromote.getPlayer(demotee.getName());
        final String name = toProm.isOnline() ? toProm.getPlayer().getName() : toProm.getOfflinePlayer().getName();

        if (toPromote.getPlayerRank(toProm.getUniqueId()).getPower() >= toPromote.getPlayerRank(cp.getUniqueId()).getPower()) {
            ErrorCheck.playerNotOurank(demoter);
            return;
        }

        final ClansRank origin = toPromote.getPlayerRank(toProm.getUniqueId());
        final boolean success = toPromote.demote(toProm.getUniqueId());
        if (!success) {
            new User(demoter).sendMessageWithPrefix("Tribe", "This player has the lowest rank!");
            return;
        }

        final ClansRank destination = toPromote.getPlayerRank(toProm.getUniqueId());
        toPromote.announceToClan(String.format("&s%s&r has demoted &s%s &rto %s%s&r!", cp.getPlayer().getName(),
                name, destination.getColor(), destination.getName()));
        Tribes.getInstance().getSQLManager().saveClan(toPromote);

        final ClansPlayer p = new ClansPlayer(demoter);
        final Clan clan = p.getRealClan();
        this.updateInventory();

        for (final ClansPlayer clansPlayer : clan.getOnlinePlayers().keySet()) {
            final Player pp = clansPlayer.getPlayer();
            pp.playSound(pp.getLocation(), Sound.NOTE_PLING, 1F, 2F);
        }

        /*
         * EVENT HANDLING
         */
        final ClanHierarchyEvent event = new ClanHierarchyEvent(toPromote, cp.getPlayer(), ClanHierarchyEvent.HierarchyAction.DEMOTE, ClanHierarchyEvent.HierarchyReason.NORMAL, toProm, origin, destination);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public ArrayList<HashMap<Clan, Long>> getWarPointClans() {
        final ArrayList<HashMap<Clan, Long>> wpclans = new ArrayList<>();

        final ArrayList<HashMap<Clan, Long>> all = new ArrayList<>();

        for (final UUID cid : this.clan.getWarpoints().keySet()) {
            final Clan c = Tribes.getInstance().getClan(cid);

            final HashMap<Clan, Long> clanwp = new HashMap<Clan, Long>();

            clanwp.put(c, this.clan.getWarpointsOnClan(c.getUniqueId()));

            all.add(clanwp);
        }

        /** Sorting **/
        for (int i = 0; i < all.size() - 1; i++) {
            for (int ii = 0; ii < all.size() - 1; ii++) {
                final HashMap<Clan, Long> data = all.get(ii);

                long wp = 0;

                for (final Clan c : data.keySet()) {
                    wp = this.clan.getWarpointsOnClan(c.getUniqueId());
                }

                final HashMap<Clan, Long> nextData = all.get(ii + 1);
                long nextWP = 0;

                for (final Clan c : nextData.keySet()) {
                    nextWP = this.clan.getWarpointsOnClan(c.getUniqueId());
                }

                if (wp < nextWP) {
                    all.set(ii, nextData);
                    all.set(ii + 1, data);
                }
            }
        }

        for (int i = 0; i < Tribes.clans.size() + 1; i++) {

            if (i + 1 > all.size()) {
                break;
            }

            final HashMap<Clan, Long> data = all.get(i);

            wpclans.add(data);
        }

        return wpclans;
    }

    private void handleHierarchyAction(final ClickType type, final Player executor, final OfflinePlayer affected) {

        if (affected == executor) {
            return;
        }

        switch (type) {

			/*
			PROMOTE
			 */
            case LEFT:
            case SHIFT_LEFT:
                this.promote(executor, affected);
                break;

			/*
			DEMOTE
			 */
            case RIGHT:
                this.demote(executor, affected);
                break;

			/*
			KICK
			 */
            case SHIFT_RIGHT:
                Kick.handleKick(executor, affected.getName());
                this.updateInventory();
                break;

        }

    }

    @Override
    public void init() {

    }

}
