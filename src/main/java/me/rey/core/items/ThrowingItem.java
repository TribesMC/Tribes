package me.rey.core.items;

import me.rey.core.Warriors;
import me.rey.core.players.User;
import me.rey.core.utils.Text;
import me.rey.core.utils.UtilBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class ThrowingItem implements Listener {
    private final double resetCooldown;
    protected Map<UUID, Long> cooldowns;
    protected boolean cooldownCanceled, ignoresCooldown, canThrowInWater, canThrowWhenSilenced, canThrowWhenSlowed, canThrowInAir;
    protected boolean skipCooldownCheck;
    String name;
    me.rey.core.gui.Item item;
    Item entityitem;
    boolean pickupable = false;
    boolean fired = false;
    boolean destroy = false;
    boolean leftClick;
    String reuseMsg;
    private double cooldown;

    public ThrowingItem(final String name, final double cooldown, final me.rey.core.gui.Item item, final boolean pickupable, final boolean allowLeftClick, final boolean canThrowInWater, final boolean canThrowWhenSilenced, final boolean canThrowWhenSlowed, final boolean canThrowInAir) {
        this.reuseMsg = Text.format(name, "You can use &a%s&7.");

        this.name = name;
        this.item = item;
        if (!pickupable) {
            this.item.setName("&4NON-PICKUPABLE");
        } else {
            this.pickupable = true;
        }
        this.leftClick = allowLeftClick;


        this.cooldown = cooldown;
        this.resetCooldown = cooldown;
        this.canThrowInWater = canThrowInWater;
        this.canThrowWhenSilenced = canThrowWhenSilenced;
        this.canThrowWhenSlowed = canThrowWhenSlowed;
        this.canThrowInAir = canThrowInAir;
        this.cooldownCanceled = false;
        this.skipCooldownCheck = false;
        this.cooldowns = new HashMap<>();
    }

    public static Set<Block> checkForBlockCollision(final Item item) {

        if (item == null) {
            return null;
        }

        final Block self = item.getLocation().getBlock();

        final Block bOne = self.getRelative(BlockFace.UP);
        final Block bTwo = self.getRelative(BlockFace.DOWN);
        final Block bThree = self.getRelative(BlockFace.WEST);
        final Block bFour = self.getRelative(BlockFace.EAST);
        final Block bFive = self.getRelative(BlockFace.NORTH);
        final Block bSix = self.getRelative(BlockFace.SOUTH);
        final List<Block> list = new ArrayList<>(Arrays.asList(bOne, bTwo, bThree, bFour, bFive, bSix));
        final Set<Block> toReturn = new HashSet<>();

        for (final Block b : list) {
            if (!UtilBlock.airFoliage(b)) {
                toReturn.add(b);
            }
        }


        return toReturn.isEmpty() ? null : toReturn;
    }

    public static Set<LivingEntity> checkForEntityCollision(final Item item, final double nearbyX, final double nearbyY, final double nearbyZ, final Set<Entity> blacklist) {

        if (item == null) {
            return null;
        }

        final Iterator<Entity> nearby = item.getNearbyEntities(nearbyX, nearbyY, nearbyZ).iterator();
        final Set<LivingEntity> toReturn = new HashSet<>();

        while (nearby.hasNext()) {
            final Entity entity = nearby.next();
            if (blacklist.contains(entity)) {
                continue;
            }
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            if (entity instanceof ArmorStand) {
                continue;
            }
            toReturn.add((LivingEntity) entity);
        }

        return toReturn.isEmpty() ? null : toReturn;
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        final Action cur = e.getAction();
        final boolean allow = (cur.equals(Action.RIGHT_CLICK_AIR) || cur.equals(Action.RIGHT_CLICK_BLOCK) || (this.leftClick && (cur.equals(Action.LEFT_CLICK_AIR)
                || cur.equals(Action.LEFT_CLICK_BLOCK)))) && (e.getClickedBlock() == null || e.getClickedBlock().getType().equals(Material.AIR) ||
                !UtilBlock.usableBlocks().contains(e.getClickedBlock().getType())) && (e.getItem() != null && e.getItem().getType().equals(this.item.get().getType()));
        if (allow) {
            this.execute(new User(e.getPlayer()), e.getPlayer(), cur);
        }
    }

    public Item fire(final Location loc, final double multiplier, final double addY) {
        this.fired = true;
        final Item item;
        this.entityitem = (item = loc.getWorld().dropItem(loc, this.item.get()));
        final Vector direction = this.entityitem.getLocation().getDirection();
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(direction.getY() + addY));
        return item;
    }

    public Item fire(final Location loc, final double multiplier, final double baseY, final double addY) {
        this.fired = true;
        final Item item;
        this.entityitem = (item = loc.getWorld().dropItem(loc, this.item.get()));
        final Vector direction = this.entityitem.getLocation().getDirection();
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(baseY + addY));
        return item;
    }

    public Item fire(final Location loc, final Vector direction, final double multiplier, final double addY) {
        this.fired = true;
        final Item item;
        this.entityitem = (item = loc.getWorld().dropItem(loc, this.item.get()));
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(direction.getY() + addY));
        return item;
    }

    public Item fire(final Location loc, final Vector direction, final double multiplier, final double baseY, final double addY) {
        this.fired = true;
        final Item item;
        this.entityitem = (item = loc.getWorld().dropItem(loc, this.item.get()));
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(baseY + addY));
        return item;
    }

    public Item fire(final Location loc, final Vector v) {
        this.fired = true;
        final Item item;
        this.entityitem = (item = loc.getWorld().dropItem(loc, this.item.get()));
        this.entityitem.setVelocity(v);
        return item;
    }

    public me.rey.core.gui.Item getItem() {
        return this.item;
    }

    public void setItem(final me.rey.core.gui.Item item) {
        this.item = item;
    }

    public Item getEntityitem() {
        return this.entityitem;
    }

    public void setEntityitem(final Item entityitem) {
        this.entityitem = entityitem;
    }

    public boolean isPickupable() {
        return this.pickupable;
    }

    public void setPickupable(final boolean pickupable) {
        this.pickupable = pickupable;
    }

    public void drop(final Location loc, final boolean naturally) {
        if (!naturally) {
            this.entityitem = loc.getWorld().dropItem(loc, this.getItem().get());
        } else {
            this.entityitem = loc.getWorld().dropItemNaturally(loc, this.getItem().get());
        }
    }

    public void destroyWhenOnGround() {
        if (this.entityitem.isOnGround()) {
            this.destroy();
        }
    }

    public void destroy() {
        this.destroy = true;
        this.entityitem.remove();
    }

    @EventHandler
    public void onPickup(final PlayerPickupItemEvent e) {
        final Item i = e.getItem();
        final ItemStack is = i.getItemStack();

        if (is == null || !is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) {
            return;
        }

        final ItemMeta im = is.getItemMeta();

        if (im.getDisplayName().equals(this.item.getName()) && !this.pickupable) {
            e.setCancelled(true);
        }

    }

    protected abstract void execute(User u, Player p, Object... conditions);

    public void applyCooldown(final Player p) {
        if (!this.ignoresCooldown && !this.cooldownCanceled) {
            this.cooldowns.put(p.getUniqueId(), System.currentTimeMillis() + ((long) this.cooldown * 1000));

            new BukkitRunnable() {
                @Override
                public void run() {
                    ThrowingItem.this.cooldowns.remove(p.getUniqueId());
                }
            }.runTaskLater(Warriors.getInstance().getPlugin(), (long) this.cooldown * 20);
        }

        this.resetCooldown();
        this.setCooldownCanceled(false);
    }

    public void setWhileSilenced(final boolean silence) {
        this.canThrowWhenSilenced = silence;
    }

    public void setIgnoresCooldown(final boolean ignore) {
        this.ignoresCooldown = ignore;
    }

    private void resetCooldown() {
        this.cooldown = this.resetCooldown;
    }

    public String getName() {
        return this.name;
    }

    public double getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(final double time) {
        this.cooldown = time;
    }

    public void sendEnergyError(final Player p) {
        new User(p).sendMessageWithPrefix("Error", String.format("You don't have enough energy to use &a%s&7!", this.name));
    }

    public boolean sendUsedMessageToPlayer(final Player p, final String name) {
        new User(p).sendMessageWithPrefix(this.getName(), "You used &a" + name + "&7.");
        return true;
    }

    public boolean sendReadyMessageToPlayer(final Player p, final String name) {
        new User(p).sendMessage(this.reuseMsg.replace("%s", name));
        return true;
    }

    public boolean sendAbilityMessage(final LivingEntity p, final String text) {
        if (text == null) {
            return false;
        }
        p.sendMessage(Text.format(this.name, text));
        return true;
    }

    public void setInLiquid(final boolean inLiquid) {
        this.canThrowInWater = inLiquid;
    }

    public void setWhileSlowed(final boolean whileSlowed) {
        this.canThrowWhenSlowed = whileSlowed;
    }

    public void setWhileInAir(final boolean inAir) {
        this.canThrowInAir = inAir;
    }

    public void setCooldownCanceled(final boolean canceled) {
        this.cooldownCanceled = canceled;
    }

    public void setSkipCooldownCheck(final boolean canceled) {
        this.skipCooldownCheck = canceled;
    }

    protected void sendCooldownMessage(final Player p) {
        new User(p).sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 for &a" + (((this.cooldowns.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000) + 1) + " &7seconds.");
    }
}
