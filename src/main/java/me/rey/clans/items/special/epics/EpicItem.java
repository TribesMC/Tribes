package me.rey.clans.items.special.epics;

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

public abstract class EpicItem implements Listener, SpecialItem {

    protected Tribes plugin;
    protected String id;
    protected EpicType epic;
    protected ClansTool tool, disabledTool;

    protected SpecialItemUpdater specialItemUpdater;

    public EpicItem(final Tribes plugin, final String id, final EpicType epic, final ClansTool tool, final ClansTool disabledTool) {
        this.plugin = plugin;
        this.id = id;
        this.epic = epic;
        this.tool = tool;
        this.disabledTool = disabledTool;
        Bukkit.getPluginManager().registerEvents(this, plugin.getPlugin());
        this.specialItemUpdater = plugin.getSpecialItemUpdater();
    }

    public boolean isOfArchetype(final ItemStack stack, final EpicType type) {
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

    public EpicType getEpicType() {
        return this.epic;
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
        final ItemStack item = this.epic.getTool().stack;
        return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : UtilItem.getItemByMaterial(item.getType()).name;
    }

    @Override
    public ItemStack getItemStack() {
        return this.epic.getTool().stack;
    }

    @Override
    public Class<? extends SpecialItem> getType() {
        return this.getClass();
    }

    public enum EpicType {
        RUNED_PICKAXE("RUNED_PICKAXE", "cGlja2F4ZXkgcGlja2F4ZSB3aGljaCBtaW5lcyB0aGUgYmxvY3MgdmVyeSBxdWlj"),
        MYSTIC_HOE("MYSTIC_HOE", "YSBob2UgdGhhdCBncm93cyB0aGUgc3R1ZmYsIHdvd3plcnM="),
        PRIMORDIAL_AXE("PRIMORDIAL_AXE", "d3RmIGlzIHRoaXMgaGVja2luIGF4ZSwgaXQgZG9lcyBheGV5IHRoaW5ncw==");

        public String toolString, archetype;

        EpicType(final String tool, final String securityString) {
            this.toolString = tool;
            this.archetype = securityString;
        }

        public ClansTool getTool() {
            return ClansTool.valueOf(this.toolString);
        }
    }
}
