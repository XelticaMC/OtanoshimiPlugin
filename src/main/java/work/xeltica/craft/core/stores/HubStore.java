package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.models.SignData;
import work.xeltica.craft.core.utils.Config;
import work.xeltica.craft.core.utils.LocationComparator;

public class HubStore {
    public HubStore() {
        ConfigurationSerialization.registerClass(SignData.class, "SignData");
        HubStore.instance = this;
        logger = Bukkit.getLogger();
        signs = new Config("signs", (s) -> {
            signData = (List<SignData>) s.getConf().getList("signs", new ArrayList<SignData>());
        });
        loadSignsFile();
        loadOrInitializeHub();
    }

    public static HubStore getInstance() {
        return HubStore.instance;
    }

    public void teleport(Player player, HubType type) {
        teleport(player, type, false);
    }

    public void teleport(Player player, HubType type, boolean bulk) {
        var server = Bukkit.getServer();
        var world = Bukkit.getWorld(type.getWorldName());
        if (world == null) {
            player.sendMessage("未生成");
            return;
        }
        var isWarping = isWarpingMap.get(player.getUniqueId());
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！");
            return;
        }
        if (player.getWorld().getUID().equals(world.getUID())) {
            player.sendMessage("既にロビーです！");
            return;
        }
        var currentWorldName = player.getWorld().getName();
        var isSaveIgnoredWorld = bulk || Arrays.stream(inventorySaverIgnoredWorldNames)
                .anyMatch(name -> name.equalsIgnoreCase(currentWorldName));
        server.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            WorldStore.getInstance().saveCurrentLocation(player);
            var loc = type.getLocation() != null ? type.getSpigotLocation() : world.getSpawnLocation();
            player.teleport(loc, TeleportCause.PLUGIN);
            isWarpingMap.put(player.getUniqueId(), false);
        }, isSaveIgnoredWorld ? 1 : 20 * 5);
        if (!isSaveIgnoredWorld) {
            player.sendMessage("5秒後にロビーに移動します...");
            isWarpingMap.put(player.getUniqueId(), true);
        }
    }

    public UUID getHubId() {
        return worldUuid;
    }

    public World getHub() {
        return Bukkit.getWorld(worldUuid);
    }

    public boolean getForceAll() {
        return forceAll;
    }

    public void setForceAll(boolean value) {
        forceAll = value;
    }

    public void returnToWorld(Player player) {
        player.setGameMode(GameMode.SURVIVAL);

        WorldStore.getInstance().teleportToSavedLocation(player, "world");
    }

    public boolean processSigns(Location loc, Player p) {
        var signData = getSignDataOf(loc);
        if (signData == null) return false;

        var cmd = signData.getCommand();
        if (cmd.equalsIgnoreCase("teleport")) {
            var worldName = signData.getArg1();
            WorldStore.getInstance().teleport(p, worldName);
        } else if (cmd.equalsIgnoreCase("xteleport")) {
            var worldName = signData.getArg1();
            WorldStore.getInstance().teleportToSavedLocation(p, worldName);
        } else if (cmd.equalsIgnoreCase("return")) {
            returnToWorld(p);
        }
        return true;
    }

    public void removeSign(Player p, Location loc) {
        var sign = getSignDataOf(loc);
        if (sign != null) {
            signData.remove(sign);
            try {
                saveSignsFile();
                p.sendMessage("看板を撤去しました。");
            } catch (IOException e1) {
                p.sendMessage("§b看板の撤去に失敗しました。");
                e1.printStackTrace();
            }
        }
    }

    public void placeSign(Player p, Location loc, String command, String arg1, String arg2) {
        signData.add(new SignData(loc, command, arg1, arg2));
        try {
            saveSignsFile();
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1, 1);
            p.sendMessage("設置に成功しました。");
        } catch (IOException e1) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1, 0.5f);
            p.sendMessage("内部エラー発生。");
            e1.printStackTrace();
        }
    }

    public void loadSignsFile() {
        signs.reload();
    }

    public void saveSignsFile() throws IOException {
        signs.getConf().set("signs", signData);
        signs.save();
    }

    private void loadOrInitializeHub() {
        var world = Bukkit.getServer().getWorld("hub");
        if (world == null) {
            logger.severe("No hub found. Create it.");
            return;
        }
        worldUuid = world.getUID();
    }

    private SignData getSignDataOf(Location loc) {
        return signData.stream().filter(s -> LocationComparator.equals(loc, s.getLocation())).findFirst().orElse(null);
    }

    private final String[] inventorySaverIgnoredWorldNames = {
        "sandbox",
        "art", 
        "pvp",
        "test",
    };

    private static HubStore instance;

    private Config signs;
    private Logger logger;
    private HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();
    private UUID worldUuid;
    private boolean forceAll;
    private List<SignData> signData;
}
