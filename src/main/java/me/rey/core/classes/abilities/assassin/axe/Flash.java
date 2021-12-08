package me.rey.core.classes.abilities.assassin.axe;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.gui.Item;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.utils.Text;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class Flash extends Ability implements IConstant {

    private static final String NOT_CHARGED = "○", CHARGING = "◎", CHARGED = "●";
    private static final int cooldownUntilNewCharge = 100, // 20 ticks * 5 seconds = 100 ticks (per flash recharge)
            range = 6;

    private Map<UUID, Integer> charges = new HashMap<>();
    private Map<UUID, Integer> cd = new HashMap<>();

    private List<UUID> ghostdelay = new ArrayList<>();

    public Flash() {
        super(12, "Flash", ClassType.LEATHER, AbilityType.AXE, 1, 4, 0, Arrays.asList(
                "Flash forwards 6 Blocks.",
                "Store up to <variable>1 + l</variable> Flash Charges.",
                "Cannot be used while Slowed."
        ));

        setIgnoresCooldown(true);
        setWhileSlowed(false);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
    	
    	/*
    	 * CHARGE REGENERATION
    	 */
    	if(conditions != null && conditions.length == 1 && conditions[0] != null && conditions[0] instanceof UpdateEvent) {
    		
            int maxCharges = level + 1;
            checkInFlashList(p);

            if(charges.get(p.getUniqueId()) < maxCharges) {
                if(cd.get(p.getUniqueId()) >= 5) {
                    ghostdelay.remove(p.getUniqueId());
                }

                if (cd.get(p.getUniqueId()) >= cooldownUntilNewCharge) {
                    addCharge(p);
                    setCD(p, 0);

                    sendAbilityMessage(p, "Charges: " + ChatColor.YELLOW + charges.get(p.getUniqueId()) /* + " " + ChatColor.GREEN + "(+1)" */);
                } else {
                    setCD(p, cd.get(p.getUniqueId()) + 1);
                }
            } 
            
            // Capping people's charges
            while(this.charges.get(p.getUniqueId()) > maxCharges) {
            	removeCharge(p);
            }
            
            
            // Action Bar
            if(this.matchesAbilityTool(this.match(p.getItemInHand() == null ? new Item(Material.AIR).get() : p.getItemInHand()))) {
	            String chargeList = "";
	            int flashCount = charges.get(p.getUniqueId());
	            
	            for(int i = 0; i < flashCount; i++) chargeList += ChatColor.GREEN + CHARGED + " ";
	            if(flashCount < maxCharges) chargeList += ChatColor.YELLOW + CHARGING + " ";
	            for(int i = 0; i < (maxCharges - flashCount - 1); i++) chargeList += ChatColor.RED + NOT_CHARGED + " ";
	            
	            new ActionBar(Text.color("&f&lFlash Charges " + chargeList)).send(p);;
            }
       
    		return false;
    	}
    	//END
    	

        if(charges.get(p.getUniqueId()) <= 0 || ghostdelay.contains(p.getUniqueId())) return false;

        Block b = null;

        if(isSafeForTeleport(p.getLocation())) { /* TODO Temporary Condition Solution for non-cubic blocks */

            if (!UtilLoc.atBlockGap(p, p.getLocation().getBlock()) && !UtilLoc.atBlockGap(p, UtilBlock.getBlockAbove(p.getLocation().getBlock()))) {
                for (double i = 1; i < range; i += 0.5D) {

                    if(i >= 8D) break;

                    if (UtilLoc.atBlockGap(p, UtilBlock.getTargetBlock(p, i)) || UtilLoc.atBlockGap(p, UtilBlock.getBlockAbove(UtilBlock.getTargetBlock(p, i)))) {
                        b = UtilBlock.getTargetBlock(p, i - 1);
                        break;
                    }

                    if (isSafeForTeleport(UtilBlock.getTargetBlock(p, i).getLocation())) {
                        b = UtilBlock.getTargetBlock(p, i);
                    } else {
                        break;
                    }
                }
            }
        }

        Location loc = null;

        if(b != null) {
            loc = b.getLocation();
            loc.add(0.5 , -1, 0.5);

            loc.setYaw(p.getLocation().getYaw());
            loc.setPitch(p.getLocation().getPitch());
        }

        if(loc != null) {
            Location tpLoc = null;

            for (int i = 1; i <= range; i += 1) {

                if (isSafeForTeleport(p.getTargetBlock((Set<Material>) null, i).getLocation())) {

                    Block tb = p.getTargetBlock((Set<Material>) null, i);
                    float dir = (float) Math.toDegrees(Math.atan2(p.getLocation().getBlockX() - tb.getX(), tb.getZ() - p.getLocation().getBlockZ()));
                    BlockFace face = UtilBlock.getClosestFace(dir);

                    if (face == BlockFace.NORTH || face == BlockFace.EAST || face == BlockFace.SOUTH || face == BlockFace.WEST) {
                        Location tloc = tb.getLocation();

                        if (face == BlockFace.NORTH) {
                            tloc.setX(tloc.getX() + 1.35);
                            tloc.setZ(tloc.getZ() + 0.5);
                        }

                        if (face == BlockFace.EAST) {
                            tloc.setZ(tloc.getZ() + 1.35);
                            tloc.setX(tloc.getX() + 0.5);
                        }

                        if (face == BlockFace.SOUTH) {
                            tloc.setX(tloc.getX() - 0.35);
                            tloc.setZ(tloc.getZ() + 0.5);
                        }

                        if (face == BlockFace.WEST) {
                            tloc.setZ(tloc.getZ() - 0.35);
                            tloc.setX(tloc.getX() + 0.5);
                        }

                        tloc.setY(loc.getY());

                        if(loc.getPitch() >= 5.7)
                            tloc.add(0, +1, 0);

                        tloc.setYaw(p.getLocation().getYaw());
                        tloc.setPitch(p.getLocation().getPitch());

                        if (isSafeForTeleport(tloc)) {
                            tpLoc = tloc;
                        } else {
                            break;
                        }
                    }
                }
            }

            if(tpLoc != null) {
                makeParticlesBetween(p.getLocation(), tpLoc);
                p.teleport(tpLoc);
            }

        }

        p.setFallDistance(0);

        p.getWorld().playSound(p.getLocation(), Sound.WITHER_SHOOT, 0.4f, 1.2f);
        p.getWorld().playSound(p.getLocation(), Sound.SILVERFISH_KILL, 1f, 1.6f);

        ghostdelay.add(p.getUniqueId());
        removeCharge(p);

        sendAbilityMessage(p, "Charges: " + ChatColor.YELLOW + charges.get(p.getUniqueId()) /*+ " " + ChatColor.RED + "(-1)" */);

        return true;

    }

    public void checkInFlashList(Player p) {
        if(!cd.containsKey(p.getUniqueId()) || !charges.containsKey(p.getUniqueId())) {
            cd.remove(p.getUniqueId());
            charges.remove(p.getUniqueId());

            cd.put(p.getUniqueId(), 0);
            charges.put(p.getUniqueId(), 0);
        }
    }

    public void addCharge(Player p) {
        charges.replace(p.getUniqueId(), charges.get(p.getUniqueId()) + 1);
    }

    public void removeCharge(Player p) {
        charges.replace(p.getUniqueId(), charges.get(p.getUniqueId()) - 1);
    }

    public void setCD(Player p, int cooldown) {
        cd.replace(p.getUniqueId(), cooldown);
    }

    private void makeParticlesBetween(Location init, Location loc) {
        Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
        for(double i = 1; i <= init.distance(loc); i += 0.2) {

            if(init.distance(loc) >= 8) break;

            pvector.multiply(i);
            init.add(pvector);
            Location toSpawn = init.clone();
            toSpawn.setY(toSpawn.getY() + 0.5);
            init.getWorld().spigot().playEffect(toSpawn, Effect.FIREWORKS_SPARK, 0, 0, 0F, 0F, 0F, 0F, 5, 50);
            init.subtract(pvector);
            pvector.normalize();
        }
    }

    private boolean isSafeForTeleport(Location loc) {
        Block blockunder = loc.getBlock().getRelative(BlockFace.DOWN);
        Block blockabove = loc.getBlock().getRelative(BlockFace.UP);

        if(!loc.getBlock().getType().isSolid() && !blockabove.getType().isSolid() && !isLiftingBlock(blockunder)) {
            return true;
        }

        if(!loc.getBlock().getType().isSolid() && !blockunder.getType().isSolid() && !isLiftingBlock(blockabove)) {
            return true;
        }

        return false;
    }

    /*
    private Location returnSafeTeleportLoc(Location init, Location loc) {
        Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
        Location safeloc = null;
        for(double i = 1; i <= init.distance(loc); i += 0.2) {
            pvector.multiply(i);
            init.add(pvector);
            Location l = init.clone();

            if(isSafeForTeleport(l))
                safeloc = l;

        }

        return safeloc;
    }
     */

    private boolean isLiftingBlock(Block b) {
        return b.getType().toString().contains("WALL")
                || b.getType().toString().contains("FENCE");
    }

}
