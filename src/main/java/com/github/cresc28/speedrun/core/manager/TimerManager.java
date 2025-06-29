package com.github.cresc28.speedrun.core.manager;

import com.github.cresc28.speedrun.data.CourseEntry;
import com.github.cresc28.speedrun.data.CourseType;
import com.github.cresc28.speedrun.data.RunState;
import com.github.cresc28.speedrun.config.message.CourseMessage;
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
    private final CourseDataManager cdm;

    public TimerManager(CourseDataManager cdm) {
        this.cdm = cdm;
    }

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
    public void checkRunState(Player player) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation().getBlock().getLocation();
        CourseEntry entry = cdm.getCourseEntry(loc);
        RunState state = playerStates.computeIfAbsent(uuid, k -> new RunState());

        if(entry == null) {
            state.setOnEnd(false);
            state.setOnViaPoint(false);
            return;
        }

        CourseType type = entry.getType();
        String courseName = entry.getCourseName();


        switch(type) {
            case START: //プレイヤーの現在座標がいずれかのスタート地点と一致するならば処理を開始。
                if (state.startCourse(courseName, tick, loc)) {
                    CourseMessage.startMessage(player, courseName);
                }
                break;

            case END:
                int record = state.endCourse(tick, courseName);
                if (record > 0) {
                    CourseMessage.endMessage(player, courseName, record);
                }

                //TAを開始したパルクール以外のパルクールのゴールを踏むとクリア表示のみを出す。
                // isOnEndはゴール連発防止のため。またスタートとは違い、別のゴールを連続して踏んでも重複表示は行わない。
                else if (!state.isOnEnd()) {
                    CourseMessage.endMessage(player, courseName, null);
                }

                state.setOnEnd(true);
                break;

            case VIA_POINT:
                String[] parts = courseName.split("\\."); //xx.yyという登録の場合yyは中継地点の名称を指す。
                courseName = parts[0];

                int currentRecord = state.getCurrentRecord(tick, courseName);

                if(currentRecord > 0 && !state.isOnViaPoint()){ //タイム計測が行えている場合
                    if (parts.length == 2) { //中継地点に名称が設定されている場合の処理。
                        CourseMessage.viaPointPassMessage(player, courseName, parts[1], currentRecord);
                    }

                    else {
                        CourseMessage.viaPointPassMessage(player, courseName, null, currentRecord);
                    }
                }

                else if (!state.isOnViaPoint() ) { //タイム計測が行えていない場合
                    if (parts.length == 2) { //中継地点に名称が設定されている場合の処理。
                        CourseMessage.viaPointPassMessage(player, courseName, parts[1], null);
                    }
                    else {
                        CourseMessage.viaPointPassMessage(player, courseName, null, null);
                    }
                }

                state.setOnViaPoint(true);
                break;
        }

        state.updateLastStartLocation(loc);
    }
}