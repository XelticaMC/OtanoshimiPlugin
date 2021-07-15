package work.xeltica.craft.core.runnables;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.core.events.NewMorningEvent;

public class DaylightObserver extends BukkitRunnable {
    public DaylightObserver(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        var time = plugin.getServer().getWorld("world").getTime();
        if (time < this.prevTime) {
            // 時間が前よりも小さくなったのであれば、おそらく日をまたいだことになる
            var event = new NewMorningEvent(time);
            Bukkit.getPluginManager().callEvent(event);
        }
        this.prevTime = time;
    }

    private Plugin plugin;
    private long prevTime;
}
