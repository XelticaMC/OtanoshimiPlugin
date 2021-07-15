package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import work.xeltica.craft.core.gui.Gui;

public class CommandDebug extends CommandBase {

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        var subCommand = args[0];
        var player = sender instanceof Player ? (Player)sender : null;
        if (subCommand.equalsIgnoreCase("dialog")) {
            if (player == null) return false;
            Gui.getInstance().openDialog(player, "情報", "おめでとう、貴様！貴様は晴れて市民た！制限は緩和されます", (a) -> {
                player.sendActionBar(Component.text("ほに"));
                Bukkit.getLogger().info("ほにににににに");
            });
        } else {
            return false;
        }
        return true;
    }
    
}
