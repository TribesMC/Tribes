package me.rey.clans.features;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ServerClan;
import me.rey.clans.currency.PickupBalance;
import me.rey.clans.events.clans.PlayerEditClaimEvent;
import me.rey.clans.events.clans.PlayerTerritoryChangeEvent;
import me.rey.clans.utils.References;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.gui.Item;
import me.rey.core.utils.UtilMath;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Fields implements Listener {

    public static ArrayList<FieldsOre> fieldsOres = new ArrayList<>();

    /**
     * Replacing drops for certain ores to other items.
     * <p>
     * Diamond Ore - drops diamonds
     * Moss Stone (retextured) - drops titanium ingot (slimeball retextured)
     * Gold Ore - drops gold
     * Iron Ore - drops smelted iron
     * Redstone Ore (retextured) - drops leather
     * Lapis Ore (retextured) - drops kevlar (nether brick retextured)
     * Emerald Ore - drops emeralds
     * Lapis Blocks - drops lapis blocks (for water) (reduced spawn rate)
     * Gold Blocks - drops currency (500-1000)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOreBreak(final PlayerEditClaimEvent e) {
        if (!e.getAction().equals(PlayerEditClaimEvent.EditAction.BREAK)) {
            return;
        }
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        /**
         *
         *  All items with their max drop count
         *  ITEM | MAX COUNT with MINIMUM of 1
         *
         */
        final HashMap<Item, Integer> drops = new HashMap<>();

        final Block block = e.getBlockToReplace();
        final Clan clan = Tribes.getInstance().getClanFromTerritory(block.getChunk());
        final boolean isFields = clan != null && clan.isServerClan() && clan.compare(ServerClan.FIELDS.getClan());

        switch (block.getType()) {

            case DIAMOND_ORE:
                drops.put(new Item(Material.DIAMOND), isFields ? 5 : 1);
                break;

            case MOSSY_COBBLESTONE:
                drops.put(new Item(Material.SLIME_BALL), isFields ? 5 : 1);
                break;

            case GOLD_ORE:
                drops.put(isFields ? new Item(Material.GOLD_INGOT) : new Item(Material.GOLD_ORE), isFields ? 5 : 1);
                break;

            case IRON_ORE:
                drops.put(isFields ? new Item(Material.IRON_INGOT) : new Item(Material.IRON_ORE), isFields ? 5 : 1);
                break;

            case GLOWING_REDSTONE_ORE:
            case REDSTONE_ORE:
                drops.put(new Item(Material.LEATHER), isFields ? 5 : 1);
                break;

            case LAPIS_ORE:
                drops.put(new Item(Material.NETHER_BRICK_ITEM), isFields ? 5 : 1);
                break;

            case EMERALD_ORE:
                drops.put(new Item(Material.EMERALD), isFields ? 5 : 1);
                break;

            case LAPIS_BLOCK:
                drops.put(new Item(Material.LAPIS_BLOCK), isFields ? 3 : 1);
                break;

            case GOLD_BLOCK:
                if (isFields) {
                    drops.put(PickupBalance.getPickupCurrency(500, 1000), 1);
                } else {
                    drops.put(new Item(Material.GOLD_BLOCK), 1);
                }
                break;

            case COAL_ORE:
                drops.put(new Item(Material.COAL), isFields ? 10 : 1);
                break;
        }

        if (isFields && !drops.isEmpty()) {
            e.setPermission(PlayerEditClaimEvent.ClaimPermission.ALLOW);
        }
        if (!e.getPermission().equals(PlayerEditClaimEvent.ClaimPermission.ALLOW)) {
            return;
        }

        /*
         * Handling permissions and fields scheduler
         */
        if (!drops.isEmpty()) {
            e.setSendPermissionMessage(false);
            e.setPermission(PlayerEditClaimEvent.ClaimPermission.DENY);

            if (isFields) {

                for (final FieldsOre ore : fieldsOres) {
                    if (ore.getBlock().getLocation().equals(block.getLocation())) {
                        return;
                    }
                }

                final FieldsOre ore = new FieldsOre(block);
                ore.scheduleReplacement();

                fieldsOres.add(ore);
                block.setType(Material.STONE);

            } else {
                block.setType(Material.AIR);
            }

            new ParticleEffect(Effect.TILE_BREAK).setData(block.getData()).play(block.getLocation().add(0.5, 0.5, 0.5));
        }

        for (final Map.Entry<Item, Integer> entry : drops.entrySet()) {
            final Item item = entry.getKey();
            final int maxDrop = entry.getValue();

            int toDrop = Math.max(maxDrop, 1);
            if (maxDrop > 1) {
                toDrop = UtilMath.randBetween(1, maxDrop);
            }

            item.setAmount(toDrop);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), item.get());
        }

    }


    /*

     FIELDS

      */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnterFields(final PlayerTerritoryChangeEvent e) {

        if (e.getTo() != null && e.getTo().compare(ServerClan.FIELDS.getClan())) {
            if (e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
                e.getPlayer().setGameMode(GameMode.SURVIVAL);
            }
        }

    }

    @EventHandler
    public void onEntityChangeBlockEvent(final EntityChangeBlockEvent e) {
        if (e.getBlock().getType().equals(Material.REDSTONE_ORE)
                || e.getBlock().getType().equals(Material.GLOWING_REDSTONE_ORE)) {
            e.setCancelled(true);
        }

        if (e.getTo().equals(Material.GLOWING_REDSTONE_ORE)) {
            e.setCancelled(true);
        }
    }

    public static class FieldsOre {

        final Block block;
        final Material replacement;

        BukkitTask task = null;

        public FieldsOre(final Block block) {
            this.block = block;
            this.replacement = block.getType();
        }

        public void scheduleReplacement() {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    FieldsOre.this.replaceForcefully();
                    Fields.fieldsOres.remove(FieldsOre.this);
                }
            }.runTaskLater(Tribes.getInstance().getPlugin(), (int) (References.FIELDS_ORE_COOLDOWN_SECONDS * 20));
        }

        public void replaceForcefully() {
            this.block.setType(this.replacement);

            if (this.task != null) {
                this.task.cancel();
            }
            this.task = null;
        }

        public Block getBlock() {
            return this.block;
        }

        public Material getOre() {
            return this.replacement;
        }
    }

}