package work.xeltica.craft.core.utils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.bukkit.configuration.file.YamlConfiguration;

import work.xeltica.craft.core.XCorePlugin;

/**
 * Spigot のコンフィグ機能のラッパー
 * 
 * @author Xeltica
 */
public class Config {
    public Config(String configName) {
        this(configName, null);
    }

    public Config(String configName, Consumer<Config> onReloaded) {
        this.configName = configName;
        this.onReloaded = onReloaded;
        this.reload();
    }

    public String getConfigName() {
        return this.configName;
    }

    public YamlConfiguration getConf() {
        return this.config;
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.openFile());
        if (onReloaded != null) onReloaded.accept(this);
    }

    public void save() throws IOException {
        this.config.save(this.openFile());
        this.reload();
    }

    private File openFile() {
        var folder = XCorePlugin.getInstance().getDataFolder();
        return new File(folder, this.configName + ".yml");
    }

    private final String configName;
    private final Consumer<Config> onReloaded;
    private YamlConfiguration config;
}
