package work.xeltica.craft.core.runnables;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

public class FlyingObserver extends BukkitRunnable {
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().stream().filter(p -> p.getAllowFlight() && p.getGameMode() != GameMode.SPECTATOR).forEach(p -> {
            // /summon area_effect_cloud ~ ~ ~ {Particle:"flame",Radius:.5f,Duration:10}
            var loc = p.getLocation();
            var world = p.getWorld();
            world.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD, SpawnReason.CUSTOM, (e) -> {
                var aec = (AreaEffectCloud)e;
                aec.setParticle(Particle.FLAME);
                aec.setRadius(0.5f);
                aec.setDuration(10);
            });
        });
    }
}
