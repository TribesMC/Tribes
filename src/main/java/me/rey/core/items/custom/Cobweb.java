package me.rey.core.items.custom;

import me.rey.core.Warriors;
import me.rey.core.items.ThrowingItem;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Cobweb extends ThrowingItem {

    public Cobweb() {
        super("Cobweb", 2.5, new me.rey.core.gui.Item(Material.WEB), false, true, true, true, true, true);
    }

    @Override
    protected void execute(final User u, final Player p, final Object... conditions) {
        if (conditions[0] != Action.LEFT_CLICK_AIR && conditions[0] != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (this.cooldowns.containsKey(p.getUniqueId())) {
            this.sendCooldownMessage(p);
            return;
        }

        final Vector vec = (p.getLocation().getDirection().normalize()
                .multiply(0.5)
                .setY(p.getLocation().getDirection().getY() + 0.2));
        final Item item = this.fire(p.getEyeLocation(), vec);
        this.applyCooldown(p);

        if (p.getItemInHand().getType() != Material.WEB) {
            for (final ItemStack stack : p.getInventory().getContents()) {
                if (stack.getType() == Material.WEB) {
                    if (stack.getAmount() == 1) {
                        p.getInventory().remove(stack);
                    } else {
                        stack.setAmount(stack.getAmount() - 1);
                    }
                    break;
                }
            }
        } else {
            if (p.getItemInHand().getAmount() == 1) {
                p.getInventory().remove(p.getItemInHand());
            } else {
                p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
            }
        }

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                final boolean direct = checkForEntityCollision(item, 0.5, 0.5, 0.5, new HashSet<>(Collections.singletonList(p))) != null;

                if (direct) {
                    final Set<LivingEntity> ents = checkForEntityCollision(item, 0.5, 0.5, 0.5, new HashSet<>(Collections.singletonList(p)));
                    final LivingEntity target = (LivingEntity) Cobweb.this.getRandom(ents);
                    if (target != null) {
                        Location location;
                        if (target.getWorld().getBlockAt(location = target.getLocation()).getType() == Material.AIR) {
                            final Location finalLocation1 = location;
                            Bukkit.getScheduler().runTaskAsynchronously(Warriors.getInstance().getPlugin(), () -> Cobweb.this.putWeb(finalLocation1));
                            Cobweb.this.destroy();
                            p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1.5F);
                            this.cancel();
                        } else if (target.getWorld().getBlockAt(location = target.getLocation().add(0, 1, 0)).getType() == Material.AIR) {
                            final Location finalLocation = location;
                            Bukkit.getScheduler().runTaskAsynchronously(Warriors.getInstance().getPlugin(), () -> Cobweb.this.putWeb(finalLocation));
                            Cobweb.this.destroy();
                            p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1.5F);
                            this.cancel();
                        }
                    }
                } else {
                    if (checkForBlockCollision(item) != null) {
                        final Block block = item.getWorld().getBlockAt(item.getLocation());
                        Bukkit.getScheduler().runTaskAsynchronously(Warriors.getInstance().getPlugin(), () -> Cobweb.this.putWeb(block.getLocation()));
                        Cobweb.this.destroy();
                        this.cancel();
                    }
                }

                if (this.ticks > 34) {
                    final Block block = item.getWorld().getBlockAt(item.getLocation());
                    Bukkit.getScheduler().runTaskAsynchronously(Warriors.getInstance().getPlugin(), () -> Cobweb.this.putWeb(block.getLocation()));
                    Cobweb.this.destroy();
                    this.cancel();
                }

                this.ticks++;
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);
    }

    private Object getRandom(final Set<?> objs) {
        final int item = new Random().nextInt(objs.size());
        int i = 0;
        for (final Object obj : objs) {
            if (i == item) {
                return obj;
            }
            i++;
        }
        return null;
    }

    private List<Block> relatives(final Block block) {
        final List<Block> blocks = new ArrayList<>();
        for (final BlockFace face : BlockFace.values()) {
            blocks.add(block.getRelative(face));
        }
        return blocks;
    }

    private void putWeb(final Location loc) {
        Bukkit.getScheduler().runTask(Warriors.getInstance().getPlugin(), () -> {
            if (loc.getWorld().getBlockAt(loc).getType() == Material.AIR) {
                loc.getWorld().getBlockAt(loc).setType(Material.WEB);
                Bukkit.getScheduler().runTaskLater(Warriors.getInstance().getPlugin(), () -> {
                    if (loc.getWorld().getBlockAt(loc).getType() != Material.AIR) {
                        loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                    }
                }, 60);
            }
        });
    }
}
