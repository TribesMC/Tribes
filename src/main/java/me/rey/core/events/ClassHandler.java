package me.rey.core.events;

import me.rey.core.Warriors;
import me.rey.core.events.customevents.ability.PlayerClassUpdateEvent;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ClassHandler extends BukkitRunnable {

    public ClassHandler() {
        this.start();
    }

    private void start() {
        this.runTaskTimer(Warriors.getInstance().getPlugin(), 0, 1);
    }

    @Override
    public void run() {

        for (final Player p : Bukkit.getOnlinePlayers()) {

            final User user = new User(p);
            user.updateClassEffects();

            if (Warriors.userCache.containsKey(p)) {

                if (!this.hasArmor(p)) {

                    final PlayerClassUpdateEvent event = new PlayerClassUpdateEvent(p, Warriors.userCache.get(p), null);
                    Bukkit.getPluginManager().callEvent(event);

                    Warriors.userCache.remove(p);
                    user.resetEffects();
                    user.sendMessageWithPrefix("Class", "You took off your armor set.");

                    continue;
                }

                if (Warriors.userCache != null && Warriors.userCache.get(p) != null && this.hasArmor(p)
                        && !Warriors.userCache.get(p).equals(user.getWearingClass())) {

                    final PlayerClassUpdateEvent event = new PlayerClassUpdateEvent(p, Warriors.userCache.get(p), user.getWearingClass());
                    Bukkit.getPluginManager().callEvent(event);

                    Warriors.userCache.replace(p, user.getWearingClass());
                    user.resetEffects();
                    user.sendMessageWithPrefix("Class", "You took off your armor set.");
                    if (user.getWearingClass() != null) {
                        user.sendMessageWithPrefix("Class", "You equipped &e" + user.getWearingClass().getName() + "&7.");
                    }
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                    user.sendBuildEquippedMessage(user.getWearingClass());
                }
            } else {

                if (!this.hasArmor(p)) {
                    continue;
                }
                if (user.getWearingClass() == null) {
                    return;
                }

                Warriors.userCache.put(p, user.getWearingClass());
                user.resetEffects();
                user.sendMessageWithPrefix("Selector", "You equipped &e" + user.getWearingClass().getName() + "&7.");
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                user.sendBuildEquippedMessage(user.getWearingClass());

                final PlayerClassUpdateEvent event = new PlayerClassUpdateEvent(p, null, Warriors.userCache.get(p));
                Bukkit.getPluginManager().callEvent(event);
            }
        }
    }

    private boolean hasArmor(final Player p) {
        return p.getInventory().getHelmet() != null && p.getInventory().getChestplate() != null
                && p.getInventory().getLeggings() != null && p.getInventory().getBoots() != null;
    }

}
