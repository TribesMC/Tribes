package me.rey.core.players;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.commands.CommandType;
import me.rey.core.database.SQLManager;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.ability.PlayerBuildEditEvent;
import me.rey.core.events.customevents.ability.PlayerBuildSelectEvent;
import me.rey.core.events.customevents.team.TeamProcessEvent;
import me.rey.core.events.customevents.update.EnergyConsumeEvent;
import me.rey.core.players.combat.PlayerHitCache;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.Build.BuildSet;
import me.rey.core.utils.ItemUtils;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class User {

    private final Player player;
    private final Warriors plugin = Warriors.getInstance();
    private final SQLManager sql;
    private final EnergyHandler energyHandler = new EnergyHandler();
    private final PlayerHitCache cache = Warriors.getInstance().getHitCache();

    public User(final Player player) {
        this.player = player;

        this.sql = this.plugin.getSQLManager();
    }

    public Player getPlayer() {
        return this.player;
    }

    public Set<Player> getTeam() {
        final TeamProcessEvent event = new TeamProcessEvent(this.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(event);

        event.addTeammate(this.getPlayer());
        return event.getTeammates();
    }

    public void resetCombatTimer() {
        this.cache.startCombatTimer(this.getPlayer());
    }

    public void stopCombatTimer() {
        this.cache.clearPlayerCache(this.getPlayer());
        this.cache.stopCombatTimer(this.getPlayer());
    }

    public UUID getUniqueId() {
        return this.getPlayer().getUniqueId();
    }

    public boolean isInCombat() {
        return this.cache.hasCombatTimer(this.getPlayer());
    }

    public double getEnergy() {
        return this.energyHandler.getUserEnergy(this.getUniqueId());
    }

    public void consumeEnergy(final double energy) {
        final EnergyConsumeEvent event = new EnergyConsumeEvent(energy, this.player);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            final double toSet = this.getEnergy() - energy;
            this.energyHandler.setEnergy(this.getUniqueId(), toSet);

            if (energy <= 0) {
                return;
            }
            this.energyHandler.togglePauseEnergy(State.ENABLED, this.player.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    User.this.energyHandler.togglePauseEnergy(State.DISABLED, User.this.player.getUniqueId());
                }
            }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (20L * 2.5));
        }
    }

    public void addEnergy(final double energy) {
        final double toSet = this.getEnergy() + energy;
        this.energyHandler.setEnergy(this.getUniqueId(), toSet);
    }

    public float getEnergyExp() {
        return Math.min(0.999F, ((float) EnergyHandler.MAX_ENERGY * (float) this.getEnergy() / (float) this.energyHandler.getCapacity(this.getUniqueId())) / (float) EnergyHandler.MAX_ENERGY);
    }

    public boolean isUsingAbility(final Ability ability) {
        if (this.getWearingClass() != null && this.getSelectedBuild(this.getWearingClass()) != null
                && this.getSelectedBuild(this.getWearingClass()).getAbility(ability.getAbilityType()) == ability) {
            return true;
        }
        return false;
    }

    public boolean hasPotionEffect(final PotionEffectType effect) {
        if (this.player.getActivePotionEffects().isEmpty()) {
            return false;
        }

        for (final PotionEffect e : this.player.getActivePotionEffects()) {
            if (e.getType().equals(effect)) {
                return true;
            }
        }

        return false;
    }

    public User resetEffects() {

        final List<PotionEffectType> activeEffects = new ArrayList<>();
        for (final PotionEffect active : this.getPlayer().getActivePotionEffects()) {
            activeEffects.add(active.getType());
        }

        if (!activeEffects.isEmpty()) {
            for (final ClassType classType : ClassType.values()) {
                if (classType.getEffects().length == 0) {
                    continue;
                }

                for (final PotionEffect effect : classType.getEffects()) {
                    if (activeEffects.contains(effect.getType())) {
                        this.getPlayer().removePotionEffect(effect.getType());
                    }
                }
            }
        }

        this.updateClassEffects();
        return this;
    }

    public User updateClassEffects() {

        if (this.getWearingClass() == null || this.getWearingClass().getEffects() == null) {
            return this;
        }

        final List<PotionEffectType> activeEffects = new ArrayList<>();
        for (final PotionEffect active : this.getPlayer().getActivePotionEffects()) {
            activeEffects.add(active.getType());
        }

        for (final PotionEffect effect : this.getWearingClass().getEffects()) {
            if (activeEffects.contains(effect.getType())) {
                continue;
            }
            this.getPlayer().addPotionEffect(effect);
        }
        return this;
    }

    public void sendMessageWithPrefix(final String prefix, final String message) {
        this.getPlayer().sendMessage(Text.format(prefix, message));
    }

    public void sendMessageWithPrefix(final CommandType commandType, final String message) {
        this.sendMessageWithPrefix(commandType.getName(), message);
    }

    public void sendMessage(final String message) {
        this.getPlayer().sendMessage(Text.color(message));
    }

    public ClassType getWearingClass() {
        final PlayerInventory inventory = this.getPlayer().getInventory();

        for (final ClassType classType : ClassType.values()) {
            try {

                if (!ItemUtils.compareLeatherArmor(inventory.getHelmet(), classType.getHelmet().get())) {
                    continue;
                }
                if (!ItemUtils.compareLeatherArmor(inventory.getChestplate(), classType.getChestplate().get())) {
                    continue;
                }
                if (!ItemUtils.compareLeatherArmor(inventory.getLeggings(), classType.getLeggings().get())) {
                    continue;
                }
                if (!ItemUtils.compareLeatherArmor(inventory.getBoots(), classType.getBoots().get())) {
                    continue;
                }
                return classType;

            } catch (final NullPointerException e) {
                return null;
            }
        }

        return null;
    }

    public BuildSet getBuilds(final ClassType classType) {
        final BuildSet b = Warriors.buildCache.containsKey(this.getUniqueId()) && Warriors.buildCache.get(this.getUniqueId()).containsKey(classType)
                ? new BuildSet(Warriors.buildCache.get(this.getUniqueId()).get(classType)) : new BuildSet();
        return b;
    }

