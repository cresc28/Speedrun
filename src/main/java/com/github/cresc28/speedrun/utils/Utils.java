package com.github.cresc28.speedrun.utils;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.Collator;
import java.util.*;

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
    public static String tickToTime(int ticks){
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
    public static void completionFromCollection(Collection<String> source, String prefix, List<String> completions) {
        for (String value : new HashSet<>(source)) {
            if (value != null && value.toLowerCase().startsWith(prefix)) {
                completions.add(value);
            }
        }
    }

    /**
     * 指定されたcollectionの要素を全てTAB補完リストに追加する。
     *
     * @param source 補完対象のコレクション
     * @param prefix 補完する文字列の頭文字
     * @param completions 補完候補の追加先
     */
    public static void completionExcludeViaPoint(Collection<String> source, String prefix, List<String> completions) {
        Set<String> added = new HashSet<>(); // 重複防止

        for (String value : source) {
            if (value != null) {
                int dotIndex = value.indexOf('.');
                String beforeDot;
                if (dotIndex > 0) {
                    beforeDot = value.substring(0, dotIndex);
                } else {
                    beforeDot = value;
                }

                if (beforeDot.toLowerCase().startsWith(prefix) && added.add(beforeDot)) {
                    completions.add(beforeDot);
                }
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

    /**
     * 特定の文字が文字列に含まれるかをチェックする。
     *
     * @param str チェックする文字列
     * @param ch チェックする文字
     * @return 含まれているかどうか
     */
    public static boolean containsChar(String str, char ch){
        return str != null && str.indexOf(ch) >= 0;
    }

    /**
     * ある要素をソートする。
     *
     * @param collection ソートするコレクション
     * @return ソートされたコレクション
     */
    public static Collection<String> sortCollection(Collection<String> collection){
        List<String> sortedCollection = new ArrayList<>(collection);

        Collator collator = Collator.getInstance(Locale.JAPAN);
        collator.setStrength(Collator.TERTIARY);

        sortedCollection.sort(collator);

        return sortedCollection;
    }

    /**
     * アイテムの名前を変更する。
     *
     * @param item アイテム
     * @param name 変更後の名前
     */
    public static void changeItemName(ItemStack item, String name){
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return;
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    /**
     * ブロックデータから方角を得る。
     *
     * @param data block.getData()
     * @return 方角(yaw)
     */
    public static float getYaw(byte data) {
        switch (data) {
            case 0: return 180f; //北
            case 4: return -90f; //東
            case 8: return 0f; //南
            case 12: return 90f; //西

            default: return 180f;
        }
    }

    /**
     * 入力された文字列が数字か判断する。
     *
     * @param str 文字列
     * @return 数字か
     */
    public static boolean isPositiveInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}
