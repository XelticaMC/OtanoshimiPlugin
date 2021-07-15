package work.xeltica.craft.core.handlers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import work.xeltica.craft.core.stores.PlayerFlagsStore;

public class WakabaHandler implements Listener {
    public WakabaHandler() {
        deniedBlocks.add(Material.DISPENSER);
        deniedBlocks.add(Material.PISTON);
        deniedBlocks.add(Material.STICKY_PISTON);
        deniedBlocks.add(Material.TNT);
        deniedBlocks.add(Material.LEVER);
        deniedBlocks.add(Material.REDSTONE_TORCH);
        deniedBlocks.add(Material.SLIME_BLOCK);
        deniedBlocks.add(Material.HONEY_BLOCK);
        deniedBlocks.add(Material.TRIPWIRE_HOOK);
        deniedBlocks.add(Material.TRAPPED_CHEST);
        deniedBlocks.add(Material.DAYLIGHT_DETECTOR);
        deniedBlocks.add(Material.REDSTONE_BLOCK);
        deniedBlocks.add(Material.HOPPER);
        deniedBlocks.add(Material.DROPPER);
        deniedBlocks.add(Material.OBSERVER);
        deniedBlocks.add(Material.FIRE);
        deniedBlocks.add(Material.LAVA);
        deniedBlocks.add(Material.WATER);
        deniedBlocks.add(Material.CARVED_PUMPKIN);
        deniedBlocks.add(Material.JACK_O_LANTERN);
        deniedBlocks.add(Material.WITHER_SKELETON_SKULL);

        deniedTags.add(Tag.BUTTONS);

        // deniedItems.add(Material.BUCKET);
        deniedItems.add(Material.TNT_MINECART);
        deniedItems.add(Material.END_CRYSTAL);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (flags().isCitizen(e.getPlayer())) return;
        var mat = e.getBlock().getType();
        if (isDeniedMaterial(mat)) {
            prevent(e, e.getPlayer(), "ブロック " + mat + " を設置できません。");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (flags().isCitizen(e.getPlayer())) return;
        var mat = e.getBlock().getType();
        if (isDeniedMaterial(mat)) {
            prevent(e, e.getPlayer(), "ブロック " + mat + " を破壊できません。");
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (flags().isCitizen(e.getPlayer())) return;
        var clickedBlock = e.getClickedBlock();
        if (clickedBlock != null) {
            var mat = e.getClickedBlock().getType();
            if (isDeniedMaterial(mat)) {
                prevent(e, e.getPlayer(), "ブロック " + mat + " を使用できません。");
            }
        }
        var isUsingItem = e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null;
        if (isUsingItem && isDeniedItemMaterial(e.getItem().getType())) {
            prevent(e, e.getPlayer(), "アイテム " + e.getItem().getType() + " を使用できません。");
        }
        if (isUsingItem && e.getItem().getType().toString().contains("BUCKET")) {
            prevent(e, e.getPlayer(), "バケツを使用できません。");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        var p = (Player)e.getDamager();
        if (flags().isCitizen(p))
            return;
        if (e.getEntityType() == EntityType.ENDER_CRYSTAL) {
            prevent(e, p, "エンドクリスタルを破壊できません。");
        }
    }

    private void prevent(Cancellable e, Player p, String message) {
        e.setCancelled(true);
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 0.5f);
        p.sendMessage("§aわかば§rプレイヤーは§c" + message);
        p.sendMessage("§b市民への昇格§rが必要です。詳しくは§b/promo§rコマンドを実行してください！");
    }

    private boolean isDeniedMaterial(Material mat) {
        if (deniedBlocks.contains(mat)) return true;
        if (deniedTags.stream().anyMatch(t -> t.isTagged(mat))) return true;
        return false;
    }

    private boolean isDeniedItemMaterial(Material mat) {
        if (deniedItems.contains(mat)) return true;
        if (deniedItemTags.stream().anyMatch(t -> t.isTagged(mat))) return true;
        return false;
    }

    private PlayerFlagsStore flags() {
        return PlayerFlagsStore.getInstance();
    }

    private final Set<Material> deniedBlocks = new HashSet<>();
    private final Set<Material> deniedItems = new HashSet<>();
    private final Set<Tag<Material>> deniedTags = new HashSet<>();
    private final Set<Tag<Material>> deniedItemTags = new HashSet<>();
}
