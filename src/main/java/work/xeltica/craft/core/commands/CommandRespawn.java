package work.xeltica.craft.core.commands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.stores.WorldStore;

public class CommandRespawn extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        String respawnWorldName;
        try {
            respawnWorldName = getRespawnWorld(player.getWorld());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "このワールドでは許可されていません");
            return true;
        }
        var respawnWorld = Bukkit.getWorld(respawnWorldName);
        var respawn = respawnWorld.getSpawnLocation();
        var isSameWorld = player.getWorld().getUID().equals(respawnWorld.getUID());
        var respawnWorldDisplayName = WorldStore.getInstance().getWorldDisplayName(respawnWorld);

        var isWarping = isWarpingMap.get(player.getUniqueId());
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！");
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(respawn, TeleportCause.PLUGIN);
                isWarpingMap.put(player.getUniqueId(), false);
            }
        }.runTaskLater(XCorePlugin.getInstance(), 20 * 5);
        var mes = isSameWorld
            ? "5秒後に初期スポーンに移動します..." 
            : "5秒後に" + respawnWorldDisplayName + "の初期スポーンに移動します...";
        player.sendMessage(mes);
        isWarpingMap.put(player.getUniqueId(), true);

        return true;
    }

    private String getRespawnWorld(World w) throws Exception {
        // TODO 旅行券のときに位置情報保存しておいてーとかそういう処理に対応したい
        if (w.getName().startsWith("travel_")) return "world";
        return switch (w.getName()) {
            default -> w.getName();

            case "wildarea2_nether" -> "wildarea2";
            case "wildarea2_the_end" -> "wildarea2";

            case "world_nether" -> "world";
            case "world_the_end" -> "world";
            case "wildarea" -> "world";
            case "nightmare" -> "world";

            case "pvp" -> throw new Exception();
            case "hub" -> throw new Exception();
            case "hub2" -> throw new Exception();
        };
    }
    
    private HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();

}
