package work.xeltica.craft.core.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class CommandLocalTime extends CommandPlayerOnlyBase {
    public CommandLocalTime() {
        builtinTimeMap.put("day", 1000);
        builtinTimeMap.put("night", 13000);
        builtinTimeMap.put("noon", 6000);
        builtinTimeMap.put("midnight", 18000);
        builtinTimeMap.put("sunrise", 23000);
        builtinTimeMap.put("sunset", 12000);
    }

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        var w = player.getWorld();
        var c = args[0];
        if (c.equals("set")) {
            if (args.length != 2) return false;
            var timeString = args[1];
            try {
                var time = toTime(timeString);
                w.setTime(time);
                player.sendMessage(ChatColor.RED + "時刻を " + time + "に設定しました");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "時間指定が異常です");
            }
            return true;
        }
        if (c.equals("add")) {
            if (args.length != 2) return false;
            var timeString = args[1];
            try {
                var time = toTime(timeString);
                w.setTime(w.getTime() + time);
                player.sendMessage(ChatColor.RED + "時刻を " + w.getTime() + "に設定しました");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "時間指定が異常です");
            }
            return true;
        }
        if (c.equals("query")) {
            if (args.length != 1) return false;
            player.sendMessage(Long.toString(w.getTime()));
            return true;
        }
        return false;
    }

    private int toTime(String timeString) {
        if (builtinTimeMap.containsKey(timeString)) {
            return builtinTimeMap.get(timeString);
        }
        return Integer.parseInt(timeString);
    }

    private final HashMap<String, Integer> builtinTimeMap = new HashMap<>();
    
}

