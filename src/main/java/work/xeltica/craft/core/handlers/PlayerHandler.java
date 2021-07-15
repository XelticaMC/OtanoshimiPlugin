package work.xeltica.craft.core.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import work.xeltica.craft.core.models.OmikujiScore;
import work.xeltica.craft.core.stores.OmikujiStore;
import work.xeltica.craft.core.stores.PlayerFlagsStore;
import work.xeltica.craft.core.utils.TravelTicketUtil;

public class PlayerHandler implements Listener {
    public PlayerHandler(Plugin p) {
        this.plugin = p;
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        var p = (Player)e.getEntity();
        if (p.getHealth() - e.getFinalDamage() > 0)
            return;

        var score = OmikujiStore.getInstance().get(p);
        var th = 
            score == OmikujiScore.Tokudaikichi ? 5 : 
            score == OmikujiScore.Daikichi ? 1 : 0;
        
        if ((int)(Math.random() * 100) >= th) return;

        var i = p.getInventory();
        var heldItem = i.getItemInMainHand();
        i.remove(heldItem);
        i.setItemInMainHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        new BukkitRunnable(){
            @Override
            public void run() {
                i.setItemInMainHand(heldItem);
            }
        }.runTaskLater(this.plugin, 1);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();
        var name = PlainTextComponentSerializer.plainText().serialize(p.displayName());
        e.joinMessage(Component.text("§a" + name + "§b" + "さんがやってきました"));
        if (!p.hasPlayedBefore()) {
            e.joinMessage(Component.text("§a" + name + "§b" + "が§6§l初参加§rです"));
            PlayerFlagsStore.getInstance().addNewcomer(p);
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                PlayerFlagsStore.getInstance().updateHasOnlineStaff();   
            }
        }.runTask(plugin);

        var f = PlayerFlagsStore.getInstance();

        p.showTitle(Title.title(
            Component.text("§aXelticaMCへ§6ようこそ！"),
            Component.text("§f詳しくは §b§nhttps://craft.xeltica.work§fを見てね！")
        ));

