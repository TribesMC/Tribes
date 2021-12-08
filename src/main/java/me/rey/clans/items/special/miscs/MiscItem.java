package me.rey.clans.items.special.miscs;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.clans.items.special.SpecialItem;
import me.rey.clans.items.special.SpecialItemUpdater;
import me.rey.core.utils.UtilItem;
import me.rey.core.utils.UtilTool;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class MiscItem implements Listener, SpecialItem {

    protected Tribes plugin;
    protected String id;
    protected MiscType misc;
    protected ClansTool tool, disabledTool;
    protected SpecialItemUpdater specialItemUpdater;

    public MiscItem(final Tribes plugin, final String id, final MiscType misc, final ClansTool tool, final ClansTool disabledTool) {
        this.plugin = plugin;
        this.id = id;
        this.misc = misc;
        this.tool = tool;
        this.disabledTool = disabledTool;
        Bukkit.getPluginManager().registerEvents(this, plugin.getPlugin());
        this.specialItemUpdater = plugin.getSpecialItemUpdater();
    }

    public boolean isOfArchetype(final ItemStack stack, final MiscType type) {
        if (stack == null) {
            return false;
        }
        if (stack.getType() != type.getTool().stack.getType()) {
            return false;
        }
        final String archetype = (String) UtilTool.getNBTTag(stack, "LegendArchetype", UtilTool.NBTTagType.STRING);
        if (archetype == null) {
            return false;
        }
        return type.archetype.equals(archetype);
    }

    public abstract void update();

    public abstract void serverShutdown();

    public MiscType getMiscType() {
        return this.misc;
    }

    public Pair<ClansTool, ClansTool> getItems() {
        return new ImmutablePair<>(this.tool, this.disabledTool);
    }

    @Override
    public String getItemId() {
        return this.id;
    }

    @Override
    public String getName() {
        return ChatColor.stripColor(this.getDisplayName());
    }

    @Override
    public String getDisplayName() {
        final ItemStack item = this.misc.getTool().stack;
        return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : UtilItem.getItemByMaterial(item.getType()).name;
    }

    @Override
    public ItemStack getItemStack() {
        return this.misc.getTool().stack;
    }

    @Override
    public Class<? extends SpecialItem> getType() {
        return this.getClass();
    }

    public enum MiscType {
        MUSHROOM_SOUP("SOUP", "c291dXV1dXV1dXV1dXV1dXV1dXV1dXV1dXV1dXV1dXV1dXV1cA=="),
        WATER_BOTTLE("BOTTLE", "bW1tbW0gdmVyeSByZWZyZXNoaW5n"),
        TNT("TNT", "dWggb2gsIGV4cGxvZGV5"),
        TNT_GENERATOR("TNT_GENERATOR", "dWggb2ggeDIsIGV4cGxvZGV5IGV4cGxvZGV5IG1ha2Vy"),
        BLESSED_BEACON("BLESSED_BEACON", "bW1tbW1tbW1tbSBzcGlubnkgYmVhY29u"),
        DEBUG_STICK("DEBUG_STICK", "b29wIGhvdyBleGNpdGluZywgaXQncyB0aGUgc3R1ZmZpc2Jyb2tlbiBzdGljayE="),
        DEBUG_WEAPON("DEBUG_WEAPON", "dGhlIGJpbGxpb24gZGFtYWdlIHN0aWNrIHRoYXQncyBzdXBwb3NlZCB0byBpbnN0YS1raWxsIHN0dWZm");

        public String toolString, archetype;

        MiscType(final String tool, final String securityString) {
            this.toolString = tool;
            this.archetype = securityString;
        }

        public ClansTool getTool() {
            return ClansTool.valueOf(this.toolString);
        }
    }
}