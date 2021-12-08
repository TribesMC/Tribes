package me.rey.core.items.custom;

import me.rey.core.items.Consumable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilsTime;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class WaterBottle extends Consumable {

    public WaterBottle() {
        super(10000, "Water Bottle", Material.POTION, true);
    }

    @Override
    protected boolean ConsumeItem(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.DRINK, 2.0F, 1.0F);
        p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, Material.WATER);

        p.setFireTicks(0);
        return true;
    }

    @Override
    protected void cooldownBlocked(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand().getType() == Material.POTION) {
            if (cooldown.containsKey(event.getPlayer()) && System.currentTimeMillis() - cooldown.get(event.getPlayer()) <= cooldownInMillis) {
                // yes this is ugly pls
                long time = cooldown.getOrDefault(event.getPlayer(), 0L);
                long miliseconds = (time + cooldownInMillis) - System.currentTimeMillis();
                new User(event.getPlayer()).sendMessageWithPrefix("Cooldown", "You cannot use a water bottle for another " + UtilsTime.getSimpleDurationStringFromSeconds(Long.parseLong(Long.toString(miliseconds).substring(0, Long.toString(miliseconds).length() - 3))) + "!");
                event.setCancelled(true);
            }
        }
    }
}
