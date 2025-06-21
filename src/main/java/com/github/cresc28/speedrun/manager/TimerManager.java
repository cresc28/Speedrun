package com.github.cresc28.speedrun.manager;

import com.github.cresc28.speedrun.data.RunState;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * タイマーの管理と、計測開始・終了判定を行うクラス。
 */
public class TimerManager {
    private int tick = 0;
    private final Map<UUID, RunState> playerStates = new HashMap<>();

    /**
     * タイマーを開始する。
     */
    public void startTimer(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                tick++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * プレイヤーの現在位置をチェックし、スタート地点またはゴール地点にいるなら計測開始・終了を行う。
     *
     * @param player プレイヤー
     */
    public void detectStartOrEnd(Player player) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation().getBlock().getLocation();
        String start = CourseDataManager.getCourseName(loc, "start");
        String end = CourseDataManager.getCourseName(loc,"end");
        RunState state = playerStates.computeIfAbsent(uuid, k -> new RunState());

        //プレイヤーの現在座標がいずれかのスタート地点と一致するならば処理を開始。
        if (start != null) {
            if (state.startCourse(start, tick, loc)) {
                player.sendMessage("計測開始！");
            }
        }

        else if (end != null) {
            int record = state.endCourse(tick, end);
            if (record > 0) {
                String timeString = Utils.formatTime(record);
                Bukkit.broadcastMessage(player.getName() + "さんが" + end + "を" + timeString + " (" + record + "tick)でクリア！");
            }

            //TAを開始したパルクール以外のパルクールのゴールを踏むとクリア表示のみを出す。
            // isOnEndはゴール連発防止のため。またスタートとは違い、別のゴールを連続して踏んでも重複表示は行わない。
            else if (!state.isOnEnd()) {
                Bukkit.broadcastMessage(player.getName() + "さんが" + end + "をクリア！");
            }

            state.setOnEnd(true);
        }

        else {
            state.setOnEnd(false);
        }
        state.updateLastStartLocation(loc);
    }
}