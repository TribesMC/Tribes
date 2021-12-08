package me.rey.clans.commands;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.enums.CommandType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

public class Kill extends ClansCommand {

    private final static Set<Player> confirming;

    static {
        confirming = new HashSet<>();
    }

    public Kill() {
        super("kill", "Kills a player", "/kill", ClansRank.NONE, CommandType.FEATURE, true);
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        Player player = (Player) sender;
        if (sender.isOp() && args.length > 0) {
            try {
                player = Bukkit.getPlayer(args[0]);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        if (confirming.contains(player) || !player.equals(sender)) {
            confirming.remove(player);

            final Map<EntityDamageEvent.DamageModifier, Double> modifiers = new HashMap<>();
            modifiers.put(EntityDamageEvent.DamageModifier.BASE, player.getHealthScale());

            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.VOID, modifiers, new EnumMap<EntityDamageEvent.DamageModifier, Function<? super Double, Double>>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(-0.0)))));
            player.setHealth(0);
            this.sendMessageWithPrefix("Kill", player.equals(sender) ? "You have killed yourself!" : "You were killed!");
        } else {
            this.sendMessageWithPrefix("Kill", "Use the command again to confirm this action!");
            confirming.add(player);
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }
}
