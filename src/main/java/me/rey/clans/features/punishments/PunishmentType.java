package me.rey.clans.features.punishments;

public enum PunishmentType {
    IPBAN(0, "ip-banned"),
    BAN(1, "banned"),
    MUTE(2, "muted"),
    KICK(3, "kicked"),
    WARN(4, "warned"),
    REPORTBAN(5, "report banned");

    private final int dbIdentifier;
    private final String pastTense;

    PunishmentType(int dbIdentifier, String pastTense) {
        this.dbIdentifier = dbIdentifier;
        this.pastTense = pastTense;
    }

    public int getDbIdentifier() {
        return dbIdentifier;
    }

    public String pastTense() {
        return pastTense;
    }

    public static PunishmentType getValue(int value) {
        for (PunishmentType type : values()) {
            if (type.dbIdentifier != value) continue;
            return type;
        }
        return null;
    }
}
