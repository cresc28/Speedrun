package com.github.cresc28.speedrun.data;

import org.bukkit.Location;
/**
 * プレイヤーの走行状態を管理するクラス。
 *　管理する情報はコメントアウトの通り。
 *
 */
public class RunState {
    private int startTime; //スタート時のtick
    private boolean isRunning; //スタートを踏んだ後か
    private boolean isOnEnd; //ゴール地点の上にいるか(ゴールの連発防止用)
    private String currentCourse; //現在走行中のコース
    private Location lastStartLocation; //最後に踏んだスタートの位置

    public RunState() {
        this.isRunning = false;
        this.isOnEnd = false;
        this.startTime = 0;
        this.currentCourse = null;
        this.lastStartLocation = null;
    }

    public boolean isOnEnd() {
        return isOnEnd;
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
        if (lastStartLocation == null || !lastStartLocation.equals(currentLoc)) {
            this.currentCourse = courseName;
            this.startTime = tick;
            this.isRunning = true;
            this.lastStartLocation = currentLoc;
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
        return tick - startTime;
    }

    public void setOnEnd(boolean onEnd) {
        this.isOnEnd = onEnd;
    }

    public void updateLastStartLocation(Location loc) {
        this.lastStartLocation = loc;
    }
}
