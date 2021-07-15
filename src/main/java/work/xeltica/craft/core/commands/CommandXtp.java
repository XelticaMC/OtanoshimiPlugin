package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.stores.WorldStore;

public class CommandXtp extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length != 1 && args.length != 2) {
            return false;
        }
        if (args.length == 2 && !player.hasPermission("otanoshimi.command.xtp.other")) {
            player.sendMessage("§c権限がありません。");
            return true;
        }
        var worldName = args[0];
        var p = args.length == 2 ? Bukkit.getPlayer(args[1]) : player;
        if (p == null) {
            player.sendMessage("§cプレイヤーが存在しません");
            return true;
        }
        WorldStore.getInstance().teleportToSavedLocation(player, worldName);
        return true;
    }
    
}
