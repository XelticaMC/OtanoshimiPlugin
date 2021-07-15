package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.gui.Gui;

public class CommandXCoreGuiEvent extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        Gui.getInstance().handleCommand(args[0]);
        return true;
    }
    
}
