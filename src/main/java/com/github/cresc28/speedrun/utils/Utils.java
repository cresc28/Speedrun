package com.github.cresc28.speedrun.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Utils {

    //tickを現実時間に変換
    public static String formatTime(int ticks){
        int commas = (ticks % 20) * 5;
        int seconds = (ticks / 20) % 60;
        int minutes = (ticks / (60 * 20)) % 60;
        int hours = ticks / (60 * 60 * 20);

        return String.format("%02d:%02d:%02d.%02d0", hours, minutes, seconds, commas);
    }

    //Collection内の全ての要素をTAB補完として表示する。
    public static void completionFromMap(Collection<String> source, String prefix, List<String> completions) {
        for (String value : new HashSet<>(source)) {
            if (value.toLowerCase().startsWith(prefix)) {
                completions.add(value);
            }
        }
    }
}
