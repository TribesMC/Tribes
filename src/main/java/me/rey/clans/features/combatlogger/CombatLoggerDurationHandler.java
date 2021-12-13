package me.rey.clans.features.combatlogger;

public interface CombatLoggerDurationHandler {
    boolean shouldDestroy();
    String[] getQuitMessages();
}
