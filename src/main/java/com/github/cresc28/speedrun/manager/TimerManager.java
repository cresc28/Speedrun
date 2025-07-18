package com.github.cresc28.speedrun.manager;

import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.data.CourseEntry;
import com.github.cresc28.speedrun.data.CourseType;
import com.github.cresc28.speedrun.data.RunState;
import com.github.cresc28.speedrun.config.message.CourseMessage;
import com.github.cresc28.speedrun.db.course.RecordDao;
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
    private final CourseManager courseManager;
    private final CheckpointManager cpManager;
    private final RecordDao recordDao;

    public TimerManager(CourseManager courseManager, CheckpointManager cpManager, RecordDao recordDao) {
        this.courseManager = courseManager;
        this.cpManager = cpManager;
        this.recordDao = recordDao;
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
        CourseEntry entry = courseManager.getCourseEntry(loc);
        RunState state = playerStates.computeIfAbsent(uuid, k -> new RunState());

        if(entry == null) {
            state.setOnEnd(false);
            state.setOnViaPoint(false);
            state.updateRecentStandLocation(loc);
            return;
        }

        CourseType type = entry.getType();
        String courseName = entry.getCourseName();


        switch(type) {
            case START:
                //直前に同じスタート地点を踏んでいる場合はreturnする。
                if (!state.startCourse(courseName, tick, loc)) return;

                if (ConfigManager.isDeleteCpOnStart()) {
                    if (cpManager.removeCheckpoint(uuid, loc.getWorld(), courseName)) {
                        player.sendMessage(courseName + "のCPを削除しました。");
                    }
                }
                CourseMessage.startMessage(player, courseName);

                break;

            case END:
                int record = state.endCourse(tick, courseName);
                Integer recordValue = record > 0 ? record : null;
                int rank = recordDao.getRank(courseName, record);

                if(record > 0){ //記録を登録
                    int recordId = recordDao.insertAndRemoveSomeRecord(uuid, courseName, record);

                    Map<String, Integer> viaPointRecord = state.getRecordMap();
                    if(!viaPointRecord.isEmpty()) {
                        recordDao.insertViaPointRecord(recordId, viaPointRecord);
                    }
                }

                //TAを開始したパルクール以外のパルクールのゴールを踏むとクリア表示のみを出す。
                // isOnEndはゴール連発防止のため。またスタートとは違い、別のゴールを連続して踏んでも重複表示は行わない。
                if (!state.isOnEnd()) {
                    Map.Entry<Integer, Integer> bestRecord = recordDao.getRankAndRecordNoDup(uuid, courseName, false);
                    CourseMessage.endMessage(player, courseName, recordValue, rank, bestRecord);
                }

                state.setOnEnd(true);
                state.updateRecentStandLocation(loc);
                break;

            case VIA_POINT:
                String[] parts = courseName.split("\\."); //xx.yyという登録の場合yyは中継地点の名称を指す。
                courseName = parts[0];

                String viaPointName = parts.length == 2 ? parts[1] : null;
                int currentRecord = state.viaPointPass(tick, courseName, viaPointName);
                Integer currentRecordValue = currentRecord > 0 ? currentRecord : null;

                if (!state.isOnViaPoint()) {
                    CourseMessage.viaPointPassMessage(player, courseName, viaPointName, currentRecordValue);
                }

                state.setOnViaPoint(true);
                state.updateRecentStandLocation(loc);
                break;
        }
    }
}