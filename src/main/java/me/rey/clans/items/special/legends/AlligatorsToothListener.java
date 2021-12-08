package me.rey.clans.items.special.legends;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AlligatorsToothListener extends LegendaryItem {

    // CHANGES \\
    // Now does 9 damage when it's raining

    public AlligatorsToothListener(final Tribes plugin) {
        super(plugin, "ALLIGATORS_TOOTH", LegendType.ALLIGATORS_TOOTH, ClansTool.ALLIGATORS_TOOTH, ClansTool.ALLIGATORS_TOOTH_DISABLED);
    }

    @Override
    public void update() {
        if (this.updater.isDisabled(this)) {
            return;
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final ItemStack itemInHand = player.getItemInHand();
            if (!this.isOfArchetype(itemInHand, this.legend)) {
                return;
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 100, 255));
        }
    }

    @Override
    public void serverShutdown() {

    }

    @EventHandler
    private void onInteract(final PlayerInteractEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        if (!event.hasItem()) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!this.isOfArchetype(event.getItem(), this.legend)) {
                return;
            }
            if (player.getLocation().getBlock().getType() != Material.WATER && player.getLocation().getBlock().getType() != Material.STATIONARY_WATER) {
                return;
            }
            player.setVelocity(player.getLocation().getDirection().multiply(1.3));

            final Block block = player.getLocation().getBlock();
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 8);
            block.getWorld().playSound(block.getLocation(), Sound.SPLASH, 0.4f, 1.0f);
        }
    }

    @EventHandler
    private void onDamage(final DamagedByEntityEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getDamager();
        if (this.plugin.getPvpTimer().getPvpTimer(player) > 0) {
            return;
        }
        if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
            return;
        }
        if (player.getLocation().getBlock().isLiquid()) {
            UtilEnt.damage(12.0, "Alligators Tooth", event.getDamagee(), event.getDamager());
        } else {
            UtilEnt.damage(this.updater.isRaining(event.getDamager().getWorld()) ? 9.0 : 7.0, "Alligators Tooth", event.getDamagee(), event.getDamager());
        }
    }
}