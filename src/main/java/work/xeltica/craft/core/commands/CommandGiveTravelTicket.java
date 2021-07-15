package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.travels.TicketType;
import work.xeltica.craft.core.utils.TravelTicketUtil;

public class CommandGiveTravelTicket extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var name = args.length >= 1 ? args[0] : null;
        var p = name == null ? player : Bukkit.getPlayer(name);
        if (p == null) {
            player.sendMessage(ChatColor.RED + "そのようなプレイヤーはいません");
            return true;
        }
        try {
        var typeString = args.length >= 2 ? args[1] : null;
        var type = typeString == null ? TicketType.WILDAREA : TicketType.valueOf(typeString);
        var amount = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
        p.getInventory().addItem(TravelTicketUtil.GenerateTravelTicket(amount, type));
        } catch (IllegalArgumentException e) {
            player.sendMessage("引数がおかしい");
            return true;
        }
        return true;
    }
    
}