//	public Build getSelectedBuild(ClassType classType) {
//		if(Warriors.buildCache.containsKey(this.getPlayer())) {
//			for(ClassType query : Warriors.buildCache.get(this.getPlayer()).keySet()) {
//				if(query == classType) return Warriors.buildCache.get(this.getPlayer()).get(query);
//			}
//		}
//		
//		return null;
//	}

    public Build getSelectedBuild(final ClassType classType) {
        for (final Build b : this.getBuilds(classType)) {
            if (b.getCurrentState()) {
                return b;
            }
        }

        return null;
    }


    /*
     * Build selection
     */
    public void selectBuild(final Build build, final ClassType classType, final boolean message) {
        Build before = null;

        for (final Build b : this.sql.getPlayerBuilds(this.getUniqueId(), classType)) {
            if (b.getCurrentState()) {
                before = b;
            }
            b.setCurrentState(false);
            this.sql.saveBuild(this.player.getUniqueId(), b, classType);
        }

        if (build != null) {
            build.setCurrentState(true);
            this.sql.saveBuild(this.player.getUniqueId(), build, classType);
        }

        final PlayerBuildSelectEvent event = new PlayerBuildSelectEvent(this.player, before, build);
        Bukkit.getServer().getPluginManager().callEvent(event);

        this.saveBuildsInCache();
        if (message) {
            this.sendBuildEquippedMessage(classType);
        }

    }

    public void selectBuild(final Build build, final ClassType classType) {
        this.selectBuild(build, classType, true);
    }

    public void sendBuildEquippedMessage(final ClassType classType) {
        if (this.getWearingClass() == null || !this.getWearingClass().equals(classType)) {
            return;
        }

        final Build build = this.getSelectedBuild(classType);
        final Build query = build == null ? classType.getDefaultBuild() : build;

        this.sendListingClassSkills(classType);

        this.sendMessageWithPrefix("Class", "You equipped &a" + query.getName() + "&7.");
    }

    public void sendListingClassSkills(final ClassType classType) {
        this.sendMessageWithPrefix("Skill", "Listing Class Skills:");
        if (this.getWearingClass() == null || !this.getWearingClass().equals(classType)) {
            return;
        }

        final Build build = this.getSelectedBuild(classType);
        final Build query = build == null ? classType.getDefaultBuild() : build;

        for (final AbilityType abilityType : AbilityType.values()) {
            if (query.getAbility(abilityType) == null) {
                continue;
            }

            this.sendMessage(String.format("&2%s:&r&f %s Lvl%s",
                    abilityType.getName(),
                    query.getAbility(abilityType).getName(),
                    query.getAbilityLevel(abilityType)
            ));
        }
    }

    public void editBuild(final Build old, final Build newBuild, final ClassType classType) {
        for (Build b : this.sql.getPlayerBuilds(this.getUniqueId(), classType)) {
            if (b.getUniqueId().toString().trim().equals(old.getUniqueId().toString().trim())) {
                final PlayerBuildEditEvent event = new PlayerBuildEditEvent(this.player, b, newBuild);
                Bukkit.getServer().getPluginManager().callEvent(event);

                b = newBuild;
                this.sql.deletePlayerBuild(this.getPlayer().getUniqueId(), b, classType);
                this.sql.createPlayerBuild(this.getPlayer().getUniqueId(), b, classType);


            }
        }

        this.saveBuildsInCache();
    }

    private void saveBuildsInCache() {
        if (!Warriors.buildCache.containsKey(this.getUniqueId())) {
            Warriors.buildCache.put(this.getUniqueId(), new HashMap<>());
        }

        for (final ClassType type : ClassType.values()) {
            if (!Warriors.buildCache.get(this.getUniqueId()).containsKey(type)) {
                Warriors.buildCache.get(this.getUniqueId()).put(type, new BuildSet().getArray());
            }

            final HashMap<ClassType, Build[]> builds = Warriors.buildCache.get(this.getUniqueId());

            builds.replace(type, this.sql.getPlayerBuilds(this.getUniqueId(), type).getArray());
            Warriors.buildCache.replace(this.getUniqueId(), builds);
        }
    }

}
