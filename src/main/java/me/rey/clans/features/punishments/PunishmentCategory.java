package me.rey.clans.features.punishments;

import me.rey.core.utils.UtilText;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public enum PunishmentCategory {
    CHAT("Chat", Material.BOOK_AND_QUILL, 0),
    GAMEPLAY("Gameplay", Material.HOPPER, 1),
    CLIENT("Cheating", Material.IRON_SWORD, 2),
    KICK("Kick", Material.FEATHER, 3),
    WARN("Warning", Material.PAPER, 4),
    PERM_BAN("Permanent Ban", Material.REDSTONE_BLOCK, 5),
    PERM_MUTE("Permanent Mute", Material.BOOK_AND_QUILL, 6),
    REPORT("Report Ban", Material.BOOK, 7),
    OTHER("Other", Material.REDSTONE_BLOCK, 8);

    private final String name;
    private final Material item;
    private final int dbIdentifier;
    private final String[] description;

    PunishmentCategory(String name, Material item, int dbIdentifier, String... description) {
        this.name = name;
        this.item = item;
        this.dbIdentifier = dbIdentifier;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Material getItem() {
        return item;
    }

    public int getDbIdentifier() {
        return dbIdentifier;
    }

    public String[] getDescription() {
        return description;
    }

    public String[] compileDescriptionAsItemLore() {
        List<String> lines = new ArrayList<>();
        for (String desc : description) {
            lines.addAll(UtilText.wrap(desc, 36));
        }
        return lines.toArray(new String[0]);
    }

    public static PunishmentCategory getValue(int value) {
        for (PunishmentCategory category : values()) {
            if (category.dbIdentifier != value) continue;
            return category;
        }
        return null;
    }
}
