package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.stores.HubStore;

public class CommandHub extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var isAdmin = player.hasPermission("hub.admin") || player.isOp();
        var store = HubStore.getInstance();

        if (args.length == 0) {
            store.teleport(player, switch (player.getWorld().getName()) {
                default -> HubType.Main;
                case "hub" -> HubType.Classic;
                case "world" -> HubType.Classic;
                case "world_nether" -> HubType.Classic;
                case "world_the_end" -> HubType.Classic;
                case "nightmare" -> HubType.Classic;
                case "sandbox" -> HubType.Classic;
                case "wildarea" -> HubType.Classic;
            });
            return true;
        }

        if (!isAdmin) {
            player.sendMessage("/hub");
        }
        if (args[0].equalsIgnoreCase("help")) {
            player.sendMessage("/hub [help]");
        } else {
            return false;
        }

        return true;
    }
}
