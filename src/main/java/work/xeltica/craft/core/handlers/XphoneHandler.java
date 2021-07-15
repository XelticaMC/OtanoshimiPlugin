package work.xeltica.craft.core.handlers;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.ItemStore;
import work.xeltica.craft.core.stores.PlayerFlagsStore;

public class XphoneHandler implements Listener {
    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        var item = e.getItem();
        var player = e.getPlayer();
        if (!store().compareCustomItem(item, store().getXPhone())) return;
        var items = new ArrayList<MenuItem>();
        var worldName = player.getWorld().getName();

        var appClassicHub = new MenuItem("クラシックロビーへ", i -> {
            HubStore.getInstance().teleport(player, HubType.Classic, true);
        }, Material.GOLD_BLOCK);
        var appMainHub = new MenuItem("メインロビーへ", i -> {
            HubStore.getInstance().teleport(player, HubType.Main, true);
        }, Material.NETHERITE_BLOCK);
        var appHub = new MenuItem("ロビーへ", i -> {
            player.performCommand("hub");
        }, Material.NETHERITE_BLOCK);
        var appRespawn = new MenuItem("初期スポーンへ", i -> {
            player.performCommand("respawn");
        }, Material.FIREWORK_ROCKET);
        var appPromo = new MenuItem("市民システム", i -> {
            player.performCommand("promo");
        }, Material.NETHER_STAR, null);
        var appSidebar = new MenuItem("サイドバー切り替え", i -> {
            player.performCommand("sb toggle");
        }, Material.FILLED_MAP, null);
        var appOmikuji = new MenuItem("おみくじ", i -> {
            player.performCommand("omikuji");
        }, Material.GOLD_INGOT, null);
        var catMode = PlayerFlagsStore.getInstance().getCatMode(player);
        var appCat = new MenuItem("ネコ語モードを" + (catMode ? "オフ" : "オン") + "にする", i -> {
            player.performCommand("cat " + (catMode ? "off" : "on"));
        }, Material.COD, null, catMode);
        var appBoat = new MenuItem("ボート", i -> {
            player.performCommand("boat");
        }, Material.OAK_BOAT, null);
        var appCart = new MenuItem("トロッコ", i -> {
            player.performCommand("cart");
        }, Material.MINECART, null);
        var appCPrivate = new MenuItem("プライベート保護", i -> {
            player.performCommand("cprivate");
        }, Material.TRIPWIRE_HOOK, null);
        var appCPublic = new MenuItem("パブリック保護", i -> {
            player.performCommand("cpublic");
        }, Material.TRIPWIRE_HOOK, null);
        var appCRemove = new MenuItem("保護削除", i -> {
            player.performCommand("cremove");
        }, Material.TRIPWIRE_HOOK, null);
        var appStore = new MenuItem("エビパワーストア", i -> {
            player.sendMessage("近日公開予定！");
            player.sendMessage("貯めたエビパワーでアイテムを買おう！");
        }, Material.HEART_OF_THE_SEA, null);
        var appPunishment = new MenuItem("処罰", i -> {
            player.performCommand("report");
        }, Material.BARRIER, null);
        var appHelp = new MenuItem("ヘルプ", i -> {
            player.sendMessage("近日公開予定！");
        }, Material.LIGHT, null);

        if (worldName.equals("hub2")) {
            items.add(appClassicHub);
        } else if (worldName.equals("hub")) {
            items.add(appMainHub);
        } else {
            items.add(appHub);
        }

        switch (worldName) {
            case "main":
            case "world":
            case "world_nether":
            case "world_the_end":
                items.add(appCPrivate);
                items.add(appCPublic);
                items.add(appCRemove);
                break;
        }
        
        items.add(appRespawn);
        items.add(appPromo);
        items.add(appSidebar);
        items.add(appOmikuji);
        items.add(appCat);
        items.add(appBoat);
        items.add(appCart);
        items.add(appStore);

        if (e.getPlayer().hasPermission("otanoshimi.command.report")) {
            items.add(appPunishment);
        }

        items.add(appHelp);

        ui().openMenu(e.getPlayer(), "X Phone OS", items);
    }

    private ItemStore store() { return ItemStore.getInstance(); }
    private Gui ui() { return Gui.getInstance(); }
}
