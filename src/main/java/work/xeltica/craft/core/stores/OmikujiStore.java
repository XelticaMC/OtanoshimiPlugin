package work.xeltica.craft.core.stores;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import work.xeltica.craft.core.models.OmikujiScore;
import work.xeltica.craft.core.utils.Config;

public class OmikujiStore {
    public OmikujiStore() {
        OmikujiStore.instance = this;
        scoreNameMap.put(OmikujiScore.Tokudaikichi, "特大吉");
        scoreNameMap.put(OmikujiScore.Daikichi, "大吉");
        scoreNameMap.put(OmikujiScore.Kichi, "吉");
        scoreNameMap.put(OmikujiScore.Chukichi, "中吉");
        scoreNameMap.put(OmikujiScore.Shokichi, "小吉");
        scoreNameMap.put(OmikujiScore.Kyou, "凶");
        scoreNameMap.put(OmikujiScore.Daikyou, "大凶");
        scoreNameMap.put(OmikujiScore.None, "無し");
        this.conf = new Config("omikujistore");
    }

    public static OmikujiStore getInstance () {
        return OmikujiStore.instance;
    }

    public String getScoreName(OmikujiScore score) {
        return scoreNameMap.get(score);
    }

    public String getScoreName(Player player) {
        return getScoreName(get(player));
    }

    public boolean isDrawnBy(Player player) {
        return get(player) != OmikujiScore.None;
    }

    public OmikujiScore get(Player player) {
        var str = conf.getConf().getString(player.getUniqueId().toString(), OmikujiScore.None.name());
        return OmikujiScore.valueOf(str);
    }

    public void set(Player player, OmikujiScore score) {
        conf.getConf().set(player.getUniqueId().toString(), score.name());
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadStore();
    }

    public void reset() {
        conf.getConf().getKeys(false).forEach((key) -> {
            conf.set(key, null);
        });
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadStore();
    }

    public void reloadStore() {
        conf.reload();
    }

    public void writeStore() throws IOException {
        conf.save();
    }

    public OmikujiScore generateScore() {
        var dice = random.nextInt(100000);

        if (dice == 777) return OmikujiScore.Tokudaikichi;
        if (dice == 666) return OmikujiScore.Daikyou;
        if (dice < 10000) return OmikujiScore.Daikichi;
        if (dice < 30000) return OmikujiScore.Kichi;
        if (dice < 55000) return OmikujiScore.Chukichi;
        if (dice < 80000) return OmikujiScore.Shokichi;
        return OmikujiScore.Kyou;
    }

    private final HashMap<OmikujiScore, String> scoreNameMap = new HashMap<>();

    private Config conf;
    private static OmikujiStore instance;
    private Random random = new Random();
}