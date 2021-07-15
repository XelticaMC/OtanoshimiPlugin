package work.xeltica.craft.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CommandPlayerOnlyBase extends CommandBase {
    public abstract boolean execute(Player player, Command command, String label, String[] args);

    @Override
    public final boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "プレイヤーが実行してください");
            return true;
        }
        return execute((Player)sender, command, label, args);
    }
}
