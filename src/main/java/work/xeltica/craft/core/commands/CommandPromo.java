package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.query.QueryOptions;
import work.xeltica.craft.core.stores.PlayerFlagsStore;

public class CommandPromo extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        var luckPerms = provider.getProvider();
        var lpUser = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        var f = PlayerFlagsStore.getInstance();
        var isManualCitizen = lpUser.getInheritedGroups(QueryOptions.defaultContextualOptions()).stream().anyMatch(g -> g.getName().equals("citizen"));
        if (!f.isCitizen(player)) {
            player.sendMessage("本サーバーでは、プレイヤーさんを§aわかば§r、§b市民§rという大きく2つのロールに分類しています。");
            player.sendMessage("§b市民§rにならなくても基本的なプレイはできますが、");
            player.sendMessage("・§c一部ブロックが使えない§r");
            player.sendMessage("・§c一部のオリジナル機能が使えない§r");
            // player.sendMessage("・§cスタッフ不在時に大きく機能が制限される§r");
            player.sendMessage("という欠点があります。§b市民§rに昇格することで全ての機能が開放されます。§b市民§rに昇格する方法の一つに、クイック認証があります。");
        }
        if (isManualCitizen) {
            player.sendMessage("既に手動認証されているため、あなたは市民です！");
        } else {
            player.sendMessage("§b§lクイック認証に必要な条件: ");
            var ctx = luckPerms.getContextManager().getContext(player);
            var linked = ctx.contains("discordsrv:linked", "true");
            var crafterRole = ctx.contains("discordsrv:role", "クラフター");
            var citizenRole = ctx.contains("discordsrv:role", "市民");
            var elapsedTime = f.getNewcomerTime(player);
            var elapsedTimeMinutes = elapsedTime / 60;
            var elapsedTimeSeconds = elapsedTime % 60;
            var elapsedTimeString = 
                elapsedTimeMinutes > 0 
                    ? elapsedTimeMinutes + "分" + elapsedTimeSeconds + "秒"
                    : elapsedTimeSeconds + "秒";
            sendMessage(player, "Discord 連携済み", linked);
            sendMessage(player, "クラフターロール付与済み", crafterRole);
            sendMessage(player, "市民ロール付与済み", citizenRole);
            sendMessage(player, "初参加から30分経過(残り" + elapsedTimeString + ")", elapsedTime == 0);
            player.sendMessage(
                linked && crafterRole && citizenRole
                    ? "§b全ての条件を満たしているため、あなたは市民です！"
                    : "§cあなたはまだいくつかの条件を満たしていないため、市民ではありません。"
            );
            if (!(linked && crafterRole && citizenRole)) {
                player.sendMessage("クイック認証が面倒であれば§6§l手動認証§rもできます。");
            }
        }
        player.sendMessage("詳しくは https://craft.xeltica.work/docs/citizen を確認してください！");
        return true;
    }

    private void sendMessage(Player player, String str, boolean isSuccess) {
        player.sendMessage((isSuccess ? "§a✔ " : "§c✘ ") + str + "§r");
    }
}
