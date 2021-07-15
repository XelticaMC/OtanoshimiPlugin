package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.events.StaffJoinEvent;
import work.xeltica.craft.core.events.StaffLeaveEvent;
import work.xeltica.craft.core.utils.Config;

public class PlayerFlagsStore {
    public PlayerFlagsStore() {
        PlayerFlagsStore.instance = this;
        flags = new Config("flags");
        newcomers = new Config("newcomers");
    }

    public static PlayerFlagsStore getInstance() {
        return PlayerFlagsStore.instance;
    }

    public void tickNewcomers(int tick) {
        var conf = newcomers.getConf();
        conf.getKeys(false).forEach(key -> {
            if (Bukkit.getPlayer(UUID.fromString(key)) == null) return;
            var time = conf.getInt(key, 0);
            time -= tick;
            conf.set(key, time <= 0 ? null : time);
        });
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewcomer(Player p) {
        newcomers.getConf().set(p.getUniqueId().toString(), 20 * 60 * 30);
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isNewcomer(Player p) {
        return newcomers.getConf().contains(p.getUniqueId().toString());
    }

    public int getNewcomerTime(Player p) {
        return newcomers.getConf().getInt(p.getUniqueId().toString(), 0) / 20;
    }

    public void setCatMode(Player p, boolean flag) {
        // 既に同値が設定されている場合はスキップ
        if (getCatMode(p) == flag) return;
        var uuid = p.getUniqueId().toString();
        if (flag) {
            // 有効化
            catUUIDs.add(uuid);
        } else {
            // 無効化
            catUUIDs.remove(uuid);
        }
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getCatMode(Player p) {
        return catUUIDs.contains(p.getUniqueId().toString());
    }

    public void reloadStore() {
        flags.reload();
        newcomers.reload();
        var fc = flags.getConf();

        catUUIDs = fc.getStringList("cats");
    }

    public void writeStore() throws IOException {
        var fc = flags.getConf();
        fc.set("cats", catUUIDs);

        flags.save();
        newcomers.save();
    }

    public boolean hasOnlineStaff() {
        return _hasOnlineStaff;
    }

    public boolean isCitizen(Player p) {
        return p.hasPermission("otanoshimi.citizen");
    }

    public void updateHasOnlineStaff() {
        var flag = Bukkit.getOnlinePlayers().stream().anyMatch(p -> p.hasPermission("otanoshimi.staff"));
        if (_hasOnlineStaff != flag) {
            Bukkit.getPluginManager().callEvent(flag ? new StaffJoinEvent() : new StaffLeaveEvent());
        }
        _hasOnlineStaff = flag;
    }
    
    private static PlayerFlagsStore instance;
    private List<String> catUUIDs = new ArrayList<>();
    private boolean _hasOnlineStaff;
    private Config flags;
    private Config newcomers;
}
