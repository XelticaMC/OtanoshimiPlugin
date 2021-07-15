package work.xeltica.craft.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class CommandPvp extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }
        var w = player.getWorld();
        var flag = args[0];
        if (flag.equalsIgnoreCase("on")) {
            w.setPVP(true);
            player.sendMessage(ChatColor.GOLD + "ワールド " + w.getName() + " のPvPを許可しました");
        } else if (flag.equalsIgnoreCase("off")) {
            w.setPVP(false);
            player.sendMessage(ChatColor.GOLD + "ワールド " + w.getName() + " のPvPを拒否しました");
        } else {
            return false;
        }
        return true;
    }
    
}
