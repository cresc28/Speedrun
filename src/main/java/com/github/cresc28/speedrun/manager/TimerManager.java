package com.github.cresc28.speedrun.manager;

import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.data.CourseEntry;
import com.github.cresc28.speedrun.data.PointType;
import com.github.cresc28.speedrun.data.RunState;
import com.github.cresc28.speedrun.config.message.CourseMessage;
import com.github.cresc28.speedrun.db.record.RecordDao;
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
        RunState state = playerStates.computeIfAbsent(uuid, k -> new RunState());
        CourseEntry entry = courseManager.getCourseEntry(loc);

        if(entry == null) {
            state.updatePreviousLocation(loc);
            state.setOnEnd(false);
            state.setOnViaPoint(false);
            return;
        }

        PointType type = entry.getType();
        String courseName = entry.getCourseName();

        switch(type) {
            case START: handleStart(player, uuid, loc, courseName, state); break;
            case END: handleEnd(player, uuid, loc, courseName, state); break;
            case VIA_POINT: handleViaPoint(player, loc, courseName, state); break;
        }

        if(type != PointType.START) state.updatePreviousLocation(loc); //直前の位置を更新
        if(type != PointType.END) state.setOnEnd(false);
        if(type != PointType.VIA_POINT) state.setOnViaPoint(false);
    }

    private void handleStart(Player player, UUID uuid, Location loc, String courseName, RunState state){
        if (!state.startCourse(courseName, tick, loc)) return; //直前に同じスタート地点を踏んでいる場合はreturnする。

        if (ConfigManager.isDeleteCpOnStart()) {
            if (cpManager.removeCheckpoint(uuid, loc.getWorld(), courseName)) {
                player.sendMessage(courseName + "のCPを削除しました。");
            }
        }
        CourseMessage.startMessage(player, courseName);
    }

    private void handleEnd(Player player, UUID uuid, Location loc, String courseName, RunState state){
        int record = state.endCourse(tick, courseName);
        Integer recordValue = record > 0 ? record : null;
        int rank = -1;

        if(record > 0){ //有効な記録の場合は登録。
            int recordId = recordDao.insertAndRemoveSomeRecord(uuid, courseName, record);
            Map<String, Integer> viaPointRecord = state.getRecordMap();
            rank = recordDao.getRank(courseName, record);
            if(!viaPointRecord.isEmpty()) recordDao.insertViaPointRecord(recordId, viaPointRecord);
        }

        //この条件はゴール連発防止のため。またスタートとは違い、別のゴールを連続して踏んでも重複表示は行わない。
        if (!state.isOnEnd()) {
            Map.Entry<Integer, Integer> bestRecord = recordDao.getRankAndRecordNoDup(uuid, courseName);
            CourseMessage.endMessage(player, courseName, recordValue, rank, bestRecord);
            CourseMessage.showRanks(player, courseName, recordValue, rank, bestRecord);
        }

        state.setOnEnd(true);
    }

    private void handleViaPoint(Player player, Location loc, String courseName, RunState state){
        String[] parts = courseName.split("\\."); //xx.yyという登録の場合yyは中継地点の名称を指す。
        String baseCourseName = parts[0]; //コース名部分
        String viaPointName = parts.length == 2 ? parts[1] : null; //中継地点名部分
        int currentRecord = state.passViaPoint(tick, baseCourseName, viaPointName);
        Integer currentRecordValue = currentRecord > 0 ? currentRecord : null;

        if (!state.isOnViaPoint()) {
            CourseMessage.viaPointPassMessage(player, baseCourseName, viaPointName, currentRecordValue);
        }

        state.setOnViaPoint(true);
    }
}