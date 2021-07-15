package work.xeltica.craft.core.handlers;

import java.util.List;

import com.google.common.collect.Lists;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.Component;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.WorldStore;

public class HubHandler implements Listener {
    @EventHandler
    public void onPlayerHurt(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        var id = store().getHubId();
        if (id == null) return;
        var player = (Player)e.getEntity();
        if (player.getWorld().getUID().equals(id)) {
            e.setCancelled(true);
            if (e.getCause() == DamageCause.VOID) {
                // 落ちた
                var loc = store().getHub().getSpawnLocation();
                player.teleport(loc, TeleportCause.PLUGIN);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        var world = store().getHub();
        if (store().getForceAll()) {
            player.teleport(world.getSpawnLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        var p = e.getPlayer();
        if (playerIsInClassicHub(p)) {
            store().removeSign(p, e.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractBlock(PlayerInteractEvent e) {
        var p = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (playerIsInHub(p)) {
            e.setCancelled(store().processSigns(e.getClickedBlock().getLocation(), p));
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        var player = e.getPlayer();
        if (playerIsInClassicHub(player)) {
            e.setCancelled(true);
            store().returnToWorld(player);
        }
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent e) {
        var player = e.getEntity();
        if (playerIsInHub(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        var p = e.getPlayer();
        if (!playerIsInClassicHub(p)) return;

        var lines = e.lines().stream()
            .map(c -> PlainTextComponentSerializer.plainText().serialize(c))
            .toList();

        if (lines.get(0).equals("[Hub]")) {
            var command = lines.get(1);
            var arg1 = lines.get(2);
            var arg2 = lines.get(3);

            if (command.equalsIgnoreCase("teleport")) {
                // arg1: わーるど名
                // arg2: 座標(カンマ区切り) 未実装
                e.line(0, Component.text("[§a§lテレポート§r]"));
                e.line(1, Component.text(""));
                e.line(2, Component.text("§b" + WorldStore.getInstance().getWorldDisplayName(arg1)));
                e.line(3, Component.text("§rクリックorタップ"));
            } else if (command.equalsIgnoreCase("xteleport")) {
                e.line(0, Component.text("[§b§lテレポート§r]"));
                e.line(1, Component.text(""));
                e.line(2, Component.text("§b" + WorldStore.getInstance().getWorldDisplayName(arg1)));
                e.line(3, Component.text("§rクリックorタップ"));
            } else if (command.equalsIgnoreCase("return")) {
                e.line(0, Component.text("§a元の場所に帰る"));
                e.line(1, Component.text(""));
                e.line(2, Component.text(""));
                e.line(3, Component.text("§rクリックorタップ"));
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1, 0.5f);
                p.sendMessage("設置に失敗しました。存在しないコマンドです。");
                return;
            }
            store().placeSign(p, e.getBlock().getLocation(), command, arg1, arg2);
        }
    }

    private boolean playerIsInHub(Entity p) {
        return hubs.contains(p.getWorld().getName());
    }

    private boolean playerIsInClassicHub(Entity p) {
        return p.getWorld().getName().equals("hub");
    }

    private HubStore store() {
        return HubStore.getInstance();
    }

    private List<String> hubs = Lists.newArrayList(
        "hub",
        "hub2",
        "hub_dev"
    );
}
