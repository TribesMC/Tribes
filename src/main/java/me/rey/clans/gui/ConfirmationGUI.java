package me.rey.clans.gui;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.base.Promote;
import me.rey.clans.commands.base.Unclaim;
import me.rey.clans.events.clans.ClanDisbandEvent;
import me.rey.clans.events.clans.ClanHierarchyEvent;
import me.rey.clans.events.clans.ClanTerritoryUnclaimEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;

public class ConfirmationGUI implements Listener {

    private Inventory inv;

    public ConfirmationGUI() {

    }

    public ConfirmationGUI(final String title) {

        this.inv = Bukkit.createInventory(null, 9 * 3, title);

        final ItemStack red = new ItemStack(Material.REDSTONE_BLOCK);
        final ItemMeta redmeta = red.getItemMeta();
        redmeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "CANCEL");
        red.setItemMeta(redmeta);

        final ItemStack green = new ItemStack(Material.EMERALD_BLOCK);
        final ItemMeta greenmeta = green.getItemMeta();
        greenmeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM");
        green.setItemMeta(greenmeta);

        this.inv.setItem(9 + 6, red);
        this.inv.setItem(9 + 2, green);
    }

    public Inventory getInv() {
        return this.inv;
    }

    @EventHandler
    public void onClick(final InventoryClickEvent e) {
        if (e.getView() == null || (!e.getView().getTitle().equals("Unclaim this territory?")
                && !e.getView().getTitle().equals("Unclaim ALL territories?")
                && !e.getView().getTitle().equals("Disband your clan?")
                && !(e.getView().getTitle().contains("Make ") && e.getView().getTitle().contains(" Leader?")))) {
            return;
        }

        e.setCancelled(true);

        if (e.getCurrentItem() == null) {
            return;
        }

        final Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.LAVA_POP, 1F, 1.5F);
            return;
        }

        if (e.getCurrentItem().getType() == Material.EMERALD_BLOCK) {

            /* TRANSFER OWNERSHIP ACTION */
            if (e.getView().getTitle().contains("Make ") && e.getView().getTitle().contains(" Leader?")) {

                final ClansPlayer cp = Promote.cpCache.get(p.getUniqueId());

                if (cp != null) {
                    cp.confirm_toPromote.promote(cp.confirm_toProm.getUniqueId());

                    final ClansRank destination = cp.confirm_toPromote.getPlayerRank(cp.confirm_toProm.getUniqueId());

                    cp.confirm_toPromote.announceToClan(String.format("&s%s&r has promoted &s%s &rto %s%s&r!", cp.getPlayer().getName(),
                            cp.confirm_name, destination.getColor(), destination.getName()));

                    cp.confirm_toPromote.demote(cp.getUniqueId());

                    Tribes.getInstance().getSQLManager().saveClan(cp.confirm_toPromote);

                    /*
                     * EVENT HANDLING
                     */
                    final ClanHierarchyEvent event = new ClanHierarchyEvent(cp.confirm_toPromote, cp.getPlayer(), ClanHierarchyEvent.HierarchyAction.PROMOTE, ClanHierarchyEvent.HierarchyReason.NORMAL, cp.confirm_toProm, cp.confirm_origin, destination);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                }

                for (final ClansPlayer clansPlayer : cp.getClan().getOnlinePlayers().keySet()) {
                    final Player pp = clansPlayer.getPlayer();
                    pp.playSound(pp.getLocation(), Sound.NOTE_PLING, 1F, 2F);
                }

                p.closeInventory();

                return;
            }

            /* UNCLAIM ACTION */
            if (e.getView().getTitle().equals("Unclaim this territory?")) {

                final Chunk standing = p.getLocation().getChunk();
                final ClansPlayer cp = new ClansPlayer(p);
                final Clan self = cp.getClan();

                // EVENT
                final ClanTerritoryUnclaimEvent event;

                Tribes.getInstance().territoryCooldowns.put(standing, System.currentTimeMillis() + Unclaim.chunkCooldown);
                self.removeTerritory(standing);
                self.announceToClan("&s" + p.getName() + " &rhas &qunclaimed &ra piece of land. (&s" + standing.getX() + "&r, &s" + standing.getZ() + "&r)");
                Tribes.getInstance().getSQLManager().saveClan(self);

                /*
                 * EVENT HANDLING
                 */
                event = new ClanTerritoryUnclaimEvent(self, p, new ArrayList<>(Collections.singletonList(standing)), ClanTerritoryUnclaimEvent.UnclaimReason.NORMAL, false);
                Bukkit.getServer().getPluginManager().callEvent(event);

                p.playSound(p.getLocation(), Sound.ITEM_BREAK, 1F, 0.75F);
                p.closeInventory();

                return;
            }

            /* UNCLAIM ALL ACTION */
            if (e.getView().getTitle().equals("Unclaim ALL territories?")) {

                final ClansPlayer cp = new ClansPlayer(p);
                final Clan self = cp.getClan();

                // EVENT
                ClanTerritoryUnclaimEvent event = null;

                /*
                 * EVENT HANDLING
                 */
                event = new ClanTerritoryUnclaimEvent(self, p, self.getTerritory(), ClanTerritoryUnclaimEvent.UnclaimReason.NORMAL, true);
                Bukkit.getServer().getPluginManager().callEvent(event);


                self.unclaimAll();
                self.announceToClan("&s" + p.getName() + " &rhas &qUNCLAIMED &rall your land.");
                Tribes.getInstance().getSQLManager().saveClan(self);

                p.playSound(p.getLocation(), Sound.ITEM_BREAK, 1F, 0.75F);
                p.closeInventory();

                return;
            }

            /* DISBAND ACTION */
            if (e.getView().getTitle().equals("Disband your clan?")) {
                final ClansPlayer cp = new ClansPlayer(p);

                cp.getClan().announceToClan("&q" + cp.getPlayer().getName() + " &rdisbanded the Clan!");

                final Clan self = cp.getClan();
                cp.disbandClan();

                /*
                 * EVENT HANDLING
                 */
                final ClanDisbandEvent event = new ClanDisbandEvent(self, cp.getPlayer(), ClanDisbandEvent.DisbandReason.NORMAL);
                Bukkit.getServer().getPluginManager().callEvent(event);

                p.playSound(p.getLocation(), Sound.ITEM_BREAK, 1F, 0.75F);
                p.closeInventory();

                return;
            }
        }
    }

}
