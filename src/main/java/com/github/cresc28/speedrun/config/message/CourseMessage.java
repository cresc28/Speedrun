package com.github.cresc28.speedrun.config.message;

import com.github.cresc28.speedrun.utils.GameUtils;
import com.github.cresc28.speedrun.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ユーザ指定のメッセージを取得するためのクラス。
 */

public class CourseMessage {
    private static final File FILE = new File("plugins/Speedrun/message.yml");
    private static FileConfiguration config;
    private static final Logger LOGGER = Logger.getLogger("MessageManager");


    /**
     * message.ymlがなければ作成し、あればデータを読み込む。
     */
    public static void init() {
        if (!FILE.exists()) {
            setDefault();
        }

        config = YamlConfiguration.loadConfiguration(FILE);
    }

    /**
     * messageファイルにデフォルト値をセットする。
     */
    public static void setDefault(){
        String content =
                "# メッセージ編集用ファイル\n"
                        + "# -----------------------------\n"
                        + "# 以下のはメッセージのテンプレートです。\n"
                        + "# プレイヤー名やコース名、タイムの表示を行う際は「%」で次のように囲ってください。\n"
                        + "#\n"
                        + "# 例:\n"
                        + "# %player%   → プレイヤー名\n"
                        + "# %course%   → コース名\n"
                        + "# %time%     → タイム(xx:xx:xx.xx表記)\n"
                        + "# %tick%     → ライム(tick表記)\n"
                        + "# %rank%     → 記録の順位\n"
                        + "# %bestRank%     → ベスト記録の順位\n"
                        + "# %bestTime%     → ベスト記録のタイム(xx:xx:xx.xx表記)\n"
                        + "# %bestTick%     → ベスト記録のタイム(tick表記)\n"
                        + "# %viapoint% → 中継地点名\n"
                        + "# %n% → 改行\n"
                        + "#\n"
                        + "# 色・装飾コードについて:\n"
                        + "# &0: 黒         &1: 濃青        &2: 濃緑        &3: 青緑\n"
                        + "# &4: 濃赤       &5: 紫          &6: 金          &7: 灰\n"
                        + "# &8: 濃灰       &9: 青          &a: 緑          &b: 水色\n"
                        + "# &c: 赤         &d: ピンク      &e: 黄          &f: 白\n"
                        + "# &l: 太字       &n: 下線        &o: 斜体        &m: 打ち消し線\n"
                        + "# &r: リセット（すべての装飾を解除）\n"
                        + "#\n"
                        + "# 例: \"%player%さんが&a&l計測開始！&r\"\n"
                        + "# 反映にはサーバーの再起動が必要です。\n"
                        + "# メッセージ以外の部分を編集すると正常に読み込みが行われなくなります。\n\n"
                        + "# その場合、このファイルを一度削除してください。\n"

                        + "# 計測開始時のメッセージ（表示可能:プレイヤー名、コース名）\n"
                        + "start: \"計測開始！\"\n\n"

                        + "# ゴールメッセージ（表示可能:プレイヤー名、コース名、タイム、順位、ベスト順位、ベスト記録）\n"
                        + "complete1: \"%player%さんが%course%を&a%time%(%tick%ticks)&rでクリア！%n%この記録の順位は&6%rank%位&rです。(ベスト記録:&a%bestTime%&r, ベスト順位:&6%bestRank%位&r)\"\n\n"

                        + "# ゴールメッセージ（タイム計測なし）（表示可能:プレイヤー名、コース名）\n"
                        + "complete2: \"%player%さんが%course%をクリア！\"\n\n"

                        + "# 中継地点通過メッセージ（表示可能:プレイヤー名、コース名、中継地点名、タイム）\n"
                        + "pass1: \"%player%さんが%viapoint%を&a%time%(%tick%ticks)&rで通過！\"\n\n"

                        + "# 中継地点通過メッセージ（タイム計測なし）（表示可能:プレイヤー名、コース名、中継地点名）\n"
                        + "pass2: \"%player%さんが%course%の中継地点:%viapoint%を通過！\"\n\n"

                        + "# 中継地点通過メッセージ（中継地点名なし）（表示可能:プレイヤー名、コース名、タイム）\n"
                        + "pass3: \"%player%さんが%course%の中継地点を&a%time%(%tick%ticks)&rで通過！\"\n\n"

                        + "# 中継地点通過メッセージ（タイム計測なし、中継地点名なし）（表示可能:プレイヤー名、コース名）\n"
                        + "pass4: \"%player%さんが%course%の中継地点を通過！\"\n";

        try (FileWriter writer = new FileWriter(FILE)) {
            writer.write(content);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "message.ymlへの書き込みに失敗しました");
        }
    }

    /**
     * 計測開始時のメッセージを体裁を整えて返す。
     *
     * @param player プレイヤー
     * @param courseName コース名
     */
    public static void startMessage(Player player, String courseName){
        String message = config.getString("start", "計測開始！");
        message = message.replace("%player%", player.getName());
        message = message.replace("%course%", courseName);
        message = ChatColor.translateAlternateColorCodes('&', message);

        for (String line : message.split("%n%")) {
            player.sendMessage(message);
        }
    }

    /**
     * ゴール時のメッセージを体裁を整えて返す。
     *
     * @param player プレイヤー
     * @param courseName コース名
     * @param tick タイム(tick)
     */
    public static void endMessage(Player player, String courseName, Integer tick, int rank, Map.Entry<Integer, Integer> rankAndTime) {
        String message;

        if (tick != null) {
            message = config.getString("complete1", "%player%さんが%course%を&a%time%(%tick%ticks)&rでクリア！%n%あなたの順位は&6%rank%位&rです。(ベスト記録:&a%bestTime%&r, ベスト順位:&6%bestRank%位)&r");
            message = message.replace("%time%", GameUtils.tickToTime(tick));
            message = message.replace("%tick%", String.valueOf(tick));
            message = message.replace("%rank%", String.valueOf(rank));
            message = message.replace("%bestRank%", String.valueOf(rankAndTime.getKey()));
            message = message.replace("%bestTick%", String.valueOf(rankAndTime.getValue()));
            message = message.replace("%bestTime%", GameUtils.tickToTime(rankAndTime.getValue()));
        }

        else {
            message = config.getString("complete2", "%player%さんが%course%をクリア！");
        }

        message = message.replace("%player%", player.getName());
        message = message.replace("%course%", courseName);
        message = ChatColor.translateAlternateColorCodes('&', message);

        for (String line : message.split("%n%")) {
            Bukkit.broadcastMessage(line);
        }
    }

    /**
     * 中継地点通過時のメッセージを体裁を整えて返す。
     *
     * @param player プレイヤー
     * @param courseName コース名
     * @param viaPointName 中間地点名
     * @param tick タイム(tick)
     */
    public static void viaPointPassMessage(Player player, String courseName, String viaPointName, Integer tick) {
        String key;
        String message;

        if (viaPointName != null && tick != null) {
            key = "pass1";
            message = config.getString(key, "%player%さんが%viapoint%を&a%time%(%tick%ticks)&rで通過！");
        }
        else if (viaPointName != null) {
            key = "pass2";
            message = config.getString(key, "%player%さんが%viapoint%を通過！");
        }
        else if (tick != null) {
            key = "pass3";
            message = config.getString(key, "%player%さんが%course%の中継地点を&a%time%(%tick%ticks)&rで通過！");
        }
        else {
            key = "pass4";
            message = config.getString(key, "%player%さんが%course%の中継地点を通過！");
        }

        if (viaPointName != null) message = message.replace("%viapoint%", viaPointName);
        if (tick != null) {
            message = message.replace("%time%", GameUtils.tickToTime(tick));
            message = message.replace("%tick%", String.valueOf(tick));
        }

        message = message.replace("%player%", player.getName());
        message = message.replace("%course%", courseName);
        message = ChatColor.translateAlternateColorCodes('&', message);

        for (String line : message.split("%n%")) {
            Bukkit.broadcastMessage(line);
        }
    }
}
