package me.rey;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class Module {

    private final String name;
    private final JavaPlugin plugin;

    public Module(final String name, final JavaPlugin plugin) {
        this.name = name;
        this.plugin = plugin;
    }

    public String getName() {
        return this.name;
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    protected abstract void onEnable();

    protected abstract void onDisable();
}
