package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.stores.PlayerFlagsStore;

public class CommandCat extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player sender, Command command, String label, String[] args) {
        var flag = PlayerFlagsStore.getInstance();
        if (args.length > 0) {
            var arg = args[0];
            if ("on".equals(arg)) {
                flag.setCatMode(sender, true);
                sender.sendMessage("CATモードを§aオン§rにしました。");
            } else if ("off".equals(arg)) {
                flag.setCatMode(sender, false);
                sender.sendMessage("CATモードを§cオフ§rにしました。");
            } else {
                return false;
            }
        } else {
            var mes = flag.getCatMode(sender) ? "§aあなたはCATモードです。§r" : "§aあなたはCATモードではありません。§r";
            sender.sendMessage(mes);
        }
        return true;
    }
    
}
