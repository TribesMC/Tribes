package me.rey.clans.items.special.miscs;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DebugWeaponListener extends MiscItem {

    public DebugWeaponListener(final Tribes plugin) {
        super(plugin, "DEBUG_WEAPON", MiscType.DEBUG_WEAPON, ClansTool.DEBUG_WEAPON, ClansTool.DEBUG_WEAPON);
    }

    @Override
    public void update() {
    }

    @Override
    public void serverShutdown() {
    }

    @EventHandler
    private void onDamage(final DamagedByEntityEvent event) {
        if (event.getDamager() == null) {
            return;
        }
        if (!(event.getDamagee() instanceof LivingEntity)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (!this.isOfArchetype(((Player) event.getDamager()).getItemInHand(), this.misc)) {
            return;
        }
        if (!event.getDamager().isOp()) {
            //todo make this check better
            new User((Player) event.getDamager()).sendMessageWithPrefix("Debug", "You are not supposed to have this item!");
        } else {
            UtilEnt.damage(1000000.0d, ChatColor.RED + "Debug Weapon", event.getDamagee(), event.getDamager());
        }
    }

    @EventHandler
    private void onDamage(final EntityDamageByEntityEvent event) {
        if (event.getDamager() == null) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (!this.isOfArchetype(((Player) event.getDamager()).getItemInHand(), this.misc)) {
            return;
        }
        if (!event.getDamager().isOp()) {
            new User((Player) event.getDamager()).sendMessageWithPrefix("Debug", "You are not supposed to have this item!");
        } else {
            UtilEnt.damage(1000000.0d, "Debug Weapon", (LivingEntity) event.getEntity(), (LivingEntity) event.getDamager());
        }
    }
}
