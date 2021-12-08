package me.rey.core.effects;

import me.rey.core.Warriors;
import me.rey.core.effects.EffectType.Applyable;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public abstract class Effect implements Listener, Applyable {

    private static final PotionEffectType[] BAD_EFFECTS = {
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.HARM,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    };
    private static final HashMap<Class<? extends Effect>, Set<LivingEntity>> players = new HashMap<>();
    private static Effect[] CUSTOM_EFFECTS = new Effect[0];
    protected final String defaultApplyMessage, defaultExpireMessage;

    private final EffectType type;

    public Effect(final String name, final EffectType type) {
        if (!players.containsKey(this.getClass())) {
            players.put(this.getClass(), new HashSet<>());
        }
        this.type = type;

        this.defaultApplyMessage = "You are now " + name.toLowerCase() + (name.endsWith("e") ? "d" : "ed") + ".";
        this.defaultExpireMessage = "You are no longer " + name.toLowerCase() + (name.endsWith("e") ? "d" : "ed") + ".";

        Bukkit.getServer().getPluginManager().registerEvents(this, Warriors.getInstance().getPlugin());

        final Effect[] copy = CUSTOM_EFFECTS.clone();
        boolean found = false;
        for (final Effect eff : CUSTOM_EFFECTS) {
            if (eff.getType().equals(type)) {
                found = true;
            }
        }

        if (!found) {
            CUSTOM_EFFECTS = new Effect[CUSTOM_EFFECTS.length + 1];
            for (int i = 0; i < copy.length; i++) {
                CUSTOM_EFFECTS[i] = copy[i];
            }

            CUSTOM_EFFECTS[copy.length] = this;
        }
    }

    public static boolean hasEffect(final Class<? extends Effect> clazz, final LivingEntity ent) {
        return players.containsKey(clazz) && players.get(clazz).contains(ent);
    }

    public static void clearAllEffects(final LivingEntity ent, final List<Class<? extends Effect>> exclude) {

        final List<PotionEffectType> active = new ArrayList<>();
        for (final PotionEffectType type : BAD_EFFECTS) {
            ent.getActivePotionEffects().forEach((e) -> {
                if (e.getType().equals(type)) {
                    active.add(type);
                }
            });
        }

        for (final PotionEffectType act : active) {
            ent.removePotionEffect(act);
        }

        for (final Effect eff : CUSTOM_EFFECTS) {
            if (hasEffect(eff.getClass(), ent) && (exclude == null || exclude.size() < 1 || !exclude.contains(eff.getClass()))) {
                eff.expireForcefully(ent);
            }
        }

    }

    @EventHandler
    public void onLog(final PlayerQuitEvent e) {
        if (hasEffect(this.getClass(), e.getPlayer())) {
            this.expireForcefully(e.getPlayer());
        }
    }

    public EffectType getType() {
        return this.type;
    }

    public void expireForcefully(final LivingEntity p) {
        if (players.get(this.getClass()).contains(p)) {
            players.get(this.getClass()).remove(p);
        }
    }

    public void apply(final LivingEntity ent, final double seconds) {
        if (ent == null) {
            return;
        }

        players.get(this.getClass()).add(ent);
        this.onApply(ent, seconds);

        if (this.applySound() != null && ent instanceof Player) {
            this.applySound().play((Player) ent);
        }
        if (this.applyMessage() != null && ent instanceof Player) {
            ent.sendMessage(Text.format("Effect", this.applyMessage()));
        }

        final Class<? extends Effect> clazz = this.getClass();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!players.get(clazz).contains(ent)) {
                    return;
                }
                players.get(clazz).remove(ent);

                if (Effect.this.expireSound() != null && ent instanceof Player) {
                    Effect.this.expireSound().play((Player) ent);
                }
                if (Effect.this.expireMessage() != null) {
                    ent.sendMessage(Text.format("Effect", Effect.this.expireMessage()));
                }
            }

        }.runTaskLater(Warriors.getInstance().getPlugin(), (int) Math.round(seconds * 20));
    }

}
