package me.rey.core.classes.abilities.brute.passive_a;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.effects.SoundEffect;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Stampede extends Ability implements IConstant, IDamageTrigger.IPlayerDamagedByEntity, IDamageTrigger.IPlayerDamagedEntity {

    public HashMap<UUID, StampedeProfile> stampedeProfiles = new HashMap<UUID, StampedeProfile>();

    public Stampede() {
        super(633, "Stampede", ClassType.DIAMOND, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
                "Keep sprinting for <variable>5-0.5*l</variable> (-0.5) seconds to",
                "gain up a level of momentum up to Speed III.",
                "Attacks during stampede deal <variable>0.25 + 0.25*l</variable> (+0.25)",
                "bonus damage and 50% more knockback."
        ));

        this.setIgnoresCooldown(true);
        this.setWhileSlowed(true);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        StampedeProfile sp;

        if (stampedeProfiles.containsKey(p.getUniqueId())) {
            sp = stampedeProfiles.get(p.getUniqueId());
        } else {
            sp = new StampedeProfile(p);
            stampedeProfiles.put(p.getUniqueId(), sp);
        }

        if (conditions != null && conditions.length > 0 && conditions[0] instanceof UpdateEvent) {

            if (p.isSprinting()) {
                sp.buildUp(level);
                sp.nonSprintTicks = 0;
            } else {
                sp.nonSprintTicks++;

                if (sp.nonSprintTicks > sp.graceTicks) {
                    sp.nonSprintTicks = 0;
                    sp.ticks = 0;
                }

                sp.update();
            }

            return false;
        }

        if (conditions != null && conditions.length > 0 && conditions[0] instanceof DamagedByEntityEvent) {
            DamagedByEntityEvent e = (DamagedByEntityEvent) conditions[0];

            if (e.getDamage() <= 0.5) return false;

            sp.ticks = 0;
            sp.update();

            return true;
        }

        if (conditions != null && conditions.length > 0 && conditions[0] instanceof DamageEvent) {
            DamageEvent e = (DamageEvent) conditions[0];

            if (sp.level <= 0) return false;

            sp.ticks = 0;
            double bonusdamage = 0.25 + 0.25 * level;

            new SoundEffect(Sound.ZOMBIE_WOOD, 0.4F * level).play(p.getLocation());

            e.setDamage(e.getDamage() + bonusdamage);
            e.setKnockbackMult(1.5);
            sp.update();

            return true;
        }

        return false;
    }

    class StampedeProfile {

        final int graceTicks = 20;

        Player p;
        int ticks;
        int level = 0;
        int lastlevel = 0;
        int nonSprintTicks = 0;

        final int maxlevel = 3;

        public StampedeProfile(Player p) {
            this.p = p;
        }

        void buildUp(int l) {
            int buildtime = (int) (20 * ((5.5 - 0.5*l)) * 1);

            if (ticks < buildtime * maxlevel)
                ticks++;

            if (ticks >= buildtime)
                level=1;

            if (ticks >= buildtime*2)
                level=2;

            if (ticks >= buildtime*3)
                level=3;

            if (level > 0 && lastlevel != level) {
                new SoundEffect(Sound.ZOMBIE_IDLE, 0.5F + 0.5F * level).play(p.getLocation());
            }

            lastlevel = level;
            update();
        }

        void update() {
            if (level <= 0) return;

            if (ticks == 0) {
                level = 0;
                p.removePotionEffect(PotionEffectType.SPEED);
                return;
            }

            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, graceTicks, level-1);
            p.addPotionEffect(speed);
        }

    }

}
