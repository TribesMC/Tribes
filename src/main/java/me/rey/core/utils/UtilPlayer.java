package me.rey.core.utils;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class UtilPlayer {
    public static boolean willFitInInventory(Player player, ItemStack stack) {
        if (player.getInventory().firstEmpty() >= 0) {
            return true;
        }

        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem == null) continue;
            if (inventoryItem.isSimilar(stack)) {
                if (inventoryItem.getMaxStackSize() >= inventoryItem.getAmount() + stack.getAmount()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int fitInInventory(Player player, ItemStack stack) {
        if (player.getInventory().firstEmpty() >= 0) {
            player.getInventory().addItem(stack);
            return 0;
        }

        int amount = stack.getAmount();

        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem == null) continue;
            if (inventoryItem.isSimilar(stack)) {
                if (inventoryItem.getMaxStackSize() >= inventoryItem.getAmount()) {
                    int incrementBy = inventoryItem.getMaxStackSize() - inventoryItem.getAmount();
                    amount -= incrementBy;
                    if (amount < 0) {
                        incrementBy -= amount * -1;
                    }
                    inventoryItem.setAmount(inventoryItem.getAmount() + incrementBy);
                    if (amount <= 0) {
                        return 0;
                    }
                }
            }
        }
        return amount;
    }

    public static boolean hasAmountOf(Player player, ItemStack stack, int amount) {
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem == null) continue;
            if (inventoryItem.isSimilar(stack)) {
                amount -= inventoryItem.getAmount();
            }
            if (amount <= 0) {
                return true;
            }
        }
        return false;
    }

    public static void removeAmountOf(Player player, ItemStack stack, int amount) {

        // spigot appears to have broken PlayerInventor#removeItem ._.

        if (amount == -1) {
            for (ItemStack inventoryItem : player.getInventory().getContents()) {
                if (inventoryItem == null) continue;
                if (inventoryItem.isSimilar(stack)) {
                    player.getInventory().removeItem(inventoryItem);
                }
            }
            return;
        }

        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem == null) continue;
            if (inventoryItem.isSimilar(stack)) {
                int itemAmount = inventoryItem.getAmount();
                int newAmount = Math.max(0, itemAmount - amount);
                amount = Math.max(0, amount - itemAmount);
                if (newAmount == 0) {
                    player.getInventory().removeItem(inventoryItem);
                } else {
                    inventoryItem.setAmount(newAmount);
                }

                if (amount <= 0) {
                    break;
                }
            }
        }
    }

    public static int countItem(Player player, ItemStack stack) {
        int amount = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem == null) continue;
            if (inventoryItem.isSimilar(stack)) {
                amount += inventoryItem.getAmount();
            }
        }
        return amount;
    }

    public static boolean onGround(LivingEntity player) {
        return player.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock().getType() != Material.AIR && player.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock().getType() != Material.WATER && player.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock().getType() != Material.STATIONARY_WATER && player.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock().getType() != Material.LAVA && player.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock().getType() != Material.STATIONARY_LAVA && !player.getLocation().getBlock().isLiquid();
    }

    public static boolean withinRegion(LivingEntity player, Region3D region) {
        double[] axis = new double[2];

        axis[0] = region.loc1.getX();
        axis[1] = region.loc2.getX();
        Arrays.sort(axis);
        if (player.getLocation().getX() > axis[1] || player.getLocation().getX() < axis[0]) return false;

        axis[0] = region.loc1.getY();
        axis[1] = region.loc2.getY();
        Arrays.sort(axis);
        if (player.getLocation().getY() > axis[1] || player.getLocation().getY() < axis[0]) return false;

        axis[0] = region.loc1.getZ();
        axis[1] = region.loc2.getZ();
        Arrays.sort(axis);
        return !(player.getLocation().getZ() > axis[1]) && !(player.getLocation().getZ() < axis[0]);
    }
}
