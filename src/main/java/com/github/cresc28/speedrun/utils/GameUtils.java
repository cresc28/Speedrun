package com.github.cresc28.speedrun.utils;

import org.bukkit.Location;

/**
 * ゲームに関する汎用ユーティリティクラス。
 */
public class GameUtils {
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
     * 現実時間をtick数にに変更する
     *
     * @param timeString %02d:%02d:%02d.%02d0表示の時間
     * @return 現実時間表示
     */
    public static int timeStringToTick(String timeString) {
        String[] parts = timeString.split("[:.]");
        if (parts.length != 4) return -1;

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int millis = Integer.parseInt(parts[3]);

        if (millis % 50 != 0) return -1;

        int tickMillis = millis / 50;
        return hours * 60 * 60 * 20 + minutes * 60 * 20 + seconds * 20 + tickMillis;
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
}
