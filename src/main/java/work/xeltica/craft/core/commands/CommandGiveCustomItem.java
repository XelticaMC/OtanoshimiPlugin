package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.stores.ItemStore;

public class CommandGiveCustomItem extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var name = args.length >= 1 ? args[0] : null;
        var p = name == null ? player : Bukkit.getPlayer(name);
        var store = ItemStore.getInstance();
        if (p == null) {
            player.sendMessage(ChatColor.RED + "そのようなプレイヤーはいません");
            return true;
        }
        try {
            var typeString = args.length >= 2 ? args[1] : "";
            var item = switch (typeString.toLowerCase()) {
                case "xphone" -> store.getXPhone().clone();
                default -> null;
            };
            if (item != null) {
                p.getInventory().addItem(item);
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("引数がおかしい");
            return true;
        }
        return true;
    }
    
}
