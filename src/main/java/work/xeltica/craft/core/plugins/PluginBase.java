package work.xeltica.craft.core.plugins;

import org.bukkit.Bukkit;

import work.xeltica.craft.core.XCorePlugin;

public abstract class PluginBase {
    public void onEnable(XCorePlugin plugin) {
        Bukkit.getLogger().info("Enabling sub-plugin " + this.getClass().getName() + "...");
    }

    public void onDisable(XCorePlugin plugin) {
        Bukkit.getLogger().info("Disabling sub-plugin " + this.getClass().getName() + "...");
    }
}
