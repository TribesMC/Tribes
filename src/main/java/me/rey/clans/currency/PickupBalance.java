package me.rey.clans.currency;

import me.rey.clans.clans.ClansPlayer;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.gui.Gui;
import me.rey.core.gui.Item;
import me.rey.core.packets.Title;
import me.rey.core.utils.UtilMath;
import me.rey.core.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PickupBalance implements Listener {

    public static String NAME_PREFIX = "&e" + CurrencyHandler.CURRENCY_NAME + " +";
    public static Pattern regexForName = Pattern.compile("(" + CurrencyHandler.CURRENCY_NAME + " \\+\\d+)");
    public static Material MATERIAL = Material.GOLD_NUGGET;

    public static Item getPickupCurrency(int minCurrency, int maxCurrency) {
        int currencyToGive = UtilMath.randBetween(minCurrency, maxCurrency);
        return new Item(Material.GOLD_NUGGET).setAmount(1).setGlow(true).setName(PickupBalance.NAME_PREFIX + currencyToGive);
    }

    public static boolean isPickupCurrency(ItemStack stack) {
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName() || !stack.getItemMeta().hasDisplayName()) return false;
        if (!stack.getType().equals(PickupBalance.MATERIAL)) return false;

        Matcher matcher = regexForName.matcher(ChatColor.stripColor(stack.getItemMeta().getDisplayName()));
        return matcher.find();
    }

    public static int getPickupCurrency(ItemStack stack) {
        if (!isPickupCurrency(stack)) return 0;

        Matcher matcher = regexForName.matcher(ChatColor.stripColor(stack.getItemMeta().getDisplayName()));
        boolean found = matcher.find();

        return !found ? 0 : Integer.parseInt(matcher.group().replace(ChatColor.stripColor(Text.color(PickupBalance.NAME_PREFIX)), ""));
    }

    /**
     * Handling pickupable currency
     */
    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();

        /* Returning if the item is not pickupable currency */
        if (!PickupBalance.isPickupCurrency(item)) return;

        int reward = PickupBalance.getPickupCurrency(item);
        if (reward <= 0) return;

        /* Effects */
        new ParticleEffect.ColoredParticle(232, 224, 74).setParticleCount(5).setEffect(Effect.COLOURED_DUST).play(e.getItem().getLocation());
        new SoundEffect(Sound.NOTE_PLING, 2F).play(e.getPlayer());
        new SoundEffect(Sound.LEVEL_UP, 2F).play(e.getPlayer());

        /* Removing item */
        e.setCancelled(true);
        e.getItem().remove();

        /* Telling player*/
        ClansPlayer player = new ClansPlayer(e.getPlayer());
        new Title("", Text.color("&e+" + reward + " " + CurrencyHandler.CURRENCY_NAME), 1, 15, 1).send(player.getPlayer());
        player.setBalance(player.getBalance() + reward);
    }

}
