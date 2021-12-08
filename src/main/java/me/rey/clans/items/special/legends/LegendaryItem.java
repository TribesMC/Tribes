package me.rey.clans.items.special.legends;

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

public abstract class LegendaryItem implements Listener, SpecialItem {
    protected Tribes plugin;
    protected String id;
    protected LegendType legend;
    protected ClansTool tool, disabledTool;

    protected SpecialItemUpdater updater;

    public LegendaryItem(final Tribes plugin, final String id, final LegendType legend, final ClansTool tool, final ClansTool disabledTool) {
        this.plugin = plugin;
        this.id = id;
        this.legend = legend;
        this.tool = tool;
        this.disabledTool = disabledTool;
        Bukkit.getPluginManager().registerEvents(this, plugin.getPlugin());
        this.updater = plugin.getSpecialItemUpdater();
    }

    public boolean isOfArchetype(final ItemStack stack, final LegendType type) {
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

    public LegendType getLegendType() {
        return this.legend;
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
        final ItemStack item = this.legend.getTool().stack;
        return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : UtilItem.getItemByMaterial(item.getType()).name;
    }

    @Override
    public ItemStack getItemStack() {
        return this.legend.getTool().stack;
    }

    @Override
    public Class<? extends SpecialItem> getType() {
        return this.getClass();
    }

    public enum LegendType {
        ALLIGATORS_TOOTH("ALLIGATORS_TOOTH", "SXQncyBhIHdlYXBvbiB0aGF0IGxldHMgeW91IHRyYXZlbCB0aHJvdWdoIHdhdGVyLCB3aGF0J3MgdGhlIHBvaW50IG9mIGRlY29kaW5nIHRoaXMuLj8="),
        GIANTS_BROADSWORD("GIANTS_BROADSWORD", "TGV0cyB0aHJvdyB5b3UgaW50byBhIGJhc2U2NCBkZWNvZGVyIGFuZCBzZWUgd2hhdCBoYXBwZW5zIHNoYWxsIHdlPw=="),
        HYPERAXE("HYPERAXE", "U3RvcCBkZWNvZGluZyB0aGVzZSB0b2tlbnMsIHlvdSdyZSByZW1vdmluZyB0aGUgbXlzdGVyeSEgPjoo"),
        MAGNETIC_MAUL("MAGNETIC_MAUL", "V2Ugd2lzaGVkIGZvciBuaWNlIHRoaW5ncywgeWV0IHdlIGdvdCBuZXJkcyBkZWNvZGluZyBvdXIgc2VjdXJpdHkgc3RyaW5ncy4uLg=="),
        MERIDIAN_SCEPTER("MERIDIAN_SCEPTER", "SSdtIGdvaW5nIHRvIHNjZXB0ZXIgeW91ciBmYWNlLi4uIEhvdyBkaWQgeW91IGdldCBhaG9sZCBvZiBvdXIgc2VjdXJpdHkgc3RyaW5ncyBhbnl3YXk/"),
        WINDBLADE("WINDBLADE", "SGF2ZW4ndCB5b3UgZ290IGFueXRoaW5nIGJldHRlciB0byBiZSBkb2luZz8gTGlrZSBsaXRlcmFsbHksIGFueXRoaW5nLCBlbHNlLi4"),
        SCYTHE_OF_THE_FALLEN_LORD("SCYTHE_OF_THE_FALLEN_LORD", "R28gYXdBWSwgc3RvUCBkZUNPZElOZyBtWSBzdFJJTmdT"),
        KNIGHTS_GREATLANCE("KNIGHTS_GREATLANCE", "Q2FuJ3QgYmUgYm90aGVyZWQgd3JpdGluZyBhbiBleGNpdGluZyBzdHJpbmcgZm9yIHRoaXMgb25lLCBlbmpveS4uLg==");

        public String toolString, archetype;

        LegendType(final String tool, final String securityString) {
            this.toolString = tool;
            this.archetype = securityString;
        }

        public ClansTool getTool() {
            return ClansTool.valueOf(this.toolString);
        }
    }
}
