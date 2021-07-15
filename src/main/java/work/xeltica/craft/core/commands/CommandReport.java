package work.xeltica.craft.core.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;

public class CommandReport extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player reporter, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }
        var playerName = args[0];
        var reportee = Bukkit.getOfflinePlayer(Bukkit.getPlayerUniqueId(playerName));
        if (reportee == null) {
            reporter.sendMessage("そのような名前のプレイヤーはこのサーバーにはいないようです。");
            return true;
        }
        choosePunishmentType(reporter, reportee);
        return true;
    }


    private void choosePunishmentType(Player reporter, OfflinePlayer reportee) {
        Consumer<MenuItem> cb = (m) -> chooseReason(reporter, reportee, (String)m.getCustomData(), null);
        Gui.getInstance().openMenu(reporter, "処罰の種類"
            , new MenuItem("BAN", cb, Material.BARRIER, "ban")
            , new MenuItem("警告", cb, Material.BELL, "warn")
            , new MenuItem("キック", cb, Material.RABBIT_FOOT, "kick")
            , new MenuItem("ミュート", cb, Material.MUSIC_DISC_11, "mute")
        );
    }
    

    private void chooseReason(Player reporter, OfflinePlayer reportee, String command, HashSet<AbuseType> state) {
        var types = AbuseType.values();
        final HashSet<AbuseType> currentState = state == null ? new HashSet<>() : state;
        var menuItems = Arrays.stream(types).map(t -> {
            return new MenuItem(t.shortName, _null -> {
                if (currentState.contains(t)) {
                    currentState.remove(t);
                } else {
                    currentState.add(t);
                }
                chooseReason(reporter, reportee, command, currentState);
            }, t.icon, null, currentState.contains(t));
        }).collect(Collectors.toList());

        menuItems.add(new MenuItem("戻る", _null -> {
            choosePunishmentType(reporter, reportee);
        }, Material.RED_WOOL));

        menuItems.add(
            new MenuItem("決定", _null -> {
                if (currentState.size() == 0) {
                    reporter.sendMessage(ChatColor.RED + "理由が指定されなかったため、何もしません。");
                    return;
                }
                if (command.equals("ban") || command.equals("mute")) {
                    chooseTime(reporter, reportee, command, currentState);
                } else {
                    takeDown(reporter, reportee, command, currentState, null);
                }
            }, Material.GREEN_WOOL)
        );

        Gui.getInstance().openMenu(reporter, command + "すべき理由（複数選択可）", menuItems.toArray(MenuItem[]::new));
    }

    private void chooseTime(Player reporter, OfflinePlayer reportee, String command, HashSet<AbuseType> state) {
        Consumer<MenuItem> cb = (m) -> takeDown(reporter, reportee, command, state, (String)m.getCustomData());

        String[] times = {
            "1d", "3d", "5d", "7d", "14d", "1mo", "3mo", "6mo", "12mo", null
        };

        Gui.getInstance().openMenu(reporter, "期間を指定してください", Arrays.stream(times).map(
            t -> new MenuItem(convertTimeToLocaleString(t), cb, Material.LIGHT_GRAY_WOOL, t)
        ).toArray(MenuItem[]::new));
    }

    private void takeDown(Player reporter, OfflinePlayer reportee, String command, HashSet<AbuseType> state, String time) {
        var abuses = String.join(",", state.stream().map(s -> s.shortName).toArray(String[]::new));
        var timeString = convertTimeToLocaleString(time);
        String reason;
        if (command.equals("warn")) {
            var instructions = String.join(" & ", state.stream().filter(s -> s.instruction != null).map(s -> s.instruction).toArray(String[]::new));
            reason = instructions.isEmpty() ? String.format(warnTemplateWithoutAfterDoing, abuses) : String.format(warnTemplate, abuses, instructions);
            command = "tell";
        } else if (command.equals("ban")) {
            reason = String.format(banTemplate, abuses, timeString);
        } else if (command.equals("kick")) {
            reason = String.format(kickTemplate, abuses);
        } else if (command.equals("mute")) {
            reason = String.format(muteTemplate, abuses, timeString);
        } else {
            reporter.sendMessage(ChatColor.RED + "無効なコマンド: " + command);
            return;
        }

        var name = reportee.getName();
        var cmd = time != null ? String.format("temp%s %s %s %s", command, name, time, reason) : String.format("%s %s %s", command, name, reason);
        reporter.performCommand(cmd);
    }

    private String convertTimeToLocaleString(String time) {
        return time == null ? "無期限" : time.replace("d", "日間").replace("mo", "ヶ月");
    }

    private final String warnTemplateWithoutAfterDoing = "利用規約の「%s」に違反しています。今すぐ停止してください。本警告を無視した場合、アカウントは直ちにBANされます。";
    private final String warnTemplate = "利用規約の「%s」に違反しています。今すぐ停止し、%s。本警告を無視した場合、アカウントは直ちにBANされます。";
    private final String banTemplate = "利用規約「%s」に違反しているため、%sBANとします。 異議申し立てはDiscord Xeltica#7081 まで";
    private final String kickTemplate = "利用規約「%s」に違反しているため、キックします。 異議申し立てはDiscord Xeltica#7081 まで";
    private final String muteTemplate = "利用規約「%s」に違反しているため、%sミュートします。 異議申し立てはDiscord Xeltica#7081 まで";

    enum AbuseType {
        GRIEFING("禁止行為: 破壊", Material.DIAMOND_PICKAXE, "直ちに本来の形に修復するか、意図的でない場合はその旨を返信してください。"),
        STEALING("禁止行為: 窃盗", Material.ENDER_CHEST, "盗んだアイテムを直ちに元の場所、持ち主に返却してください"),
        MONOPOLY_SHARED_ITEMS("禁止行為: 共有資産独占", Material.OAK_SIGN, "直ちに元の状態に戻すことで独占状態を解いてください。"),
        FORCED_PVP("禁止行為: 取り決め無きPvP", Material.DIAMOND_SWORD),
        PRIVATE_INVADING("禁止行為: 無許可での私有地侵入", Material.OAK_DOOR),
        OBSCENE_BUILDING("禁止行為: わいせつ物建築", Material.RED_MUSHROOM, "撤去してください"),
        LAW_VIOLATION_BUILDING("禁止行為: 国内法違反建築", Material.TNT, "撤去してください"),
        INVALID_CHAT("禁止行為: 秩序を乱すチャット", Material.PLAYER_HEAD),
        REAL_TRADING("禁止行為: 資産の現実での取引", Material.GOLD_BLOCK),
        BLACKMAIL("禁止行為: 恐喝", Material.CROSSBOW),
        COLLUSION("禁止行為: 共謀", Material.CAMPFIRE),
        DOS("禁止行為: 意図的に負荷をかける行為", Material.CAMPFIRE),
        OTHER("禁止行為: トラブルの原因となる行為", Material.CAMPFIRE, "この後に続く指示に従ってください"),
        GLITCH("不正行為: 不具合悪用", Material.COMMAND_BLOCK),
        AVOID_PANISHMENT("不正行為: 処罰回避", Material.TRIPWIRE_HOOK),
        FAKE_REPORT("不正行為: 虚偽通報", Material.PUFFERFISH),
        IGNORE_WARN("不正行為: 運営からのPMの無視", Material.PUFFERFISH),
        EXPOSE_PM("不正行為: PMの晒し上げ行為", Material.PUFFERFISH),
        INVALID_MOD("不正行為: 不正MODの使用", Material.COMPARATOR),
        INVALID_LIVE("禁止行為: 許諾無しでの配信・動画公開", Material.MUSIC_DISC_PIGSTEP, "配信した動画を削除してください"),
        ;

        AbuseType(String shortName, Material icon) {
            this(shortName, icon, null);
        }

		AbuseType(String shortName, Material icon, String whatToDoToAvoidPunishment) {
            this.shortName = shortName;
            this.icon = icon;
            this.instruction = whatToDoToAvoidPunishment;
        }

        private final String shortName;
        private final Material icon;
        private final String instruction;
    };
}
