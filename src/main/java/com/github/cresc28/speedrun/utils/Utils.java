package com.github.cresc28.speedrun.utils;

import org.bukkit.Location;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * 汎用ユーティリティクラス。
 */
public class Utils {
    /**
     * tick数を現実時間に変更する
     *
     * @param ticks tick数
     * @return 現実時間表示
     */
    public static String formatTime(int ticks){
        int commas = (ticks % 20) * 5;
        int seconds = (ticks / 20) % 60;
        int minutes = (ticks / (60 * 20)) % 60;
        int hours = ticks / (60 * 60 * 20);

        return String.format("%02d:%02d:%02d.%02d0", hours, minutes, seconds, commas);
    }

    /**
     * 指定されたcollectionの要素を全てTAB補完リストに追加する。
     *
     * @param source 補完対象のコレクション
     * @param prefix 補完する文字列の頭文字
     * @param completions 補完候補の追加先
     */
    public static void completionFromMap(Collection<String> source, String prefix, List<String> completions) {
        for (String value : new HashSet<>(source)) {
            if (value.toLowerCase().startsWith(prefix)) {
                completions.add(value);
            }
        }
    }

    /**
     * プレイヤーの真下のブロックのLocationをintで返す。
     *
     * @param loc 座標
     * @return 整数化座標
     */
    public static Location getBlockLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
