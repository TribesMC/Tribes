package me.rey;

import me.rey.clans.Tribes;
import me.rey.core.Warriors;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private JavaPlugin plugin;
    private Module[] modules;

    @Override
    public void onEnable() {
        this.plugin = this;
        this.modules = new Module[]{
                new Warriors(this.plugin),
                new Tribes(this.plugin)
        };

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        for (final Module module : this.modules) {
            module.onEnable();
        }
    }

    @Override
    public void onDisable() {
        this.plugin = null;

        for (final Module module : this.modules) {
            module.onDisable();
        }
    }

}
