package work.xeltica.craft.core.commands;

import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import net.kyori.adventure.text.Component;
public class CommandSignEdit extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        var block = player.getTargetBlock(null, 5);
        if (!Tag.SIGNS.isTagged(block.getType())) {
            player.sendMessage(ChatColor.RED + "変更する対象の看板を見てください");
            return true;
        }
        var state = (Sign)block.getState();
        try {
            var index = Integer.parseInt(args[0]);
            if (index < 0 || index > 3) {
                player.sendMessage(ChatColor.RED + "行番号には0,1,2,3を指定してください");
                return true;
            };
            var l = new LinkedList<String>(Arrays.asList(args));
            l.remove(0);
            var line = String.join(" ", l);
            state.line(index, Component.text(line));
            // イベント
            var e = new SignChangeEvent(block, player, state.lines());
            Bukkit.getPluginManager().callEvent(e);
            if (!e.isCancelled()) {
                state.update();
                player.sendMessage("看板の" + index + "行目を「" + line + "」と書き換えました");
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
}
