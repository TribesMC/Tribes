package me.rey.clans.items.special.epics;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.players.User;
import me.rey.core.utils.UtilPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RunedPickaxeListener extends EpicItem {

    // CHANGES \\
    // Can now be used in water
    // No longer displays title
    // Implemented block cap

    private final Map<UUID, Long> cooldown;
    private final Map<UUID, Long> instantMining;
    private final Map<UUID, Integer> blocksMinedPerUnit10Second;

    private int updateCounter = 0;

    public RunedPickaxeListener(final Tribes plugin) {
        super(plugin, "RUNED_PICKAXE", EpicType.RUNED_PICKAXE, ClansTool.RUNED_PICKAXE, ClansTool.RUNED_PICKAXE_DISABLED);
        this.cooldown = new HashMap<>();
        this.instantMining = new HashMap<>();
        this.blocksMinedPerUnit10Second = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockBreak(final BlockBreakEvent event) {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        final int blocksMined = this.blocksMinedPerUnit10Second.getOrDefault(event.getPlayer().getUniqueId(), 0);
        if (this.isOfArchetype(event.getPlayer().getItemInHand(), this.epic)) {
            if (blocksMined > 70) {
                event.setCancelled(true);
                new User(event.getPlayer()).sendMessageWithPrefix("Runed Pickaxe", "You are mining blocks too quickly! You cannot mine any blocks with the &sRuned Pickaxe &rfor a few seconds.");
                return;
            }

            event.getBlock().breakNaturally();
        }
        this.blocksMinedPerUnit10Second.put(event.getPlayer().getUniqueId(), blocksMined + 1);
    }

    @EventHandler
    public void onClick(final PlayerInteractEvent event) {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (this.isOfArchetype(item, this.epic)) {
                if (this.cooldown.containsKey(player.getUniqueId())) {
                    final double x = 15.0 - Math.pow(10.0, -1.0) * ((double) (System.currentTimeMillis() - this.cooldown.get(player.getUniqueId())) / 100L);
                    final String[] doubleParse = Double.toString(x).replace('.', '-').split("-");
                    final String concat = doubleParse[0] + "." + doubleParse[1].charAt(0);
                    new User(player).sendMessageWithPrefix("Runed Pickaxe", "You cannot use &sInstant Mine &r for &s" + concat + " &r seconds!");
                    return;
                }
                if (this.instantMining.containsKey(player.getUniqueId())) {
                    return;
                }

                this.instantMining.put(player.getUniqueId(), System.currentTimeMillis());
                player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                new User(player).sendMessageWithPrefix("Runed Pickaxe", "You used &sInstant Mine&r!");
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && this.isOfArchetype(item, this.epic)) {
            if (this.instantMining.containsKey(player.getUniqueId())) {
                if (event.isCancelled()) {
                    return;
                }
                final BlockBreakEvent fakeEvent = new BlockBreakEvent(event.getClickedBlock(), event.getPlayer());
                Bukkit.getServer().getPluginManager().callEvent(fakeEvent);
            }
        }
    }

    @Override
    public void update() {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }

        if (this.updateCounter % 200 == 0) {
            this.updateCounter = -1;
            this.blocksMinedPerUnit10Second.clear();
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!this.instantMining.containsKey(player.getUniqueId()) && this.isOfArchetype(player.getItemInHand(), this.epic)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 103));
            }
        }

        for (final UUID uuid : this.instantMining.keySet()) {
            final Player player = Bukkit.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                this.instantMining.remove(uuid);
            } else if ((System.currentTimeMillis() - this.instantMining.get(player.getUniqueId())) / 1000L > 11L) {
                this.instantMining.remove(player.getUniqueId());
                this.cooldown.put(player.getUniqueId(), System.currentTimeMillis());
            } else {
                if (!this.isOfArchetype(player.getItemInHand(), this.epic)) {
                    continue;
                }
                final double divide = (System.currentTimeMillis() - this.instantMining.get(player.getUniqueId())) / 12000.0;
                UtilPacket.displayProgress(ChatColor.BOLD + "Instant Mine", divide, null, true, player);
            }
        }

        for (final UUID uuid : this.cooldown.keySet()) {
            final Player player = Bukkit.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                this.cooldown.remove(uuid);
            } else if ((System.currentTimeMillis() - this.cooldown.get(uuid)) / 1000L > 14L) {
                this.cooldown.remove(uuid);
                new User(player).sendMessageWithPrefix("Runed Pickaxe", "You can use &sInstant Mine&r!");
                if (this.isOfArchetype(player.getItemInHand(), this.epic)) {
                    UtilPacket.sendActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Instant Mine Recharged");
                }
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 5.0f, 1.0f);
            } else {
                if (!this.isOfArchetype(player.getItemInHand(), this.epic)) {
                    continue;
                }
                final double x = 15.0 - Math.pow(10.0, -1.0) * ((double) (System.currentTimeMillis() - this.cooldown.get(player.getUniqueId())) / 100L);
                final double divide = (System.currentTimeMillis() - this.cooldown.get(uuid)) / 15000.0;
                final String[] doubleParse = Double.toString(x).replace('.', '-').split("-");
                final String concat = doubleParse[0] + "." + doubleParse[1].charAt(0);
                UtilPacket.displayProgress(ChatColor.BOLD + "Instant Mine", divide, ChatColor.WHITE + " " + concat + " Seconds", false, player);
            }
        }

        this.updateCounter++;
    }

    @Override
    public void serverShutdown() {
        this.cooldown.clear();
        this.instantMining.clear();
        this.blocksMinedPerUnit10Second.clear();
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
        this.instantMining.remove(event.getPlayer().getUniqueId());
        this.blocksMinedPerUnit10Second.remove(event.getPlayer().getUniqueId());
    }
}