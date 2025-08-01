package com.github.cresc28.speedrun.data;

import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * プレイヤーの走行状態を管理するクラス。
 *　管理する情報はコメントアウトの通り。
 */
public class RunState {
    private int startTick; //スタート時のtick
    private boolean isRunning; //スタートを踏んだ後か
    private boolean isOnEnd; //ゴール地点の上にいるか(ゴールの連発防止用)
    private boolean isOnViaPoint;
    private String currentCourse; //現在走行中のコース
    private Location previousLocation; //最後にいた位置
    Map<String, Integer> recordMap = new LinkedHashMap<>();

    public RunState() {
        this.isRunning = false;
        this.isOnEnd = false;
        this.isOnViaPoint = false;
        this.startTick = 0;
        this.currentCourse = null;
        this.previousLocation = null;
    }

    /**
     * 計測を開始する。
     *
     * @param courseName コース名
     * @param tick 現在のtick
     * @param currentLoc プレイヤーの現在位置
     * @return 計測開始時はtrueを返す。
     */
    public boolean startCourse(String courseName, int tick, Location currentLoc) {
        //直前に同じスタート地点を踏んでいなければ処理を開始。
        if (!Objects.equals(previousLocation, currentLoc)) {
            this.currentCourse = courseName;
            this.startTick = tick;
            this.isRunning = true;
            this.previousLocation = currentLoc;
            recordMap.clear();
            return true;
        }
        return false;
    }

    /**
     * 計測を終了し、タイムを返す。
     *
     * @param tick 現在のick
     * @param courseName コース名
     * @return 計測開始がされていたコースならタイムを返す。
     */
    public int endCourse(int tick, String courseName) {
        //speedrunを開始していないまたはゴールしたコースが走行中のコースと不一致なら-1を返す。
        if (!isRunning || !courseName.equals(currentCourse)) {
            return -1;
        }
        isRunning = false;
        return tick - startTick;
    }

    /**
     * 現時点のタイムを返す。
     *
     * @param tick 現在のick
     * @param courseName コース名
     * @return 計測開始がされていたコースならタイムを返す。
     */
    public int passViaPoint(int tick, String courseName, String viaPointName){
        //speedrunを開始していないまたはゴールしたコースが走行中のコースと不一致なら-1を返す。
        if (!isRunning || !courseName.equals(currentCourse)) {
            return -1;
        }

        if(viaPointName == null) viaPointName = "中継地点";
        recordMap.putIfAbsent(viaPointName, tick - startTick);

        return tick - startTick;
    }

    public boolean isOnEnd() {
        return isOnEnd;
    }

    public void setOnEnd(boolean onEnd) {
        this.isOnEnd = onEnd;
    }

    public boolean isOnViaPoint() {
        return isOnViaPoint;
    }

    public void setOnViaPoint(boolean isOnViaPoint) {
        this.isOnViaPoint = isOnViaPoint;
    }

    public void updatePreviousLocation(Location loc) {
        this.previousLocation = loc;
    }

    public Map<String, Integer> getRecordMap(){
        return recordMap;
    }
}