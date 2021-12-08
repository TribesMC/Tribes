package me.rey.core.classes.abilities.assassin.sword;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.UtilBlock;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Evade extends Ability implements IConstant {

    private final double invincibilitySeconds = 0.8;

    public HashMap<UUID, Integer> preparingEvade = new HashMap<UUID, Integer>();
    public HashMap<UUID, Long> invincible = new HashMap<UUID, Long>();

    public Evade() {
        super(2, "Evade", ClassType.LEATHER, AbilityType.SWORD, 2, 1, 8, Arrays.asList(
                "Teleports you behind your attacker, and gives",
                "invincibility frames for a very short amount of time.",
                "Frames are canceled immediately on an attack.",
                "0.5 second cooldown on a successful evade.",
                "8 second cooldown on an unsuccessful evade.",
                "2 seconds while holding sword fails evade."
        ));
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if (hasCooldown(p)) {
            return false;
        }

        if(conditions != null && conditions.length == 1 && conditions[0] != null && conditions[0] instanceof UpdateEvent) {
            this.setCooldownCanceled(true);

            if(preparingEvade.containsKey(p.getUniqueId())) {
                if(preparingEvade.get(p.getUniqueId()) >= 20 || !p.isBlocking()) {
                    preparingEvade.remove(p.getUniqueId());

                    setIgnoresCooldown(false);
                    this.setCooldown(8);
                    applyCooldown(p);
                    sendAbilityMessage(p, ChatColor.RED + "Failed to evade.");

                } else {
                    preparingEvade.replace(p.getUniqueId(), preparingEvade.get(p.getUniqueId()) + 1);
                }
            }

            return false;
        }

        if (!preparingEvade.containsKey(p.getUniqueId())) {
            preparingEvade.put(p.getUniqueId(), 0);

            sendUsedMessageToPlayer(p, this.getName());
            invincible.put(p.getUniqueId(), System.currentTimeMillis());
        }

        setIgnoresCooldown(true);
        return true;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDamage(DamageEvent e) {
        if (!(e.getDamagee() instanceof Player)) {
            return;
        }

        if (e.getHitType() != ToolType.HitType.MELEE) {
            return;
        }

        Player damager = e.getDamager();
        Player damagee = (Player) e.getDamagee();

        if (new User(damager).isUsingAbility(this)) {
            if (invincible.containsKey(damager.getUniqueId())) {
                e.setCancelled(true);
            }
        }

        if (!new User(damagee).isUsingAbility(this)) {
            return;
        }

        if (invincible.containsKey(damagee.getUniqueId())) {
            final long passedMillis = System.currentTimeMillis() - invincible.getOrDefault(damagee.getUniqueId(), 0L);
            if (passedMillis / 1_000D >= invincibilitySeconds) {
                invincible.remove(damagee.getUniqueId());
            }

            e.setCancelled(true);
        }

        if (!preparingEvade.containsKey(damagee.getUniqueId())) {
            return;
        }

        e.setCancelled(true);

        preparingEvade.remove(damagee.getUniqueId());

        this.setIgnoresCooldown(false);
        this.setCooldownCanceled(false);
        this.setCooldown(0.5F);
        this.applyCooldown(damagee);

        for(int i=0; i<=8; i++) {
            damagee.getWorld().playSound(damagee.getLocation(), Sound.NOTE_BASS_DRUM, 0.1f, 0.2f);
        }
        damagee.getWorld().spigot().playEffect(damagee.getLocation(), Effect.LARGE_SMOKE, 0, 0, 0F, 0F, 0F, 0F, 10, 100);

        tpBehindPlayer(damagee, damager);
    }

    private void tpBehindPlayer(Player damagee, Player damager) {
        Location tpLoc = damager.getLocation();
        tpLoc.add(tpLoc.getDirection().multiply(-2));

        Location locInBetween = damager.getLocation();
        locInBetween.add(locInBetween.getDirection().multiply(-1));

        tpLoc.setY(damager.getLocation().getY());
        locInBetween.setY(damager.getLocation().getY());

        if(!damager.getLocation().getBlock().getType().isSolid() && !UtilBlock.getBlockAbove(damager.getLocation().getBlock()).getType().isSolid()) {
            if (!locInBetween.getBlock().getType().isSolid() && !UtilBlock.getBlockAbove(locInBetween.getBlock()).getType().isSolid()) {

                if (!tpLoc.getBlock().getType().isSolid() && !UtilBlock.getBlockAbove(tpLoc.getBlock()).getType().isSolid()) {
                    damagee.teleport(tpLoc);
                    return;
                }

                damagee.teleport(locInBetween);
                return;
            }
        }

        damagee.teleport(damager.getLocation());
    }

}
