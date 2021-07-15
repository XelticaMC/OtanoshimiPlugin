package work.xeltica.craft.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.plugins.VaultPlugin;
import work.xeltica.craft.core.stores.OmikujiStore;

public class CommandOmikuji extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var s = OmikujiStore.getInstance();

        if (s.isDrawnBy(player)) {
            var score = s.getScoreName(player);
            player.sendMessage(
                ChatColor.RED + "既に引いています！" +
                ChatColor.GOLD + "あなたの運勢は" +
                ChatColor.RESET + "「" +
                ChatColor.GREEN + score +
                ChatColor.RESET + "」" +
                ChatColor.AQUA + "です！"
            );
            player.sendMessage(ChatColor.GOLD + "また次の朝、引いてください！");
            return true;
        }
        var vault = VaultPlugin.getInstance();
        if (vault.isEconomyEnabled() && !vault.tryDepositPlayer(player, 100)) {
            player.sendMessage(ChatColor.RED + "お金が足りません！おみくじは1回100 Cloverです。");
            return true;
        }
        player.sendMessage("何が出るかな...?");
        
        new BukkitRunnable(){
            @Override
            public void run() {
                var score = s.generateScore();
                s.set(player, score);

                player.sendMessage(
                    ChatColor.GOLD + "あなたの運勢は" +
                    ChatColor.RESET + "「" +
                    ChatColor.GREEN + s.getScoreName(score) +
                    ChatColor.RESET + "」" +
                    ChatColor.AQUA + "です！"
                );

                switch (score) {
                    case Daikichi:
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 0.7f);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60 * 20 * 20, 1));
                        break;
                    case Daikyou:
                        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1, 0.5f);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, 20 * 60 * 10, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 10, 2));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 60 * 20 * 20, 1));
                        break;
                    case Kyou:
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1, 0.5f);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 60 * 20 * 20, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 5, 1));
                        break;
                    case Tokudaikichi:
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1, 1.4f);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60 * 20 * 20, 1));
                        break;
                    default:
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1.6f);
                        break;
                }
            }
        }.runTaskLater(XCorePlugin.getInstance(), 20 * 3);
        
        return true;
    }
}