        if (f.isCitizen(p))
            return;
        if (!f.isNewcomer(p)) {
            p.sendMessage("総プレイ時間が30分を超えたため、§b市民§rへの昇格ができます！");
            p.sendMessage("詳しくは §b/promo§rコマンドを実行してください。");
        }
        if (f.hasOnlineStaff()) {
            p.sendMessage("スタッフが参加しているため、「観光モード」は無効です。");
        } else {
            p.sendMessage("スタッフが全員不在のため、「観光モード」は有効です。");
            p.sendMessage("観光モードがオンの時は、ブロックの設置、破壊、使用やモブへの攻撃などができなくなり、モブから襲われることも、体力や満腹度が減ることもありません。");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        var name = PlainTextComponentSerializer.plainText().serialize(e.getPlayer().displayName());
        e.quitMessage(Component.text("§a" + name + "§b" + "さんがかえりました"));
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerFlagsStore.getInstance().updateHasOnlineStaff();
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        // サンドボックスと旅行先からポータルを開けることを禁止
        var name = e.getPlayer().getWorld().getName();
        if (name.startsWith("travel_") || name.equals("sandbox") || name.equals("wildarea")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        var isEnderChest = e.getInventory().getType() == InventoryType.ENDER_CHEST;
        var isSandbox = e.getPlayer().getWorld().getName().equals("sandbox");
        if (isEnderChest && isSandbox) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatForCat(AsyncPlayerChatEvent e) {
        // ネコであれば文章をいじる
        if (PlayerFlagsStore.getInstance().getCatMode(e.getPlayer())) {
            var mes = e.getMessage();
            mes = mes.replace("な", "にゃ");
            mes = mes.replace("ナ", "ニャ");
            mes = mes.replace("ﾅ", "ﾆｬ");
            mes = mes.replace("everyone", "everynyan");
            mes = mes.replace("morning", "mornyan");
            mes = mes.replace("na", "nya");
            mes = mes.replace("EVERYONE", "EVERYNYAN");
            mes = mes.replace("MORNING", "MORNYAN");
            mes = mes.replace("NA", "NYA");
            e.setMessage(mes);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayer草ed(AsyncPlayerChatEvent e) {
        var logger = Bukkit.getLogger();
        if (e.getMessage().equals("草") || e.getMessage().equalsIgnoreCase("kusa") || e.getMessage().equalsIgnoreCase("w")) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    var block = e.getPlayer().getLocation().subtract(0, 1, 0).getBlock();
                    if (block.getType() != Material.GRASS_BLOCK) {
                        return;
                    }

                    var id = e.getPlayer().getUniqueId();
                    var lastTime = last草edTimeMap.containsKey(id) ? last草edTimeMap.get(id) : Integer.MIN_VALUE;
                    var nowTime = Bukkit.getCurrentTick();
                    if (lastTime == Integer.MIN_VALUE || nowTime - lastTime > 20 * 60) {
                        block.applyBoneMeal(BlockFace.UP);
                        logger.info("Applied 草");
                    }
                    last草edTimeMap.put(id, nowTime);
                }
            }.runTask(plugin);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent e) {
        var p = e.getPlayer();
        if (p.getWorld().getName().startsWith("travel_")) {
            var world = Bukkit.getWorld("world");
            p.teleportAsync(world.getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        // TODO 旅行券を持つとタイトル表示が発生するようにする
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        var p = e.getPlayer();
        var isSneaking = p.isSneaking();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            var worldName = p.getWorld().getName();
            var isBedDisabledWorld = 
                   worldName.startsWith("travel_")
                || worldName.equals("hub")
                || worldName.equals("sandbox")
                ;
            if (isBedDisabledWorld && Tag.BEDS.isTagged(e.getClickedBlock().getType())) {
                e.setCancelled(true);
            }
            return;
        }
        var item = e.getItem();
        if (TravelTicketUtil.isTravelTicket(item) && e.getAction() != Action.PHYSICAL) {
            e.setCancelled(true);

            if (movingPlayer != null) {
                p.sendMessage(p.getUniqueId().equals(movingPlayer) ? "移動中です！" : "誰かが移動中です。少し待ってから再試行してください。");
                return;
            }

            var type = TravelTicketUtil.getTicketType(item);
            var worldName = "travel_" + type.toString().toLowerCase();
            var world = Bukkit.getWorld(worldName);
            
            if (world == null) {
                p.sendMessage("申し訳ありませんが、現在" + type.getDisplayName() + "には行けません。");
                return;
            }
            if (world.getUID().equals(p.getWorld().getUID())) {
                p.sendMessage("既に来ています！");
                return;
            }
            movingPlayer = p.getUniqueId();

            if (p.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }
            int x, z;
            var otherPlayers = world.getPlayers();
            Bukkit.getLogger().info(String.format("%sに向かうプレイヤー%sはスニークしていま%s。", type.getDisplayName(), p.getName(), isSneaking ? "す" : "せん"));

            if (isSneaking || otherPlayers.size() == 0) {
                x = rnd.nextInt(20000) - 10000;
                z = rnd.nextInt(20000) - 10000;
            } else {
                var chosen = otherPlayers.get(rnd.nextInt(otherPlayers.size()));
                var loc = chosen.getLocation();
                final var range = 10;
                x = loc.getBlockX() + rnd.nextInt(range) - range / 2;
                z = loc.getBlockZ() + rnd.nextInt(range) - range / 2;
            }
            p.sendMessage("旅行券を使用しました。現在手配中です。その場で少しお待ちください！");
            var res = world.getChunkAtAsync(x, z);
            res.whenComplete((ret, ex) -> {
                var y = world.getHighestBlockYAt(x, z);
                var loc = new Location(world, x, y, z);
                var block = world.getBlockAt(loc);
                // 危険ブロックの場合、安全な石ブロックを敷いておく
                // TODO: 対象ブロックをHashSetに入れてそれを使うようにする
                if (block.getType() == Material.LAVA || block.getType() == Material.WATER) {
                    block.setType(Material.STONE, false);
                }
                for (var pl : Bukkit.getOnlinePlayers()) {
                    pl.sendMessage(String.format("§6%s§rさんが§a%s§rに行きます！§b行ってらっしゃい！", p.getDisplayName(), type.getDisplayName()));
                }
                p.teleport(loc);
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1, 0.5f);
                p.sendTitle(ChatColor.GOLD + type.getDisplayName(), "良い旅を！", 5, 100, 5);
                p.sendMessage("ようこそ、§6" + type.getDisplayName() + "§rへ！");
                p.sendMessage("元の世界に帰る場合は、§a/respawn§rコマンドを使用します。");
                movingPlayer = null;
            });
        }
    }

    private Plugin plugin;
    private final Random rnd = new Random();
    private UUID movingPlayer = null;
    private Map<UUID, Integer> last草edTimeMap = new HashMap<>();
}
