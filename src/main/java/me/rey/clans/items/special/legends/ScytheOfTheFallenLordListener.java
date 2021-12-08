package me.rey.clans.items.special.legends;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.clans.items.special.data.SpecialItemStatusChangeEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ScytheOfTheFallenLordListener extends LegendaryItem {

    // CHANGES \\
    // Skull speed slightly increased
    // Skull explosions no longer destroy blocks
    // Skulls now do 5 damage
    // Players within 3 blocks of the explosion receive speed 1 for 3 blocks to get away

    private final Set<WitherSkull> skulls;
    private final Map<UUID, Long> cooldown;

    public ScytheOfTheFallenLordListener(final Tribes plugin) {
        super(plugin, "SCYTHE_OF_THE_FALLEN_LORD", LegendType.SCYTHE_OF_THE_FALLEN_LORD, ClansTool.SCYTHE_OF_THE_FALLEN_LORD, ClansTool.SCYTHE_OF_THE_FALLEN_LORD_DISABLED);
        this.skulls = new HashSet<>();
        this.cooldown = new HashMap<>();
    }

    @Override
    public void update() {
        if (this.updater.isDisabled(this)) {
            return;
        }
        for (final UUID uuid : this.cooldown.keySet()) {
            final Player player = Bukkit.getPlayer(uuid);
            if ((System.currentTimeMillis() - this.cooldown.get(uuid)) / 1000L > 3L) {
                this.cooldown.remove(uuid);
                if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
                    continue;
                }
                UtilPacket.sendActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Wither Skull Recharged");
            } else {
                if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
                    continue;
                }
                final double x = 4.0 - Math.pow(10.0, -1.0) * ((double) (System.currentTimeMillis() - this.cooldown.get(player.getUniqueId())) / 100L);
                final double divide = (System.currentTimeMillis() - this.cooldown.get(uuid)) / 4000.0;
                final String[] doubleParse = Double.toString(x).replace('.', '-').split("-");
                final String concat = doubleParse[0] + "." + doubleParse[1].charAt(0);
                UtilPacket.displayProgress(ChatColor.BOLD + "Wither Skull", divide, ChatColor.WHITE + " " + concat + " Seconds", false, player);
            }
        }
    }

    @Override
    public void serverShutdown() {
        this.skulls.clear();
        this.cooldown.clear();
    }

    @EventHandler
    private void onDamage(final DamagedByEntityEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }

        if (event.getDamagee() instanceof Player) {
            if (this.plugin.getPvpTimer().getPvpTimer((Player) event.getDamagee()) > 0) {
                return;
            }
        }
        if (event.getOriginalEvent().getDamager() instanceof Player) {
            final Player player = (Player) event.getDamager();
            if (this.isOfArchetype(player.getItemInHand(), this.legend)) {
                UtilEnt.damage(6.0, "Scythe of the Fallen Lord", event.getDamagee(), event.getDamager());
                player.setHealth(Math.min(20.0, player.getHealth() + 2.0));
            }
        } else if (event.getOriginalEvent().getDamager() instanceof WitherSkull) {
            if (this.skulls.contains((WitherSkull) event.getDamager())) {
                UtilEnt.damage(4.0, "Scythe of the Fallen Lord", event.getDamagee(), (LivingEntity) ((WitherSkull) event.getDamager()).getShooter());
            }
        }
    }

    @EventHandler
    private void onInteract(final PlayerInteractEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (!this.isOfArchetype(item, this.legend)) {
                return;
            }
            if (this.plugin.getPvpTimer().getPvpTimer(event.getPlayer()) > 0) {
                new User(event.getPlayer()).sendMessageWithPrefix("Scythe of the Fallen Lord", "You cannot use this weapons ability with an active PVP timer!");
                return;
            }
            if (this.cooldown.containsKey(player.getUniqueId())) {
                return;
            }
            if (player.getLocation().getBlock().isLiquid()) {
                new User(player).sendMessageWithPrefix("Scythe of the Fallen Lord", "You cannot use &sWither Skull &rin water!");
                return;
            }

            this.cooldown.put(player.getUniqueId(), System.currentTimeMillis());

            player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 13.0f, 1.0f);

            final WitherSkull ws = event.getPlayer().launchProjectile(WitherSkull.class);
            ws.setVelocity(event.getPlayer().getLocation().getDirection());
            ws.setIsIncendiary(false);
            ws.setYield(0);
            ws.setShooter(event.getPlayer());
            this.skulls.add(ws);
        }
    }

    @EventHandler
    private void onSkullHit(final EntityExplodeEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!(event.getEntity() instanceof WitherSkull)) {
            return;
        }
        if (!this.skulls.contains((WitherSkull) event.getEntity())) {
            return;
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (event.getLocation().distance(player.getLocation()) > 3) {
                continue;
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, true, true));
        }
        event.setCancelled(true);
        this.skulls.remove((WitherSkull) event.getEntity());
        event.getLocation().getWorld().playEffect(event.getLocation(), Effect.EXPLOSION_LARGE, 1);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onStatusChange(final SpecialItemStatusChangeEvent event) {
        if (!event.item.equals(this)) {
            return;
        }
        if (event.status) {
            return;
        }
        for (final WitherSkull ws : this.skulls) {
            ws.remove();
        }
        this.skulls.clear();
    }
}