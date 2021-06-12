package work.xeltica.craft.otanoshimiplugin.handlers;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkPopulateEvent;

import work.xeltica.craft.otanoshimiplugin.stores.HubStore;
import work.xeltica.craft.otanoshimiplugin.stores.WorldStore;

public class WorldHandler implements Listener {
    public WorldHandler() {
        blockToReplaceInWorld2.add(Material.CAVE_AIR);
        blockToReplaceInWorld2.add(Material.COAL_ORE);
        blockToReplaceInWorld2.add(Material.IRON_ORE);
        blockToReplaceInWorld2.add(Material.GOLD_ORE);
        blockToReplaceInWorld2.add(Material.DIAMOND_ORE);
        blockToReplaceInWorld2.add(Material.LAPIS_ORE);
        blockToReplaceInWorld2.add(Material.REDSTONE_ORE);
        blockToReplaceInWorld2.add(Material.EMERALD_ORE);
        Bukkit.getLogger().info("Loaded " + blockToReplaceInWorld2.size() + " blocks to replace in world2");
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        var p = e.getPlayer();
        if (p.getWorld().getName().equals("sandbox")) {
            var advancement = e.getAdvancement();

            for (var criteria : advancement.getCriteria()) {
                p.getAdvancementProgress(advancement).revokeCriteria(criteria);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        var p = e.getPlayer();
        if (p.getWorld().getName().equals("sandbox")) {
            var block = e.getBlock().getType();
            // エンダーチェストはダメ
            if (block == Material.ENDER_CHEST) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (e.getPlayer().getWorld().getName().equalsIgnoreCase("nightmare")) {
            // 悪夢から目覚める
            var store = HubStore.getInstance();
            store.writePlayerConfig(e.getPlayer(), false, false);
            var lobby = store.getHub().getSpawnLocation();
            e.setRespawnLocation(lobby);
        }
    }

    @EventHandler
    public void onPlayerTeleportGuard(PlayerTeleportEvent e) {
        var p = e.getPlayer();
        var world = e.getTo().getWorld();
        var name = world.getName();
        var store = WorldStore.getInstance();

        var isLockedWorld = store.isLockedWorld(name);
        var isCreativeWorld = store.isCreativeWorld(name);
        var displayName = store.getWorldDisplayName(name);
        var desc = store.getWorldDescription(name);

        if (isLockedWorld && !p.hasPermission("hub.teleport." + name)) {
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 0.5f);
            p.sendMessage(
                "§aわかば§rプレイヤーは§6" + displayName + "§rに行くことができません！\n" +
                "§b/promo§rコマンドを実行して、昇格方法を確認してください！"
            );
            e.setCancelled(true);
            return;
        }

        if (isCreativeWorld) {
            p.setGameMode(GameMode.CREATIVE);
        }
        if (name.equals("nightmare")) {
            world.setDifficulty(Difficulty.HARD);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setTime(18000);
            world.setStorm(true);
            world.setWeatherDuration(20000);
            world.setThundering(true);
            world.setThunderDuration(20000);
            var hubStore = HubStore.getInstance();
            hubStore.restoreInventory(p);
            hubStore.restoreParams(p);
            p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1, 0.5f);
        }
        if (desc != null) {
            p.sendMessage(desc);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleportNotify(PlayerTeleportEvent e) {
        var worldStore = WorldStore.getInstance();
        var player = e.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        var from = e.getFrom().getWorld();
        var to = e.getTo().getWorld();
        if (from.getName().equals(to.getName()))
            return;
        var fromName = worldStore.getWorldDisplayName(from);
        var toName = worldStore.getWorldDisplayName(to);

        var toPlayers = to.getPlayers();
        var allPlayersExceptInDestination = Bukkit.getOnlinePlayers().stream()
                // tpとマッチするUUIDがひとつも無いpのみを抽出
                .filter(p -> toPlayers.stream().allMatch(tp -> !tp.getUniqueId().equals(p.getUniqueId())))
                .collect(Collectors.toList());

        // fromにいる人宛に「toに行く旨」を伝える
        if (toName != null) {
            for (Player p : allPlayersExceptInDestination) {
                if (p.getUniqueId().equals(player.getUniqueId()))
                    continue;
                p.sendMessage(String.format("§a%s§bが§e%s§bに行きました", player.getDisplayName(), toName));
            }
        }

        // toにいる人宛に「fromから来た旨」を伝える
        if (fromName != null) {
            for (Player p : toPlayers) {
                if (p.getUniqueId().equals(player.getUniqueId()))
                    continue;
                p.sendMessage(String.format("§a%s§bが§e%s§bから来ました", player.getDisplayName(), fromName));
            }
        }
    }

    @EventHandler()
    public void onChunkPopulateEvent(ChunkPopulateEvent e) {
        // TODO ハードコードをやめる
        var c = e.getChunk();
        for (var z = 0; z < 16; z++) {
            for (var x = 0; x < 16; x++) {
                var yMax = c.getWorld().getHighestBlockYAt(x, z) - 1;
                for (int y = 1; y < yMax; y++) {
                    var block = c.getBlock(x, y, z);
                    if (blockToReplaceInWorld2.contains(block.getType())) {
                        block.setType(Material.STONE);
                    }
                }
            }
        }
    }

    private HashSet<Material> blockToReplaceInWorld2 = new HashSet<Material>();
}